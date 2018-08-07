package ticketinfo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ticketinfo.async.AsyncTask;
import ticketinfo.domain.QueryForStationId;
import ticketinfo.domain.QueryForTravel;
import ticketinfo.domain.ResultForTravel;

import java.util.concurrent.TimeUnit;

/**
 * Created by Chenjie Xu on 2017/6/6.
 */
@Service
public class TicketInfoServiceImpl implements TicketInfoService{

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private AsyncTask asyncTask;

    @Override
    public ResultForTravel queryForTravel(QueryForTravel info,HttpHeaders headers)throws Exception{
//        HttpEntity requestEntity = new HttpEntity(info,headers);
//        ResponseEntity<ResultForTravel> re = restTemplate.exchange(
//                "http://ts-basic-service:15680/basic/queryForTravel",
//                HttpMethod.POST,
//                requestEntity,
//                ResultForTravel.class);
//        ResultForTravel result = re.getBody();
////        ResultForTravel result = restTemplate.postForObject(
////                "http://ts-basic-service:15680/basic/queryForTravel", info, ResultForTravel.class);
//        return result;

//        try{
            return asyncTask.queryForTravel(info, headers);
//        }catch(Exception e){
//            //e.printStackTrace();
//            throw e;
//        }
    }

    @Override
    public String queryForStationId(QueryForStationId info,HttpHeaders headers){
        HttpEntity requestEntity = new HttpEntity(info,headers);
        ResponseEntity<String> re = restTemplate.exchange(
                "http://ts-basic-service:15680/basic/queryForStationId",
                HttpMethod.POST,
                requestEntity,
                String.class);
        String id = re.getBody();
//        String id = restTemplate.postForObject(
//                "http://ts-basic-service:15680/basic/queryForStationId", info,String.class);
        return id;
    }
}
