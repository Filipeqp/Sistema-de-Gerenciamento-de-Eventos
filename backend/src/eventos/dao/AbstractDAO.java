package eventos.dao;

import eventos.index.BPlusTreeIndex;
import eventos.index.ExtensibleHashIndex;
import eventos.model.Record;
import eventos.persistence.BinaryRecordFile;
import eventos.persistence.RecordEnvelope;
import eventos.util.ExternalSorter;

import java.util.ArrayList;
import java.util.List;

/**
 * AbstractDAO com Hash Extensível (busca por ID) e Árvore B+ (listagem ordenada).
 *
 * Subclasses devem implementar:
 *   - sortKey(T record): retorna a string usada como chave na B+
 *     (ex: Evento → getNome(), Participante → getNome())
 *
 * Hooks opcionais (já usados por InscricaoDAO e PalestranteDAO):
 *   - afterCreate / afterUpdate / afterDelete
 */
public abstract class AbstractDAO<T extends Record> {

    protected final BinaryRecordFile<T> dataFile;
    protected final ExtensibleHashIndex primaryIndex;  // Hash: id → posição no arquivo
    protected final BPlusTreeIndex sortIndex;          // B+: chave string → id (para listagem ordenada)

    protected AbstractDAO(String basePath, String entityName, Class<T> recordClass) throws Exception {
        this.dataFile = new BinaryRecordFile<>(
                basePath + "/dados/" + entityName,
                entityName,
                recordClass.getConstructor());

        this.primaryIndex = new ExtensibleHashIndex(
                basePath + "/indices/" + entityName,
                entityName + "_primary",
                4);

        this.sortIndex = new BPlusTreeIndex(
                basePath + "/indices/" + entityName,
                entityName + "_sort");

        rebuildIndexesIfNeeded();
    }

    // ============================================================
    // Busca por ID (usa Hash Extensível)
    // ============================================================

    public T findById(int id) throws Exception {
        long position = primaryIndex.get(id);
        if (position < 0) return null;
        return dataFile.read(position);
    }

    // ============================================================
    // Listagem completa (scan direto no arquivo)
    // ============================================================

    public List<T> listAll() throws Exception {
        List<T> items = new ArrayList<>();
        for (RecordEnvelope<T> envelope : dataFile.scanActive()) {
            items.add(envelope.getRecord());
        }
        return items;
    }

    // ============================================================
    // Listagem ORDENADA via Árvore B+ (travessia das folhas)
    // ============================================================

    /**
     * Retorna todos os registros em ordem crescente pela chave da B+.
     * A travessia percorre as folhas da árvore da esquerda para a direita.
     */
    public List<T> listAllOrdered() throws Exception {
        List<Integer> ids = sortIndex.listAllOrdered();
        List<T> result = new ArrayList<>();
        for (int id : ids) {
            T record = findById(id);
            if (record != null) result.add(record);
        }
        return result;
    }

    /**
     * Retorna todos os registros em ordem decrescente pela chave da B+.
     */
    public List<T> listAllOrderedDesc() throws Exception {
        List<Integer> ids = sortIndex.listAllOrderedDesc();
        List<T> result = new ArrayList<>();
        for (int id : ids) {
            T record = findById(id);
            if (record != null) result.add(record);
        }
        return result;
    }

    // ============================================================
    // Ordenação externa por intercalação (ExternalSorter)
    // ============================================================

    /**
     * Ordena todos os registros usando ordenação externa por intercalação.
     * Demonstra o algoritmo de merge sort externo exigido na Fase 2.
     */
    public List<T> listAllExternalSorted() throws Exception {
        List<T> all = listAll();
        return ExternalSorter.sort(all, this::sortKey);
    }

    public List<T> listAllExternalSortedDesc() throws Exception {
        List<T> all = listAll();
        return ExternalSorter.sortDesc(all, this::sortKey);
    }

    // ============================================================
    // CRUD interno
    // ============================================================

    protected T createInternal(T record) throws Exception {
        record.setId(dataFile.nextId());
        long position = dataFile.create(record);
        primaryIndex.put(record.getId(), position);
        sortIndex.insert(sortKey(record), record.getId());
        afterCreate(record);
        return record;
    }

    protected T updateInternal(int id, T updated) throws Exception {
        long currentPosition = primaryIndex.get(id);
        if (currentPosition < 0) return null;

        T previous = dataFile.read(currentPosition);
        if (previous == null) {
            primaryIndex.remove(id);
            return null;
        }

        updated.setId(id);
        long newPosition = dataFile.update(currentPosition, updated);
        if (newPosition < 0) return null;

        primaryIndex.put(id, newPosition);

        // Atualiza B+: remove chave antiga, insere nova
        sortIndex.remove(sortKey(previous), id);
        sortIndex.insert(sortKey(updated), id);

        afterUpdate(previous, updated);
        return updated;
    }

    protected boolean deleteInternal(int id) throws Exception {
        long position = primaryIndex.get(id);
        if (position < 0) return false;

        T previous = dataFile.read(position);
        if (previous == null) {
            primaryIndex.remove(id);
            return false;
        }

        boolean removed = dataFile.delete(position);
        if (removed) {
            primaryIndex.remove(id);
            sortIndex.remove(sortKey(previous), id);
            afterDelete(previous);
        }
        return removed;
    }

    // ============================================================
    // Hooks para subclasses
    // ============================================================

    protected void afterCreate(T record) throws Exception {}
    protected void afterUpdate(T previous, T updated) throws Exception {}
    protected void afterDelete(T previous) throws Exception {}

    /**
     * Chave usada na Árvore B+ para ordenação.
     * Subclasses devem sobrescrever para retornar o campo desejado.
     * Default: usa o ID como string (fallback).
     */
    protected String sortKey(T record) {
        return String.valueOf(record.getId());
    }

    // ============================================================
    // Reconstrução de índices
    // ============================================================

    private void rebuildIndexesIfNeeded() throws Exception {
        boolean hashEmpty = primaryIndex.isEmpty();
        boolean treeEmpty = sortIndex.isEmpty();

        if (!hashEmpty && !treeEmpty) return;

        for (RecordEnvelope<T> envelope : dataFile.scanActive()) {
            T record = envelope.getRecord();
            if (hashEmpty) {
                primaryIndex.put(record.getId(), envelope.getPosition());
            }
            if (treeEmpty) {
                sortIndex.insert(sortKey(record), record.getId());
            }
        }
    }
}