package ticketinfo.async;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ticketinfo.domain.QueryForTravel;
import ticketinfo.domain.ResultForTravel;

import java.util.concurrent.Future;

@Component  
public class AsyncTask {  

    @Autowired
	private RestTemplate restTemplate;

    public static int size;

    @Async("mySimpleAsync")
//    public Future<ResultForTravel> queryForTravel(QueryForTravel info, HttpHeaders headers){
    public ResultForTravel queryForTravel(QueryForTravel info, HttpHeaders headers) throws Exception{
        size += 1;
        System.out.println("[Ticket Info] Thread Size: " + size);
        HttpEntity requestEntity = new HttpEntity(info,headers);
        try{
            ResponseEntity<ResultForTravel> re = restTemplate.exchange(
                    "http://ts-basic-service:15680/basic/queryForTravel",
                    HttpMethod.POST,
                    requestEntity,
                    ResultForTravel.class);
            ResultForTravel result = re.getBody();
//        ResultForTravel result = restTemplate.postForObject(
//                "http://ts-basic-service:15680/basic/queryForTravel", info, ResultForTravel.class);
            size -= 1;
//        return new AsyncResult<>(result);
            return result;
        }catch (Exception e){
            throw e;
        }

    }

}  
