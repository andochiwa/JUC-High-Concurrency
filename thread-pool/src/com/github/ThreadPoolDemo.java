package com.github;

import java.util.concurrent.*;

/**
 * @author HAN
 * @version 1.0
 * @create 2021/4/3
 */
public class ThreadPoolDemo {

    public static void main(String[] args){
//        ExecutorService threadPool = Executors.newFixedThreadPool(5);
//        ExecutorService threadPool = Executors.newSingleThreadExecutor();
//        ExecutorService threadPool = Executors.newCachedThreadPool();
//        ExecutorService threadPool = Executors.newScheduledThreadPool(5);
        ExecutorService threadPool =
                new ThreadPoolExecutor(5,
                        7,
                        20,
                        TimeUnit.SECONDS,
                        new LinkedBlockingQueue<>(3),
                        Executors.defaultThreadFactory(),
                        new ThreadPoolExecutor.CallerRunsPolicy()); // 自定义

        try {
            for (int i = 0; i < 20; i++) {
                threadPool.execute(() -> {
                    System.out.println(Thread.currentThread().getName() + "进入");
                    try {
                        TimeUnit.MILLISECONDS.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });
//                try {
//                    TimeUnit.MILLISECONDS.sleep(500);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
            }
        } finally {
            threadPool.shutdown();
        }

    }
}
