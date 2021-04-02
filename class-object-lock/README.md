# class lock VS object lock

对象锁也叫实例锁，对应synchronized关键字，当多个线程访问多个实例时，它们互不干扰。如果是单例模式时，就会变成类锁似的行为

类锁是一个全局锁，因为类加载器只会加载一个类

**对象锁和类锁不会互相影响**

# 对象锁

* 一个实例对应一个对象锁
* synchronized标记在方法上时，表示整个对象(实例)锁，也就是synchronized(this)

# 类锁

* synchronized标记在静态方法上时，表示的是类锁