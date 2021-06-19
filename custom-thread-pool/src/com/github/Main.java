package com.github;

import java.util.concurrent.TimeUnit;

/**
 * @author HAN
 * @create 06-19-19:06
 * @version 1.0
 */
public class Main {
    public static void main(String[] args){
        ThreadPool threadPool = new ThreadPool(2, 5, 10, TimeUnit.SECONDS, 5);
        for (int i = 0; i < 20; i++) {
            int temp = i;
            threadPool.execute(() -> {
                System.out.println(Thread.currentThread() + "\t" + temp);
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        }
    }
}
