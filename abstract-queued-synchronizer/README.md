# 1. AQS抽象类

<img src="img/img.png" style="zoom:150%;" />

它维护了一个 volatile int state（代表共享资源）和一个 FIFO 线程等待队列（多线程争用资源被阻塞时会进入此队列）。这里 volatile 是核心关键词，具体 volatile 的语义，在此不述。state 的访问方式有三种:

* `getState()`
* `setState()`
* `compareAndSetState()`

AQS定义两种资源共享方式：Exclusive（独占，只有一个线程能执行，如ReentrantLock）和Share（共享，多个线程可同时执行，如Semaphore | CountDownLatch）

不同的自定义同步器争用共享资源的方式也不同。**自定义同步器在实现时只需要实现共享资源state的获取与释放方式即可**，至于具体线程等待队列的维护（如获取资源失败入队/唤醒出队等），AQS已经在顶层实现好了。自定义同步器实现时主要实现以下几种方法：

* `isHeldExclusively()`：该线程是否正在独占资源。只有用到 condition 才需要去实现它。
* `tryAcquire(int)`：独占方式。尝试获取资源，成功则返回 true，失败则返回 false。
* `tryRelease(int)`：独占方式。尝试释放资源，成功则返回 true，失败则返回 false。
* `tryAcquireShared(int)`：共享方式。尝试获取资源。负数表示失败；0 表示成功，但没有剩余可用资源；正数表示成功，且有剩余资源。
* `tryReleaseShared(int)`：共享方式。尝试释放资源，如果释放后允许唤醒后续等待结点返回true，否则返回false。

以ReentrantLock为例，state初始化为 0，表示未锁定状态。A 线程`lock()`时，会调用`tryAcquire()`独占该锁并将 state + 1。此后，其他线程再`tryAcquire()`时就会失败，直到 A线程`unlock()`到 state=0（即释放锁）为止，其它线程才有机会获取该锁。当然，释放锁之前，A线程 自己是可以重复获取此锁的（state 会累加），这就是可重入的概念。但要注意，获取多少次就要释放多么次，这样才能保证 state 是能回到零态的。

再以 CountDownLatch 以例，任务分为 N 个子线程去执行，state 也初始化为 N（注意 N 要与线程个数一致）。这 N 个子线程是并行执行的，每个子线程执行完后`countDown()`一次，state 会 CAS 减 1。等到所有子线程都执行完后(即 state = 0 )，会`unpark()`主调用线程，然后主调用线程就会从`await()`函数返回，继续后余动作。

一般来说，自定义同步器要么是独占方法，要么是共享方式，他们也只需实现 `tryAcquire-tryRelease`、`tryAcquireShared-tryReleaseShared`中的一种即可。但AQS也支持自定义同步器同时实现独占和共享两种方式，如 ReentrantReadWriteLock。

# 2. 源码解析

依照`acquire-release`、`acquireShared-releaseShared`的次序来解析AQS的源码

## 2.1 结点状态 waitStatus

Node结点是对每一个等待获取资源的线程的封装，其包含了需要同步的线程本身及其等待状态，如是否被阻塞、是否等待唤醒、是否已经被取消等。变量waitStatus则表示当前Node结点的等待状态，共有5种取值 CANCELLED、SIGNAL、CONDITION、PROPAGATE、0。

- **CANCELLED**(1)：表示当前结点已取消调度。当 timeout 或被中断（响应中断的情况下），会触发变更为此状态，进入该状态后的结点将不会再变化。
- **SIGNAL**(-1)：表示后继结点在等待当前结点唤醒。后继结点入队时，会将前继结点的状态更新为 SIGNAL。
- **CONDITION**(-2)：表示结点等待在 Condition 上，当其他线程调用了 Condition 的 signal() 方法后，CONDITION 状态的结点将**从等待队列转移到同步队列中**，等待获取同步锁。
- **PROPAGATE**(-3)：共享模式下，前继结点不仅会唤醒其后继结点，同时也可能会唤醒后继的后继结点。
- **0**：新结点入队时的默认状态。

注意，**负值表示结点处于有效等待状态，而正值表示结点已被取消。所以源码中很多地方用>0、<0来判断结点的状态是否正常**。

## 2.1 acquire(int)

