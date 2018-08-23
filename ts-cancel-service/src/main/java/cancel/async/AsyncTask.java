package cancel.async;

import cancel.config.MockLog;
import cancel.domain.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.Future;


/**
 * Asynchronous Tasks
 *
 * @author Xu
 */
@Component
public class AsyncTask {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    MockLog mockLog;
    ThreadLocal<String> parentSpanId = new ThreadLocal<String>();
    ThreadLocal<String> requestId = new ThreadLocal<String>();
    ThreadLocal<String> traceId = new ThreadLocal<String>();
    ThreadLocal<String> spanId = new ThreadLocal<String>();

    @Async("myAsync")
    public Future<ChangeOrderResult> updateOtherOrderStatusToCancel(ChangeOrderInfo info, HttpHeaders headers) throws InterruptedException {
        Thread.sleep(4000);

        Map<String, String> map = headers.toSingleValueMap();
        String rid = map.get("x-request-id") != null ? map.get("x-request-id") : "";
        String tid = map.get("x-b3-traceid") != null ? map.get("x-b3-traceid") : "";
        String sid = map.get("x-b3-spanid") != null ? map.get("x-b3-spanid") : "";
        String psid = map.get("x-b3-parentspanid") != null ? map.get("x-b3-parentspanid") : "";
        requestId.set(rid);
        traceId.set(tid);
        spanId.set(sid);
        parentSpanId.set(psid);

        mockLog.printLog("[Cancel Order Service][Change Order Status]");
//        ChangeOrderResult result = restTemplate.postForObject("http://ts-order-other-service:12032/orderOther/update", info, ChangeOrderResult.class);
        HttpEntity requestEntity = new HttpEntity(info, headers);
        ResponseEntity<ChangeOrderResult> re = restTemplate.exchange(
                "http://ts-order-other-service:12032/orderOther/update",
                HttpMethod.POST,
                requestEntity,
                ChangeOrderResult.class);
        return new AsyncResult<>(re.getBody());

    }

    @Async("mySimpleAsync")
    public Future<Boolean> drawBackMoneyForOrderCancel(String money, String userId, String orderId, String loginToken, HttpHeaders headers) throws InterruptedException {

        Map<String, String> map = headers.toSingleValueMap();
        String rid = map.get("x-request-id") != null ? map.get("x-request-id") : "";
        String tid = map.get("x-b3-traceid") != null ? map.get("x-b3-traceid") : "";
        String sid = map.get("x-b3-spanid") != null ? map.get("x-b3-spanid") : "";
        String psid = map.get("x-b3-parentspanid") != null ? map.get("x-b3-parentspanid") : "";
        requestId.set(rid);
        traceId.set(tid);
        spanId.set(sid);
        parentSpanId.set(psid);

        /*********************** Fault Reproduction - Error Process Seq *************************/
        double op = new Random().nextDouble();
        if (op < 0.5) {
            mockLog.printLog("[Cancel Order Service] Delay Process，Wrong Cancel Process");
            Thread.sleep(8000);
        } else {
            mockLog.printLog("[Cancel Order Service] Normal Process，Normal Cancel Process");
        }


        //1.Search Order Info
        mockLog.printLog("[Cancel Order Service][Get Order] Getting....");
        GetOrderByIdInfo getOrderInfo = new GetOrderByIdInfo();
        getOrderInfo.setOrderId(orderId);

//        GetOrderResult cor = restTemplate.postForObject(
//                "http://ts-order-other-service:12032/orderOther/getById/"
//                , getOrderInfo, GetOrderResult.class);

        HttpEntity requestEntity = new HttpEntity(getOrderInfo, headers);
        ResponseEntity<GetOrderResult> re = restTemplate.exchange(
                "http://ts-order-other-service:12032/orderOther/getById",
                HttpMethod.POST,
                requestEntity,
                GetOrderResult.class);
        GetOrderResult cor = re.getBody();

        Order order = cor.getOrder();
        //2.Change order status to cancelling
        order.setStatus(OrderStatus.Canceling.getCode());
        ChangeOrderInfo changeOrderInfo = new ChangeOrderInfo();
        changeOrderInfo.setOrder(order);
        changeOrderInfo.setLoginToken(loginToken);
//        ChangeOrderResult changeOrderResult = restTemplate.postForObject("http://ts-order-other-service:12032/orderOther/update", changeOrderInfo, ChangeOrderResult.class);

        requestEntity = new HttpEntity(changeOrderInfo, headers);
        ResponseEntity<ChangeOrderResult> re2 = restTemplate.exchange(
                "http://ts-order-other-service:12032/orderOther/update",
                HttpMethod.POST,
                requestEntity,
                ChangeOrderResult.class);
        ChangeOrderResult changeOrderResult = re2.getBody();

        if (changeOrderResult.isStatus() == false) {
            mockLog.printLog("[Cancel Order Service]Unexpected error");
        }
        //3.do drawback money
        mockLog.printLog("[Cancel Order Service][Draw Back Money] Draw back money...");
        DrawBackInfo info = new DrawBackInfo();
        info.setMoney(money);
        info.setUserId(userId);
//        String result = restTemplate.postForObject("http://ts-inside-payment-service:18673/inside_payment/drawBack", info, String.class);

        requestEntity = new HttpEntity(info, headers);
        ResponseEntity<String> re3 = restTemplate.exchange(
                "http://ts-inside-payment-service:18673/inside_payment/drawBack",
                HttpMethod.POST,
                requestEntity,
                String.class);
        String result = re3.getBody();

        if (result.equals("true")) {
            return new AsyncResult<>(true);
        } else {
            return new AsyncResult<>(false);
        }
        /*****************************************************************************/
    }

}  
