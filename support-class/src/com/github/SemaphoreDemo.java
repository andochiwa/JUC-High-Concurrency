package com.github;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * @author HAN
 * @version 1.0
 * @create 2021/4/3
 */
public class SemaphoreDemo {

    public static void main(String[] args){
        Semaphore semaphore = new Semaphore(3);

        for (int i = 0; i < 10; i++) {
            new Thread(() -> {
                try {
                    // 控制住一个位置
                    semaphore.acquire();
                    System.out.println(Thread.currentThread().getName() + "\t抢到了");
                    TimeUnit.SECONDS.sleep(2);
                    System.out.println(Thread.currentThread().getName() + "\t离开了");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    // 释放这个位置
                    semaphore.release();
                }
            }).start();
        }
    }
}
