import java.util.ArrayList;
import java.util.Scanner;

public class MenuParticipantes {

    private ParticipanteDAO participanteDAO;
    private Scanner console;

    public MenuParticipantes() throws Exception {
        participanteDAO = new ParticipanteDAO();
        console = new Scanner(System.in);
    }

    public void menu() {
        int opcao;
        do {
            System.out.println("\n\nMENU PARTICIPANTES");
            System.out.println("1 - Listar participantes");
            System.out.println("2 - Buscar participante por ID");
            System.out.println("3 - Incluir participante");
            System.out.println("4 - Alterar participante");
            System.out.println("5 - Excluir participante");
            System.out.println("0 - Voltar");
            System.out.print("\nOpção: ");

            try {
                opcao = Integer.parseInt(console.nextLine());
            } catch (Exception e) {
                opcao = -1;
            }

            switch (opcao) {
                case 1: listar();  break;
                case 2: buscar();  break;
                case 3: incluir(); break;
                case 4: alterar(); break;
                case 5: excluir(); break;
                case 0: break;
                default: System.out.println("Opção inválida!"); break;
            }
        } while (opcao != 0);
    }

    private void listar() {
        System.out.println("\nLISTA DE PARTICIPANTES\n");
        try {
            ArrayList<Participante> lista = participanteDAO.listarParticipantes();
            if (lista.isEmpty()) {
                System.out.println("Nenhum participante cadastrado.");
            } else {
                for (Participante p : lista) {
                    System.out.println(p);
                    System.out.println("--------------------");
                }
            }
        } catch (Exception e) {
            System.out.println("Erro ao listar participantes.");
        }
    }

    private void buscar() {
        try {
            System.out.print("ID do participante: ");
            int id = Integer.parseInt(console.nextLine());
            Participante p = participanteDAO.buscarParticipante(id);
            if (p != null)
                System.out.println(p);
            else
                System.out.println("Participante não encontrado.");
        } catch (Exception e) {
            System.out.println("Erro ao buscar participante.");
        }
    }

    private void incluir() {
        try {
            System.out.print("Nome: ");
            String nome = console.nextLine();

            System.out.print("Email: ");
            String email = console.nextLine();

            System.out.print("Interesses (separados por vírgula): ");
            String interesses = console.nextLine();

            Participante p = new Participante(nome, email, interesses);
            if (participanteDAO.incluirParticipante(p))
                System.out.println("Participante incluído com sucesso. ID: " + p.getId());
            else
                System.out.println("Erro ao incluir participante.");
        } catch (Exception e) {
            System.out.println("Erro ao incluir participante.");
        }
    }

    private void alterar() {
        try {
            System.out.print("ID do participante: ");
            int id = Integer.parseInt(console.nextLine());

            Participante p = participanteDAO.buscarParticipante(id);
            if (p == null) {
                System.out.println("Participante não encontrado.");
                return;
            }

            System.out.print("Novo nome: ");
            String nome = console.nextLine();

            System.out.print("Novo email: ");
            String email = console.nextLine();

            System.out.print("Novos interesses (separados por vírgula): ");
            String interesses = console.nextLine();

            Participante novo = new Participante(id, nome, email, interesses);
            if (participanteDAO.alterarParticipante(novo))
                System.out.println("Participante alterado com sucesso.");
            else
                System.out.println("Erro ao alterar participante.");
        } catch (Exception e) {
            System.out.println("Erro ao alterar participante.");
        }
    }

    private void excluir() {
        try {
            System.out.print("ID do participante: ");
            int id = Integer.parseInt(console.nextLine());

            if (participanteDAO.excluirParticipante(id))
                System.out.println("Participante excluído com sucesso.");
            else
                System.out.println("Participante não encontrado.");
        } catch (Exception e) {
            System.out.println("Erro ao excluir participante.");
        }
    }
}