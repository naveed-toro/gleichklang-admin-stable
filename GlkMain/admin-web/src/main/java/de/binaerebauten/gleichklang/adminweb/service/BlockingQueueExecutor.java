package de.binaerebauten.gleichklang.adminweb.service;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BlockingQueueExecutor {
    private static final Logger LOG = LoggerFactory.getLogger(BlockingQueueExecutor.class);

    private static int poolNumber = 1;
    private BlockingQueueExecutor() {
        // rem: No instances
    }
    public static ExecutorService createExecutor(int maximumAcceptableThreads, int nParallelThreads, int keepAliveTimeMS){

        final BlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(maximumAcceptableThreads);
        ThreadPoolExecutor threadPool = new ThreadPoolExecutor(nParallelThreads, nParallelThreads,
                keepAliveTimeMS, TimeUnit.MILLISECONDS, queue);
        // by default (unfortunately) the ThreadPoolExecutor will throw an exception
        // when you submit the more than maximumAcceptableThreads job, to have it block you do:
        threadPool.setRejectedExecutionHandler(new RejectedExecutionHandler() {
            public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                // this will block if the queue is full
                try {
                    executor.getQueue().put(r);
                } catch (InterruptedException e) {
                    LOG.error("error while adding process to queue:", e);
                    // keep the interrupt status
                    Thread.currentThread().interrupt();
                }
            }
        });
        return threadPool;
    }
}
