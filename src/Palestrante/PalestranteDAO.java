import java.util.ArrayList;

public class PalestranteDAO {

    private Arquivo<Palestrante> arqPalestrantes;

    public PalestranteDAO() throws Exception {
        arqPalestrantes = new Arquivo<>("palestrantes", Palestrante.class.getConstructor());
    }

    public boolean incluirPalestrante(Palestrante p) throws Exception {
        return arqPalestrantes.create(p) > 0;
    }

    public Palestrante buscarPalestrante(int id) throws Exception {
        return arqPalestrantes.read(id);
    }

    public boolean alterarPalestrante(Palestrante p) throws Exception {
        return arqPalestrantes.update(p);
    }

    public boolean excluirPalestrante(int id) throws Exception {
        return arqPalestrantes.delete(id);
    }

    public ArrayList<Palestrante> listarPalestrantes() throws Exception {
        return arqPalestrantes.readAll();
    }

    // Lista todos os palestrantes de um evento específico (relacionamento 1:N)
    public ArrayList<Palestrante> listarPorEvento(int idEvento) throws Exception {
        ArrayList<Palestrante> todos = arqPalestrantes.readAll();
        ArrayList<Palestrante> resultado = new ArrayList<>();
        for (Palestrante p : todos) {
            if (p.getIdEvento() == idEvento) {
                resultado.add(p);
            }
        }
        return resultado;
    }
}