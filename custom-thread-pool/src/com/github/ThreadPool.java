package com.github;

import java.util.HashSet;
import java.util.concurrent.TimeUnit;

/**
 * @author HAN
 * @version 1.0
 * @create 06-19-18:26
 */
public class ThreadPool {
    // 任务队列
    private final BlockingQueue<Runnable> workQueue;

    // 工作线程集合
    private final HashSet<Worker> workers = new HashSet<>();

    // 核心线程数
    private final int coreSize;

    // 最大线程数
    private int maxSize;

    // 超时时间
    private long keepAliveTime;

    public ThreadPool(int coreSize, int maxSize, long keepAliveTime, TimeUnit unit, int queueSize) {
        this.coreSize = coreSize;
        this.maxSize = maxSize;
        this.keepAliveTime = unit.toNanos(keepAliveTime);
        this.workQueue = new BlockingQueue<>(queueSize);
    }

    // 执行任务
    public void execute(Runnable task) {
        if (task == null) {
            throw new NullPointerException("task is empty");
        }
        synchronized (workers) {
            // 当任务数没有超过核心线程数时
            if (workers.size() < coreSize) {
                Worker worker = new Worker(task, true);
                workers.add(worker);
                worker.start();
            } else if (workQueue.size() < queue)
            // 当任务数超过核心线程数时，加入任务队列
        }
    }

    private class Worker extends Thread {

        private Runnable task;

        private boolean isCoreThread;

        public Worker(Runnable task, boolean isCoreThread) {
            this.task = task;
            this.isCoreThread = isCoreThread;
        }

        @Override
        public void run() {
            Runnable task = this.task;
            while (task != null || (task = workQueue.take()) != null) {
                try {
                    task.run();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    task = null;
                }
            }
        }
    }
}
