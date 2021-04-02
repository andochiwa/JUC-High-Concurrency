package com.github;

import java.util.concurrent.CountDownLatch;

/**
 * @author HAN
 * @version 1.0
 * @create 2021/4/3
 */
public class CountDownLatchDemo {

    public static void main(String[] args) throws InterruptedException {
        // 计数器
        CountDownLatch countDownLatch = new CountDownLatch(10);
        for (int i = 0; i < 10; i++) {
            new Thread(() -> {
                System.out.println(Thread.currentThread().getName() + " come out");
                // 计数
                countDownLatch.countDown();
            }).start();
        }
        // 在计数结束之前阻塞
        countDownLatch.await();
        System.out.println(Thread.currentThread().getName() + " close door");
    }
}
