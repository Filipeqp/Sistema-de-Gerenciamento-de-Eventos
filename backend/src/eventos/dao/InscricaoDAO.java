package eventos.dao;

import eventos.index.LinkedEntityListIndex;
import eventos.model.Inscricao;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class InscricaoDAO extends AbstractDAO<Inscricao> {

    /*
     * A inscricao e a tabela intermediaria do relacionamento N:N:
     *   Evento <-> Inscricao <-> Participante
     *
     * Mantemos dois indices porque a demonstracao precisa consultar o relacionamento
     * nos dois sentidos: inscricoes de um evento e inscricoes de um participante.
     */
    private final LinkedEntityListIndex byEventoIndex;
    private final LinkedEntityListIndex byParticipanteIndex;

    public InscricaoDAO(String basePath) throws Exception {
        super(basePath, "inscricoes", Inscricao.class);
        this.byEventoIndex = new LinkedEntityListIndex(
                basePath + "/indices/inscricoes",
                "inscricoes_por_evento");
        this.byParticipanteIndex = new LinkedEntityListIndex(
                basePath + "/indices/inscricoes",
                "inscricoes_por_participante");
        rebuildSortIndex();
        rebuildRelationshipIndexesIfNeeded();
    }

    public Inscricao create(Inscricao inscricao) throws Exception {
        return createInternal(inscricao);
    }

    public Inscricao update(int id, Inscricao inscricao) throws Exception {
        return updateInternal(id, inscricao);
    }

    public boolean delete(int id) throws Exception {
        return deleteInternal(id);
    }

    public List<Inscricao> listByEvento(int idEvento) throws Exception {
        // Usa o indice relacional: chave = idEvento, valores = IDs das inscricoes daquele evento.
        return collect(byEventoIndex.list(idEvento), idEvento, true);
    }

    public List<Inscricao> listByParticipante(int idParticipante) throws Exception {
        // Usa o indice relacional no sentido inverso: chave = idParticipante.
        return collect(byParticipanteIndex.list(idParticipante), idParticipante, false);
    }

    public List<Inscricao> listByEventoOrdered(int idEvento, boolean descending) throws Exception {
        return filterOrdered(idEvento, true, descending);
    }

    public List<Inscricao> listByParticipanteOrdered(int idParticipante, boolean descending) throws Exception {
        return filterOrdered(idParticipante, false, descending);
    }

    public boolean existsByEventoAndParticipante(int idEvento, int idParticipante, Integer ignoredId) throws Exception {
        for (Inscricao i : listByEvento(idEvento)) {
            if (i.getIdParticipante() == idParticipante
                    && (ignoredId == null || i.getId() != ignoredId)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasEvento(int idEvento) throws Exception {
        return !listByEvento(idEvento).isEmpty();
    }

    public boolean hasParticipante(int idParticipante) throws Exception {
        return !listByParticipante(idParticipante).isEmpty();
    }

    @Override
    protected String sortKey(Inscricao i) {
        // Ordena por data de inscrição
        return normalizeDateForSorting(i.getDataInscricao());
    }

    @Override
    protected void afterCreate(Inscricao record) throws Exception {
        // Ao criar a inscricao, atualizamos imediatamente os dois indices do N:N.
        byEventoIndex.add(record.getIdEvento(), record.getId());
        byParticipanteIndex.add(record.getIdParticipante(), record.getId());
    }

    @Override
    protected void afterUpdate(Inscricao previous, Inscricao updated) throws Exception {
        if (previous.getIdEvento() != updated.getIdEvento()) {
            // Se a inscricao mudou de evento, remove do indice antigo e inclui no novo.
            byEventoIndex.remove(previous.getIdEvento(), previous.getId());
            byEventoIndex.add(updated.getIdEvento(), updated.getId());
        }
        if (previous.getIdParticipante() != updated.getIdParticipante()) {
            // Mesma logica para mudanca de participante.
            byParticipanteIndex.remove(previous.getIdParticipante(), previous.getId());
            byParticipanteIndex.add(updated.getIdParticipante(), updated.getId());
        }
    }

    @Override
    protected void afterDelete(Inscricao previous) throws Exception {
        // Na exclusao, a inscricao deixa de aparecer nos dois sentidos do relacionamento.
        byEventoIndex.remove(previous.getIdEvento(), previous.getId());
        byParticipanteIndex.remove(previous.getIdParticipante(), previous.getId());
    }

    @Override
    public void close() throws Exception {
        super.close();
        byEventoIndex.close();
        byParticipanteIndex.close();
    }

    private List<Inscricao> collect(Set<Integer> ids, int filterId, boolean filterEvento) throws Exception {
        List<Inscricao> items = new ArrayList<>();
        for (Integer id : ids) {
            Inscricao i = findById(id);
            if (i == null) continue;
            if (filterEvento  && i.getIdEvento()       == filterId) items.add(i);
            if (!filterEvento && i.getIdParticipante() == filterId) items.add(i);
        }
        return items;
    }

    private List<Inscricao> filterOrdered(int filterId, boolean filterEvento, boolean descending) throws Exception {
        // Aqui combinamos B+ e filtro do relacionamento: primeiro vem a ordem da B+, depois filtramos o lado desejado.
        List<Inscricao> source = descending ? listAllOrderedDesc() : listAllOrdered();
        List<Inscricao> items = new ArrayList<>();
        for (Inscricao i : source) {
            if (filterEvento && i.getIdEvento() == filterId) {
                items.add(i);
            }
            if (!filterEvento && i.getIdParticipante() == filterId) {
                items.add(i);
            }
        }
        return items;
    }

    private void rebuildRelationshipIndexesIfNeeded() throws Exception {
        if (!byEventoIndex.isEmpty() || !byParticipanteIndex.isEmpty()) return;
        // Se os arquivos de indice foram removidos/restaurados, eles sao reconstruidos a partir dos dados ativos.
        for (Inscricao i : listAll()) {
            byEventoIndex.add(i.getIdEvento(), i.getId());
            byParticipanteIndex.add(i.getIdParticipante(), i.getId());
        }
    }

    private String normalizeDateForSorting(String rawDate) {
        if (rawDate == null) return "";

        String value = rawDate.trim();
        if (value.matches("\\d{2}/\\d{2}/\\d{4}")) {
            String[] parts = value.split("/");
            return parts[2] + parts[1] + parts[0];
        }
        if (value.matches("\\d{4}-\\d{2}-\\d{2}")) {
            return value.replace("-", "");
        }
        return value.toLowerCase(Locale.ROOT);
    }
}
