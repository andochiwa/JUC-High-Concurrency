# Callable接口使用方法

Callable接口与Runnable接口不同的是，Callable的call(对应Runnable的run)方法可以有返回值，带泛型，抛异常

在Thread类中传入Callable接口的办法是：传入一个`FutureTask`实例，里面放入`Callable`实现接口的对象

FutureTask类实现了RunnableFuture接口，而RunnableFuture又继承与Runnable，所以可以直接放进Thread的构造器中

# 注意事项

当futureTask为同一个实例时，多个线程也只会调用一个call方法，结果复用