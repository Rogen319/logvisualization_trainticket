package cancel.controller;

import cancel.config.MockLog;
import cancel.domain.*;
import cancel.service.CancelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
public class CancelController {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    CancelService cancelService;

    @Autowired
    MockLog mockLog;

    @CrossOrigin(origins = "*")
    @RequestMapping(path = "/cancelCalculateRefund", method = RequestMethod.POST)
    public CalculateRefundResult calculate(@RequestBody CancelOrderInfo info, @RequestHeader HttpHeaders headers){
        mockLog.printLog("[Cancel Order Service][Calculate Cancel Refund] OrderId:" + info.getOrderId());
        return cancelService.calculateRefund(info, headers);
    }

    @CrossOrigin(origins = "*")
    @RequestMapping(path = "/cancelOrder", method = RequestMethod.POST)
    public CancelOrderResult cancelTicket(@RequestBody CancelOrderInfo info, @CookieValue String loginToken, @CookieValue String loginId, @RequestHeader HttpHeaders headers) throws Exception {
        mockLog.printLog("[Cancel Order Service][Cancel Ticket] info:" + info.getOrderId());
        if(loginToken == null ){
            loginToken = "admin";
        }
        mockLog.printLog("[Cancel Order Service][Cancel Order] order ID:" + info.getOrderId() + "  loginToken:" + loginToken);
        if(loginToken == null){
            mockLog.printLog("[Cancel Order Service][Cancel Order] Not receive any login token");
            CancelOrderResult result = new CancelOrderResult();
            result.setStatus(false);
            result.setMessage("No Login Token");
            return result;
        }
        VerifyResult verifyResult = verifySsoLogin(loginToken);
        if(verifyResult.isStatus() == false){
            mockLog.printLog("[Cancel Order Service][Cancel Order] Do not login.");
            CancelOrderResult result = new CancelOrderResult();
            result.setStatus(false);
            result.setMessage("Not Login");
            return result;
        }else{
            mockLog.printLog("[Cancel Order Service][Cancel Ticket] Verify Success");

//                return cancelService.cancelOrder(info,loginToken,loginId, headers);
                GetAccountByIdInfo getAccountByIdInfo = new GetAccountByIdInfo();
                getAccountByIdInfo.setAccountId(loginId);
                GetAccountByIdResult result = restTemplate.postForObject(
                        "http://ts-sso-service:12349/account/findById",
                        getAccountByIdInfo,
                        GetAccountByIdResult.class
                );
                Account account = result.getAccount();
                if(account.getName().contains("VIP")){
                    return cancelService.cancelOrder(info,loginId,loginId, headers);

                }else{
                    return cancelService.cancelOrder(info,loginToken,loginId, headers);
                }



        }
    }

    private VerifyResult verifySsoLogin(String loginToken){
        mockLog.printLog("[Cancel Order Service][Verify Login] Verifying....");
        VerifyResult tokenResult = restTemplate.getForObject(
                "http://ts-sso-service:12349/verifyLoginToken/" + loginToken,
                VerifyResult.class);
        return tokenResult;
    }

}
