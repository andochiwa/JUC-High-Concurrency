# Executors工具类创建线程池
## newFixedThreadPool

创建一个定长线程池，支持定时及周期性任务执行。如果提交任务时没有能使用的工作线程，则会进入阻塞队列中等待。

## newSingleThreadExecutor

单例线程池,只会用唯一的工作线程执行

## newCachedThreadPool

可缓存的线程池，如果线程池的大小超过了处理任务所需要的线程，那么就会回收部分空闲的线程。

实际上就是内部new了一个核心线程数为0，最大线程数为INT_MAX，等待时间为60秒的线程池

## newScheduledThreadPool

定长线程池，该线程池可以调度在给定延迟之后运行的命令，或者定期执行命令

# 线程池七大参数

* `corePoolSize` 
  
    线程池中常驻核心线程数
  
* `maximumPoolSize`

    线程池中能够容纳同时执行的最大线程数，当工作队列满了以后，额外(非核心)线程就会开始工作
  
* `keepAliveTime`

    多余的空闲线程的存活时间，当前池中线程数超过核心线程数时，当空闲时间达到`keepAliveTime`时，多余线程会被销毁直到只剩下核心线程数数量为止
  
* `unit` 
  
    `keepAliveTime`的单位
  
* `workQUeue`

    任务队列（阻塞队列），保存被提交但尚未被执行的任务
  
* `threadFactory`

    表示生成线程池中工作线程的线程工厂，用于创建线程，**一般用默认的即可**
  
* `handler`

  (RejectedExecutionHandler) 拒绝策略，表示当队列满了，并且工作线程 >= 最大线程数(maximumPoolSize)时如何来拒绝请求的Runnable的策略

# 拒绝策略

## AbortPolicy(中止策略)[默认]

直接抛出`RejectedExecutionException`异常阻止系统正常运行

## CallerRunsPolicy(调用者运行策略)

"调用者运行"的一种调节机制，该策略既不会抛弃任务，也不会抛出异常，而是将某些任务**回退到调用者(谁让你找我的，你就找谁)**，从而降低新任务的流量

（直接调用runnable.run()）

## DiscardPolicy(丢弃策略)

抛弃队列中等待最久的任务，然后将当前任务加入队列中尝试再次提交当前任务

## DiscardOldestPolicy(弃老策略)

丢弃无法处理的任务，不予任何处理也不抛出异常，如果允许任务丢失，这是最好的一种策略

# 第三方拒绝策略

## dubbo的拒绝策略

主要会做三件事，让使用者清楚触发线程拒绝策略的原因
1. 输出一条警告级别的日志，内容为线程池的详细设置参数，状态等
2. 输出当前线程的堆栈详情
3. 抛出拒绝执行异常

## Netty的拒绝策略

Netty的拒绝策略和CallerRunsPolicy很像，不过Netty是新建了一个线程来处理

## activeMq的拒绝策略

activeMq中的策略属于最大努力执行任务型，当触发拒绝策略时，在尝试一分钟的时间重新将任务塞进任务队列，当一分钟超时还没成功时，就抛出异常

## pinpoint的拒绝策略

定义了一个拒绝策略链，包装了一个拒绝策略列表，当触发拒绝策略时，会将策略链中的rejectedExecution依次执行一遍