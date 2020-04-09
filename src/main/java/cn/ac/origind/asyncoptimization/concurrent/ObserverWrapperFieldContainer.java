package cn.ac.origind.asyncoptimization.concurrent;

import java.util.function.BiConsumer;

public class ObserverWrapperFieldContainer<T> implements IFieldContainer<T>, IWrapper<IFieldContainer<T>> {
    protected IFieldContainer<T> wrapper;
    protected BiConsumer<T, T> observer;

    public ObserverWrapperFieldContainer(IFieldContainer<T> wrapper, BiConsumer<T, T> observer) {
        this.wrapper = wrapper;
        this.observer = observer;
    }

    @Override
    public T get() {
        return wrapper.get();
    }

    @Override
    public void set(T value) {
        observer.accept(get(), value);
        wrapper.set(value);
    }

    @Override
    public IFieldContainer<T> getWrapper() {
        return wrapper;
    }

    @Override
    public void setWrapper(IFieldContainer<T> wrapper) {
        this.wrapper = wrapper;
    }
}
