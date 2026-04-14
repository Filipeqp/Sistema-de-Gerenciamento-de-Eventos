import java.util.ArrayList;
import java.util.Scanner;

public class MenuPalestrantes {

    private PalestranteDAO palestranteDAO;
    private EventoDAO eventoDAO;
    private Scanner console;

    public MenuPalestrantes() throws Exception {
        palestranteDAO = new PalestranteDAO();
        eventoDAO = new EventoDAO();
        console = new Scanner(System.in);
    }

    public void menu() {
        int opcao;
        do {
            System.out.println("\n\nMENU PALESTRANTES");
            System.out.println("1 - Listar palestrantes");
            System.out.println("2 - Buscar palestrante por ID");
            System.out.println("3 - Listar palestrantes por evento");
            System.out.println("4 - Incluir palestrante");
            System.out.println("5 - Alterar palestrante");
            System.out.println("6 - Excluir palestrante");
            System.out.println("0 - Voltar");
            System.out.print("\nOpção: ");

            try {
                opcao = Integer.parseInt(console.nextLine());
            } catch (Exception e) {
                opcao = -1;
            }

            switch (opcao) {
                case 1: listar();          break;
                case 2: buscar();          break;
                case 3: listarPorEvento(); break;
                case 4: incluir();         break;
                case 5: alterar();         break;
                case 6: excluir();         break;
                case 0: break;
                default: System.out.println("Opção inválida!"); break;
            }
        } while (opcao != 0);
    }

    private void listar() {
        System.out.println("\nLISTA DE PALESTRANTES\n");
        try {
            ArrayList<Palestrante> lista = palestranteDAO.listarPalestrantes();
            if (lista.isEmpty()) {
                System.out.println("Nenhum palestrante cadastrado.");
            } else {
                for (Palestrante p : lista) {
                    System.out.println(p);
                    System.out.println("--------------------");
                }
            }
        } catch (Exception e) {
            System.out.println("Erro ao listar palestrantes.");
        }
    }

    private void buscar() {
        try {
            System.out.print("ID do palestrante: ");
            int id = Integer.parseInt(console.nextLine());
            Palestrante p = palestranteDAO.buscarPalestrante(id);
            if (p != null)
                System.out.println(p);
            else
                System.out.println("Palestrante não encontrado.");
        } catch (Exception e) {
            System.out.println("Erro ao buscar palestrante.");
        }
    }

    private void listarPorEvento() {
        try {
            System.out.print("ID do evento: ");
            int idEvento = Integer.parseInt(console.nextLine());

            // Valida se evento existe
            if (eventoDAO.buscarEvento(idEvento) == null) {
                System.out.println("Evento não encontrado.");
                return;
            }

            ArrayList<Palestrante> lista = palestranteDAO.listarPorEvento(idEvento);
            if (lista.isEmpty()) {
                System.out.println("Nenhum palestrante vinculado a este evento.");
            } else {
                System.out.println("\nPALESTRANTES DO EVENTO " + idEvento + ":\n");
                for (Palestrante p : lista) {
                    System.out.println(p);
                    System.out.println("--------------------");
                }
            }
        } catch (Exception e) {
            System.out.println("Erro ao listar palestrantes por evento.");
        }
    }

    private void incluir() {
        try {
            System.out.print("Nome: ");
            String nome = console.nextLine();

            System.out.print("Mini Currículo: ");
            String miniCurriculo = console.nextLine();

            System.out.print("Especialidades (separadas por vírgula): ");
            String especialidades = console.nextLine();

            System.out.print("ID do Evento: ");
            int idEvento = Integer.parseInt(console.nextLine());

            // Valida se evento existe
            if (eventoDAO.buscarEvento(idEvento) == null) {
                System.out.println("Evento não encontrado. Cadastre o evento antes.");
                return;
            }

            Palestrante p = new Palestrante(nome, miniCurriculo, especialidades, idEvento);
            if (palestranteDAO.incluirPalestrante(p))
                System.out.println("Palestrante incluído com sucesso. ID: " + p.getId());
            else
                System.out.println("Erro ao incluir palestrante.");
        } catch (Exception e) {
            System.out.println("Erro ao incluir palestrante.");
        }
    }

    private void alterar() {
        try {
            System.out.print("ID do palestrante: ");
            int id = Integer.parseInt(console.nextLine());

            Palestrante p = palestranteDAO.buscarPalestrante(id);
            if (p == null) {
                System.out.println("Palestrante não encontrado.");
                return;
            }

            System.out.print("Novo nome: ");
            String nome = console.nextLine();

            System.out.print("Novo mini currículo: ");
            String miniCurriculo = console.nextLine();

            System.out.print("Novas especialidades (separadas por vírgula): ");
            String especialidades = console.nextLine();

            System.out.print("Novo ID do Evento: ");
            int idEvento = Integer.parseInt(console.nextLine());

            // Valida se evento existe
            if (eventoDAO.buscarEvento(idEvento) == null) {
                System.out.println("Evento não encontrado.");
                return;
            }

            Palestrante novo = new Palestrante(id, nome, miniCurriculo, especialidades, idEvento);
            if (palestranteDAO.alterarPalestrante(novo))
                System.out.println("Palestrante alterado com sucesso.");
            else
                System.out.println("Erro ao alterar palestrante.");
        } catch (Exception e) {
            System.out.println("Erro ao alterar palestrante.");
        }
    }

    private void excluir() {
        try {
            System.out.print("ID do palestrante: ");
            int id = Integer.parseInt(console.nextLine());

            if (palestranteDAO.excluirPalestrante(id))
                System.out.println("Palestrante excluído com sucesso.");
            else
                System.out.println("Palestrante não encontrado.");
        } catch (Exception e) {
            System.out.println("Erro ao excluir palestrante.");
        }
    }
}