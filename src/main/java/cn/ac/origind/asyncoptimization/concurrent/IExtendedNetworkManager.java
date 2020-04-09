package cn.ac.origind.asyncoptimization.concurrent;

public interface IExtendedNetworkManager {
    boolean isThreadRelated();
    IThreadListener getThreadListener();
    void safeProcessReceivedPackets();
}
