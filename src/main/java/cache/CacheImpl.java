package cache;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.function.Function;

public class CacheImpl<K, V> implements Cache<K, V> {
    private final ConcurrentHashMap<K, V> itemStore = new ConcurrentHashMap<K, V>();
    private final Set<K> keySet = new HashSet<>();
    private final Function<K, V> customFct;

    public CacheImpl(Function<K, V> customFct) {
        this.customFct = customFct;
    }

    public V get(K key) {
        if (key == null){
            return null;
        }
        if (!itemStore.containsKey(key)) {
            itemStore.putIfAbsent(key, invokeFunc(key));
        }
        return itemStore.get(key);
    }

    protected V invokeFunc(K key) {
            return customFct.apply(key);
    }
}
