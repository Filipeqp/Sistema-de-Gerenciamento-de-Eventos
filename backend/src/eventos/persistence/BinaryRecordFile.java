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
        long reused = takeFreeSlot(data.length);
        long position = reused >= 0 ? reused : file.length();
        file.seek(position);
        file.writeByte(ACTIVE);
        file.writeInt(data.length);
        file.write(data);
        return position;
    }

    public synchronized T read(long position) throws Exception {
        if (position < HEADER_SIZE || position >= file.length()) {
            return null;
        }
        file.seek(position);
        byte tombstone = file.readByte();
        int size = file.readInt();
        if (size < 0 || position + 5L + size > file.length()) {
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
            file.seek(position);
            file.writeByte(ACTIVE);
            file.writeInt(currentSize);
            file.write(newData);
            if (newData.length < currentSize) {
                file.write(new byte[currentSize - newData.length]);
            }
            return position;
        }

        delete(position);
        return create(updated);
    }

    public synchronized List<RecordEnvelope<T>> scanActive() throws Exception {
        List<RecordEnvelope<T>> records = new ArrayList<>();
        file.seek(HEADER_SIZE);
        while (file.getFilePointer() < file.length()) {
            long position = file.getFilePointer();
            byte tombstone = file.readByte();
            int size = file.readInt();
            if (size < 0 || position + 5L + size > file.length()) {
                // Defensive resync: if one record is malformed, keep scanning so valid
                // records later in the file are still visible to the application.
                file.seek(position + 1L);
                continue;
            }
            byte[] data = new byte[size];
            file.readFully(data);
            if (tombstone == ACTIVE) {
                T record = constructor.newInstance();
                record.fromByteArray(data);
                records.add(new RecordEnvelope<>(position, record));
            }
        }
        return records;
    }

    public synchronized void close() throws IOException {
        file.close();
    }

    private long takeFreeSlot(int requiredLength) throws IOException {
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
                return head;
            }
            previous = head;
            head = next;
        }

        return -1L;
    }
}
