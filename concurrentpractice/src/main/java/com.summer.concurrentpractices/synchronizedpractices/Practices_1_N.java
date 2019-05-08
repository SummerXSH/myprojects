package com.summer.concurrentpractices.synchronizedpractices;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 锁和资源 1：N 的情况实践 即 一把锁保护多个资源
 *
 * @author xiashanhao
 * @date 2019-05-07 21:20
 */
public class Practices_1_N {

    /**
     * 类型1： 多个资源之间没有关联关系
     * 以账户的修改密码和查询余额为例,该类型中密码和余额是两种资源 两者之间没有关联关系 可以分别用一把锁保护
     * 也可以只用一把锁保护 见Account1_2 ，这种锁将对余额操作和密码操作都是串行化的影响性能，
     * Account1_1 中对不同的资源使用不同的锁，称为细粒度的锁
     */
    class Account1_1 {

        private String password = "123";

        private long balance = 1000;

        /**
         * 余额锁， 锁的声明用final修饰 锁用具有不可变行 否则就是不同的锁了
         */
        private final Object balLock = new Object();

        /**
         * 密码锁
         */
        private final Object pawLock = new Object();

        public String getPassword() {
            synchronized (pawLock) {
                return password;
            }
        }

        public void updatePassword(String newPassword) {
            synchronized (pawLock) {
                this.password = newPassword;
            }
        }

        public long getBalance() {
            synchronized (balLock) {
                return balance;
            }
        }

        public void withDraw(long amt) {
            synchronized (balLock) {
                this.balance -= amt;
            }
        }

    }

    /**
     * 类型1示例2： 多个资源之间没有关联关系，使用同一把锁保护 粒度更粗 影响性能
     * 所有操作使用this对象这把锁
     */
    class Account1_2 {

        private String password = "123";

        private long balance = 1000;


        public synchronized String getPassword() {
            return password;
        }

        public synchronized void updatePassword(String newPassword) {
            this.password = newPassword;
        }

        public synchronized long getBalance() {
            return balance;
        }

        public synchronized void withDraw(long amt) {
            this.balance -= amt;
        }

    }

    /**
     * 类型2： 多个资源之间存在关联关系
     * 以两个账户之间转账为例 a 向 b转账，这里有两个资源 a账户和b账户，在转账的时候 a要减掉金额而b要加上金额，为避免多个线程同时减掉a账户的金额，
     * 需要对a账户进行加锁，为避免加上金额的操作结果覆盖其他线程线程对b账户余额的操作结果这里也要限制多个线程对b账户操作。所以a账户和b账户在转账这个操作中就具有了关联关系。
     * Account2_1 这个示例中 使用this 对象限制了转出账户的多线程操作，但没有限制 account2 被转入对象的多线程操作，这是 另一个线程 对余额操作辉存在并发问题，
     * this这把锁并能保护account2 这个资源
     * Account2_2 示例中 使用全局唯一对象 Account2_2.class 这把锁 能够保护 转入和转出这两个账户的资源。但是左右账户的转账操做 将是串行化的，
     * 即同一时间系统内只能有一个人进行转账 ，不具有可行性
     */
    class Account2_1 {

        private long balance = 100;

        public synchronized void transfer(Account2_1 account2, long amt) {
            this.balance -= amt;
            account2.balance += amt;
        }
    }

    class Account2_2 {

        private long balance = 100;

        public void transfer(Account2_1 account2, long amt) {
            synchronized (Account2_2.class) {
                this.balance -= amt;
                account2.balance += amt;
            }
        }
    }


    /**
     * this 对象不能保护两个acount 资源， account.class 对象有不具有可行性
     * 在这个转账操作中重要的是 能够覆盖 转入和转出这个两个资源。这是可以使用两把锁分别覆盖如示例Account2_3
     * <p>
     * 但是这种方式存在严重的问题 死锁，死锁一旦发生 线程会无限等待下去， 只能重启系统。
     */
    class Account2_3 {

        private long balance = 100;

        public void transfer(Account2_1 account2, long amt) {
            synchronized (this) {
                synchronized (account2) {
                    this.balance -= amt;
                    account2.balance += amt;
                }
            }
        }
    }
    /**
     * 死锁问题：
     * 死锁定义：一组相互竞争资源的线程因相互等待，导致"永久"阻塞的现象
     * 经经验总结形成死锁的四个条件：
     * 1.互斥，共享资源x和y只能同时被一个线程占用
     * 2.占用且等待，线程1获取资源X等待资源y时并不释放资源x。
     * 3.不可抢占，其他线程不能强行抢占线程1占有的资源
     * 4.循环等待，线程1等待线程线程2占有的资源，线程2等待线程1占有的资源，就形成了循环等待。
     * 要避免死锁问题，可以从破坏形成死锁的条件角度出发。
     * 条件1，线程安全使用的就是互斥特性，不能破坏。
     * 条件2，占有且等待，可以一次申请所需要的资源句不回等待了。
     * 条件3，不可抢占，线程占有部分资源的时候，如果申请不到剩余资源，就主动释放所占有的资源，就破坏了不可抢占条件。
     * 条件4，循环等待，可以按照申请资源的顺序来预防，先申请小的在申请大的，资源线性化申就能避免循环等待。
     */

