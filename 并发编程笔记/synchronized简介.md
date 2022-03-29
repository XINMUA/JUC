# 简介
synchronized 实际是用对象锁保证了临界区内代码的原子性，临界区内的代码对外是不可分割的，不会被线程切换所打断。
synchronized 包裹**临界区**代码
面向对象改进保护共享资源;

## 临界区
发生多线程并发问题的代码块,以下面为案例
```java
 static int counter = 0;
//synchronized需要的锁对象
 static final Object room = new Object();

 public static void main(String[] args) throws InterruptedException {
        Thread t1 = new Thread(() -> {
            for (int i = 0; i < 5000; i++) {
                synchronized (room) {
		    //被包裹的就是临界区
                    counter++;
                }
            }
        }, "t1");
        Thread t2 = new Thread(() -> {
            for (int i = 0; i < 5000; i++) {
                synchronized (room) {
                    counter--;
                }
            }
        }, "t2");
        t1.start();
        t2.start();
        t1.join();
        t2.join();
        log.debug("{}", counter);
    }
```

在上述代码中,counter++ 和counter--两段代码出现了并发问题,其原理研究发现,**counter++在jvm中执行了四段本地方法,读取,赋值,计算和写入四步,在这四步的任意一步过程中,cpu有可能切换上下文,时间片用完,导致数据计算完成后还没有写入到counter,就被线程t2拿到,重新赋值因此产生了并发问题,使用synchronized后可以避免此问题**,原理如下图:

![image-20220321234627911](C:\Users\wzd\AppData\Roaming\Typora\typora-user-images\image-20220321234627911.png)



## 面向对象改进

锁对象不一定每次创建,可以通过面向对象思想将java类作为锁对象

```java
class Test{
    public synchronized void test() {

    }
}
等价于
class Test{
    public void test() {
        synchronized(this) {

        }
    }
}
class Test{
    public synchronized static void test() {
    }
}
等价于
class Test{
    public static void test() {
        synchronized(Test.class) {

        }
    }
```

当方法为静态方法时,synchronized拿到的锁对象为该类的字节码,当方法为普通成员方法时,锁对象则是当前类..这里的解释比较简单,下面的8个案例可以更好理解:

### 案例

情况1：12 或 21

```java
@Slf4j(topic = "c.Number")
class Number{
 public synchronized void a() {
 	log.debug("1");
 }
 public synchronized void b() {
 	log.debug("2");
 }
}
public static void main(String[] args) {
 Number n1 = new Number();
 new Thread(()->{ n1.a(); }).start();
 new Thread(()->{ n1.b(); }).start();
}
```

情况2：1s后12，或 2 1s后 1

```java
class Number{
 public synchronized void a() {
 	sleep(1);
 	log.debug("1");
 }
 public synchronized void b() {
 	log.debug("2");
 }
}
public static void main(String[] args) {
 	Number n1 = new Number();
 	new Thread(()->{ n1.a(); }).start();
 	new Thread(()->{ n1.b(); }).start();
}

```

情况3：3 1s 12 或 23 1s 1 或 32 1s 1

```java
class Number{
 public synchronized void a() {
 	sleep(1);
 	log.debug("1");
 }
 public synchronized void b() {
 	log.debug("2");
 }
 public void c() {
 	log.debug("3");
 }
}
public static void main(String[] args) {
 	Number n1 = new Number();
 	new Thread(()->{ n1.a(); }).start();
 	new Thread(()->{ n1.b(); }).start();
 	new Thread(()->{ n1.c(); }).start();
}

```

情况4：2 1s 后 1

```java
class Number{
 public synchronized void a() {
 	sleep(1);
 	log.debug("1");
 }
 public synchronized void b() {
 	log.debug("2");
 }
}
public static void main(String[] args) {
 	Number n1 = new Number();
 	Number n2 = new Number();
 	new Thread(()->{ n1.a(); }).start();
 	new Thread(()->{ n2.b(); }).start();
}

```

情况5：2 1s 后 1

```java
class Number{
 public static synchronized void a() {
 sleep(1);
 log.debug("1");
 }
 public synchronized void b() {
 log.debug("2");
 }
}
public static void main(String[] args) {
 Number n1 = new Number();
 new Thread(()->{ n1.a(); }).start();
 new Thread(()->{ n1.b(); }).start();
}
```

情况6：1s 后12， 或 2 1s后 1

```java
class Number{
 public static synchronized void a() {
 sleep(1);
 log.debug("1");
 }
 public static synchronized void b() {
 log.debug("2");
 }
}
public static void main(String[] args) {
 Number n1 = new Number();
 new Thread(()->{ n1.a(); }).start();
 new Thread(()->{ n1.b(); }).start();
}

```

情况7：2 1s 后 1

```java
class Number{
 public static synchronized void a() {
 sleep(1);
 log.debug("1");
 }
 public synchronized void b() {
 log.debug("2");
 }
}
public static void main(String[] args) {
    /*这里的a方法的锁,是加在Number类的字节码对象上,而b方法的锁加在n2这个对象上,拿到的锁不是同一个,因此不存在锁互斥.
    */
 	Number n1 = new Number();
 	Number n2 = new Number();
 	new Thread(()->{ n1.a(); }).start();
 	new Thread(()->{ n2.b(); }).start();
}
```

情况8：1s 后12， 或 2 1s后 1

```java
class Number{
 public static synchronized void a() {
 sleep(1);
 log.debug("1");
 }
 public static synchronized void b() {
 log.debug("2");
 }
}
public static void main(String[] args) {
    //此处两个方法都是static,故虽然是两个对象,但是拿到同一个字节码对象上的锁.
 	Number n1 = new Number();
 	Number n2 = new Number();
 	new Thread(()->{ n1.a(); }).start();
 	new Thread(()->{ n2.b(); }).start();
}
```

