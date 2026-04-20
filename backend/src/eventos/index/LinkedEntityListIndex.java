package eventos.index;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.LinkedHashSet;
import java.util.Set;

public class LinkedEntityListIndex {

    private static final long NULL_POINTER = -1L;

    private final ExtensibleHashIndex headIndex;
    private final RandomAccessFile nodeFile;

    public LinkedEntityListIndex(String basePath, String name) throws IOException {
        File dir = new File(basePath);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IOException("Nao foi possivel criar o diretorio " + basePath);
        }

        this.headIndex = new ExtensibleHashIndex(basePath, name + "_head", 4);
        this.nodeFile = new RandomAccessFile(new File(dir, name + "_nodes.db"), "rw");
    }

    public synchronized void add(int key, int entityId) throws IOException {
        long currentHead = headIndex.get(key);
        long newNode = nodeFile.length();
        nodeFile.seek(newNode);
        nodeFile.writeBoolean(true);
        nodeFile.writeInt(entityId);
        nodeFile.writeLong(currentHead);
        headIndex.put(key, newNode);
    }

    public synchronized void remove(int key, int entityId) throws IOException {
        long pointer = headIndex.get(key);
        while (pointer != NULL_POINTER) {
            nodeFile.seek(pointer);
            boolean active = nodeFile.readBoolean();
            int storedId = nodeFile.readInt();
            long next = nodeFile.readLong();
            if (active && storedId == entityId) {
                nodeFile.seek(pointer);
                nodeFile.writeBoolean(false);
                return;
            }
            pointer = next;
        }
    }

    public synchronized Set<Integer> list(int key) throws IOException {
        Set<Integer> ids = new LinkedHashSet<>();
        long pointer = headIndex.get(key);
        while (pointer != NULL_POINTER) {
            nodeFile.seek(pointer);
            boolean active = nodeFile.readBoolean();
            int entityId = nodeFile.readInt();
            long next = nodeFile.readLong();
            if (active) {
                ids.add(entityId);
            }
            pointer = next;
        }
        return ids;
    }

    public synchronized boolean isEmpty() throws IOException {
        return headIndex.isEmpty();
    }

    public synchronized void close() throws IOException {
        headIndex.close();
        nodeFile.close();
    }
}
