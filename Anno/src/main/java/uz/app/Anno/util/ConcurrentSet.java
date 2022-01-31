package uz.app.Anno.util;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Synchonous set container based on <b>ConcurrentHashMap</b>
 * @param <T>
 */
public class ConcurrentSet<T> extends java.util.concurrent.ConcurrentHashMap<Long, T> {
    private AtomicLong id;

    public <E> ConcurrentSet()
    {
        id = new AtomicLong();
        id.set(0);
    }

    public synchronized void add(T elem)
    {
        this.put(id.incrementAndGet(), elem);
    }

    public synchronized boolean hasElement(T elem)
    {
        return this.containsValue(elem);
    }

    public synchronized void removeElement(T elem)
    {
        if(!this.hasElement(elem))
            return;

        for (Entry<Long, T> pair: this.entrySet())
            if(pair.getValue().equals(elem))
                this.remove(pair.getKey());
    }
}
