package deadline;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

public final class DeadlineEngineImpl implements DeadlineEngine {
    private final ConcurrentHashMap<Long, DeadLineEvent> items = new ConcurrentHashMap<>();
    private final AtomicLong counter = new AtomicLong(0L); // How to reuse counter associated with canceled events?

    @Override
    public long schedule(long deadlineMs) {
        long key = counter.incrementAndGet();
        items.put(key, new DeadLineEvent(key, deadlineMs));
        return key;
    }

    @Override
    public boolean cancel(long requestId) {
        if (items.containsKey(requestId)){
            items.remove(requestId);
            return true;
        }
        return false;
    }

    @Override
    public int poll(long nowMs, Consumer<Long> handler, int maxPoll) {
        List<DeadLineEvent> entries = new ArrayList<>();
        for (DeadLineEvent e : items.values()){
            if (e.getDeadline() <=nowMs){
                entries.add(items.remove(e.getKey()));
            }
        }
        int limit = Math.min(entries.size(), maxPoll);
        for (int i = 0; i < limit; i++) {
            handler.accept(entries.get(i).getKey());
        }
        return limit;
    }

    @Override
    public int size() {
        return items.size();
    }
}
