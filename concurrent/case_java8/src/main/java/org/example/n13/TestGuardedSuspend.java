package org.example.n13;

public class TestGuardedSuspend {

    public static void main(String[] args) {

    }
}





    // 增加超时效果
    class GuardedObject {
        // 结果
        private Object response;

        // 获取结果
        // timeout 表示要等待多久 2000
        public Object get() {
            synchronized (this) {
                while (response == null) {
                    try {
                        this.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                return response;
            }
        }

        // 产生结果
        public void complete(Object response) {
            synchronized (this) {
                // 给结果成员变量赋值
                this.response = response;
                this.notifyAll();
            }
        }

}
