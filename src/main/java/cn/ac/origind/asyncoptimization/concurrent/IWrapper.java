package cn.ac.origind.asyncoptimization.concurrent;

public interface IWrapper<T> {
    T getWrapper();

    void setWrapper(T wrapper);
}
