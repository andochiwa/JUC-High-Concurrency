# List线程不安全

List集合是线程不安全的，所以在多线程操作的情况下，会造成数据不一致，异常等状况

# 解决方案
1. 使用`Vector`，但是你懂的，因为所有方法都直接加上了synchronized，性能巨差
2. 使用Collections工具类中的synchronizedList方法转换。
   
   实现方式是建立了list的包装类，对部分操作加上了`synchronized(mutex)`操作，其中mutex为内置的一个Object，而不是对整个方法，所以效率相对于Vector会有所提升
3. 使用juc里的`CopyOnWriteArrayList`
   
   使用了读写锁（写时复制），即读共享，写阻塞，也是使用了synchronized（java8时是ReentrantLock，但是因为synchronized有锁升级的优化，所以又改过去了）进行阻塞。
   
   写期间会调用`Arrays.CopyOf`方法进行复制吗，里面又调用了`System.arraycopy`方法，而这个复制方法是native的，所以效率也会比较高，但无论如何都会进行复制操作，然后set回原数组

综上可以知道，`CopyOnWriteArrayList`的读时效率很高，但是写时涉及到复制效率较低，所以如果写操作比较多时，应该使用`Collections.synchronizedList`