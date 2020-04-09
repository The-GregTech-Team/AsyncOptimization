package cn.ac.origind.asyncoptimization.concurrent;

import cn.ac.origind.asyncoptimization.AsyncOptimization;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListenableFutureTask;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

public interface IAsyncThreadListener extends IThreadListener {
    WaitableHashMap<Thread, Runnable> WAITING_THREADS = new WaitableHashMap<>();

    Logger LOGGER = LogManager.getLogger(AsyncOptimization.class);

    void startLoop();

    IFieldContainer<Boolean> running();

    BlockingDeque<Runnable> runnables();

    Thread asyncThread();

    @Nonnull
    default ListenableFuture<Object> addScheduledTask(@Nonnull Runnable runnable) {
        Callable<Object> callable = () -> {
            try {
                runnable.run();
            } catch (Throwable t) {
                if (!(t instanceof ThreadQuickExitException))
                    LOGGER.catching(t);
            }
            return null;
        };
        try {
            if (!isCallingFromMinecraftThread() || asyncThread() == null || asyncThread().isInterrupted()) {
                ListenableFutureTask<Object> futureTask = ListenableFutureTask.create(callable);
                runnables().add(futureTask);
                return futureTask;
            }
            else
                try {
                    return Futures.immediateFuture(callable.call());
                } catch (Exception e) {
                    return Futures.immediateFailedFuture(e);
                }
        } catch (Exception e) {
            try {
                LOGGER.error(asyncThread() + " - " + running() + " - " + running().get());
            } catch (Throwable throwable) {
                throw new RuntimeException(throwable);
            }
            throw new RuntimeException(e);
        }
    }

    default void syncCall(Runnable runnable) {
        // Calling from the caller thread, not the queueing thread.
        if (Thread.currentThread() == asyncThread()) {
            runnable.run();
            return;
        }
        ListenableFutureTask<?> ft = ListenableFutureTask.create(() -> {
            try {
                runnable.run();
            } catch (Throwable t) {
                if (!(t instanceof ThreadQuickExitException))
                    LOGGER.catching(t);
            }
            return null;
        });
        WAITING_THREADS.executeAndClearIfPresentAndRun(Thread.currentThread(), Runnable::run, () -> {
            WAITING_THREADS.waitAndPut(asyncThread(), ft);
            asyncThread().interrupt();
        });
        try {
            ft.get();
        } catch (InterruptedException | ExecutionException e) {
            // IGNORED
        }
    }
}
