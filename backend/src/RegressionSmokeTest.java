import eventos.controller.EventoController;
import eventos.dao.EventoDAO;
import eventos.dao.InscricaoDAO;
import eventos.dao.PalestranteDAO;
import eventos.dao.ParticipanteDAO;
import eventos.model.Evento;
import eventos.model.Palestrante;
import eventos.model.Participante;
import eventos.util.ApiResponse;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class RegressionSmokeTest {

    public static void main(String[] args) throws Exception {
        Path baseDir = Files.createTempDirectory("gestevent-regression-");
        String basePath = baseDir.toAbsolutePath().toString().replace('\\', '/');

        System.out.println("Usando base temporaria: " + basePath);

        EventoDAO eventoDAO = new EventoDAO(basePath);
        PalestranteDAO palestranteDAO = new PalestranteDAO(basePath);
        ParticipanteDAO participanteDAO = new ParticipanteDAO(basePath);
        InscricaoDAO inscricaoDAO = new InscricaoDAO(basePath);
        EventoController eventoController = new EventoController(eventoDAO, palestranteDAO, inscricaoDAO);

        Evento evento = eventoDAO.create(new Evento(
                "Evento Base",
                "Descricao longa para manter o primeiro registro maior",
                "2026-04-24",
                10.0f,
                "teste"));

        palestranteDAO.create(new Palestrante(
                "Palestrante Muito Grande",
                "Mini curriculo com bastante texto para ocupar um espaco maior no arquivo",
                "Java, Estruturas, Persistencia",
                evento.getId()));
        palestranteDAO.create(new Palestrante(
                "Outro Palestrante",
                "Outro curriculo",
                "Banco de dados",
                evento.getId()));

        palestranteDAO.delete(1);
        palestranteDAO.create(new Palestrante(
                "Curto",
                "",
                "",
                evento.getId()));

        List<Palestrante> palestrantes = palestranteDAO.listAll();
        assertEquals(2, palestrantes.size(), "Quantidade incorreta de palestrantes ativos");
        System.out.println("Palestrantes ativos: " + palestrantes.size());
        for (Palestrante palestrante : palestrantes) {
            System.out.println(" - " + palestrante.getId() + ": " + palestrante.getNome());
        }

        assertEquals(2, palestranteDAO.listByEvento(evento.getId()).size(), "Indice de palestrantes por evento ficou inconsistente");
        ApiResponse bloqueado = eventoController.delete(evento.getId());
        assertEquals(409, bloqueado.getStatus(), "Evento deveria permanecer bloqueado enquanto ha palestrantes ativos");

        participanteDAO.create(new Participante(
                "Participante Muito Grande",
                "grande@example.com",
                "interesses longos para ocupar bastante espaco"));
        participanteDAO.create(new Participante(
                "Outro Participante",
                "outro@example.com",
                "dados"));

        participanteDAO.delete(1);
        participanteDAO.create(new Participante(
                "Curto",
                "curto@example.com",
                ""));

        List<Participante> participantes = participanteDAO.listAll();
        assertEquals(2, participantes.size(), "Quantidade incorreta de participantes ativos");
        System.out.println("Participantes ativos: " + participantes.size());
        for (Participante participante : participantes) {
            System.out.println(" - " + participante.getId() + ": " + participante.getNome());
        }

        palestranteDAO.delete(2);
        palestranteDAO.delete(3);
        assertEquals(0, palestranteDAO.listByEvento(evento.getId()).size(), "Evento ainda reporta palestrantes apos excluir todos");

        ApiResponse excluido = eventoController.delete(evento.getId());
        assertEquals(200, excluido.getStatus(), "Evento deveria ser excluido apos remover todos os palestrantes");
        System.out.println("Fluxo de exclusao de evento validado com sucesso.");
    }

    private static void assertEquals(int expected, int actual, String message) {
        if (expected != actual) {
            throw new IllegalStateException(message + " Esperado=" + expected + ", atual=" + actual);
        }
    }
}
