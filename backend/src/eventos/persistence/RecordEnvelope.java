package eventos.persistence;

public class RecordEnvelope<T> {
    private final long position;
    private final T record;

    public RecordEnvelope(long position, T record) {
        this.position = position;
        this.record = record;
    }

    public long getPosition() {
        return position;
    }

    public T getRecord() {
        return record;
    }
}
