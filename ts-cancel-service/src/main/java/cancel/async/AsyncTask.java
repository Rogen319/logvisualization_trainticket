package cancel.async;

import cancel.config.MockLog;
import cancel.domain.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Random;
import java.util.concurrent.Future;


/**
 * Asynchronous Tasks
 *
 * @author Xu
 */
@Component
public class AsyncTask {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    MockLog mockLog;

    @Async("myAsync")
    public Future<ChangeOrderResult> updateOtherOrderStatusToCancel(ChangeOrderInfo info) throws InterruptedException {

        Thread.sleep(4000);

        mockLog.printLog("[Cancel Order Service][Change Order Status]");
        ChangeOrderResult result = restTemplate.postForObject("http://ts-order-other-service:12032/orderOther/update", info, ChangeOrderResult.class);
        return new AsyncResult<>(result);

    }

    @Async("mySimpleAsync")
    public Future<Boolean> drawBackMoneyForOrderCancel(String money, String userId, String orderId, String loginToken) throws InterruptedException {

        /*********************** Fault Reproduction - Error Process Seq *************************/
        double op = new Random().nextDouble();
        if (op < 1.0) {
            mockLog.printLog("[Cancel Order Service] Delay Process，Wrong Cancel Process");
            Thread.sleep(8000);
        } else {
            mockLog.printLog("[Cancel Order Service] Normal Process，Normal Cancel Process");
        }


        //1.Search Order Info
        mockLog.printLog("[Cancel Order Service][Get Order] Getting....");
        GetOrderByIdInfo getOrderInfo = new GetOrderByIdInfo();
        getOrderInfo.setOrderId(orderId);
        GetOrderResult cor = restTemplate.postForObject(
                "http://ts-order-other-service:12032/orderOther/getById/"
                , getOrderInfo, GetOrderResult.class);
        Order order = cor.getOrder();
        //2.Change order status to cancelling
        order.setStatus(OrderStatus.Canceling.getCode());
        ChangeOrderInfo changeOrderInfo = new ChangeOrderInfo();
        changeOrderInfo.setOrder(order);
        changeOrderInfo.setLoginToken(loginToken);
        ChangeOrderResult changeOrderResult = restTemplate.postForObject("http://ts-order-other-service:12032/orderOther/update", changeOrderInfo, ChangeOrderResult.class);
        if (changeOrderResult.isStatus() == false) {
            mockLog.printLog("[Cancel Order Service]Unexpected error");
        }
        //3.do drawback money
        mockLog.printLog("[Cancel Order Service][Draw Back Money] Draw back money...");
        DrawBackInfo info = new DrawBackInfo();
        info.setMoney(money);
        info.setUserId(userId);
        String result = restTemplate.postForObject("http://ts-inside-payment-service:18673/inside_payment/drawBack", info, String.class);
        if (result.equals("true")) {
            return new AsyncResult<>(true);
        } else {
            return new AsyncResult<>(false);
        }
        /*****************************************************************************/
    }

}  