此方法是独占模式下线程获取共享资源的顶层入口。如果获取到资源，线程直接返回，否则进入等待队列，直到获取到资源为止，且整个过程忽略中断的影响。这也正是`lock()`的语义，当然不仅仅只限于`lock()`。获取到资源后，线程就可以去执行其临界区代码了。下面是`acquire()`的源码：

```java
public final void acquire(int arg) {
    if (!tryAcquire(arg) &&
        acquireQueued(addWaiter(Node.EXCLUSIVE), arg))
        selfInterrupt();
}
```

函数流程如下：

1. `tryAcquire()`尝试直接去获取资源，如果成功则直接返回（这里体现了非公平锁，每个线程获取锁时会尝试直接抢占加塞一次，而 CLH队列 中可能还有别的线程在等待
2. `addWaiter()`将该线程加入等待队列的尾部，并标记为独占模式；
3. `acquireQueued()`使线程阻塞在等待队列中获取资源，一直获取到资源后才返回。如果在整个等待过程中被中断过，则返回true，否则返回false。
4. 如果线程在等待过程中被中断过，它是不响应的。只是获取资源后才再进行自我中断`selfInterrupt()`，将中断补上。

### 2.1.1 tryAcquire(int)

此方法尝试去获取独占资源。如果获取成功，则直接返回true，否则直接返回false。这也正是tryLock()的语义，还是那句话，当然不仅仅只限于tryLock()。如下是tryAcquire()的源码：

```java
protected boolean tryAcquire(int arg) {
    throw new UnsupportedOperationException();
}
```

AQS这里只定义了一个接口，具体资源的获取交由自定义同步器去实现了（通过 state 的 get/set/CAS ）！至于能不能重入，能不能加塞，那就看具体的自定义同步器怎么去设计了！当然，自定义同步器在进行资源访问时要考虑线程安全的影响。

这里之所以没有定义成abstract，是因为独占模式下只用实现tryAcquire-tryRelease，而共享模式下只用实现tryAcquireShared-tryReleaseShared。如果都定义成abstract，那么每个模式也要去实现另一模式下的接口。说到底，Doug Lea 还是站在咱们开发者的角度，尽量减少不必要的工作量。

### 2.1.2 addWaiter(Node)

此方法用于将当前线程加入到等待队列的队尾，并返回当前线程所在的结点:

```java
private Node addWaiter(Node mode) {
    //以给定模式构造结点。mode有两种：EXCLUSIVE（独占）和SHARED（共享）
    Node node = new Node(Thread.currentThread(), mode);

    //尝试快速方式直接放到队尾。
    Node pred = tail;
    if (pred != null) {
        node.prev = pred;
        if (compareAndSetTail(pred, node)) {
            pred.next = node;
            return node;
        }
    }

    //上一步失败则通过enq入队。
    enq(node);
    return node;
}
```

#### 2.1.2.1 enq(Node)

```java
private Node enq(final Node node) {
    //CAS"自旋"，直到成功加入队尾
    for (;;) {
        Node t = tail;
        if (t == null) { // 队列为空，创建一个空的标志结点作为head结点，并将tail也指向它。
            if (compareAndSetHead(new Node()))
                tail = head;
        } else {//正常流程，放入队尾
            node.prev = t;
            if (compareAndSetTail(t, node)) {
                t.next = node;
                return t;
            }
        }
    }
}
```

### 2.1.3 acquireQueued(Node, int)

通过tryAcquire()和addWaiter()，该线程获取资源失败，已经被放入等待队列尾部了，那么下一步就是：**进入等待状态休息，直到其他线程彻底释放资源后唤醒自己，自己再拿到资源，然后就可以去干自己想干的事了**。`acquireQueued()`就是干这件事：**在等待队列中排队等待资源（中间没其它事干可以休息），直到拿到资源后再返回**。这个函数非常关键，源码如下:

