package sso.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;
import sso.config.MockLog;
import sso.domain.*;
import sso.service.AccountSsoService;

import java.util.UUID;

@RestController
public class AccountSsoController {

    @Autowired
    private AccountSsoService ssoService;
    @Autowired
    MockLog mockLog;


    @RequestMapping(path = "/welcome", method = RequestMethod.GET)
    public String home() {
//        Account acc = new Account();
//        acc.setDocumentType(DocumentType.ID_CARD.getCode());
//        acc.setDocumentNum("DefaultDocumentNumber");
//        acc.setEmail("fdse_microservices@163.com");
//        acc.setPassword("DefaultPassword");
//        acc.setName("Default User");
//        acc.setGender(Gender.MALE.getCode());
//        acc.setId(UUID.fromString("4d2a46c7-71cb-4cf1-b5bb-b68406d9da6f"));
//        ssoService.createAccount(acc);
        return "Welcome to [ Accounts SSO Service ] !";
    }

    /***************For super admin(Single Service Test*******************/
    @RequestMapping(path = "/account/findAll", method = RequestMethod.GET)
    public FindAllAccountResult findAllAccount(){
        return ssoService.findAllAccount();
    }

    @RequestMapping(path = "/account/findAllLogin", method = RequestMethod.GET)
    public GetLoginAccountList findAllLoginAccount(){
        return ssoService.findAllLoginAccount();
    }

    @RequestMapping(path = "/account/modify", method = RequestMethod.POST)
    public ModifyAccountResult modifyAccount(@RequestBody ModifyAccountInfo modifyAccountInfo){
        return ssoService.saveChanges(modifyAccountInfo);
    }

    /***************************For Normal Use***************************/
    @RequestMapping(path = "/account/register", method = RequestMethod.POST)
    public RegisterResult createNewAccount(@RequestBody RegisterInfo ri){
        return ssoService.create(ri);
    }

    @RequestMapping(path = "/account/login", method = RequestMethod.POST)
    public LoginResult login(@RequestBody LoginInfo li, @RequestHeader HttpHeaders headers) {
        mockLog.printLog(String.format("The headers in sso service is %s", headers.toString()));
        LoginResult lr = ssoService.login(li);
        if(lr.getStatus() == false){
            mockLog.printLog("[SSO Service][Login] Login Fail. No token generate.");
            return lr;
        }else{
            //Post token to the sso
            mockLog.printLog("[SSO Service][Login] Password Right. Put token to sso.");
            PutLoginResult tokenResult = loginPutToken(lr.getAccount().getId().toString());
            mockLog.printLog("[SSO Service] PutLoginResult Status: " + tokenResult.isStatus());
            if(tokenResult.isStatus() == true){
                mockLog.printLog("[SSO Service][Login] Post to sso:" + tokenResult.getToken());
                lr.setToken(tokenResult.getToken());
                lr.setMessage(tokenResult.getMsg());
            }else{
                mockLog.printLog("[SSO Service][Login] Token Result Fail.");
                lr.setToken(null);
                lr.setStatus(false);
                lr.setMessage(tokenResult.getMsg());
                lr.setAccount(null);
            }
            return lr;
        }
    }

    @RequestMapping(path = "/logout", method = RequestMethod.POST)
    public LogoutResult logoutDeleteToken(@RequestBody LogoutInfo li){
        mockLog.printLog("[SSO Service][Logout Delete Token] ID:" + li.getId() + "Token:" + li.getToken());
        return ssoService.logoutDeleteToken(li);
    }

    @RequestMapping(path = "/account/findById", method = RequestMethod.POST)
    public GetAccountByIdResult getAccountById(@RequestBody GetAccountByIdInfo info){
        mockLog.printLog("[SSO Service][Find Account By Id] Account Id:" + info.getAccountId());
        return ssoService.getAccountById(info);
    }

    @RequestMapping(path = "/verifyLoginToken/{token}", method = RequestMethod.GET)
    public VerifyResult verifyLoginToken(@PathVariable String token){
        return ssoService.verifyLoginToken(token);
    }

    public PutLoginResult loginPutToken(String loginId){
        return ssoService.loginPutToken(loginId);
    }

    //Admin login
    @CrossOrigin(origins = "*")
    @RequestMapping(path = "/account/adminlogin", method = RequestMethod.POST)
    public Contacts adminLogin(@RequestBody AdminLoginInfo ali){
        mockLog.printLog("[SSO Service][Admin Login]");
        return ssoService.adminLogin(ali.getName(), ali.getPassword());
    }

    @CrossOrigin(origins = "*")
    @RequestMapping(path = "/account/admindelete", method = RequestMethod.POST)
    public DeleteAccountResult adminDelete(@RequestBody AdminDeleteAccountRequest request){
        mockLog.printLog("[SSO Service][Admin Delete Account]");
        return ssoService.deleteAccount(request.getAccountId());
    }

}
