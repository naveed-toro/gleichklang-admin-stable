package de.binaerebauten.gleichklang.adminweb.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;


class RequestExecutor {

    private static final Logger LOG = LoggerFactory.getLogger(RequestExecutor.class);
    /**
     * Maximun threads allowed in queue, after that the queue waits
     */
    private static final int MAX_THREADS_IN_QUEUE  = 500;

    private static final int MAX_CUNCURRENT_THREADS =  32;

    /**
     * Keep alive time for threads
     */
    private static final int KEEP_ALIVE_TIME_MS =  1000000;

    private ExecutorService executorService;

    private static final RequestExecutor requestExecutor = new RequestExecutor();

    public static RequestExecutor getExecutorService() {
        return requestExecutor;
    }

    public void init()
    {
        LOG.info("init executorService");
        executorService = BlockingQueueExecutor
                .createExecutor(MAX_THREADS_IN_QUEUE,
                        MAX_CUNCURRENT_THREADS,
                        KEEP_ALIVE_TIME_MS);
    }

    public void destroy()
    {
        LOG.info("shutting down executorService");
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(600, TimeUnit.MINUTES)) {
                executorService.shutdownNow();
                LOG.info("shutdown of executor service done");
            }
        } catch (InterruptedException e) {
            LOG.error("Executor service execution error",e);
            executorService.shutdownNow();
        }
        finally
        {
            LOG.info("shutdown of executor service done");
        }

    }


    private RequestExecutor() {
        init();
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                executorService.shutdown();
            }
        }));
    }

    <T> Future<T> submit(Callable<T> task){
        return executorService.submit(task);
    }


    <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException{ return executorService.invokeAll(tasks); }
}