```java
final boolean acquireQueued(final Node node, int arg) {
    boolean failed = true; // 标记是否成功拿到资源
    try {
        boolean interrupted = false; // 标记等待过程中是否被中断过

        //自旋
        for (;;) {
            final Node p = node.predecessor();//拿到前驱
            //如果前驱是head，即该结点已成老二，
            //那么便有资格去尝试获取资源（可能是老大释放完资源唤醒自己的，当然也可能被interrupt了）。
            if (p == head && tryAcquire(arg)) {
                setHead(node); // 拿到资源后，将head指向该结点。所以head所指的标杆结点，就是当前获取到资源的那个结点或null。
                p.next = null; // setHead中node.prev已置为null，此处再将head.next置为null，
                			   // 就是为了方便GC回收以前的head结点。也就意味着之前拿完资源的结点出队了！
                failed = false; // 成功获取资源
                return interrupted; // 返回等待过程中是否被中断过
            }

            // 如果自己可以休息了，就通过park()进入waiting状态，直到被unpark()。
            // 如果不可中断的情况下被中断了，那么会从park()中醒过来，发现拿不到资源，从而继续进入park()等待。
            if (shouldParkAfterFailedAcquire(p, node) &&
                parkAndCheckInterrupt())
                interrupted = true;//如果等待过程中被中断过，哪怕只有那么一次，就将interrupted标记为true
        }
    } finally {
        if (failed) // 如果等待过程中没有成功获取资源（如timeout，或者可中断的情况下被中断了），那么取消结点在队列中的等待。
            cancelAcquire(node);
    }
}
```

到这里，先不急着总结`acquireQueued()`的函数流程，先看看`shouldParkAfterFailedAcquire()`和`parkAndCheckInterrupt()`具体干些什么。

#### 2.1.3.1 shouldParkAfterFailedAcquire(Node, Node)

此方法主要用于检查状态，看看自己是否真的可以去休息了（线程进入 waiting 状态），防止队列前面的线程都放弃了

```java
private static boolean shouldParkAfterFailedAcquire(Node pred, Node node) {
    int ws = pred.waitStatus;//拿到前驱的状态
    if (ws == Node.SIGNAL)
        //如果已经告诉前驱拿完号后通知自己一下，那就可以安心休息了
        return true;
    if (ws > 0) {
        /*
           * 如果前驱放弃了，那就一直往前找，直到找到最近一个正常等待的状态，并排在它的后边。
           * 注意：那些放弃的结点，由于被自己“加塞”到它们前边，它们相当于形成一个无引用链，稍后就会被GC回收
          */
        do {
            node.prev = pred = pred.prev;
        } while (pred.waitStatus > 0);
        pred.next = node;
    } else {
        //如果前驱正常，那就把前驱的状态设置成SIGNAL，告诉它拿完号后通知自己一下。有可能失败，人家说不定刚刚释放完呢！
        compareAndSetWaitStatus(pred, ws, Node.SIGNAL);
    }
    return false;
}
```

整个流程中，如果前驱结点的状态不是SIGNAL，那么自己就不能进入 waiting，需要去找个可以 waiting 的点，同时再尝试下看有没有机会轮到自己拿号。

#### 2.1.3.2 parkAndCheckInterrupt()

如果线程找好安全休息点后，那就可以安心去休息了。此方法就是让线程去休息，真正进入等待状态

```java
private final boolean parkAndCheckInterrupt() {
    LockSupport.park(this);//调用park()使线程进入waiting状态
    return Thread.interrupted();//如果被唤醒，查看自己是不是被中断的。
}
```

`park()`会让当前线程进入waiting状态。在此状态下，有两种途径可以唤醒该线程：1.被`unpark()`；2.被`interrupt()`。需要注意的是，`Thread.interrupted()`会清除当前线程的中断标记位。 

#### 2.1.3.3 小结

看了`shouldParkAfterFailedAcquire()`和`parkAndCheckInterrupt()`，现在让我们再回到`acquireQueued()`，总结下该函数的具体流程：

1. 结点进入队尾后，检查状态，找到安全休息点；
2. 调用`park()`进入waiting状态，等待`unpark()`或`interrupt()`唤醒自己；
3. 被唤醒后，看自己是不是有资格能拿到号。如果拿到，head指向当前结点，并返回从入队到拿到号的整个过程中是否被中断过；如果没拿到，继续流程1

### 2.1.4 小结

`acquireQueued()`分析完之后，接下来再回到`acquire()`, 再贴上它的源码：

```java
public final void acquire(int arg) {
    if (!tryAcquire(arg) &&
        acquireQueued(addWaiter(Node.EXCLUSIVE), arg))
        selfInterrupt();
}
```

再来总结下它的流程：

