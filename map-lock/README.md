# HashMap线程不安全

当然也是不安全的啦。。。

# 解决方案

1. `Hashtable` 效率低

2. `Collections.synchronizedMap` 
   
    在HashMap的基础上对部分操作加了互斥锁

3. `ConcurrentHashMap` 

    jdk1.7之前使用的是分段锁，内部维护一个`Segment`类数组（分段锁类），每当一个线程占用锁访问一个Segment时，不会影响其他Segment。当获取锁失败时，则会利用尝试自旋获取锁，到达一定次数后改为阻塞获取

    jdk1.8之后使用了读写锁，读共享，写阻塞。只不过写不是用的复制方法，而是用一种更为精妙的方法===CAS（自旋，对空结点的插入保证原子性） + synchronized（对非空结点锁头结点保证安全性）。因为synchronized锁的是结点，所以性能比Hashtable高很多