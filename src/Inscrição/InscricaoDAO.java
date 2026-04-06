import java.util.ArrayList;

public class InscricaoDAO {

    private Arquivo<Inscricao> arqInscricoes;

    public InscricaoDAO() throws Exception {
        arqInscricoes = new Arquivo<>("inscricoes", Inscricao.class.getConstructor());
    }

    public boolean incluirInscricao(Inscricao i) throws Exception {
        return arqInscricoes.create(i) > 0;
    }

    public Inscricao buscarInscricao(int id) throws Exception {
        return arqInscricoes.read(id);
    }

    public boolean alterarInscricao(Inscricao i) throws Exception {
        return arqInscricoes.update(i);
    }

    public boolean excluirInscricao(int id) throws Exception {
        return arqInscricoes.delete(id);
    }

    public ArrayList<Inscricao> listarInscricoes() throws Exception {
        return arqInscricoes.readAll();
    }

    // Lista todas as inscrições de um participante (N:N)
    public ArrayList<Inscricao> listarPorParticipante(int idParticipante) throws Exception {
        ArrayList<Inscricao> todas = arqInscricoes.readAll();
        ArrayList<Inscricao> resultado = new ArrayList<>();
        for (Inscricao i : todas) {
            if (i.getIdParticipante() == idParticipante) {
                resultado.add(i);
            }
        }
        return resultado;
    }

    // Lista todas as inscrições de um evento (N:N)
    public ArrayList<Inscricao> listarPorEvento(int idEvento) throws Exception {
        ArrayList<Inscricao> todas = arqInscricoes.readAll();
        ArrayList<Inscricao> resultado = new ArrayList<>();
        for (Inscricao i : todas) {
            if (i.getIdEvento() == idEvento) {
                resultado.add(i);
            }
        }
        return resultado;
    }

    // Verifica se já existe inscrição do participante neste evento (evita duplicata)
    public boolean jaInscrito(int idEvento, int idParticipante) throws Exception {
        ArrayList<Inscricao> todas = arqInscricoes.readAll();
        for (Inscricao i : todas) {
            if (i.getIdEvento() == idEvento && i.getIdParticipante() == idParticipante) {
                return true;
            }
        }
        return false;
    }
}