1. 调用自定义同步器的`tryAcquire()`尝试直接去获取资源，如果成功则直接返回；
2. 没成功，则`addWaiter()`将该线程加入等待队列的尾部，并标记为独占模式；
3. `acquireQueued()`使线程在等待队列中休息，有机会时（轮到自己，会被`unpark()`）会去尝试获取资源。获取到资源后才返回。如果在整个等待过程中被中断过，则返回 true，否则返回 false。
4. 如果线程在等待过程中被中断过，它是不响应的。只是获取资源后才再进行自我中断`selfInterrupt()`，将中断补上。

由于此函数是重中之重，再用流程图总结一下：

<img src="img/img_1.png" style="zoom:150%;" />

至此，`acquire()`的流程算是告一段落了。这也就是`ReentrantLock.lock()`的流程

## 2.2 release(int)

上一小节已经把`acquire()`说完了，这一小节就来讲讲它的反操作`release()`吧。此方法是独占模式下线程释放共享资源的顶层入口。它会释放指定量的资源，如果彻底释放了（即 state = 0）,它会唤醒等待队列里的其他线程来获取资源。这也正是`unlock()`的语义，当然不仅仅只限于`unlock()`。下面是`release()`的源码

```java
public final boolean release(int arg) {
    if (tryRelease(arg)) {
        Node h = head;//找到头结点
        if (h != null && h.waitStatus != 0)
            unparkSuccessor(h);//唤醒等待队列里的下一个线程
        return true;
    }
    return false;
}
```

逻辑并不复杂。它调用`tryRelease()`来释放资源。有一点需要注意的是，**它是根据tryRelease()的返回值来判断该线程是否已经完成释放掉资源了！所以自定义同步器在设计tryRelease()的时候要明确这一点！**

### 2.2.1 tryRelease(int)

　　此方法尝试去释放指定量的资源。下面是`tryRelease()`的源码：

```java
protected boolean tryRelease(int arg) {
    throw new UnsupportedOperationException();
}
```

跟`tryAcquire()`一样，这个方法是需要独占模式的自定义同步器去实现的。正常来说，`tryRelease()`都会成功的，因为这是独占模式，该线程来释放资源，那么它肯定已经拿到独占资源了，直接减掉相应量的资源即可( state -= arg )，也不需要考虑线程安全的问题。但要注意它的返回值，上面已经提到了，**`release()` 是根据 `tryRelease()` 的返回值来判断该线程是否已经完成释放掉资源了**，所以自义定同步器在实现时，如果已经彻底释放资源( state = 0)，要返回 true，否则返回 false。

### 2.2.2 unparkSuccessor(Node)

　　此方法用于唤醒等待队列中下一个线程。下面是源码：

```java
private void unparkSuccessor(Node node) {
    // 这里，node一般为当前线程所在的结点。
    int ws = node.waitStatus;
    if (ws < 0) // 置零当前线程所在的结点状态，允许失败。
        compareAndSetWaitStatus(node, ws, 0);

    Node s = node.next; // 找到下一个需要唤醒的结点s
    if (s == null || s.waitStatus > 0) {//如果为空或已取消
        s = null;
        for (Node t = tail; t != null && t != node; t = t.prev) // 从后向前找。
            if (t.waitStatus <= 0) // 从这里可以看出，<=0的结点，都是还有效的结点。
                s = t;
    }
    if (s != null)
        LockSupport.unpark(s.thread); // 唤醒
}
```

这个函数并不复杂。一句话概括：**用`unpark()`唤醒等待队列中最前边的那个未放弃线程**，这里我们也用 s 来表示吧。此时，再和`acquireQueued()`联系起来，s 被唤醒后，进入`if (p == head && tryAcquire(arg))`的判断（即使 p != head 也没关系，它会再进入`shouldParkAfterFailedAcquire()`寻找一个安全点。这里既然 s 已经是等待队列中最前边的那个未放弃线程了，那么通过`shouldParkAfterFailedAcquire()`的调整，s 也必然会跑到 head 的next 结点，下一次自旋 p == head 就成立了），然后 s 把自己设置成 head 标杆结点，表示自己已经获取到资源了，`acquire()`也返回了

### 3.2.3 小结

`release()`是独占模式下线程释放共享资源的顶层入口。它会释放指定量的资源，如果彻底释放了（即 state = 0）,它会唤醒等待队列里的其他线程来获取资源。

