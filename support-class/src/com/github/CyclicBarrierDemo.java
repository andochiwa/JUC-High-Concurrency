package com.github;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

/**
 * @author HAN
 * @version 1.0
 * @create 2021/4/3
 */
public class CyclicBarrierDemo {

    public static void main(String[] args){
        CyclicBarrier cyclicBarrier = new CyclicBarrier(7, () -> {
            System.out.println("7人start");
        });

        for (int i = 0; i < 7; i++) {
            int j = i;
            new Thread(() -> {
                System.out.println(Thread.currentThread().getName() + "\t第" + (j + 1) + "个人到了");
                try {
                    cyclicBarrier.await();
                } catch (InterruptedException | BrokenBarrierException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }
}
