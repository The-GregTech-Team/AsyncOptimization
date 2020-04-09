package cn.ac.origind.asyncoptimization.concurrent.cache;

public interface ICastable<S> {
    @SuppressWarnings("unchecked")
    default <T extends S> T cast() { return (T) this; }
}
