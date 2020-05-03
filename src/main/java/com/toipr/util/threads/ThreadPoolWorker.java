package com.toipr.util.threads;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ThreadPoolWorker {
    protected static ScheduledExecutorService instance = null;

    public static synchronized boolean initThreadPool(int nThreads){
        if(instance==null){
            instance = Executors.newScheduledThreadPool(nThreads);
        }
        return true;
    }

    public static Object submit(Runnable task){
        return instance.submit(task);
    }

    public static Object schedule(Runnable task, long delay, TimeUnit unit){
        return instance.schedule(task, delay, unit);
    }

    public static Object scheduleWithFixedDelay(Runnable task, long initialDelay, long delay, TimeUnit unit){
        return instance.scheduleWithFixedDelay(task, initialDelay, delay, unit);
    }
}
