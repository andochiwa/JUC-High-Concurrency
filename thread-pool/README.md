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

## AbortPolicy(默认)

直接抛出`RejectedExecutionException`异常阻止系统正常运行

## CallerRunsPolicy

"调用者运行"的一种调节机制，该策略既不会抛弃任务，也不会抛出异常，而是将某些任务**回退到调用者(谁让你找我的，你就找谁)**，从而降低新任务的流量

（直接调用runnable.run()）

## DiscardPolicy

抛弃队列中等待最久的任务，然后将当前任务加入队列中尝试再次提交当前任务

## DiscardOldestPolicy

丢弃无法处理的任务，不予任何处理也不抛出异常，如果允许任务丢失，这是最好的一种策略