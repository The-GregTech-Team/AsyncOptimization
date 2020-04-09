package cn.ac.origind.asyncoptimization.concurrent;

import com.google.common.util.concurrent.ListenableFuture;

public interface IThreadListener {
    ListenableFuture<Object> addScheduledTask(Runnable runnableToSchedule);

    boolean isCallingFromMinecraftThread();
}
