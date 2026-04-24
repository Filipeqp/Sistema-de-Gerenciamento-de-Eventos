package eventos.index;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ExtensibleHashIndex {

    private static final long NULL_POINTER = -1L;

    private final RandomAccessFile directoryFile;
    private final RandomAccessFile bucketFile;
    private final int bucketCapacity;
    private final int bucketSize;

    public ExtensibleHashIndex(String directoryPath, String baseName, int bucketCapacity) throws IOException {
        this.bucketCapacity = bucketCapacity;
        this.bucketSize = 8 + (bucketCapacity * 12);

        File dir = new File(directoryPath);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IOException("Nao foi possivel criar o diretorio " + directoryPath);
        }

        this.directoryFile = new RandomAccessFile(new File(dir, baseName + ".dir"), "rw");
        this.bucketFile = new RandomAccessFile(new File(dir, baseName + ".bkt"), "rw");

        if (directoryFile.length() == 0 || bucketFile.length() == 0) {
            initialize();
        }
    }

    public synchronized long get(int key) throws IOException {
        long bucketOffset = directoryBucketOffset(key);
        Bucket bucket = readBucket(bucketOffset);
        for (Entry entry : bucket.entries) {
            if (entry.key == key) {
                return entry.value;
            }
        }
        return NULL_POINTER;
    }

    public synchronized void put(int key, long value) throws IOException {
        while (true) {
            long bucketOffset = directoryBucketOffset(key);
            Bucket bucket = readBucket(bucketOffset);

            for (Entry entry : bucket.entries) {
                if (entry.key == key) {
                    entry.value = value;
                    writeBucket(bucketOffset, bucket);
                    return;
                }
            }

            if (bucket.entries.size() < bucketCapacity) {
                bucket.entries.add(new Entry(key, value));
                writeBucket(bucketOffset, bucket);
                return;
            }

            splitBucket(bucketOffset, bucket);
        }
    }

    public synchronized boolean remove(int key) throws IOException {
        long bucketOffset = directoryBucketOffset(key);
        Bucket bucket = readBucket(bucketOffset);
        for (int i = 0; i < bucket.entries.size(); i++) {
            if (bucket.entries.get(i).key == key) {
                bucket.entries.remove(i);
                writeBucket(bucketOffset, bucket);
                return true;
            }
        }
        return false;
    }

    public synchronized boolean isEmpty() throws IOException {
        int globalDepth = readGlobalDepth();
        int size = 1 << globalDepth;
        for (int i = 0; i < size; i++) {
            Bucket bucket = readBucket(readDirectoryEntry(i));
            if (!bucket.entries.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public synchronized List<Integer> listKeys() throws IOException {
        int globalDepth = readGlobalDepth();
        int size = 1 << globalDepth;
        Set<Long> visitedBuckets = new HashSet<>();
        List<Integer> keys = new ArrayList<>();

        for (int i = 0; i < size; i++) {
            long bucketOffset = readDirectoryEntry(i);
            if (!visitedBuckets.add(bucketOffset)) {
                continue;
            }

            Bucket bucket = readBucket(bucketOffset);
            for (Entry entry : bucket.entries) {
                keys.add(entry.key);
            }
        }

        Collections.sort(keys);
        return keys;
    }

    public synchronized void close() throws IOException {
        directoryFile.close();
        bucketFile.close();
    }

    private void initialize() throws IOException {
        directoryFile.setLength(0);
        bucketFile.setLength(0);

        writeGlobalDepth(1);
        writeBucketCapacity(bucketCapacity);

        long first = appendBucket(new Bucket(1));
        long second = appendBucket(new Bucket(1));

        writeDirectoryEntry(0, first);
        writeDirectoryEntry(1, second);
    }

    private void splitBucket(long bucketOffset, Bucket bucket) throws IOException {
        int oldLocalDepth = bucket.localDepth;
        int globalDepth = readGlobalDepth();

        if (oldLocalDepth == globalDepth) {
            doubleDirectory(globalDepth);
            globalDepth++;
        }

        Bucket left = new Bucket(oldLocalDepth + 1);
        Bucket right = new Bucket(oldLocalDepth + 1);
        long newBucketOffset = appendBucket(right);

        int directorySize = 1 << globalDepth;
        for (int i = 0; i < directorySize; i++) {
            if (readDirectoryEntry(i) == bucketOffset) {
                if (((i >> oldLocalDepth) & 1) == 0) {
                    writeDirectoryEntry(i, bucketOffset);
                } else {
                    writeDirectoryEntry(i, newBucketOffset);
                }
            }
        }

        for (Entry entry : bucket.entries) {
            long destination = directoryBucketOffset(entry.key);
            if (destination == bucketOffset) {
                left.entries.add(entry);
            } else {
                right.entries.add(entry);
            }
        }

        writeBucket(bucketOffset, left);
        writeBucket(newBucketOffset, right);
    }

    private void doubleDirectory(int currentGlobalDepth) throws IOException {
        int oldSize = 1 << currentGlobalDepth;
        List<Long> existing = new ArrayList<>(oldSize);
        for (int i = 0; i < oldSize; i++) {
            existing.add(readDirectoryEntry(i));
        }

        writeGlobalDepth(currentGlobalDepth + 1);
        for (int i = 0; i < oldSize; i++) {
            writeDirectoryEntry(i, existing.get(i));
            writeDirectoryEntry(i + oldSize, existing.get(i));
        }
    }

    private long directoryBucketOffset(int key) throws IOException {
        int globalDepth = readGlobalDepth();
        int mask = (1 << globalDepth) - 1;
        int index = hash(key) & mask;
        return readDirectoryEntry(index);
    }

    private int hash(int value) {
        int hash = value;
        hash ^= (hash >>> 16);
        return hash & Integer.MAX_VALUE;
    }

    private Bucket readBucket(long offset) throws IOException {
        bucketFile.seek(offset);
        int localDepth = bucketFile.readInt();
        int count = bucketFile.readInt();
        Bucket bucket = new Bucket(localDepth);
        for (int i = 0; i < bucketCapacity; i++) {
            int key = bucketFile.readInt();
            long value = bucketFile.readLong();
            if (i < count) {
                bucket.entries.add(new Entry(key, value));
            }
        }
        return bucket;
    }

    private void writeBucket(long offset, Bucket bucket) throws IOException {
        bucketFile.seek(offset);
        bucketFile.writeInt(bucket.localDepth);
        bucketFile.writeInt(bucket.entries.size());
        for (int i = 0; i < bucketCapacity; i++) {
            if (i < bucket.entries.size()) {
                Entry entry = bucket.entries.get(i);
                bucketFile.writeInt(entry.key);
                bucketFile.writeLong(entry.value);
            } else {
                bucketFile.writeInt(0);
                bucketFile.writeLong(NULL_POINTER);
            }
        }
    }

    private long appendBucket(Bucket bucket) throws IOException {
        long offset = bucketFile.length();
        writeBucket(offset, bucket);
        return offset;
    }

    private int readGlobalDepth() throws IOException {
        directoryFile.seek(0);
        return directoryFile.readInt();
    }

    private void writeGlobalDepth(int depth) throws IOException {
        directoryFile.seek(0);
        directoryFile.writeInt(depth);
    }

    private void writeBucketCapacity(int capacity) throws IOException {
        directoryFile.seek(4);
        directoryFile.writeInt(capacity);
    }

    private long readDirectoryEntry(int index) throws IOException {
        directoryFile.seek(8L + (index * 8L));
        return directoryFile.readLong();
    }

    private void writeDirectoryEntry(int index, long offset) throws IOException {
        directoryFile.seek(8L + (index * 8L));
        directoryFile.writeLong(offset);
    }

    private static class Bucket {
        private final int localDepth;
        private final List<Entry> entries = new ArrayList<>();

        private Bucket(int localDepth) {
            this.localDepth = localDepth;
        }
    }

    private static class Entry {
        private final int key;
        private long value;

        private Entry(int key, long value) {
            this.key = key;
            this.value = value;
        }
    }
}
