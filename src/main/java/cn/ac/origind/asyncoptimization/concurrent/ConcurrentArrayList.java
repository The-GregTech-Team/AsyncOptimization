package cn.ac.origind.asyncoptimization.concurrent;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class ConcurrentArrayList<T> extends ArrayList<T> {
    public Queue<T> delegate;

    public ConcurrentArrayList(Queue<T> delegate) {
        this.delegate = delegate;
    }

    @Override
    public boolean add(T t) {
        return delegate.add(t);
    }

    public boolean offer(T t) {
        return delegate.offer(t);
    }

    public T remove() {
        return delegate.remove();
    }

    public T poll() {
        return delegate.poll();
    }

    public T element() {
        return delegate.element();
    }

    public T peek() {
        return delegate.peek();
    }

    @Override
    public int size() {
        return delegate.size();
    }

    @Override
    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return delegate.contains(o);
    }

    @Override
    public Iterator<T> iterator() {
        return delegate.iterator();
    }

    @Override
    public Object[] toArray() {
        return delegate.toArray();
    }

    @Override
    public <T1> T1[] toArray(T1[] a) {
        return delegate.toArray(a);
    }

    @Override
    public boolean remove(Object o) {
        return delegate.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return delegate.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        return delegate.addAll(c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return delegate.removeAll(c);
    }

    @Override
    public boolean removeIf(Predicate<? super T> filter) {
        return delegate.removeIf(filter);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return delegate.retainAll(c);
    }

    @Override
    public void clear() {
        delegate.clear();
    }

    @Override
    public boolean equals(Object o) {
        return delegate.equals(o);
    }

    @Override
    public int hashCode() {
        return delegate.hashCode();
    }

    @Override
    public Spliterator<T> spliterator() {
        return delegate.spliterator();
    }

    @Override
    public Stream<T> stream() {
        return delegate.stream();
    }

    @Override
    public Stream<T> parallelStream() {
        return delegate.parallelStream();
    }

    @Override
    public void forEach(Consumer<? super T> action) {
        delegate.forEach(action);
    }
}
