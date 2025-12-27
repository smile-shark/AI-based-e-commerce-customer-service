package com.smileshark.executor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class GlobalTreadPool {
    // 核心线程数
    private static final int CORE_POOL_SIZE = 5;
    // 最大线程数
    private static final int MAX_POOL_SIZE = 20;
    // 线程空闲时间
    private static final long KEEP_ALIVE_TIME = 60L;
    
    public static ExecutorService executor = new ThreadPoolExecutor(
        CORE_POOL_SIZE,
        MAX_POOL_SIZE,
        KEEP_ALIVE_TIME,
        TimeUnit.SECONDS,
        new LinkedBlockingQueue<>(100),
        Executors.defaultThreadFactory(),
        new ThreadPoolExecutor.CallerRunsPolicy() // 防止任务被拒绝
    );
}