    /**
     * 同时申请资源示例
     * 该中方式需要的一个统一管理者，管理所有的资源，管理者需是单例模式
     */
    class Example_1 {

        /**
         * 创建一个管理者该管理则必须为单例模式 在单例模式下 synchronized（this） 效果和 synchronized（xxx。class） 等效了
         */
        class Allocator {

            private List<Object> applyed = new ArrayList<Object>();//已经授权的申请资源

            public synchronized boolean apply(List<Object> objs) {
                if (objs == null || objs.size() < 1) {
                    return false;
                }
                boolean containsFlag = false;
                for (Object obj : objs) {
                    if (applyed.contains(obj)) {
                        containsFlag = true;
                        break;
                    }
                }
                if (containsFlag) {
                    return false;
                } else {
                    applyed.addAll(objs);
                    return true;
                }
            }

            public synchronized void release(List<Object> objs) {
                if (objs != null && objs.size() > 0) {
                    applyed.removeAll(objs);
                }
            }
        }


        /**
         * 和Allocator2 等效
         */
        class Allocator2 {

            private /**stati*/
                    List<Object> applyed = new ArrayList<Object>();//已经授权的申请资源

            public boolean apply(List<Object> objs) {
                synchronized (Allocator2.class) {
                    if (objs == null || objs.size() < 1) {
                        return false;
                    }
                    boolean containsFlag = false;
                    for (Object obj : objs) {
                        if (applyed.contains(obj)) {
                            containsFlag = true;
                            break;
                        }
                    }
                    if (containsFlag) {
                        return false;
                    } else {
                        applyed.addAll(objs);
                        return true;
                    }
                }
            }

            public void release(List<Object> objs) {
                synchronized (Allocator2.class) {
                    if (objs != null && objs.size() > 0) {
                        applyed.removeAll(objs);
                    }
                }
            }
        }


        private long balance = 100;

        private Allocator2 allocator;//单例的管理者


        /**
         * 破坏占有等待条件预防死锁
         *
         * @param account2
         * @param amt
         */
        public void transfer(Account2_1 account2, long amt) {
            // 循环一次性申请 两个资源
            while (!allocator.apply(Arrays.asList(this, account2))) {
                try {
                    synchronized (this) {
                        synchronized (account2) {
                            this.balance -= amt;
                            account2.balance += amt;
                        }
                    }
                } finally {
                    // 最终要释放资源
                    allocator.release(Arrays.asList(this, account2));
                }
            }
        }
    }

    /**
     * 破坏不可抢占条件示例
     */
    class Example_2 {
        /**
         * synchronized 关键字不能手动释放锁  需使用 locak工具
         */

    }

    /**
     * 破坏循环等待条件示例
     * 使资源有序 线性获取资源
     */
    class Example_3 {

        class Account_3 {

            private long id;

            private long balance;

            public void transfer(Account_3 target, long amt) {

                Account_3 first = this;

                Account_3 last = target;

                if (this.id > target.id) {
                    // 使资源有序
                    first = target;
                    last = this;
                }

                synchronized (first) {
                    synchronized (last) {
                        this.balance -= amt;
                        target.balance += amt;
                    }
                }
            }
        }
    }

    /**
     * 总结：
     * 在破坏占有等待条件的方案中 使用单例的管理者和while循环 对性能都有较大影响
     *
     * 破坏循环等待的条件中 仅多了几行对id的判断 性能无影响，但应具有为已的id 用于资源的排序
     */


    /**
     * 使用等待通知机制 单例模式的资源管理者优化， 避免while 空循环 影响性能
     * synchronized 与 wait notify notifyall 搭配 可以实现等待通知机制
     * 等待通知机制 时不满足添加的线程暂时阻塞，待条件变化时在唤醒，避免 while 空循环 耗费性能
     * <p>
     * 避免while 空循环问题  一般可以使用 等待通知机制 进行优化
     */
    class Allocator_3 {

        private List<Object> applyed = new ArrayList<Object>();//已经授权的申请资源

        public synchronized void apply(List<Object> objs) {
            boolean containsFlag = false;
            boolean contion = false;
            while (!contion) {
                for (Object obj : objs) {
                    if (applyed.contains(obj)) {
                        containsFlag = true;
                        break;
                    }
                }
                if (containsFlag) {
                    // 当条件不满足时 该现在在 this 这把锁上等待  进入锁的等待队列中 wait 方法会释放锁
                    try {
                        this.wait();
                    } catch (Exception e) {
                    }
                } else {
                    contion = true;
                    applyed.addAll(objs);
                }
            }
        }

        public synchronized void release(List<Object> objs) {
            if (objs != null && objs.size() > 0) {
                applyed.removeAll(objs);
                // 条件有所变化时 唤醒等待的所有线程尝试获取资源
                this.notifyAll();
            }
        }
    }

    /**
     * 优化后的管理者 进行转账示例
     */
    class Account_4 {

        private long balance = 100;

        // 单例模式
        Allocator_3 allocator_3;

        public void transfer(Account2_1 target, long amt) {
            allocator_3.apply(Arrays.asList(this, target));
            try {
                synchronized (this) {
                    synchronized (target) {
                        balance -= amt;
                        target.balance += amt;
                    }
                }
            } finally {
                // 最终要释放资源
                allocator_3.release(Arrays.asList(this, target));
            }
        }
    }
}
