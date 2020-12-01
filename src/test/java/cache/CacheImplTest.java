package cache;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class CacheImplTest {

    private final CacheImpl<Integer, String> target = new CacheImpl<Integer, String>(x->String.valueOf(x).concat("foo"));
    private final CacheImpl<Integer, String> spyTarget = Mockito.spy(target);

    @Test
    void getCachedItem() {
        assertEquals("0foo", spyTarget.get(0));
        assertEquals("0foo", spyTarget.get(0));
        verify(spyTarget, times(1)).invokeFunc(0);
    }

    @Test
    void nullKeyCase() {
        assertNull(spyTarget.get(null));
        verify(spyTarget, times(0)).invokeFunc(any());
    }

    @Test
    void exceptional(){
        final CacheImpl<Integer, String> cache = new CacheImpl<Integer, String>(x-> {throw new RuntimeException("Fault");});
        assertThrows(RuntimeException.class, () -> {cache.get(3);});
    }

    @Test
    void multiThread() throws InterruptedException {
        int numThreads = 2;
        final int testKey = 100;
        ExecutorService service = Executors.newFixedThreadPool(numThreads);
        CountDownLatch count = new CountDownLatch(numThreads);
        for(int i=0; i<numThreads; i++){
            service.submit(()->{
                    spyTarget.get(testKey);
                    count.countDown();
            });
        }
        count.await();
        verify(spyTarget, times(1)).invokeFunc(testKey);
    }


}