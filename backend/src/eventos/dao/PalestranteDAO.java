package eventos.dao;

import eventos.index.LinkedEntityListIndex;
import eventos.model.Palestrante;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class PalestranteDAO extends AbstractDAO<Palestrante> {

    private final LinkedEntityListIndex byEventoIndex;

    public PalestranteDAO(String basePath) throws Exception {
        super(basePath, "palestrantes", Palestrante.class);
        this.byEventoIndex = new LinkedEntityListIndex(
                basePath + "/indices/palestrantes",
                "palestrantes_por_evento");
        rebuildRelationshipIndexIfNeeded();
    }

    public Palestrante create(Palestrante palestrante) throws Exception {
        return createInternal(palestrante);
    }

    public Palestrante update(int id, Palestrante palestrante) throws Exception {
        return updateInternal(id, palestrante);
    }

    public boolean delete(int id) throws Exception {
        return deleteInternal(id);
    }

    /**
     * Lista palestrantes de um evento via LinkedEntityListIndex (1:N por índice).
     */
    public List<Palestrante> listByEvento(int idEvento) throws Exception {
        Set<Integer> ids = byEventoIndex.list(idEvento);
        List<Palestrante> items = new ArrayList<>();
        for (Integer id : ids) {
            Palestrante p = findById(id);
            if (p != null && p.getIdEvento() == idEvento) {
                items.add(p);
            }
        }
        return items;
    }

    /**
     * Lista palestrantes de um evento em ordem de nome (usa B+ + filtro).
     */
    public List<Palestrante> listByEventoOrdered(int idEvento) throws Exception {
        List<Palestrante> all = listByEvento(idEvento);
        all.sort((a, b) -> a.getNome().compareToIgnoreCase(b.getNome()));
        return all;
    }

    @Override
    protected String sortKey(Palestrante p) {
        return p.getNome() != null ? p.getNome() : "";
    }

    @Override
    protected void afterCreate(Palestrante record) throws Exception {
        byEventoIndex.add(record.getIdEvento(), record.getId());
    }

    @Override
    protected void afterUpdate(Palestrante previous, Palestrante updated) throws Exception {
        if (previous.getIdEvento() != updated.getIdEvento()) {
            byEventoIndex.remove(previous.getIdEvento(), previous.getId());
            byEventoIndex.add(updated.getIdEvento(), updated.getId());
        }
    }

    @Override
    protected void afterDelete(Palestrante previous) throws Exception {
        byEventoIndex.remove(previous.getIdEvento(), previous.getId());
    }

    private void rebuildRelationshipIndexIfNeeded() throws Exception {
        if (!byEventoIndex.isEmpty()) return;
        for (Palestrante p : listAll()) {
            byEventoIndex.add(p.getIdEvento(), p.getId());
        }
    }
}