package labs.externalmicroservice.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.task.DelegatingSecurityContextAsyncTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

//@Configuration
//@EnableAsync
//public class AsyncConfig implements AsyncConfigurer {
//
//    @Override
//    @Bean("threadPoolTaskExecutor")
//    public Executor getAsyncExecutor() {
////        Executor executor = Executors.newCachedThreadPool();
//        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
//        executor.setCorePoolSize(20);
//        executor.setMaxPoolSize(1000);
//        executor.setWaitForTasksToCompleteOnShutdown(true);
//        executor.setThreadNamePrefix("Async-");
//        executor.initialize();
//        return new DelegatingSecurityContextAsyncTaskExecutor(executor);
//    }
//}



//@Bean
//public DelegatingSecurityContextAsyncTaskExecutor taskExecutor(ThreadPoolTaskExecutor delegate) {
//    return new DelegatingSecurityContextAsyncTaskExecutor(delegate);
//}
