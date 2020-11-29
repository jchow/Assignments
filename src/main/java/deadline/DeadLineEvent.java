package deadline;

public class DeadLineEvent {
    private long key;
    private long deadlineMs;

    public DeadLineEvent(long key, long deadlineMs) {
        this.key = key;
        this.deadlineMs = deadlineMs;
    }

    public long getDeadline() {
        return this.deadlineMs;
    }

    public Long getKey() {
        return this.key;
    }
}
