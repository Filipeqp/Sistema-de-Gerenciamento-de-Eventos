package eventos.index;

import java.io.*;
import java.util.*;

/**
 * Árvore B+ persistida em arquivo binário.
 *
 * Serve para listagem ORDENADA por string (ex: nome do evento, nome do palestrante).
 * Cada entrada armazena: chave string (até 100 chars) + id inteiro do registro.
 *
 * Estrutura do arquivo (.btree):
 *   Cabeçalho (16 bytes):
 *     [long  raiz     ] offset da página raiz (-1 se vazia)
 *     [int   ordem    ] ordem da árvore (máx filhos por nó interno)
 *     [int   qtdNos   ] total de páginas alocadas
 *
 *   Cada página (tamanho fixo PAGE_SIZE bytes):
 *     [byte  tipo     ]  0 = interno, 1 = folha
 *     [int   qtdChaves]  número de chaves presentes
 *     [long  proximo  ]  próxima folha (só válido em folhas)
 *     Para folhas:  [String chave (102 bytes) + int id] × ORDER
 *     Para internos:[long filho0][String c1][long filho1][String c2]...
 *
 * A chave string é armazenada como: [short len][bytes UTF, padded até MAX_KEY_BYTES].
 */
public class BPlusTreeIndex {

    // Ordem da árvore: cada nó tem no máximo ORDER-1 chaves e ORDER filhos
    private static final int ORDER = 4;
    private static final int MAX_KEY_BYTES = 100;
    // Tamanho de uma entrada de chave no arquivo: 2 (short len) + MAX_KEY_BYTES + 4 (int id)
    private static final int KEY_ENTRY_SIZE = 2 + MAX_KEY_BYTES + 4;
    // Tamanho de ponteiro para filho (long)
    private static final int PTR_SIZE = 8;

    // Tamanho de página:
    // 1 (tipo) + 4 (qtdChaves) + 8 (proximo/unused)
    // + ORDER * KEY_ENTRY_SIZE (chaves)
    // + (ORDER + 1) * PTR_SIZE (filhos, só interno usa)
    private static final int PAGE_SIZE =
            1 + 4 + 8
            + ORDER * KEY_ENTRY_SIZE
            + (ORDER + 1) * PTR_SIZE;

    private static final long NULL_PTR = -1L;
    private static final int HEADER_SIZE = 16;

    private final RandomAccessFile file;

