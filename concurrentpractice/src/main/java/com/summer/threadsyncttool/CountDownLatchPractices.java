package com.summer.threadsyncttool;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;

/**
 * @author xiashanhao
 * @date 2020-01-16 12:39
 */
public class CountDownLatchPractices {

    public static void main(String[] args) {

        final CountDownLatch downLatch = new CountDownLatch(3);
        for (int i = 0; i < 3; i++) {
            new Thread(new Runnable() {
                public void run() {
                    try {
                        Thread.sleep(1000);
                        System.out.println("i=");

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    downLatch.countDown();
                }
            }).start();
        }

        try {
            System.out.println("wait");
            downLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("end");

    }
}
