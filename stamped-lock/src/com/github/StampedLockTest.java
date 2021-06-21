package com.github;

import java.util.concurrent.locks.StampedLock;

/**
 * @author HAN
 * @version 1.0
 * @create 06-22-3:17
 */
public class StampedLockTest {
    public static void main(String[] args){
        DataContainerStamper stamper = new DataContainerStamper(1);
        for (int i = 0; i < 20; i++) {
            int j = i;
            new Thread(() -> {
                try {
                    if (j == 10) {
                        stamper.write(5);
                    }
                    stamper.read(5);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }
}

class DataContainerStamper {
    private int data;
    private final StampedLock lock = new StampedLock();

    public DataContainerStamper(int data) {
        this.data = data;
    }

    public int read(int readTime) throws InterruptedException {
        long stamp = lock.tryOptimisticRead();
        System.out.println(Thread.currentThread() + "\toptimistic read locking... " + stamp);
        Thread.sleep(readTime * 1000L);

        if (lock.validate(stamp)) {
            System.out.println(Thread.currentThread() + "\tread finish... " + stamp);
            return data;
        }
        System.out.println(Thread.currentThread() + "\tupdating to read lock... " + stamp);

        // 锁升级
        try {
            stamp = lock.readLock();
            System.out.println(Thread.currentThread() + "\tread lock... " + stamp);
            Thread.sleep(readTime * 1000L);
            System.out.println(Thread.currentThread() + "\tread finish... " + stamp);
            return data;
        } finally {
            lock.unlockRead(stamp);
        }
    }

    public void write(int writeTime) throws InterruptedException {
        long stamp = lock.writeLock();
        System.out.println(Thread.currentThread() + "\twrite lock... " + stamp);
        try {
            Thread.sleep(writeTime * 1000L);
            this.data = writeTime;
        } finally {
            System.out.println(Thread.currentThread() + "\twrite unlock... " + stamp);
            lock.unlockWrite(stamp);
        }
    }
}
