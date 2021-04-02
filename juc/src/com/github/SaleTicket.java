package com.github;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 售票员卖票
 * @author HAN
 * @version 1.0
 * @create 2021/4/2
 */
public class SaleTicket {

    public static void main(String[] args){
        Ticket ticket = new Ticket();
        for (int i = 0; i < 3; i++) {
            new Thread(() -> {
                for (int j = 0; j < 105; j++) {
                    ticket.sale();
                }
            }, String.valueOf(i)).start();
        }
    }
}

class Ticket{

    private int number = 100;
    private Lock lock = new ReentrantLock();

    public void sale() {
        try {
            lock.lock();
            if (number > 0) {
                System.out.println(Thread.currentThread().getName() + "卖出第" + number + "张票");
                number--;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }
}