如果获取锁的线程在 release 时异常了，没有 unpark 队列中的其他结点，这时队列中的其他结点会怎么办？是不是没法再被唤醒了？

   答案是**YES**，这时，队列中等待锁的线程将永远处于 park 状态，无法再被唤醒！！！但是我们再回头想想，获取锁的线程在什么情形下会 release抛出异常呢？？

1. 线程突然死掉了？可以通过 thread.stop 来停止线程的执行，但该函数的执行条件要严苛的多，而且函数注明是非线程安全的，已经标明Deprecated；
2. 线程被 interupt 了？线程在运行态是不响应中断的，所以也不会抛出异常；
3. release 代码有bug，抛出异常了？目前来看，Doug Lea的 release 方法还是比较健壮的，没有看出能引发异常的情形（如果有，恐怕早被用户吐槽了）。**除非自己写的tryRelease()有bug，那就没啥说的，自己写的bug只能自己含着泪去承受了**。

## 2.3 acquireShared(int)

此方法是共享模式下线程获取共享资源的顶层入口。它会获取指定量的资源，获取成功则直接返回，获取失败则进入等待队列，直到获取到资源为止，整个过程忽略中断。下面是`acquireShared()`的源码：

```java
public final void acquireShared(int arg) {
    if (tryAcquireShared(arg) < 0)
        doAcquireShared(arg);
}
```

这里`tryAcquireShared()`依然需要自定义同步器去实现。但是 AQS 已经把其返回值的语义定义好了：负值 代表获取失败；0 代表获取成功，但没有剩余资源；正数 表示获取成功，还有剩余资源，其他线程还可以去获取。所以这里`acquireShared()`的流程就是：

1. `tryAcquireShared()`尝试获取资源，成功则直接返回；
2. 失败则通过`doAcquireShared()`进入等待队列，直到获取到资源为止才返回。

### 2.3.1 doAcquireShared(int)

此方法用于将当前线程加入等待队列尾部休息，直到其他线程释放资源唤醒自己，自己成功拿到相应量的资源后才返回。下面是`doAcquireShared()`的源码：

```java
private void doAcquireShared(int arg) {
    final Node node = addWaiter(Node.SHARED); // 加入队列尾部
    boolean failed = true; // 是否成功标志
    try {
        boolean interrupted = false; // 等待过程中是否被中断过的标志
        for (;;) {
            final Node p = node.predecessor(); // 前驱
            // 如果到head的下一个，因为head是拿到资源的线程，此时node被唤醒，很可能是head用完资源来唤醒自己的
            if (p == head) {
                int r = tryAcquireShared(arg); // 尝试获取资源
                if (r >= 0) {//成功
                    setHeadAndPropagate(node, r); // 将head指向自己，还有剩余资源可以再唤醒之后的线程
                    p.next = null; // help GC
                    if (interrupted) // 如果等待过程中被打断过，此时将中断补上。
                        selfInterrupt();
                    failed = false;
                    return;
                }
            }

            // 判断状态，寻找安全点，进入waiting状态，等着被unpark()或interrupt()
            if (shouldParkAfterFailedAcquire(p, node) &&
                parkAndCheckInterrupt())
                interrupted = true;
        }
    } finally {
        if (failed)
            cancelAcquire(node);
    }
}
```

其实流程与`acquireQueued()`并没有太大区别。只不过这里将补中断的`selfInterrupt()`放到`doAcquireShared()`里了，而独占模式是放到`acquireQueued()`之外，其实都一样

跟独占模式比，还有一点需要注意的是，这里只有线程是 head.next 时（“老二”），才会去尝试获取资源，有剩余的话还会唤醒之后的队友。那么问题就来了，假如老大用完后释放了5个资源，而老二需要6个，老三需要1个，老四需要2个。老大先唤醒老二，老二一看资源不够，他是把资源让给老三呢，还是不让？答案是否定的！老二会继续`park()`等待其他线程释放资源，也更不会去唤醒老三和老四了。独占模式，同一时刻只有一个线程去执行，这样做未尝不可；但共享模式下，多个线程是可以同时执行的，现在因为老二的资源需求量大，而把后面量小的老三和老四也都卡住了。当然，这并不是问题，只是 AQS 保证严格按照入队顺序唤醒罢了（保证公平，但降低了并发）。

#### 2.3.1.1 setHeadAndPropagate(Node, int)