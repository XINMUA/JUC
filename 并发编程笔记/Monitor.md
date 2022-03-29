# Java中的Monitor机制

## Java对象头 Object Header

> 参考页面: https://stackoverflow.com/questions/26357186/what-is-in-java-object-header

以32位jvm为例:

一般对象

![image-20220324210907957](C:\Users\wzd\AppData\Roaming\Typora\typora-user-images\image-20220324210907957.png)

数组对象

![image-20220324210919244](C:\Users\wzd\AppData\Roaming\Typora\typora-user-images\image-20220324210919244.png)

其中mark word结构

![image-20220324211553858](C:\Users\wzd\AppData\Roaming\Typora\typora-user-images\image-20220324211553858.png)

## monitor

monitor被翻译成 **监视器**,也是个锁

每个对象都可以关联一个mointor对象,就是在synchronized给对象加锁的时候(重量级锁)

对象头中的mark word就会被设置个指向monitor的至臻.

参考图

> https://www.processon.com/view/link/623c73881e085307892c0545

字节码角度分析

```java
static final Object lock = new Object();
static int counter = 0;
public static void main(String [] args){
    synchronized(lock){
        counter++;
    }
}
```





![image-20220324220408699](C:\Users\wzd\AppData\Roaming\Typora\typora-user-images\image-20220324220408699.png)