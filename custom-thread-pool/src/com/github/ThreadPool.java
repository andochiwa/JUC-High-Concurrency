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
    private BlockingQueue<Runnable> workQueue;

    // 工作线程集合
    private HashSet<Worker> workers = new HashSet<>();

    // 核心线程数
    private int coreSize;

    // 最大线程数
    private int maxSize;

    // 超时时间
    private long keepAliveTime;

    public ThreadPool(int coreSize, int maxSize, long keepAliveTime, TimeUnit unit) {
        this.coreSize = coreSize;
        this.maxSize = maxSize;
        this.keepAliveTime = unit.toNanos(keepAliveTime);
    }

    private class Worker {

    }
}
