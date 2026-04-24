package eventos.persistence;

import eventos.model.Record;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

public class BinaryRecordFile<T extends Record> {

    private static final byte ACTIVE = ' ';
    private static final byte DELETED = '*';
    private static final int HEADER_SIZE = 12;
    private static final int RECORD_HEADER_SIZE = 5;
    private static final int FREE_SLOT_POINTER_SIZE = 8;
    private static final byte[] ZERO_BUFFER = new byte[1024];

    private final RandomAccessFile file;
    private final Constructor<T> constructor;

    public BinaryRecordFile(String basePath, String fileName, Constructor<T> constructor) throws Exception {
        this.constructor = constructor;

        File directory = new File(basePath);
        if (!directory.exists() && !directory.mkdirs()) {
            throw new IOException("Nao foi possivel criar o diretorio " + basePath);
        }

        File target = new File(directory, fileName + ".db");
        this.file = new RandomAccessFile(target, "rw");
        if (file.length() < HEADER_SIZE) {
            file.seek(0);
            file.writeInt(0);
            file.writeLong(-1L);
        }
    }

    public synchronized int nextId() throws IOException {
        file.seek(0);
        int next = file.readInt() + 1;
        file.seek(0);
        file.writeInt(next);
        return next;
    }

    public synchronized long create(T record) throws Exception {
        byte[] data = record.toByteArray();
        FreeSlot reused = takeFreeSlot(data.length);
        if (reused != null) {
            writeRecord(reused.position, reused.length, data);
            return reused.position;
        }

        long position = file.length();
        writeRecord(position, data.length, data);
        return position;
    }

    public synchronized T read(long position) throws Exception {
        if (position < HEADER_SIZE || position >= file.length()) {
            return null;
        }
        file.seek(position);
        byte tombstone = file.readByte();
        int size = file.readInt();
        if (size < 0 || position + RECORD_HEADER_SIZE + size > file.length()) {
            return null;
        }
        byte[] data = new byte[size];
        file.readFully(data);
        if (tombstone != ACTIVE) {
            return null;
        }

        T record = constructor.newInstance();
        record.fromByteArray(data);
        return record;
    }

    public synchronized boolean delete(long position) throws Exception {
        if (position < HEADER_SIZE || position >= file.length()) {
            return false;
        }
        file.seek(position);
        byte tombstone = file.readByte();
        int size = file.readInt();
        if (tombstone != ACTIVE || size < 0) {
            return false;
        }

        if (size < FREE_SLOT_POINTER_SIZE) {
            file.seek(position);
            file.writeByte(DELETED);
            file.writeInt(size);
            return true;
        }

        file.seek(4);
        long currentHead = file.readLong();

        file.seek(position);
        file.writeByte(DELETED);
        file.writeInt(size);
        file.writeLong(currentHead);

        file.seek(4);
        file.writeLong(position);
        return true;
    }

    public synchronized long update(long position, T updated) throws Exception {
        if (position < HEADER_SIZE || position >= file.length()) {
            return -1L;
        }

        byte[] newData = updated.toByteArray();
        file.seek(position);
        byte tombstone = file.readByte();
        int currentSize = file.readInt();
        if (tombstone != ACTIVE || currentSize < 0) {
            return -1L;
        }

        if (newData.length <= currentSize) {
            writeRecord(position, currentSize, newData);
            return position;
        }

        delete(position);
        return create(updated);
    }

    public synchronized List<RecordEnvelope<T>> scanActive() throws Exception {
        List<RecordEnvelope<T>> records = new ArrayList<>();
        file.seek(HEADER_SIZE);
        while (file.getFilePointer() + RECORD_HEADER_SIZE <= file.length()) {
            long position = file.getFilePointer();
            byte tombstone = file.readByte();
            int size = file.readInt();
            if (size < 0 || position + RECORD_HEADER_SIZE + size > file.length()) {
                // Defensive resync: if one record is malformed, keep scanning so valid
                // records later in the file are still visible to the application.
                file.seek(position + 1L);
                continue;
            }

            try {
                byte[] data = new byte[size];
                file.readFully(data);
                if (tombstone == ACTIVE) {
                    T record = constructor.newInstance();
                    record.fromByteArray(data);
                    records.add(new RecordEnvelope<>(position, record));
                }
            } catch (Exception e) {
                // Defensive resync for partially corrupted payloads.
                file.seek(position + 1L);
            }
        }
        return records;
    }

    public synchronized void close() throws IOException {
        file.close();
    }

    private FreeSlot takeFreeSlot(int requiredLength) throws IOException {
        file.seek(4);
        long head = file.readLong();
        long previous = -1L;

        while (head != -1L) {
            file.seek(head);
            byte tombstone = file.readByte();
            int size = file.readInt();
            long next = file.readLong();
            if (tombstone == DELETED && size >= requiredLength) {
                if (previous == -1L) {
                    file.seek(4);
                    file.writeLong(next);
                } else {
                    file.seek(previous + 5L);
                    file.writeLong(next);
                }
                return new FreeSlot(head, size);
            }
            previous = head;
            head = next;
        }

        return null;
    }

    private void writeRecord(long position, int storedLength, byte[] data) throws IOException {
        file.seek(position);
        file.writeByte(ACTIVE);
        file.writeInt(storedLength);
        file.write(data);
        writePadding(storedLength - data.length);
    }

    private void writePadding(int remaining) throws IOException {
        while (remaining > 0) {
            int chunk = Math.min(remaining, ZERO_BUFFER.length);
            file.write(ZERO_BUFFER, 0, chunk);
            remaining -= chunk;
        }
    }

    private static class FreeSlot {
        private final long position;
        private final int length;

        private FreeSlot(long position, int length) {
            this.position = position;
            this.length = length;
        }
    }
}
