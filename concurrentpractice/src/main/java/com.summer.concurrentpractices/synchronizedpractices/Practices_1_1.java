package com.summer.concurrentpractices.synchronizedpractices;

/**
 * 资源和锁 1:1 类型 syncronized 实践 即 一把锁保护一个资源
 * 资源和锁的关系 最好不是 1:N 即多把锁保护一个资源 容易出现死锁的问题 ？？？
 *
 * @author xiashanhao
 * @date 2019-05-07 11:59
 */
public class Practices_1_1 {

    /**
     * 示例1 存在并发问题的计算 原子性问题和可见性问题
     */
    class SafeCalc1 {
        private long value = 0;

        /**
         * 当多线程同时调用该方法时 存在原子性问题
         */
        public void addOne() {
            value += 1;
        }

        /**
         * 当通过该方法回去值时，获取的可能不是最新的 存在可见性问题
         *
         * @return
         */
        public long getValue() {
            return value;
        }
    }

    /**
     * 示例2  使用syncronized 解决原子性和可见性问题
     */
    class SafeCalc2 {
        private long value = 0;

        /**
         * synchronized 修饰 保证同时只有一个线程调用该对象的该方法 解决了原子性问题 同时有与 synchronized
         * 的happen-befor（解锁操作对下一个的加锁操作 具有可见性）规则 在本方法内 不存在可见性问题 但是数据的一致性程度不如加锁高
         */
        public synchronized void addOne() {
            value += 1;
        }

        /**
         * 这里使用synchronized 是利用 happen-befor规则来解决value可见性问题，被加锁之后 两个方法只允许同时被被一个线程调用 降低了性能
         *
         * @return
         */
        public synchronized long getValue() {
            return value;
        }
    }

    class SafeCalc3 {

        /**
         * 使用volatile 修饰 可以解决可见性问题 get方法不需要synchronized修饰也不回存在可见性问题
         */
        private volatile long value = 0;

        public synchronized void addOne() {
            value += 1;
        }

        public long getValue() {
            return value;
        }

    }
}
