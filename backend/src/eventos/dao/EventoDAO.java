package eventos.dao;

import eventos.model.Evento;

public class EventoDAO extends AbstractDAO<Evento> {

    public EventoDAO(String basePath) throws Exception {
        super(basePath, "eventos", Evento.class);
    }

    public Evento create(Evento evento) throws Exception {
        return createInternal(evento);
    }

    public Evento update(int id, Evento evento) throws Exception {
        return updateInternal(id, evento);
    }

    public boolean delete(int id) throws Exception {
        return deleteInternal(id);
    }
}
