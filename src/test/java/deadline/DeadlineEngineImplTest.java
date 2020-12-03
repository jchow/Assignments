package deadline;

import org.junit.Before;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class DeadlineEngineImplTest {

    private DeadlineEngine target = new DeadlineEngineImpl();

    @Test
    void schedule() {
        assertEquals(1L, target.schedule(1L));
        assertEquals(2L, target.schedule(1L));
        assertEquals(3L, target.schedule(2L));
        assertEquals(4L, target.schedule(2L));
        assertEquals(5L, target.schedule(Instant.now().toEpochMilli()+3000L));
    }

    @Test
    void cancel() {
        assertEquals(1L, target.schedule(1L));
        assertEquals(2L, target.schedule(2L));
        assertTrue(target.cancel(1L));
        assertFalse(target.cancel(1L));
        assertFalse(target.cancel(3L));
        assertTrue(target.cancel(2L));
    }

    /**
     * Beware this is a long running test
     */
    @Test
    void getKey() {
        for (long i= 0L; i<Integer.MAX_VALUE+1L; i++){
            long id = target.schedule(100L+i);
            target.cancel(id);
        }
        assertEquals(2, target.schedule(100L));
    }

    @Test
    void poll() throws InterruptedException {
        long nowMs = Instant.now().toEpochMilli();
        target.schedule(1000L+nowMs);
        target.schedule(2000L+nowMs);
        target.schedule(3000L+nowMs);
        Thread.sleep(4000);
        assertEquals(3, target.poll(Instant.now().toEpochMilli(), System.out::println, 5));
    }

    @Test
    void pollWithMax() throws InterruptedException {
        long nowMs = Instant.now().toEpochMilli();
        target.schedule(1000L+nowMs);
        target.schedule(2000L+nowMs);
        target.schedule(3000L+nowMs);
        Thread.sleep(4000);
        assertEquals(2, target.poll(Instant.now().toEpochMilli(), System.out::println, 2));
    }

    @Test
    void pollRemaining() throws InterruptedException {
        long now = Instant.now().toEpochMilli();
        target.schedule(now + 1000L);
        target.schedule(now + 2000L);
        target.schedule(now + 3000L);
        target.schedule(now + 9000L);
        Thread.sleep(4000L);
        assertEquals(3, target.poll(Instant.now().toEpochMilli(), System.out::println, 6));
        Thread.sleep(6000L);
        assertEquals(4, target.poll(Instant.now().toEpochMilli(), System.out::println, 6));
    }

    @Test
    void exceptional(){
        long nowMs = Instant.now().toEpochMilli();
        target.schedule(nowMs-100L);
        assertThrows(RuntimeException.class, ()->target.poll(Instant.now().toEpochMilli(), x-> {throw new RuntimeException("Fault");}, 2));
    }

    @Test
    void size() {
        long id1 = target.schedule(1L);
        long id2 = target.schedule(1L);
        assertEquals(2, target.size());

        target.cancel(id1);
        target.cancel(id2);
        assertEquals(0, target.size());
    }
}