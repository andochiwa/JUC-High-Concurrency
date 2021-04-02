# synchronized & ReentrantLock的区别

## 低层实现

synchronized 是JVM层面的锁，是Java关键字，通过monitor对象来完成（monitorenter与monitorexit），对象只有在同步块或同步方法中才能调用wait/notify方法，ReentrantLock 是从jdk1.5以来（java.util.concurrent.locks.Lock）提供的API层面的锁。

synchronized 的实现涉及到锁的升级，具体为无锁、偏向锁、自旋锁、向OS申请重量级锁，ReentrantLock实现则是通过利用CAS（CompareAndSwap）自旋机制保证线程操作的原子性和volatile保证数据可见性以实现锁的功能。

## 是否手动释放

synchronized不需要用户去手动释放锁，在同步代码块中执行完后会自动释放锁

ReentrantLock需要用户去手动释放锁，不然可能会导致死锁。一般通过`lock()`和`unlock`方法配置try-finally语句完成，使释放更灵活

## 是否可中断

synchronized是不可中断类型的锁，除非加锁的代码中出现异常或正常执行完成

ReentrantLock则可以中断，可通过tryLock(long timeout,TimeUnit unit)设置超时方法或者将lockInterruptibly()放到代码块中，调用interrupt方法进行中断。

## 是否公平锁

synchronized为非公平锁

ReentrantLock可以在构造方法中传入boolean值决定是否为公平锁，默认为false非公平锁

## 是否绑定条件condition

synchronized不能绑定条件

ReentrantLock通过绑定Condition接口，结合await()，signal()方法实现线程的精确唤醒，而不是像synchronized通过Object类的wait(),notify()等方法要么随机唤醒一个线程要么唤醒全部线程

## 锁的对象

synchronized锁的是对象，锁是保存在对象头里面的，根据对象头数据来标识是否有线程获得锁/争抢锁

ReentrantLock锁的是线程，根据进入的线程和int类型的state标识锁的获得/争抢