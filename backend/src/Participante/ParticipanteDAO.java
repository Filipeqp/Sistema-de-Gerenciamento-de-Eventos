import java.util.ArrayList;

public class ParticipanteDAO {

    private Arquivo<Participante> arqParticipantes;

    public ParticipanteDAO() throws Exception {
        arqParticipantes = new Arquivo<>("participantes", Participante.class.getConstructor());
    }

    public boolean incluirParticipante(Participante p) throws Exception {
        return arqParticipantes.create(p) > 0;
    }

    public Participante buscarParticipante(int id) throws Exception {
        return arqParticipantes.read(id);
    }

    public boolean alterarParticipante(Participante p) throws Exception {
        return arqParticipantes.update(p);
    }

    public boolean excluirParticipante(int id) throws Exception {
        return arqParticipantes.delete(id);
    }

    public ArrayList<Participante> listarParticipantes() throws Exception {
        return arqParticipantes.readAll();
    }
}