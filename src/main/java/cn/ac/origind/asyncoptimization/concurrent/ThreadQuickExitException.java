package cn.ac.origind.asyncoptimization.concurrent;

public class ThreadQuickExitException extends RuntimeException {
    public static final ThreadQuickExitException INSTANCE = new ThreadQuickExitException();

    private ThreadQuickExitException()
    {
        this.setStackTrace(new StackTraceElement[0]);
    }

    public synchronized Throwable fillInStackTrace()
    {
        this.setStackTrace(new StackTraceElement[0]);
        return this;
    }
}
