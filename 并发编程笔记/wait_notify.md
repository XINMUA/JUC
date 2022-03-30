# wait-notify

## 原理

在monitor的解析图中,有关于wait set的原理分析

> https://www.processon.com/view/link/623c73881e085307892c0545

- obj.wait() 让进入 object 监视器的线程到 waitSet 等待
- obj.notify() 在 object 上正在 waitSet 等待的线程中挑一个唤醒
- obj.notifyAll() 让 object 上正在 waitSet 等待的线程全部唤醒  

## sleep和wait的区别

- sleep 是 Thread 方法，而 wait 是 Object 的方法  
- sleep 不需要和 synchronized使用 
- sleep 在睡眠的同时，不会释放对象锁的，但 wait 在等待的时候会释放对象锁  

## 正确使用wait notify姿势

第一个例子,

```java
    static final Object room = new Object();
    static boolean hasCigarette = false; // 有没有烟

    public static void main(String[] args) {
        new Thread(() -> {
            synchronized (room) {
                log.debug("有烟没？[{}]", hasCigarette);
                if (!hasCigarette) {
                    log.debug("没烟，先歇会！");
                    sleep(2);
                }
                log.debug("有烟没？[{}]", hasCigarette);
                if (hasCigarette) {
                    log.debug("可以开始干活了");
                }
            }
        }, "小发").start();

        for (int i = 0; i < 5; i++) {
            new Thread(() -> {
                synchronized (room) {
                    log.debug("可以开始干活了");
                }
            }, "其它人").start();
        }

        sleep(1);
        new Thread(() -> {
            //???
            //synchronized (room) {
                hasCigarette = true;
                log.debug("华子到了！");
            //}
        }, "送烟的").start();
    }

```

上面这个例子中,sleep进行时,并不会释放锁,导致运行效率低,在小法等待过程中,其他线程都阻塞了....

进行优化.

```java
new Thread(() -> {
            synchronized (room) {
                log.debug("有烟没？[{}]", hasCigarette);
                if (!hasCigarette) {
                    log.debug("没烟，先歇会！");
                    try {
                        room.wait(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                log.debug("有烟没？[{}]", hasCigarette);
                if (hasCigarette) {
                    log.debug("可以开始干活了");
                }
            }
        }, "小发").start();

        for (int i = 0; i < 5; i++) {
            new Thread(() -> {
                synchronized (room) {
                    log.debug("可以开始干活了");
                }
            }, "其它人").start();
        }

        sleep(1);
        new Thread(() -> {
            synchronized (room) {
                hasCigarette = true;
                log.debug("华子到了！");
                room.notify();
            }
        }, "送烟的").start();
```

修改之后,感觉没问题了,使用wait方法,释放了锁,有效解决了其他线程无法工作问题

但是....... 如果还有别的线程也在等待呢???

```java
static final Object room = new Object();
    static boolean hasCigarette = false;
    static boolean hasTakeout = false;

    // 虚假唤醒
    public static void main(String[] args) {
        new Thread(() -> {
            synchronized (room) {
                log.debug("有烟没？[{}]", hasCigarette);
                if (!hasCigarette) {
                    log.debug("没烟，先歇会！");
                    try {
                        room.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                log.debug("有烟没？[{}]", hasCigarette);
                if (hasCigarette) {
                    log.debug("可以开始干活了");
                } else {
                    log.debug("没干成活...");
                }
            }
        }, "小发").start();

        new Thread(() -> {
            synchronized (room) {
                Thread thread = Thread.currentThread();
                log.debug("有吃的吗？[{}]", hasTakeout);
                if (!hasTakeout) {
                    log.debug("没吃的，先歇会！");
                    try {
                        room.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                log.debug("有吃的吗？[{}]", hasTakeout);
                if (hasTakeout) {
                    log.debug("可以开始干活了");
                } else {
                    log.debug("没干成活...");
                }
            }
        }, "小瑞").start();

        sleep(1);
        new Thread(() -> {
            synchronized (room) {
                hasTakeout = true;
                log.debug("诶嘿嘿嘿,鸡汤来咯~~");
                room.notifyAll();
            }
        }, "炊事员").start();

    }
```

当崔始源唤醒了所有线程,小瑞线程正常运行,因为有了鸡汤,但是小发的烟并没有拿到,然而还是被唤醒了,方法结束....

终极优化,使用循环调用wait();

```java
static final Object room = new Object();
    static boolean hasCigarette = false;
    static boolean hasTakeout = false;

    public static void main(String[] args) {


        new Thread(() -> {
            synchronized (room) {
                log.debug("有烟没？[{}]", hasCigarette);
                while (!hasCigarette) {
                    log.debug("没烟，先歇会！");
                    try {
                        room.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                log.debug("有烟没？[{}]", hasCigarette);
                if (hasCigarette) {
                    log.debug("可以开始干活了");
                } else {
                    log.debug("没干成活...");
                }
            }
        }, "小发").start();

        new Thread(() -> {
            synchronized (room) {
                Thread thread = Thread.currentThread();
                log.debug("有吃的吗？[{}]", hasTakeout);
                while (!hasTakeout) {
                    log.debug("没，先歇会！");
                    try {
                        room.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                log.debug("有吃的吗？[{}]", hasTakeout);
                if (hasTakeout) {
                    log.debug("可以开始干活了");
                } else {
                    log.debug("没干成活...");
                }
            }
        }, "小瑞").start();

        sleep(1);
        new Thread(() -> {
            synchronized (room) {
                hasTakeout = true;
                log.debug("诶嘿嘿嘿,鸡汤来咯~~");
                room.notifyAll();
            }
        }, "炊事员").start();


    }

```

## 总结


```java
synchronized(lock) {
	while(条件不成立) {
		lock.wait();
	}
	// 干活
}
//另一个线程
synchronized(lock) {
	lock.notifyAll();
}
```

  

