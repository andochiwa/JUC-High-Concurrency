package com.github;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author HAN
 * @version 1.0
 * @create 2021/4/2
 */
public class ReentrantLockTest {

    public static void main(String[] args){
        AirConditioner conditioner = new AirConditioner();
        for (int i = 0; i < 10; i++) {
            new Thread(() -> {
                for (int j = 0; j < 20; j++) {
                    conditioner.increment();
                }
            }, "I" + i).start();
            new Thread(() -> {
                for (int j = 0; j < 20; j++) {
                    conditioner.decrement();
                }
            }, "D" + i).start();
        }
    }
}

class AirConditioner {
    private int number = 0;

    private Lock lock = new ReentrantLock();

    private Condition condition = lock.newCondition();

    public void increment() {
        try {
            lock.lock();
            while (number != 0) {
                condition.await();
            }
            number++;
            System.out.println(Thread.currentThread().getName() + "\t" + number);
            condition.signalAll();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }

    public void decrement() {
        try {
            lock.lock();
            while (number == 0) {
                condition.await();
            }
            number--;
            System.out.println(Thread.currentThread().getName() + "\t" + number);
            condition.signalAll();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }
}
