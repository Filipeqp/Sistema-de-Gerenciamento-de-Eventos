import java.util.ArrayList;
import java.util.Scanner;

public class MenuInscricoes {

    private InscricaoDAO inscricaoDAO;
    private EventoDAO eventoDAO;
    private ParticipanteDAO participanteDAO;
    private Scanner console;

    public MenuInscricoes() throws Exception {
        inscricaoDAO = new InscricaoDAO();
        eventoDAO = new EventoDAO();
        participanteDAO = new ParticipanteDAO();
        console = new Scanner(System.in);
    }

    public void menu() {
        int opcao;
        do {
            System.out.println("\n\nMENU INSCRIÇÕES");
            System.out.println("1 - Listar todas as inscrições");
            System.out.println("2 - Buscar inscrição por ID");
            System.out.println("3 - Listar inscrições por evento");
            System.out.println("4 - Listar inscrições por participante");
            System.out.println("5 - Realizar inscrição");
            System.out.println("6 - Alterar inscrição");
            System.out.println("7 - Cancelar inscrição");
            System.out.println("0 - Voltar");
            System.out.print("\nOpção: ");

            try {
                opcao = Integer.parseInt(console.nextLine());
            } catch (Exception e) {
                opcao = -1;
            }

            switch (opcao) {
                case 1: listar();              break;
                case 2: buscar();              break;
                case 3: listarPorEvento();     break;
                case 4: listarPorParticipante(); break;
                case 5: incluir();             break;
                case 6: alterar();             break;
                case 7: excluir();             break;
                case 0: break;
                default: System.out.println("Opção inválida!"); break;
            }
        } while (opcao != 0);
    }

    private void listar() {
        System.out.println("\nLISTA DE INSCRIÇÕES\n");
        try {
            ArrayList<Inscricao> lista = inscricaoDAO.listarInscricoes();
            if (lista.isEmpty()) {
                System.out.println("Nenhuma inscrição cadastrada.");
            } else {
                for (Inscricao i : lista) {
                    System.out.println(i);
                    System.out.println("--------------------");
                }
            }
        } catch (Exception e) {
            System.out.println("Erro ao listar inscrições.");
        }
    }

    private void buscar() {
        try {
            System.out.print("ID da inscrição: ");
            int id = Integer.parseInt(console.nextLine());
            Inscricao i = inscricaoDAO.buscarInscricao(id);
            if (i != null)
                System.out.println(i);
            else
                System.out.println("Inscrição não encontrada.");
        } catch (Exception e) {
            System.out.println("Erro ao buscar inscrição.");
        }
    }

    private void listarPorEvento() {
        try {
            System.out.print("ID do evento: ");
            int idEvento = Integer.parseInt(console.nextLine());

            if (eventoDAO.buscarEvento(idEvento) == null) {
                System.out.println("Evento não encontrado.");
                return;
            }

            ArrayList<Inscricao> lista = inscricaoDAO.listarPorEvento(idEvento);
            if (lista.isEmpty()) {
                System.out.println("Nenhuma inscrição para este evento.");
            } else {
                System.out.println("\nINSCRIÇÕES DO EVENTO " + idEvento + ":\n");
                for (Inscricao i : lista) {
                    // Exibe dados do participante junto
                    Participante p = participanteDAO.buscarParticipante(i.getIdParticipante());
                    System.out.println(i);
                    if (p != null)
                        System.out.println("Participante: " + p.getNome() + " | Email: " + p.getEmail());
                    System.out.println("--------------------");
                }
            }
        } catch (Exception e) {
            System.out.println("Erro ao listar inscrições por evento.");
        }
    }

    private void listarPorParticipante() {
        try {
            System.out.print("ID do participante: ");
            int idParticipante = Integer.parseInt(console.nextLine());

            if (participanteDAO.buscarParticipante(idParticipante) == null) {
                System.out.println("Participante não encontrado.");
                return;
            }

            ArrayList<Inscricao> lista = inscricaoDAO.listarPorParticipante(idParticipante);
            if (lista.isEmpty()) {
                System.out.println("Nenhuma inscrição para este participante.");
            } else {
                System.out.println("\nINSCRIÇÕES DO PARTICIPANTE " + idParticipante + ":\n");
                for (Inscricao i : lista) {
                    // Exibe dados do evento junto
                    Evento ev = eventoDAO.buscarEvento(i.getIdEvento());
                    System.out.println(i);
                    if (ev != null)
                        System.out.println("Evento: " + ev.toString());
                    System.out.println("--------------------");
                }
            }
        } catch (Exception e) {
            System.out.println("Erro ao listar inscrições por participante.");
        }
    }

    private void incluir() {
        try {
            System.out.print("ID do evento: ");
            int idEvento = Integer.parseInt(console.nextLine());

            if (eventoDAO.buscarEvento(idEvento) == null) {
                System.out.println("Evento não encontrado.");
                return;
            }

            System.out.print("ID do participante: ");
            int idParticipante = Integer.parseInt(console.nextLine());

            if (participanteDAO.buscarParticipante(idParticipante) == null) {
                System.out.println("Participante não encontrado.");
                return;
            }

            // Verifica duplicata
            if (inscricaoDAO.jaInscrito(idEvento, idParticipante)) {
                System.out.println("Este participante já está inscrito neste evento.");
                return;
            }

            System.out.print("Data da inscrição (dd/mm/aaaa): ");
            String dataInscricao = console.nextLine();

            Inscricao i = new Inscricao(idEvento, idParticipante, dataInscricao);
            if (inscricaoDAO.incluirInscricao(i))
                System.out.println("Inscrição realizada com sucesso. ID: " + i.getId());
            else
                System.out.println("Erro ao realizar inscrição.");
        } catch (Exception e) {
            System.out.println("Erro ao realizar inscrição.");
        }
    }

    private void alterar() {
        try {
            System.out.print("ID da inscrição: ");
            int id = Integer.parseInt(console.nextLine());

            Inscricao i = inscricaoDAO.buscarInscricao(id);
            if (i == null) {
                System.out.println("Inscrição não encontrada.");
                return;
            }

            System.out.print("Novo ID do evento: ");
            int idEvento = Integer.parseInt(console.nextLine());

            if (eventoDAO.buscarEvento(idEvento) == null) {
                System.out.println("Evento não encontrado.");
                return;
            }

            System.out.print("Novo ID do participante: ");
            int idParticipante = Integer.parseInt(console.nextLine());

            if (participanteDAO.buscarParticipante(idParticipante) == null) {
                System.out.println("Participante não encontrado.");
                return;
            }

            System.out.print("Nova data da inscrição (dd/mm/aaaa): ");
            String dataInscricao = console.nextLine();

            Inscricao nova = new Inscricao(id, idEvento, idParticipante, dataInscricao);
            if (inscricaoDAO.alterarInscricao(nova))
                System.out.println("Inscrição alterada com sucesso.");
            else
                System.out.println("Erro ao alterar inscrição.");
        } catch (Exception e) {
            System.out.println("Erro ao alterar inscrição.");
        }
    }

    private void excluir() {
        try {
            System.out.print("ID da inscrição: ");
            int id = Integer.parseInt(console.nextLine());

            if (inscricaoDAO.excluirInscricao(id))
                System.out.println("Inscrição cancelada com sucesso.");
            else
                System.out.println("Inscrição não encontrada.");
        } catch (Exception e) {
            System.out.println("Erro ao cancelar inscrição.");
        }
    }
}