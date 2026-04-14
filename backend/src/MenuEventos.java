import java.util.Scanner;
import java.util.ArrayList;

public class MenuEventos {

private EventoDAO eventoDAO;
private Scanner console;

public MenuEventos() throws Exception {
    eventoDAO = new EventoDAO();
    console = new Scanner(System.in);
}

public void menu() {

    int opcao;

    do {

        System.out.println("\n\nMENU EVENTOS");
        System.out.println("1 - Listar eventos");
        System.out.println("2 - Buscar evento");
        System.out.println("3 - Incluir evento");
        System.out.println("4 - Alterar evento");
        System.out.println("5 - Excluir evento");
        System.out.println("0 - Voltar");

        System.out.print("\nOpção: ");

        try {
            opcao = Integer.parseInt(console.nextLine());
        } catch (Exception e) {
            opcao = -1;
        }

        switch (opcao) {

            case 1:
                listar();
                break;

            case 2:
                buscar();
                break;

            case 3:
                incluir();
                break;

            case 4:
                alterar();
                break;

            case 5:
                excluir();
                break;

            case 0:
                break;

            default:
                System.out.println("Opção inválida!");
                break;
        }

    } while (opcao != 0);

}

private void listar() {
    System.out.println("\nLISTA DE EVENTOS\n");
    try {
        ArrayList<Evento> eventos = eventoDAO.listarEventos();
        if (eventos.isEmpty()) {
            System.out.println("Nenhum evento cadastrado.");
        } else {
            for (Evento e : eventos) {
                System.out.println(e);
                System.out.println("--------------------");
            }
        }
    } catch (Exception ex) {
        System.out.println("Erro ao listar eventos.");
    }
}


private void buscar() {

    try {

        System.out.print("ID do evento: ");
        int id = Integer.parseInt(console.nextLine());

        Evento e = eventoDAO.buscarEvento(id);

        if (e != null)
            System.out.println(e);
        else
            System.out.println("Evento não encontrado.");

    } catch (Exception e) {
        System.out.println("Erro ao buscar evento.");
    }
}

private void incluir() {

    try {

        System.out.print("Nome: ");
        String nome = console.nextLine();

        System.out.print("Descrição: ");
        String descricao = console.nextLine();

        System.out.print("Data: ");
        String data = console.nextLine();

        System.out.print("Preço: ");
        float preco = Float.parseFloat(console.nextLine());

        System.out.print("Tags: ");
        String tags = console.nextLine();

        Evento e = new Evento(nome, descricao, data, preco, tags);

        if (eventoDAO.incluirEvento(e))
            System.out.println("Evento incluído com sucesso.");
        else
            System.out.println("Erro ao incluir evento.");

    } catch (Exception e) {
        System.out.println("Erro ao incluir evento.");
    }
}

private void alterar() {

    try {

        System.out.print("ID do evento: ");
        int id = Integer.parseInt(console.nextLine());

        Evento e = eventoDAO.buscarEvento(id);

        if (e == null) {
            System.out.println("Evento não encontrado.");
            return;
        }

        System.out.print("Novo nome: ");
        String nome = console.nextLine();

        System.out.print("Nova descrição: ");
        String descricao = console.nextLine();

        System.out.print("Nova data: ");
        String data = console.nextLine();

        System.out.print("Novo preço: ");
        float preco = Float.parseFloat(console.nextLine());

        System.out.print("Novas tags: ");
        String tags = console.nextLine();

        Evento novo = new Evento(id, nome, descricao, data, preco, tags);

        if (eventoDAO.alterarEvento(novo))
            System.out.println("Evento alterado.");
        else
            System.out.println("Erro ao alterar.");

    } catch (Exception e) {
        System.out.println("Erro ao alterar evento.");
    }
}

private void excluir() {

    try {

        System.out.print("ID do evento: ");
        int id = Integer.parseInt(console.nextLine());

        if (eventoDAO.excluirEvento(id))
            System.out.println("Evento excluído.");
        else
            System.out.println("Evento não encontrado.");

    } catch (Exception e) {
        System.out.println("Erro ao excluir evento.");
    }
}
}
