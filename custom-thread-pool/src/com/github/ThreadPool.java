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
            // 当任务数超过核心线程数时，如果任务队列不满，就加入到任务队列
            // 如果任务队列满了，如果没有超过最大线程，就创建额外线程
            // 如果超过最大线程，就执行拒绝策略
            if (workers.size() < coreSize) {
                Worker worker = new Worker(task, true);
                workers.add(worker);
                System.out.println(Thread.currentThread() + "\t新增核心线程");
                worker.start();
            } else if (!workQueue.isFull()) {
                System.out.println(Thread.currentThread() + "\t加入到任务队列");
                workQueue.put(task);
            } else if (workers.size() < maxSize) {
                Worker worker = new Worker(task, false);
                workers.add(worker);
                System.out.println(Thread.currentThread() + "\t新增额外线程");
                worker.start();
            }

        }
    }

    private class Worker extends Thread {

        private Runnable task;

        private final boolean isCoreThread;

        public Worker(Runnable task, boolean isCoreThread) {
            this.task = task;
            this.isCoreThread = isCoreThread;
        }

        @Override
        public void run() {
            Runnable task = this.task;
            while (task != null || (task = getTask()) != null) {
                try {
                    System.out.println(Thread.currentThread() + "\t正在执行任务 " + task);
                    task.run();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    task = null;
                }
            }
            synchronized (this) {
                System.out.println(Thread.currentThread() + "\t删除额外线程");
                workers.remove(this);
            }
        }

        private Runnable getTask() {
            if (isCoreThread) {
                return workQueue.take();
            } else {
                return workQueue.poll(keepAliveTime, TimeUnit.NANOSECONDS);
            }
        }
    }
}
