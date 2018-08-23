package cancel.service;

import cancel.async.AsyncTask;
import cancel.config.MockLog;
import cancel.domain.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.Future;

@Service
public class CancelServiceImpl implements CancelService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private AsyncTask asyncTask;

    @Autowired
    MockLog mockLog;

    @Override
    public CancelOrderResult cancelOrder(CancelOrderInfo info, String loginToken, String loginId, HttpHeaders headers) throws Exception {
        GetOrderByIdInfo getFromOrderInfo = new GetOrderByIdInfo();
        getFromOrderInfo.setOrderId(info.getOrderId());
        GetOrderResult orderResult = getOrderByIdFromOrder(getFromOrderInfo, headers);
        if (orderResult.isStatus() == true) {
            mockLog.printLog("[Cancel Order Service][Cancel Order] Order found G|H");
            Order order = orderResult.getOrder();
            if (order.getStatus() == OrderStatus.NOTPAID.getCode()
                    || order.getStatus() == OrderStatus.PAID.getCode() || order.getStatus() == OrderStatus.CHANGE.getCode()) {

                order.setStatus(OrderStatus.CANCEL.getCode());
                ChangeOrderInfo changeOrderInfo = new ChangeOrderInfo();
                changeOrderInfo.setLoginToken(loginToken);
                changeOrderInfo.setOrder(order);
                ChangeOrderResult changeOrderResult = cancelFromOrder(changeOrderInfo, headers);
                if (changeOrderResult.isStatus() == true) {
                    CancelOrderResult finalResult = new CancelOrderResult();
                    finalResult.setStatus(true);
                    finalResult.setMessage("Success.Cancel Order");
                    mockLog.printLog("[Cancel Order Service][Cancel Order] Success.");
                    //Draw back money
                    String money = calculateRefund(order);
                    boolean status = drawbackMoney(money, loginId, headers);
                    if (status == true) {
                        mockLog.printLog("[Cancel Order Service][Draw Back Money] Success.");

                        GetAccountByIdInfo getAccountByIdInfo = new GetAccountByIdInfo();
                        getAccountByIdInfo.setAccountId(order.getAccountId().toString());
                        GetAccountByIdResult result = getAccount(getAccountByIdInfo, headers);
                        if (result.isStatus() == false) {
                            return null;
                        }

                        NotifyInfo notifyInfo = new NotifyInfo();
                        notifyInfo.setDate(new Date().toString());


                        notifyInfo.setEmail(result.getAccount().getEmail());
                        notifyInfo.setStartingPlace(order.getFrom());
                        notifyInfo.setEndPlace(order.getTo());
                        notifyInfo.setUsername(result.getAccount().getName());
                        notifyInfo.setSeatNumber(order.getSeatNumber());
                        notifyInfo.setOrderNumber(order.getId().toString());
                        notifyInfo.setPrice(order.getPrice());
                        notifyInfo.setSeatClass(SeatClass.getNameByCode(order.getSeatClass()));
                        notifyInfo.setStartingTime(order.getTravelTime().toString());

                        sendEmail(notifyInfo, headers);

                    } else {
                        mockLog.printLog("[Cancel Order Service][Draw Back Money] Fail.");
                    }


                    return finalResult;
                } else {
                    CancelOrderResult finalResult = new CancelOrderResult();
                    finalResult.setStatus(false);
                    finalResult.setMessage(changeOrderResult.getMessage());
                    mockLog.printLog("[Cancel Order Service][Cancel Order] Fail.Reason:" + changeOrderResult.getMessage());
                    return finalResult;
                }

            } else {
                CancelOrderResult result = new CancelOrderResult();
                result.setStatus(false);
                result.setMessage("Order Status Cancel Not Permitted");
                mockLog.printLog("[Cancel Order Service][Cancel Order] Order Status Not Permitted.");
                return result;
            }
        } else {
            GetOrderByIdInfo getFromOtherOrderInfo = new GetOrderByIdInfo();
            getFromOtherOrderInfo.setOrderId(info.getOrderId());
            GetOrderResult orderOtherResult = getOrderByIdFromOrderOther(getFromOtherOrderInfo, headers);
            if (orderOtherResult.isStatus() == true) {
                mockLog.printLog("[Cancel Order Service][Cancel Order] Order found Z|K|Other");

                Order order = orderOtherResult.getOrder();
                if (order.getStatus() == OrderStatus.NOTPAID.getCode()
                        || order.getStatus() == OrderStatus.PAID.getCode() || order.getStatus() == OrderStatus.CHANGE.getCode()) {

                    mockLog.printLog("[Cancel Order Service][Cancel Order] Order status ok");

                    order.setStatus(OrderStatus.CANCEL.getCode());
                    ChangeOrderInfo changeOrderInfo = new ChangeOrderInfo();
                    changeOrderInfo.setLoginToken(loginToken);
                    changeOrderInfo.setOrder(order);
//                    ChangeOrderResult changeOrderResult = cancelFromOtherOrder(changeOrderInfo);


                    /*********************** Fault Reproduction - Error Process Seq *************************/
                    //1.return money
                    String money = calculateRefund(order);
                    Future<Boolean> taskDrawBackMoney = asyncTask.drawBackMoneyForOrderCancel(money, loginId, order.getId().toString(), loginToken, headers);

                    //2.change status to [canceled]
                    Future<ChangeOrderResult> taskCancelOrder = asyncTask.updateOtherOrderStatusToCancel(changeOrderInfo, headers);

                    ChangeOrderResult changeOrderResult;
                    boolean drawBackMoneyStatus;

//                    boolean status = true;
                    while (!taskCancelOrder.isDone() || !taskDrawBackMoney.isDone()) {

//                        if(!taskDrawBackMoney.isDone() && taskCancelOrder.isDone()){
//                            status = false;
//                        }
                    }

                    drawBackMoneyStatus = taskDrawBackMoney.get();
                    changeOrderResult = taskCancelOrder.get();
                    mockLog.printLog("[Cancel Order Service][Cancel Order] Two Process Done");


                    /********************************************************************************/
                    GetOrderByIdInfo localInfo = new GetOrderByIdInfo();
                    localInfo.setOrderId(order.getId().toString());
                    GetOrderResult gor = getOrderByIdFromOrderOther(localInfo, headers);
                    boolean status;
                    if (gor.getOrder().getStatus() != OrderStatus.CANCEL.getCode()) {
                        mockLog.printLog("订单状态不对");
                        status = false;
                    } else {
                        mockLog.printLog("订单状态正确");
                        status = true;
                    }


                    if (changeOrderResult.isStatus() == true && drawBackMoneyStatus == true) {
                        if (status == false) {
                            mockLog.printLog("[Cancel Order Service]Fail. Processes Seq");
                            throw new RuntimeException("[Error Process Seq]");
                        } else {
                            CancelOrderResult finalResult = new CancelOrderResult();
                            finalResult.setStatus(true);
                            finalResult.setMessage("Success.Processes Seq.");
                            mockLog.printLog("[Cancel Order Service]Success.Processes Seq.");
                            return finalResult;
                        }
                    } else if (changeOrderResult.isStatus() == true && drawBackMoneyStatus == false) {
                        throw new RuntimeException("[????] Draw Back Money Fail but Cancel Order Success.");

                    } else if (changeOrderResult.isStatus() == false && drawBackMoneyStatus == true) {
                        throw new RuntimeException("[???????] Draw Back Money Success but Cancel Order Fail.");
                    } else {
                        throw new RuntimeException("[???????] All Fail");
                    }

                } else {
                    CancelOrderResult result = new CancelOrderResult();
                    result.setStatus(false);
                    result.setMessage("Order Status Cancel Not Permitted");
                    mockLog.printLog("[Cancel Order Service][Cancel Order] Order Status Not Permitted.");
                    return result;
                }
            } else {
                CancelOrderResult result = new CancelOrderResult();
                result.setStatus(false);
                result.setMessage("Order Not Found");
                mockLog.printLog("[Cancel Order Service][Cancel Order] Order Not Found.");
                return result;
            }
        }
    }

    public boolean sendEmail(NotifyInfo notifyInfo, HttpHeaders headers) {
        mockLog.printLog("[Cancel Order Service][Send Email]");

        HttpEntity requestEntity = new HttpEntity(notifyInfo, headers);
        ResponseEntity<Boolean> re = restTemplate.exchange(
                "http://ts-notification-service:17853/notification/order_cancel_success",
                HttpMethod.POST,
                requestEntity,
                Boolean.class);
        return re.getBody();
    }

    @Override
    public CalculateRefundResult calculateRefund(CancelOrderInfo info, HttpHeaders headers) {
        GetOrderByIdInfo getFromOrderInfo = new GetOrderByIdInfo();
        getFromOrderInfo.setOrderId(info.getOrderId());
        GetOrderResult orderResult = getOrderByIdFromOrder(getFromOrderInfo, headers);
        if (orderResult.isStatus() == true) {
            Order order = orderResult.getOrder();
            if (order.getStatus() == OrderStatus.NOTPAID.getCode()
                    || order.getStatus() == OrderStatus.PAID.getCode()) {
                if (order.getStatus() == OrderStatus.NOTPAID.getCode()) {
                    CalculateRefundResult result = new CalculateRefundResult();
                    result.setStatus(true);
                    result.setMessage("Success.Calculate Refund Not paid");
                    result.setRefund("0");
                    mockLog.printLog("[Cancel Order][Refund Price] From Order Service.Not Paid.");
                    return result;
                } else {
                    CalculateRefundResult result = new CalculateRefundResult();
                    result.setStatus(true);
                    result.setMessage("Success.Calculate Refund");
                    result.setRefund(calculateRefund(order));
                    mockLog.printLog("[Cancel Order][Refund Price] From Order Service.Paid.");
                    return result;
                }
            } else {
                CalculateRefundResult result = new CalculateRefundResult();
                result.setStatus(false);
                result.setMessage("Order Status Cancel Not Permitted");
                result.setRefund("error");
                mockLog.printLog("[Cancel Order][Refund Price] Order. Cancel Not Permitted.");

                return result;
            }
        } else {
            GetOrderByIdInfo getFromOtherOrderInfo = new GetOrderByIdInfo();
            getFromOtherOrderInfo.setOrderId(info.getOrderId());
            GetOrderResult orderOtherResult = getOrderByIdFromOrderOther(getFromOtherOrderInfo, headers);
            if (orderOtherResult.isStatus() == true) {
                Order order = orderOtherResult.getOrder();
                if (order.getStatus() == OrderStatus.NOTPAID.getCode()
                        || order.getStatus() == OrderStatus.PAID.getCode()) {
                    if (order.getStatus() == OrderStatus.NOTPAID.getCode()) {
                        CalculateRefundResult result = new CalculateRefundResult();
                        result.setStatus(true);
                        result.setMessage("Success.Calculate not pay");
                        result.setRefund("0");
                        mockLog.printLog("[Cancel Order][Refund Price] From Order Other Service.Not Paid.");
                        return result;
                    } else {
                        CalculateRefundResult result = new CalculateRefundResult();
                        result.setStatus(true);
                        result.setMessage("Success.Calculate pay");
                        result.setRefund(calculateRefund(order));
                        mockLog.printLog("[Cancel Order][Refund Price] From Order Other Service.Paid.");
                        return result;
                    }
                } else {
                    CalculateRefundResult result = new CalculateRefundResult();
                    result.setStatus(false);
                    result.setMessage("Order Status Cancel Not Permitted");
                    result.setRefund("error");
                    mockLog.printLog("[Cancel Order][Refund Price] Order Other. Cancel Not Permitted.");
                    return result;
                }
            } else {
                CalculateRefundResult result = new CalculateRefundResult();
                result.setStatus(false);
                result.setMessage("Order Not Found");
                result.setRefund("error");
                mockLog.printLog("[Cancel Order][Refund Price] Order not found.");
                return result;
            }
        }
    }

    private String calculateRefund(Order order) {
        if (order.getStatus() == OrderStatus.NOTPAID.getCode()) {
            return "0.00";
        }
        mockLog.printLog("[Cancel Order] Order Travel Date:" + order.getTravelDate().toString());
        Date nowDate = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(order.getTravelDate());
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(order.getTravelTime());
        int hour = cal2.get(Calendar.HOUR);
        int minute = cal2.get(Calendar.MINUTE);
        int second = cal2.get(Calendar.SECOND);
        Date startTime = new Date(year,
                month,
                day,
                hour,
                minute,
                second);
        mockLog.printLog("[Cancel Order] nowDate  :" + nowDate.toString());
        mockLog.printLog("[Cancel Order] startTime:" + startTime.toString());
        if (nowDate.after(startTime)) {
            mockLog.printLog("[Cancel Order] Ticket expire refund 0");
            return "0";
        } else {
            double totalPrice = Double.parseDouble(order.getPrice());
            double price = totalPrice * 0.8;
            DecimalFormat priceFormat = new java.text.DecimalFormat("0.00");
            String str = priceFormat.format(price);
            mockLog.printLog("[Cancel Order]calculate refund - " + str);
            return str;
        }
    }


    private ChangeOrderResult cancelFromOrder(ChangeOrderInfo info, HttpHeaders headers) {
        mockLog.printLog("[Cancel Order Service][Change Order Status] Changing....");
        HttpEntity<ChangeOrderInfo> httpEntity = new HttpEntity<>(info, headers);
        return restTemplate.exchange("http://ts-order-service:12031/order/update",
                HttpMethod.POST, httpEntity, ChangeOrderResult.class).getBody();
    }

    private ChangeOrderResult cancelFromOtherOrder(ChangeOrderInfo info, HttpHeaders headers) {
        mockLog.printLog("[Cancel Order Service][Change Order Status] Changing....");
        HttpEntity<ChangeOrderInfo> requestEntity = new HttpEntity<>(info, headers);
        ResponseEntity<ChangeOrderResult> re = restTemplate.exchange(
                "http://ts-order-other-service:12032/orderOther/update",
                HttpMethod.POST,
                requestEntity,
                ChangeOrderResult.class);
        ChangeOrderResult result = re.getBody();
        return result;
    }

    public boolean drawbackMoney(String money, String userId, HttpHeaders headers) {
        mockLog.printLog("[Cancel Order Service][Draw Back Money] Draw back money...");
        DrawBackInfo info = new DrawBackInfo();
        info.setMoney(money);
        info.setUserId(userId);
        HttpEntity<DrawBackInfo> requestEntity = new HttpEntity<>(info, headers);
        ResponseEntity<String> re = restTemplate.exchange(
                "http://ts-inside-payment-service:18673/inside_payment/drawBack",
                HttpMethod.POST,
                requestEntity,
                String.class);
        String result = re.getBody();
        if (result.equals("true")) {
            return true;
        } else {
            return false;
        }
    }

    public GetAccountByIdResult getAccount(GetAccountByIdInfo info, HttpHeaders headers) {
        mockLog.printLog("[Cancel Order Service][Get By Id]");
        HttpEntity requestEntity = new HttpEntity(info, headers);
        ResponseEntity<GetAccountByIdResult> re = restTemplate.exchange(
                "http://ts-sso-service:12349/account/findById",
                HttpMethod.POST,
                requestEntity,
                GetAccountByIdResult.class);
        GetAccountByIdResult result = re.getBody();
        return result;
    }

    private GetOrderResult getOrderByIdFromOrder(GetOrderByIdInfo info, HttpHeaders headers) {
        mockLog.printLog("[Cancel Order Service][Get Order] Getting....");
        HttpEntity<GetOrderByIdInfo> requestEntity = new HttpEntity<>(info, headers);
        ResponseEntity<GetOrderResult> re = restTemplate.exchange(
                "http://ts-order-service:12031/order/getById/",
                HttpMethod.POST,
                requestEntity,
                GetOrderResult.class);
        GetOrderResult cor = re.getBody();
        return cor;
    }

    private GetOrderResult getOrderByIdFromOrderOther(GetOrderByIdInfo info, HttpHeaders headers) {
        mockLog.printLog("[Cancel Order Service][Get Order] Getting....");
        HttpEntity requestEntity = new HttpEntity(info, headers);
        ResponseEntity<GetOrderResult> re = restTemplate.exchange(
                "http://ts-order-other-service:12032/orderOther/getById/",
                HttpMethod.POST,
                requestEntity,
                GetOrderResult.class);
        GetOrderResult cor = re.getBody();
        return cor;

    }

}
