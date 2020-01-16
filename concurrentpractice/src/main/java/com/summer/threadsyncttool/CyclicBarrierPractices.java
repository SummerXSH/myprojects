package com.summer.threadsyncttool;

import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;

/**
 * @author xiashanhao
 * @date 2020-01-16 12:47
 */
public class CyclicBarrierPractices {
    public static void main(String[] args) throws Exception {

        System.out.println("begin");
        CyclicBarrier barrier = new CyclicBarrier(3);
        CountDownLatch latch = new CountDownLatch(3);
        for (int i = 0; i < 3; i++) {
            new Thread(new Task(i, barrier,latch)).start();
        }
        latch.await();

        System.out.println("end");


    }

    static class Task implements Runnable {

        private Integer i;

        private CyclicBarrier barrier;

        private CountDownLatch latch;

        public Task(Integer i, CyclicBarrier barrier, CountDownLatch latch) {
            this.i = i;
            this.barrier = barrier;
            this.latch = latch;
        }

        public void run() {
            for (int j = 0; j < 5; j++) {
                try {
                    Thread.sleep(new Random().nextInt(10000));
                    System.out.println("i = " + i + " , j = " + j);
                    barrier.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                }
            }
            latch.countDown();
        }
    }
}
