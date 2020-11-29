package cache;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

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


}