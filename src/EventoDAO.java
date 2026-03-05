public class EventoDAO {

private Arquivo<Evento> arqEventos;

public EventoDAO() throws Exception {
    arqEventos = new Arquivo<>("eventos", Evento.class.getConstructor());
}

public Evento buscarEvento(int id) throws Exception {
    return arqEventos.read(id);
}

public boolean incluirEvento(Evento e) throws Exception {
    return arqEventos.create(e) > 0;
}

public boolean alterarEvento(Evento e) throws Exception {
    return arqEventos.update(e);
}

public boolean excluirEvento(int id) throws Exception {
    return arqEventos.delete(id);
}

}
