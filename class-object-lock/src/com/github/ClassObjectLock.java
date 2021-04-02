package com.github;

import java.util.concurrent.TimeUnit;

/**
 * @author HAN
 * @version 1.0
 * @create 2021/4/2
 */
public class ClassObjectLock {

    public static void main(String[] args){
        Phone phone1 = new Phone();
        Phone phone2 = new Phone();

        new Thread(() -> {
            phone1.sendEmail();
//            phone2.sendLine();
//            Phone.sendWechat();
        }).start();
        new Thread(() -> {
//            phone1.sendSms();
            phone1.sendLine();
            Phone.sendWechat();
            phone2.sendSms();
        }).start();
    }
}

class Phone {

    public synchronized void sendEmail() {
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(Thread.currentThread().getName() + "\t" + "send email");
    }

    public synchronized void sendSms() {
        System.out.println(Thread.currentThread().getName() + "\t" + "send sms");
    }

    public static synchronized void sendWechat() {
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(Thread.currentThread().getName() + "\t" + "send wechat");
    }

    public void sendLine() {
        System.out.println(Thread.currentThread().getName() + "\t" + "send line");
    }
}
