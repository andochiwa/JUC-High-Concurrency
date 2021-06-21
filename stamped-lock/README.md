# StampedLock

该类自 jdk8 加入，是为了进一步优化读性能，它的特点是在使用读锁，写锁时都必须配合`戳`使用

值得注意的是，StampedLock并不是基于AQS实现的

加解读锁：

```java
long stamp = lock.readLock();
lock.unlockRead(stamp);
```

加解写锁：

```java
long stamp = lock.writeLock();
lock.unlockWrite(stamp);
```

StampLock 支持`tryOptimisticRead()`方法（乐观读），读取完毕后需要做一次`戳校验`，如果校验通过，表示这期间没有写操作，数据可以安全使用；如果校验没通过需要重新获取读锁，保证数据安全

```java
long stamp = lock.tryOptimisticRead();
// 校验
if (!lock.validate(stamp)) {
    // 锁升级
}
```

