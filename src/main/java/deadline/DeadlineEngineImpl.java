package deadline;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

public final class DeadlineEngineImpl implements DeadlineEngine {
    private final ConcurrentHashMap<Long, Long> idToDeadlines = new ConcurrentHashMap<>();
    private final ConcurrentSkipListMap<Long, Set<Long>> deadlineToIds = new ConcurrentSkipListMap<>();
    private final AtomicLong counter = new AtomicLong(0);

    @Override
    public long schedule(long deadlineMs) {
        long id = getKey();
        idToDeadlines.put(id, deadlineMs);
        // No concurrency requirement. The following needs to be in lock session if thread safe
        if (!deadlineToIds.containsKey(deadlineMs)){
            Set<Long> entries = new HashSet<>();
            entries.add(id);
            deadlineToIds.put(deadlineMs, entries);
        } else {
            deadlineToIds.get(deadlineMs).add(id);
        }
        return id;
    }

    /**
     * Get the requested Id in the range of long min value to max value
     * @return requested Id for the deadline event
     */
    private long getKey() {
        if (size() == Integer.MAX_VALUE){
            throw new RuntimeException("Max number of deadlines exceeded.");
        }
        // Collision might happen. Check if the key is used already. O(n)
        long nextCnt = counter.incrementAndGet();
        if (nextCnt == Integer.MAX_VALUE){
            counter.set(0);
        }
        while(idToDeadlines.containsKey(counter.get())){
            nextCnt = counter.incrementAndGet();
            if (nextCnt == Integer.MAX_VALUE){
                counter.set(0);
            }
        }
        return nextCnt;
    }

    @Override
    public boolean cancel(long requestId) {
        if (idToDeadlines.containsKey(requestId)){
            long deadLineMs = idToDeadlines.remove(requestId);
            Set<Long> ids = deadlineToIds.get(deadLineMs);
            if (ids != null){
                boolean removed = ids.remove(requestId);
                if (ids.isEmpty()){
                    deadlineToIds.remove(deadLineMs);
                }
                return removed;
            }
        }
        return false;
    }

    @Override
    public int poll(long nowMs, Consumer<Long> handler, int maxPoll) {
        ConcurrentNavigableMap<Long, Set<Long>> expired = deadlineToIds.headMap(nowMs, true);
        int count = 0;
        for (Map.Entry<Long, Set<Long>> sets : expired.entrySet()){
            for (long e : sets.getValue()){
                handler.accept(e);
                count++;
                if (count >= maxPoll){
                    return count;
                }
            }
        }
        return count;
    }

    @Override
    public int size() {
        return idToDeadlines.size(); // max size can only be integer max given the interface specification
    }
}
