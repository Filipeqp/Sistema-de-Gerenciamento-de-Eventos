package eventos.dao;

import eventos.model.Participante;

public class ParticipanteDAO extends AbstractDAO<Participante> {

    public ParticipanteDAO(String basePath) throws Exception {
        super(basePath, "participantes", Participante.class);
    }

    public Participante create(Participante participante) throws Exception {
        return createInternal(participante);
    }

    public Participante update(int id, Participante participante) throws Exception {
        return updateInternal(id, participante);
    }

    public boolean delete(int id) throws Exception {
        return deleteInternal(id);
    }

    @Override
    protected String sortKey(Participante p) {
        return p.getNome() != null ? p.getNome() : "";
    }
}