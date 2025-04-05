package com.xz.aiTest.utils;

import io.reactivex.Flowable;
import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;

import java.util.concurrent.TimeUnit;

/**
 * RxJava 响应式编程学习
 */
public class RxJavaUtils {

    public static void main(String[] args) {
        // RxJava 响应式编程学习
        // RxJava 是一个基于事件流的异步编程库，它提供了一个丰富的操作符，可以帮助我们简化异步编程的复杂性。
        // RxJava 的核心是 Observable 和 Observer，Observable 表示事件流，Observer 表示事件的消费者。
        // RxJava 提供了很多操作符，可以帮助我们对事件流进行处理，比如 map、filter、reduce 等。
        // RxJava 还提供了线程切换的操作符，可以帮助我们在不同的线程中处理事件流。
        // RxJava 还提供了错误处理的操作符，可以帮助我们处理异常事件。
        // RxJava 还提供了背压处理的操作符，可以帮助我们处理事件流速度不一致的问题。
        // RxJava 还提供了很多其他的操作符，可以帮助我们处理各种复杂的场景。

        // RxJava 的核心是 Observable 和 Observer，Observable 表示事件流，Observer 表示事件的消费者。
//创建数据流
        Flowable<Long> flowable = Flowable.interval(1, TimeUnit.SECONDS)
                .map(i -> i + 1)
                //.subscribe();
                .subscribeOn(Schedulers.io());
        //订阅
        flowable.observeOn(Schedulers.io())
                .doOnNext(System.out::println)
                .subscribe();
        try {
            Thread.sleep(1000*60);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }
}
