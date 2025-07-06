package labs.externalmicroservice.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.task.DelegatingSecurityContextAsyncTaskExecutor;
import org.springframework.stereotype.Component;

@Component
@Configuration
@EnableAsync
public class EnableSecurityCtxForThreadPool {

    @Bean
    @Primary
    public ThreadPoolTaskExecutor threadPoolTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(8);
        executor.setMaxPoolSize(15);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("async-");
        //executor.initialize();
// this is not need as stated above because ThreadPoolTaskExecutor extends ExecutorConfigurationSupport
//        which again extends InitializingBean which call this method automatically using @Overidded afterPropertiesSet method
        return executor;
    }

    @Bean("abcTaskExecutor")
    public DelegatingSecurityContextAsyncTaskExecutor taskExecutor(ThreadPoolTaskExecutor delegate) {
        System.out.println("delegate");
        return new DelegatingSecurityContextAsyncTaskExecutor(delegate);
    }
}