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
        this.byEventoIndex = new LinkedEntityListIndex(basePath + "/indices/palestrantes", "palestrantes_por_evento");
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

    public List<Palestrante> listByEvento(int idEvento) throws Exception {
        Set<Integer> ids = byEventoIndex.list(idEvento);
        List<Palestrante> items = new ArrayList<>();
        for (Integer id : ids) {
            Palestrante palestrante = findById(id);
            if (palestrante != null && palestrante.getIdEvento() == idEvento) {
                items.add(palestrante);
            }
        }
        return items;
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
        if (!byEventoIndex.isEmpty()) {
            return;
        }
        for (Palestrante palestrante : listAll()) {
            byEventoIndex.add(palestrante.getIdEvento(), palestrante.getId());
        }
    }
}
