package cn.ac.origind.asyncoptimization.concurrent;

import java.util.function.Consumer;

public class RunnableRunner implements Consumer<Runnable> {
    @Override
    public void accept(Runnable runnable) {
        runnable.run();
    }
}
