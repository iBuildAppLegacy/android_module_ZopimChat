/****************************************************************************
*                                                                           *
*  Copyright (C) 2014-2015 iBuildApp, Inc. ( http://ibuildapp.com )         *
*                                                                           *
*  This file is part of iBuildApp.                                          *
*                                                                           *
*  This Source Code Form is subject to the terms of the iBuildApp License.  *
*  You can obtain one at http://ibuildapp.com/license/                      *
*                                                                           *
****************************************************************************/
package com.ibuildapp.ZopimChatPlugin.core;

/**
 * Created by doubledeath on 24.07.2015.
 */

import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public enum Core {

    INSTANCE;

    private final LruCache<String, Bitmap> cacheBitmap;

    private final ThreadPoolExecutor threadPoolNetwork;
    private final ThreadPoolExecutor threadPoolLocal;

    private final BlockingQueue<Runnable> queueNetwork;
    private final BlockingQueue<Runnable> queueLocal;

    Core() {
        final int CORES = Runtime.getRuntime().availableProcessors();
        final int THREAD_POOL_SIZE_NETWORK = CORES + 1;
        final int THREAD_POOL_SIZE_NETWORK_MAX = CORES * 2 + 1;
        final long KEEP_ALIVE_VALUE = 1;
        final TimeUnit KEEP_ALIVE_VALUE_TIME_UNIT = TimeUnit.SECONDS;
        final int CACHE_BITMAP_SIZE = (int)(Runtime.getRuntime().maxMemory() / 8192f);

        cacheBitmap = new LruCache<String, Bitmap>(CACHE_BITMAP_SIZE) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return (int) ((bitmap.getRowBytes() * bitmap.getHeight()) / 1024f);
            }
        };

        queueNetwork = new LinkedBlockingQueue<>();
        queueLocal = new LinkedBlockingQueue<>();

        threadPoolNetwork = new ThreadPoolExecutor(
                THREAD_POOL_SIZE_NETWORK,
                THREAD_POOL_SIZE_NETWORK_MAX,
                KEEP_ALIVE_VALUE,
                KEEP_ALIVE_VALUE_TIME_UNIT,
                queueNetwork
        );
        threadPoolLocal = new ThreadPoolExecutor(
                CORES,
                CORES,
                KEEP_ALIVE_VALUE,
                KEEP_ALIVE_VALUE_TIME_UNIT,
                queueLocal
        );
    }

    public void cacheBitmap(String key, Bitmap bitmap) {
        if (getCachedBitmap(key) == null)
            cacheBitmap.put(key, bitmap);
    }

    public Bitmap getCachedBitmap(String key) {
        return cacheBitmap.get(key);
    }

    public void addTask(Runnable task) {
        threadPoolLocal.execute(task);
    }

    public void cancelTask(Runnable task) {
        if(task != null) {
            queueNetwork.remove(task);
            queueLocal.remove(task);
        }
    }

    public <T extends Result> T getInFuture(Callable<T> task) throws ExecutionException, InterruptedException {
        return threadPoolNetwork.submit(task).get();
    }

    public void addComplexTask(ComplexTask task) {
        threadPoolNetwork.execute(task);
    }

    public interface Result<T> {
        T getResult() throws Exception;
    }

    public static abstract class ComplexTask implements Runnable {

        public enum DownloadResult {
            COMPLETED, FAILED, NEEDLESS
        }

        private DownloadResult downloadResult;

        abstract protected DownloadResult download();
        abstract protected void work(DownloadResult downloadResult);

        @Override
        final public void run() {
            if(downloadResult == null) {
                downloadResult = download();
                Core.INSTANCE.threadPoolLocal.execute(this);
            } else
                work(downloadResult);
        }

    }

}
