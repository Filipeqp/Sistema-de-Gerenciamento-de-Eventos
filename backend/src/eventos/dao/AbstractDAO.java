package eventos.dao;

import eventos.index.ExtensibleHashIndex;
import eventos.model.Record;
import eventos.persistence.BinaryRecordFile;
import eventos.persistence.RecordEnvelope;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractDAO<T extends Record> {

    protected final BinaryRecordFile<T> dataFile;
    protected final ExtensibleHashIndex primaryIndex;

    protected AbstractDAO(String basePath, String entityName, Class<T> recordClass) throws Exception {
        this.dataFile = new BinaryRecordFile<>(basePath + "/dados/" + entityName, entityName, recordClass.getConstructor());
        this.primaryIndex = new ExtensibleHashIndex(basePath + "/indices/" + entityName, entityName + "_primary", 4);
        rebuildPrimaryIndexIfNeeded();
    }

    public T findById(int id) throws Exception {
        long position = primaryIndex.get(id);
        if (position < 0) {
            return null;
        }
        return dataFile.read(position);
    }

    public List<T> listAll() throws Exception {
        List<T> items = new ArrayList<>();
        for (RecordEnvelope<T> envelope : dataFile.scanActive()) {
            items.add(envelope.getRecord());
        }
        return items;
    }

    protected T createInternal(T record) throws Exception {
        record.setId(dataFile.nextId());
        long position = dataFile.create(record);
        primaryIndex.put(record.getId(), position);
        afterCreate(record);
        return record;
    }

    protected T updateInternal(int id, T updated) throws Exception {
        long currentPosition = primaryIndex.get(id);
        if (currentPosition < 0) {
            return null;
        }

        T previous = dataFile.read(currentPosition);
        if (previous == null) {
            primaryIndex.remove(id);
            return null;
        }

        updated.setId(id);
        long newPosition = dataFile.update(currentPosition, updated);
        if (newPosition < 0) {
            return null;
        }
        primaryIndex.put(id, newPosition);
        afterUpdate(previous, updated);
        return updated;
    }

    protected boolean deleteInternal(int id) throws Exception {
        long position = primaryIndex.get(id);
        if (position < 0) {
            return false;
        }

        T previous = dataFile.read(position);
        if (previous == null) {
            primaryIndex.remove(id);
            return false;
        }

        boolean removed = dataFile.delete(position);
        if (removed) {
            primaryIndex.remove(id);
            afterDelete(previous);
        }
        return removed;
    }

    protected void afterCreate(T record) throws Exception {
    }

    protected void afterUpdate(T previous, T updated) throws Exception {
    }

    protected void afterDelete(T previous) throws Exception {
    }

    protected void rebuildPrimaryIndexIfNeeded() throws Exception {
        if (!primaryIndex.isEmpty()) {
            return;
        }
        for (RecordEnvelope<T> envelope : dataFile.scanActive()) {
            primaryIndex.put(envelope.getRecord().getId(), envelope.getPosition());
        }
    }
}
