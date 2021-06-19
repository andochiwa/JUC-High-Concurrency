package com.github;

import java.util.concurrent.TimeUnit;

/**
 * @author HAN
 * @version 1.0
 * @create 06-19-19:06
 */
public class Main {
    public static void main(String[] args) {
        ThreadPool threadPool = new ThreadPool(2,
                5,
                10,
                TimeUnit.SECONDS,
                5,
                (task, thread) -> {
                    new Thread(() -> {
                        System.out.println(Thread.currentThread() + "\t拒绝策略额外线程正在执行任务 " + task);
                        task.run();
                    }).start();
                });
        for (int i = 0; i < 20; i++) {
            int temp = i;
            threadPool.execute(() -> {
                System.out.println(Thread.currentThread() + "\t任务" + temp);
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        }
    }
}
