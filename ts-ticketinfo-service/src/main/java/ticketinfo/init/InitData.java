package ticketinfo.init;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class InitData implements CommandLineRunner{

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public void run(String... args)throws Exception{
        //设置超时
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(10*1000);
        requestFactory.setReadTimeout(10*1000);
        restTemplate.setRequestFactory(requestFactory);
    }
}
