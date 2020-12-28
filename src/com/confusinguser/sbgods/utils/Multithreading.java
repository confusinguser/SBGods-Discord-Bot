package com.confusinguser.sbgods.utils;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;


public class Multithreading {

    public static ExecutorService executor = Executors.newCachedThreadPool(new ThreadFactory() {
        final AtomicInteger counter = new AtomicInteger(0);

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, String.format("Thread %s", counter.incrementAndGet()));
        }
    });
    private static ScheduledExecutorService scheduledExecutor = Executors.newScheduledThreadPool(3, new ThreadFactory() {
        private final AtomicInteger counter = new AtomicInteger(0);

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "Thread " + counter.incrementAndGet());
        }
    });

    public static void scheduleAtFixedRate(Runnable r, long initialDelay, long delay, TimeUnit unit) {
        if (scheduledExecutor.isTerminated())
            scheduledExecutor = Executors.newScheduledThreadPool(3, new ThreadFactory() {
                private final AtomicInteger counter = new AtomicInteger(0);

                @Override
                public Thread newThread(@NotNull Runnable r) {
                    return new Thread(r, "Thread " + counter.incrementAndGet());
                }
            });
        scheduledExecutor.scheduleAtFixedRate(r, initialDelay, delay, unit);
    }

    public static void scheduleOnce(Runnable r, long delay, TimeUnit unit) {
        if (scheduledExecutor.isTerminated())
            scheduledExecutor = Executors.newScheduledThreadPool(3, new ThreadFactory() {
                private final AtomicInteger counter = new AtomicInteger(0);

                @Override
                public Thread newThread(@NotNull Runnable r) {
                    return new Thread(r, "Thread " + counter.incrementAndGet());
                }
            });
        scheduledExecutor.schedule(r, delay, unit);
    }

    public static void runAsync(Runnable runnable) {
        executor.execute(runnable);
    }

    public static int getTotal() {
        ThreadPoolExecutor tpe = (ThreadPoolExecutor) Multithreading.executor;
        return tpe.getActiveCount();
    }
}