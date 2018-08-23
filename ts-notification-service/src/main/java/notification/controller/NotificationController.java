package notification.controller;

import notification.domain.NotifyInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

/**
 * Created by Wenyi on 2017/6/15.
 */
@RestController
public class NotificationController {

    @RequestMapping(value="/notification/preserve_success", method = RequestMethod.POST)
    public boolean preserve_success(@RequestBody NotifyInfo info, @RequestHeader HttpHeaders headers){
        return true;
    }

    @RequestMapping(value="/notification/order_create_success", method = RequestMethod.POST)
    public boolean order_create_success(@RequestBody NotifyInfo info, @RequestHeader HttpHeaders headers){
        return true;
    }

    @RequestMapping(value="/notification/order_changed_success", method = RequestMethod.POST)
    public boolean order_changed_success(@RequestBody NotifyInfo info, @RequestHeader HttpHeaders headers){
        return true;
    }

    @RequestMapping(value="/notification/order_cancel_success", method = RequestMethod.POST)
    public boolean order_cancel_success(@RequestBody NotifyInfo info, @RequestHeader HttpHeaders headers){
        return true;
    }
}
