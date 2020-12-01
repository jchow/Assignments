package cache;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

public class CacheImpl<K, V> implements Cache<K, V> {
    private final ConcurrentHashMap<K, V> itemStore = new ConcurrentHashMap<K, V>();
    private final Function<K, V> customFct;
    private final ReentrantLock lockMe = new ReentrantLock();

    public CacheImpl(Function<K, V> customFct) {
        this.customFct = customFct;
    }

    @Override
    public V get(K key) {
        if (key == null){
            return null;
        }
        lockMe.lock();
        try {
            if (!itemStore.containsKey(key)) {
                itemStore.putIfAbsent(key, invokeFunc(key));
            }
        } finally {
            lockMe.unlock();
        }
        return itemStore.get(key);
    }

    protected V invokeFunc(K key) {
            return customFct.apply(key);
    }
}
