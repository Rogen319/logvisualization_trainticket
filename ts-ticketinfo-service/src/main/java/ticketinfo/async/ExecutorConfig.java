package ticketinfo.async;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration  
@EnableAsync  
public class ExecutorConfig {  
  
    /** Set the ThreadPoolExecutor's core pool size. */  
    private int corePoolSize = 2;
    /** Set the ThreadPoolExecutor's maximum pool size. */ 
    private int maxPoolSize = 3;
    /** Set the capacity for the ThreadPoolExecutor's BlockingQueue. */  
    private int queueCapacity = 10;
  
    @Bean  
    public Executor mySimpleAsync() {  
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();  
        executor.setCorePoolSize(corePoolSize);  
        executor.setMaxPoolSize(maxPoolSize);  
        executor.setQueueCapacity(queueCapacity);  
        executor.setThreadNamePrefix("MySimpleExecutor-");  
        executor.initialize();  
        return executor;  
    }  

}
