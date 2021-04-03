package com.github;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.stream.LongStream;

/**
 * @author HAN
 * @version 1.0
 * @create 2021/4/4
 */
public class ForkJoinPoolDemo {

    private static long start = 1L;
    private static long end = 10000000000L;
    private static long critical = 10000L;

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        ForkJoinPool forkJoinPool = new ForkJoinPool();
        long left = System.currentTimeMillis();
        // 使用ForkJoin
//        ForkJoinTask<Long> submit = forkJoinPool.submit(new MyForkJoinTask(start, end));
//        long sum = submit.get();
        // 使用流式计算
        long sum = LongStream.rangeClosed(start, end).parallel().reduce(0, Long::sum);
        long right = System.currentTimeMillis();
        System.out.println("结果为：" + sum + "\t花费时间：" + (right - left));
    }

    public static class MyForkJoinTask extends RecursiveTask<Long> {

        private long startValue;
        private long endValue;


        public MyForkJoinTask(long startValue, long endValue) {
            this.startValue = startValue;
            this.endValue = endValue;
        }

        @Override
        protected Long compute() {
            // 如果足够小说明不再需要任务拆分
            if (endValue - startValue < critical) {
//                System.out.println("开始计算: " + startValue +"到" + endValue);
                long res = 0;
                for (long i = startValue; i <= endValue; i++) {
                    res += i;
                }
                return res;
            }
            // 划分任务
            long mid = endValue - (endValue - startValue) / 2;
            MyForkJoinTask task1 = new MyForkJoinTask(startValue, mid);
//            task1.fork();
            MyForkJoinTask task2 = new MyForkJoinTask(mid + 1, endValue);
//            task2.fork();
            // 不应该用fork，fork拆分的任务不会并发执行
            invokeAll(task1, task2);

            return task1.join() + task2.join();
        }
    }
}
