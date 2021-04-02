# HashSet线程不安全

HashSet是线程不安全的，在使用是建议用线程安全的类

# 线程安全类
1. `Collections.synchronizedSet`
2. `CopyOnWriteArraySet`
    
    低层实际上就是`CopyOnWriteArrayList`，所以写操作会比较耗时