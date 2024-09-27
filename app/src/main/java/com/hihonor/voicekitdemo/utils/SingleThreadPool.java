/*
 * Copyright (c) Honor Device Co., Ltd. 2024-2024. All rights reserved.
 */

package com.hihonor.voicekitdemo.utils;

import android.text.TextUtils;
import android.util.Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 单线程的线程池工具
 *
 * @since 2024-07-18
 */
public class SingleThreadPool {
    private static final String TAG = SingleThreadPool.class.getSimpleName();

    /**
     * 线程池队列大小，每个线程至少占用64k，
     */
    private static final int MAX_TASK_SIZE = 16;

    /**
     * 线程池对象
     */
    private ExecutorService threadPool;

    /**
     * 线程池工厂
     */
    private ThreadFactory threadFactory = runnable -> new Thread(runnable, "SingleThreadPool-" + System.nanoTime());

    private SingleThreadPool() {
        threadPool = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(MAX_TASK_SIZE),
            threadFactory, new ThreadPoolExecutor.DiscardPolicy());
    }

    /**
     * 单例的静态内部类
     *
     * @since 2024-07-18
     */
    private static class SingletonHolder {
        private static final SingleThreadPool INSTANCE = new SingleThreadPool();
    }

    /**
     * 获取单实例
     *
     * @return instance
     */
    public static SingleThreadPool getInstance() {
        return SingletonHolder.INSTANCE;
    }

    /**
     * 执行Runnable
     *
     * @param runnable Runnable实例
     * @param threadName 线程名称
     */
    public void execute(Runnable runnable, String threadName) {
        if (threadPool == null) {
            Log.e(TAG, "threadPool is null ");
            return;
        }
        if (runnable == null) {
            Log.e(TAG, "Runnable is null ");
            return;
        }
        Log.d(TAG, "Execute runnable in ThreadPool:" + getClass().getSimpleName() + ", name: " + threadName);
        try {
            if (TextUtils.isEmpty(threadName)) {
                threadPool.execute(runnable);
            } else {
                threadPool.execute(() -> {
                    Thread.currentThread().setName(threadName);
                    runnable.run();
                });
            }
        } catch (RejectedExecutionException exception) {
            Log.e(TAG, threadName + "execute is Rejected，Reason:" + exception);
        }
    }
}
