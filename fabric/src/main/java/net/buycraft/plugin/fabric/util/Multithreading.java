package net.buycraft.plugin.fabric.util;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Multithreading {
    private static final AtomicInteger counter = new AtomicInteger(0);

    private static final ScheduledExecutorService RUNNABLE_POOL = Executors.newScheduledThreadPool(10, r ->
            new Thread(r, "Tebex Thread " + counter.incrementAndGet()));

    public static ThreadPoolExecutor POOL = new ThreadPoolExecutor(10, 30,
            0L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(),
            r -> new Thread(r, String.format("Thread %s", counter.incrementAndGet())));

    public static ScheduledFuture<?> schedule(Runnable r, long initialDelay, long delay, TimeUnit unit) {
        return RUNNABLE_POOL.scheduleAtFixedRate(r, initialDelay, delay, unit);
    }

    public static ScheduledFuture<?> schedule(Runnable r, long delay, TimeUnit unit) {
        return RUNNABLE_POOL.schedule(r, delay, unit);
    }

    public static Executor delayedExecutor(long delay, TimeUnit unit) {
        return task -> schedule(task, delay, unit);
    }

    public static void runAsync(Runnable runnable) {
        POOL.execute(runnable);
    }

    public static Future<?> submit(Runnable runnable) {
        return POOL.submit(runnable);
    }
}
