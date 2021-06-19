package com.github;

/**
 * @author HAN
 * @version 1.0
 * @create 06-19-19:29
 */
@FunctionalInterface
public interface RejectPolicy {
    void reject(Runnable task, ThreadPool threadPool);
}
