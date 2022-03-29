# Synchronized 优化--轻量级锁,重入锁,锁膨胀原理



多个线程访问同一个对象的时候,如果他们访问时间是错开的,就是不会出现竞争的话,就会使用轻量级锁优化

这个优化是**没有特别语法的,会优先使用轻量级锁,如果枷锁失败会使用重量级锁**.

假如有两个代码块,用了同一个对象加锁.:

```java
static final Object obj = new Object():
public static void method1(){
    synchronized(obj){
        //同步区A
        method2();
    }
}

public static void method2(){
    synchronized(obj){
        //同步区B
       
    }
}
```

其原理在下面的链接中查看

> https://www.processon.com/view/link/623c73881e085307892c0545

![image-20220325093120066](C:\Users\wzd\AppData\Roaming\Typora\typora-user-images\image-20220325093120066.png)