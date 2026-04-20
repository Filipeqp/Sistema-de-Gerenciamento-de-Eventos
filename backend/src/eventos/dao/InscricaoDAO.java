package eventos.dao;

import eventos.index.LinkedEntityListIndex;
import eventos.model.Inscricao;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class InscricaoDAO extends AbstractDAO<Inscricao> {

    private final LinkedEntityListIndex byEventoIndex;
    private final LinkedEntityListIndex byParticipanteIndex;

    public InscricaoDAO(String basePath) throws Exception {
        super(basePath, "inscricoes", Inscricao.class);
        this.byEventoIndex = new LinkedEntityListIndex(basePath + "/indices/inscricoes", "inscricoes_por_evento");
        this.byParticipanteIndex = new LinkedEntityListIndex(basePath + "/indices/inscricoes", "inscricoes_por_participante");
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
        return collect(byEventoIndex.list(idEvento), idEvento, true);
    }

    public List<Inscricao> listByParticipante(int idParticipante) throws Exception {
        return collect(byParticipanteIndex.list(idParticipante), idParticipante, false);
    }

    public boolean existsByEventoAndParticipante(int idEvento, int idParticipante, Integer ignoredId) throws Exception {
        for (Inscricao inscricao : listByEvento(idEvento)) {
            if (inscricao.getIdParticipante() == idParticipante && (ignoredId == null || inscricao.getId() != ignoredId)) {
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
    protected void afterCreate(Inscricao record) throws Exception {
        byEventoIndex.add(record.getIdEvento(), record.getId());
        byParticipanteIndex.add(record.getIdParticipante(), record.getId());
    }

    @Override
    protected void afterUpdate(Inscricao previous, Inscricao updated) throws Exception {
        if (previous.getIdEvento() != updated.getIdEvento()) {
            byEventoIndex.remove(previous.getIdEvento(), previous.getId());
            byEventoIndex.add(updated.getIdEvento(), updated.getId());
        }
        if (previous.getIdParticipante() != updated.getIdParticipante()) {
            byParticipanteIndex.remove(previous.getIdParticipante(), previous.getId());
            byParticipanteIndex.add(updated.getIdParticipante(), updated.getId());
        }
    }

    @Override
    protected void afterDelete(Inscricao previous) throws Exception {
        byEventoIndex.remove(previous.getIdEvento(), previous.getId());
        byParticipanteIndex.remove(previous.getIdParticipante(), previous.getId());
    }

    private List<Inscricao> collect(Set<Integer> ids, int filterId, boolean filterEvento) throws Exception {
        List<Inscricao> items = new ArrayList<>();
        for (Integer id : ids) {
            Inscricao inscricao = findById(id);
            if (inscricao == null) {
                continue;
            }
            if (filterEvento && inscricao.getIdEvento() == filterId) {
                items.add(inscricao);
            }
            if (!filterEvento && inscricao.getIdParticipante() == filterId) {
                items.add(inscricao);
            }
        }
        return items;
    }

    private void rebuildRelationshipIndexesIfNeeded() throws Exception {
        if (!byEventoIndex.isEmpty() || !byParticipanteIndex.isEmpty()) {
            return;
        }
        for (Inscricao inscricao : listAll()) {
            byEventoIndex.add(inscricao.getIdEvento(), inscricao.getId());
            byParticipanteIndex.add(inscricao.getIdParticipante(), inscricao.getId());
        }
    }
}