    public BPlusTreeIndex(String directoryPath, String baseName) throws IOException {
        File dir = new File(directoryPath);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IOException("Nao foi possivel criar o diretorio: " + directoryPath);
        }
        this.file = new RandomAccessFile(new File(dir, baseName + ".btree"), "rw");
        if (file.length() == 0) {
            writeHeader(NULL_PTR, ORDER, 0);
        }
    }

    // ============================================================
    // API pública
    // ============================================================

    /**
     * Insere (chave, id) na árvore.
     * Permite duplicatas de chave (nomes iguais → IDs diferentes).
     */
    public synchronized void insert(String key, int id) throws IOException {
        String normalKey = normalize(key);
        long root = readRoot();
        if (root == NULL_PTR) {
            // Árvore vazia: cria folha raiz
            long newRoot = allocatePage();
            Page leaf = new Page(true);
            leaf.keys.add(normalKey);
            leaf.ids.add(id);
            leaf.next = NULL_PTR;
            writePage(newRoot, leaf);
            writeRoot(newRoot);
            return;
        }
        SplitResult split = insertRec(root, normalKey, id);
        if (split != null) {
            // Raiz foi dividida: cria nova raiz
            long newRoot = allocatePage();
            Page newRootPage = new Page(false);
            newRootPage.keys.add(split.promotedKey);
            newRootPage.children.add(split.leftChild);
            newRootPage.children.add(split.rightChild);
            writePage(newRoot, newRootPage);
            writeRoot(newRoot);
        }
    }

    /**
     * Remove a entrada (chave, id) da árvore.
     * Retorna true se removeu.
     */
    public synchronized boolean remove(String key, int id) throws IOException {
        String normalKey = normalize(key);
        long root = readRoot();
        if (root == NULL_PTR) return false;
        return removeRec(root, normalKey, id, NULL_PTR, -1);
    }

    /**
     * Retorna todos os registros em ordem crescente de chave.
     * Cada elemento do resultado é um int[] = {id}.
     * Para obter a lista de IDs em ordem: chamar listAllOrdered().
     */
    public synchronized List<Integer> listAllOrdered() throws IOException {
        List<Integer> result = new ArrayList<>();
        long root = readRoot();
        if (root == NULL_PTR) return result;
        // Desce até a folha mais à esquerda
        long leafPtr = leftmostLeaf(root);
        while (leafPtr != NULL_PTR) {
            Page leaf = readPage(leafPtr);
            for (int i = 0; i < leaf.keys.size(); i++) {
                result.add(leaf.ids.get(i));
            }
            leafPtr = leaf.next;
        }
        return result;
    }

    /**
     * Retorna IDs em ordem decrescente de chave.
     */
    public synchronized List<Integer> listAllOrderedDesc() throws IOException {
        List<Integer> asc = listAllOrdered();
        Collections.reverse(asc);
        return asc;
    }

    /**
     * Busca todos os IDs cuja chave começa com o prefixo dado.
     */
    public synchronized List<Integer> search(String prefix) throws IOException {
        List<Integer> result = new ArrayList<>();
        String normalPrefix = normalize(prefix);
        long root = readRoot();
        if (root == NULL_PTR) return result;
        long leafPtr = findLeafFor(root, normalPrefix);
        while (leafPtr != NULL_PTR) {
            Page leaf = readPage(leafPtr);
            boolean found = false;
            for (int i = 0; i < leaf.keys.size(); i++) {
                if (leaf.keys.get(i).startsWith(normalPrefix)) {
                    result.add(leaf.ids.get(i));
                    found = true;
                }
            }
            if (!found && !result.isEmpty()) break; // passou do prefixo
            leafPtr = leaf.next;
        }
        return result;
    }

    public synchronized boolean isEmpty() throws IOException {
        return readRoot() == NULL_PTR;
    }

    public synchronized void close() throws IOException {
        file.close();
    }

    // ============================================================
    // Inserção recursiva
    // ============================================================

    private SplitResult insertRec(long pagePtr, String key, int id) throws IOException {
        Page page = readPage(pagePtr);
        if (page.isLeaf) {
            // Insere mantendo ordem
            int pos = findInsertPos(page.keys, key);
            page.keys.add(pos, key);
            page.ids.add(pos, id);
            if (page.keys.size() <= ORDER - 1) {
                writePage(pagePtr, page);
                return null; // sem split
            }
            return splitLeaf(pagePtr, page);
        } else {
            // Nó interno: desce para o filho correto
            int idx = findChildIndex(page.keys, key);
            long childPtr = page.children.get(idx);
            SplitResult split = insertRec(childPtr, key, id);
            if (split == null) return null;
            // Insere chave promovida neste nó
            page.keys.add(idx, split.promotedKey);
            page.children.set(idx, split.leftChild);
            page.children.add(idx + 1, split.rightChild);
            if (page.keys.size() <= ORDER - 1) {
                writePage(pagePtr, page);
                return null;
            }
            return splitInternal(pagePtr, page);
        }
    }

    private SplitResult splitLeaf(long pagePtr, Page leaf) throws IOException {
        int mid = leaf.keys.size() / 2;
        Page right = new Page(true);
        right.keys.addAll(leaf.keys.subList(mid, leaf.keys.size()));
        right.ids.addAll(leaf.ids.subList(mid, leaf.ids.size()));
        right.next = leaf.next;

        leaf.keys.subList(mid, leaf.keys.size()).clear();
        leaf.ids.subList(mid, leaf.ids.size()).clear();

        long rightPtr = allocatePage();
        leaf.next = rightPtr;

        writePage(pagePtr, leaf);
        writePage(rightPtr, right);

        SplitResult result = new SplitResult();
        result.promotedKey = right.keys.get(0); // cópia (folha mantém)
        result.leftChild = pagePtr;
        result.rightChild = rightPtr;
        return result;
    }

    private SplitResult splitInternal(long pagePtr, Page node) throws IOException {
        int mid = node.keys.size() / 2;
        String promoted = node.keys.get(mid);

        Page right = new Page(false);
        right.keys.addAll(node.keys.subList(mid + 1, node.keys.size()));
        right.children.addAll(node.children.subList(mid + 1, node.children.size()));

        node.keys.subList(mid, node.keys.size()).clear();
        node.children.subList(mid + 1, node.children.size()).clear();

        long rightPtr = allocatePage();
        writePage(pagePtr, node);
        writePage(rightPtr, right);

        SplitResult result = new SplitResult();
        result.promotedKey = promoted;
        result.leftChild = pagePtr;
        result.rightChild = rightPtr;
        return result;
    }

    // ============================================================
    // Remoção recursiva (simples: apenas marca, sem merge)
    // ============================================================

    private boolean removeRec(long pagePtr, String key, int id,
                               long parentPtr, int childIdx) throws IOException {
        Page page = readPage(pagePtr);
        if (page.isLeaf) {
            for (int i = 0; i < page.keys.size(); i++) {
                if (page.keys.get(i).equals(key) && page.ids.get(i) == id) {
                    page.keys.remove(i);
                    page.ids.remove(i);
                    writePage(pagePtr, page);
                    return true;
                }
            }
            return false;
        }
        int idx = findChildIndex(page.keys, key);
        return removeRec(page.children.get(idx), key, id, pagePtr, idx);
    }

    // ============================================================
    // Navegação
    // ============================================================

    private long leftmostLeaf(long ptr) throws IOException {
        Page page = readPage(ptr);
        if (page.isLeaf) return ptr;
        return leftmostLeaf(page.children.get(0));
    }

    private long findLeafFor(long ptr, String key) throws IOException {
        Page page = readPage(ptr);
        if (page.isLeaf) return ptr;
        int idx = findChildIndex(page.keys, key);
        return findLeafFor(page.children.get(idx), key);
    }

    private int findInsertPos(List<String> keys, String key) {
        for (int i = 0; i < keys.size(); i++) {
            if (key.compareTo(keys.get(i)) <= 0) return i;
        }
        return keys.size();
    }

    private int findChildIndex(List<String> keys, String key) {
        for (int i = 0; i < keys.size(); i++) {
            if (key.compareTo(keys.get(i)) < 0) return i;
        }
        return keys.size();
    }

    // ============================================================
    // Leitura / escrita de páginas
    // ============================================================

    private Page readPage(long offset) throws IOException {
        file.seek(offset);
        byte[] buf = new byte[PAGE_SIZE];
        file.readFully(buf);
        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(buf));

        boolean isLeaf = dis.readByte() == 1;
        int count = dis.readInt();
        long next = dis.readLong();

        Page page = new Page(isLeaf);
        page.next = next;

        if (isLeaf) {
            for (int i = 0; i < ORDER; i++) {
                short len = dis.readShort();
                byte[] keyBytes = new byte[MAX_KEY_BYTES];
                dis.readFully(keyBytes);
                int id = dis.readInt();
                if (i < count) {
                    String key = new String(keyBytes, 0, Math.min(len, MAX_KEY_BYTES), "UTF-8").trim();
                    page.keys.add(key);
                    page.ids.add(id);
                }
            }
            // Pula espaço dos filhos (não usado em folha)
            for (int i = 0; i <= ORDER; i++) dis.readLong();
        } else {
            // Lê primeiro filho antes das chaves
            for (int i = 0; i < ORDER; i++) {
                // Espaço de chaves (lido depois)
                dis.readShort(); dis.readFully(new byte[MAX_KEY_BYTES]); dis.readInt();
            }
            // Nó interno: chaves + filhos entrelacados no buffer separado
            // Relê a página com estrutura correta
            DataInputStream dis2 = new DataInputStream(new ByteArrayInputStream(buf));
            dis2.readByte(); dis2.readInt(); dis2.readLong(); // header
            // Lê chaves
            List<String> keys = new ArrayList<>();
            for (int i = 0; i < ORDER; i++) {
                short len = dis2.readShort();
                byte[] keyBytes = new byte[MAX_KEY_BYTES];
                dis2.readFully(keyBytes);
                dis2.readInt(); // id (não usado em interno)
                if (i < count) {
                    String key = new String(keyBytes, 0, Math.min(len, MAX_KEY_BYTES), "UTF-8").trim();
                    keys.add(key);
                }
            }
            // Lê filhos
            List<Long> children = new ArrayList<>();
            for (int i = 0; i <= ORDER; i++) {
                long ptr = dis2.readLong();
                if (i <= count) children.add(ptr);
            }
            page.keys = keys;
            page.children = children;
        }
        return page;
    }

    private void writePage(long offset, Page page) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(PAGE_SIZE);
        DataOutputStream dos = new DataOutputStream(baos);

        dos.writeByte(page.isLeaf ? 1 : 0);
        dos.writeInt(page.keys.size());
        dos.writeLong(page.next);

        // Chaves (ORDER slots)
        for (int i = 0; i < ORDER; i++) {
            if (i < page.keys.size()) {
                byte[] keyBytes = page.keys.get(i).getBytes("UTF-8");
                int len = Math.min(keyBytes.length, MAX_KEY_BYTES);
                dos.writeShort((short) len);
                dos.write(keyBytes, 0, len);
                // Padding
                for (int p = len; p < MAX_KEY_BYTES; p++) dos.writeByte(0);
                // id (só válido em folha)
                dos.writeInt(page.isLeaf && i < page.ids.size() ? page.ids.get(i) : 0);
            } else {
                dos.writeShort(0);
                dos.write(new byte[MAX_KEY_BYTES]);
                dos.writeInt(0);
            }
        }

        // Filhos (ORDER+1 slots, só usados em nós internos)
        for (int i = 0; i <= ORDER; i++) {
            if (!page.isLeaf && i < page.children.size()) {
                dos.writeLong(page.children.get(i));
            } else {
                dos.writeLong(NULL_PTR);
            }
        }

        byte[] data = baos.toByteArray();
        // Garante tamanho fixo
        byte[] padded = new byte[PAGE_SIZE];
        System.arraycopy(data, 0, padded, 0, Math.min(data.length, PAGE_SIZE));

        file.seek(offset);
        file.write(padded);
    }

    private long allocatePage() throws IOException {
        long pos = file.length();
        // Garante que a página existe no arquivo
        file.seek(pos);
        file.write(new byte[PAGE_SIZE]);
        int count = readPageCount();
        writePageCount(count + 1);
        return pos;
    }

    // ============================================================
    // Cabeçalho
    // ============================================================

    private void writeHeader(long root, int order, int pageCount) throws IOException {
        file.seek(0);
        file.writeLong(root);
        file.writeInt(order);
        file.writeInt(pageCount);
    }

    private long readRoot() throws IOException {
        file.seek(0);
        return file.readLong();
    }

    private void writeRoot(long root) throws IOException {
        file.seek(0);
        file.writeLong(root);
    }

    private int readPageCount() throws IOException {
        file.seek(12);
        return file.readInt();
    }

    private void writePageCount(int count) throws IOException {
        file.seek(12);
        file.writeInt(count);
    }

    // ============================================================
    // Helpers
    // ============================================================

    private String normalize(String key) {
        if (key == null) return "";
        // Lowercase para ordenação case-insensitive, trunca em MAX_KEY_BYTES
        String lower = key.toLowerCase(java.util.Locale.ROOT);
        try {
            byte[] bytes = lower.getBytes("UTF-8");
            if (bytes.length <= MAX_KEY_BYTES) return lower;
            return new String(bytes, 0, MAX_KEY_BYTES, "UTF-8");
        } catch (Exception e) {
            return lower.length() > 50 ? lower.substring(0, 50) : lower;
        }
    }

    // ============================================================
    // Classes internas
    // ============================================================

    private static class Page {
        boolean isLeaf;
        List<String> keys = new ArrayList<>();
        List<Integer> ids = new ArrayList<>();      // válido em folhas
        List<Long> children = new ArrayList<>();    // válido em internos
        long next = NULL_PTR;                       // próxima folha

        Page(boolean isLeaf) { this.isLeaf = isLeaf; }
    }

    private static class SplitResult {
        String promotedKey;
        long leftChild;
        long rightChild;
    }
}