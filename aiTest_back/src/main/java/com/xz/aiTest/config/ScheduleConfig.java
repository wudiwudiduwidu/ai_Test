package com.xz.aiTest.config;


import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;


@Configuration
public class ScheduleConfig {
    private final AtomicInteger threadNumber = new AtomicInteger(1);
    // 自定义线程
    @Bean
    public Scheduler vipScheduler() {

        ThreadFactory threadFactory = new ThreadFactory() {
            @Override
            public Thread newThread(@NotNull Runnable r) {
                Thread t = new Thread(r,"vip-scheduler-"+threadNumber.getAndAdd(1));
                //非守护线程
                t.setDaemon(false);
                return t;
            }
        };


        //自定义线程池
        ExecutorService scheduledExecutorService = new ThreadPoolExecutor(5, 7, 5, TimeUnit.SECONDS,new LinkedBlockingQueue<>(100),threadFactory,
                new ThreadPoolExecutor.AbortPolicy());
        return Schedulers.from(scheduledExecutorService);

    }
}
