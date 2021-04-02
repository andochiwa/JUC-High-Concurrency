package com.github;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

/**
 * @author HAN
 * @version 1.0
 * @create 2021/4/3
 */
public class CallableDemo {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        // FutureTask类实现了RunnableFuture接口，而RunnableFuture又继承与Runnable，所以可以直接放进Thread的构造器中
        FutureTask<String> futureTask = new FutureTask<>(new callable());
        // 当futureTask为同一个实例时，多个线程也只会调用一个，结果复用
        new Thread(futureTask, "A").start();
        new Thread(futureTask, "B").start();
        new Thread(futureTask, "C").start();
        new Thread(futureTask, "D").start();
        new Thread(futureTask, "E").start();

        // 可以获得call方法里的返回值
        System.out.println(futureTask.get());
    }

}

class callable implements Callable<String> {

    @Override
    public String call() throws Exception {
        for (int i = 0; i < 10; i++) {
            System.out.println(Thread.currentThread().getName() + " come in");
        }
        return "hello world";
    }
}
