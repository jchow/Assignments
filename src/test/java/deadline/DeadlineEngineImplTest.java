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
        assertEquals(2L, target.schedule(2L));
        assertEquals(3L, target.schedule(Instant.now().toEpochMilli()+3000L));
    }

    @Test
    void cancel() {
        assertEquals(1L, target.schedule(1L));
        assertEquals(2L, target.schedule(2L));
        assertTrue(target.cancel(1L));
        assertFalse(target.cancel(3L));
        assertFalse(target.cancel(0L));
    }

    @Test
    void poll() throws InterruptedException {
        target.schedule(1000L);
        target.schedule(2000L);
        target.schedule(3000L);
        Thread.sleep(4000L);
        assertEquals(3, target.poll(Instant.now().toEpochMilli(), System.out::println, 5));
    }

    @Test
    void pollWithMax() throws InterruptedException {
        target.schedule(1000L);
        target.schedule(2000L);
        target.schedule(3000L);
        Thread.sleep(4000L);
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
        assertEquals(1, target.poll(Instant.now().toEpochMilli(), System.out::println, 6));
    }

    @Test
    void size() {
        target.schedule(1L);
        target.schedule(1L);
        assertEquals(2, target.size());
    }
}