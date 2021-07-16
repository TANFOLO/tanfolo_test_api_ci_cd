package com.kamtar.transport.api;

import java.util.concurrent.Executor;

import javax.servlet.Filter;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.filter.ShallowEtagHeaderFilter;

@Configuration
@EnableAsync(proxyTargetClass=true) //detects @Async annotation
public class SpringAsyncConfig implements AsyncConfigurer {

 public Executor threadPoolTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10); // create 10 Threads at the time of initialization
        executor.setQueueCapacity(10); // queue capacity
        executor.setMaxPoolSize(25); // if queue is full, then it will create new thread and go till 25
        executor.setThreadNamePrefix("KAMTAR-TRANSPORT-API");
        executor.initialize();//Set up the ExecutorService.
        return executor;
    }

    @Override
    public Executor getAsyncExecutor() {
        return threadPoolTaskExecutor();
    }
    
    /**
     *  etag http headers
     *  https://www.javadevjournal.com/spring/etags-for-rest-with-spring/
     * @return
     */
    @Bean
    public Filter shallowEtagHeaderFilter() {
      return new ShallowEtagHeaderFilter();
    }

} 