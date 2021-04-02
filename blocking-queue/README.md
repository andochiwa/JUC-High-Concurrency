# BlockingQueue阻塞队列

当队列是空的，从队列中**获取**元素的操作会被阻塞，直到其他线程插入新的元素

当队列是满的，从队列中**添加**元素的操作会被阻塞，直到其他线程移除一个或多个元素，使队列空闲起来并新增元素

# 为什么要阻塞队列？

有了阻塞队列后，我们不需要关系什么时候需要阻塞线程，什么时候需要唤醒线程，一切都交给阻塞队列来管理

# 实现类
## ArrayBlockingQueue *

由数组组成的有界阻塞队列

## SynchronousQueue *

不存储元素的阻塞队列，即单个元素的队列

## LinkedBlockingQueue *

由链表组成的近似无界(Integer.MAX_VALUE)阻塞队列

## DelayQueue

优先队列实现的延迟无界阻塞队列

## PriorityBlockingQueue

优先队列实现的无界阻塞队列

## LinkedBlockingDeque

链表组成的双向阻塞队列

## LinkedTransferQueue

链表组成的无界阻塞队列

# 核心方法

| 方法类型 |  抛异常   |  特殊值  |  阻塞  |         超时         |
| :------: | :-------: | :------: | :----: | :------------------: |
|   插入   |  add(e)   | offer(e) | put(e) | offer(e, time, unit) |
|   移除   | remove()  |  poll()  | take() |   poll(time, unit)   |
|   检查   | element() |  peek()  |   无   |          无          |

* 抛异常：

  当队列满时，再add元素会抛`IllegalStateException:Queue full`异常

  当队列空时，再remove元素会抛`NoSuchElementException`异常

* 特殊值

  插入方法，成功时返回true，失败时返回false

  移除方法，成功时返回队列元素，没有元素失败时返回null

* 阻塞

  当队列满时，生产者线程继续往队列里put元素，队列会一直阻塞生产者线程直到put元素或响应中断退出

  当队列空时，消费者线程试图从队列里take元素，队列会一直阻塞消费者线程直到队列可用

* 超时

  当队列满时，队列会阻塞生产者线程一定时间，超过规定时间后生产者线程会退出