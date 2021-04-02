package com.github;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author HAN
 * @version 1.0
 * @create 2021/4/3
 */
public class ReadWriteLockDemo {

    public static void main(String[] args){
        Cache cache = new Cache();
        // write
        for (int i = 0; i < 5; i++) {
            int temp = i;
            new Thread(() -> {
                cache.put(temp, String.valueOf(temp));
            }).start();
        }
        // read
        for (int i = 0; i < 5; i++) {
            int temp = i;
            new Thread(() -> {
                cache.get(temp);
            }).start();
        }
    }
}

class Cache {
    private Map<Integer, String> map = new HashMap<>();

    private ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    public void put(int key, String value) {
        try {
            // 写锁
            readWriteLock.writeLock().lock();

            System.out.println(Thread.currentThread().getName() + "\t写入开始" + key + "," + value);
            Thread.sleep(300);
            map.put(key, value);
            System.out.println(Thread.currentThread().getName() + "\t写入完毕");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            readWriteLock.writeLock().unlock();
        }

    }

    public void get(int key) {
        try {
            // 读锁
            readWriteLock.readLock().lock();

            System.out.println(Thread.currentThread().getName() + "\t读取开始");
            Thread.sleep(300);
            String value = map.get(key);
            System.out.println(Thread.currentThread().getName() + "\t读取完毕" + value);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            readWriteLock.readLock().unlock();
        }

    }
}
