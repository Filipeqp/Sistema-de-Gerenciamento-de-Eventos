import java.util.Scanner;

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
        System.out.println("1 - Buscar evento");
        System.out.println("2 - Incluir evento");
        System.out.println("3 - Alterar evento");
        System.out.println("4 - Excluir evento");
        System.out.println("0 - Voltar");

        System.out.print("\nOpção: ");

        try {
            opcao = Integer.parseInt(console.nextLine());
        } catch (Exception e) {
            opcao = -1;
        }

        switch (opcao) {

            case 1:
                buscar();
                break;

            case 2:
                incluir();
                break;

            case 3:
                alterar();
                break;

            case 4:
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
