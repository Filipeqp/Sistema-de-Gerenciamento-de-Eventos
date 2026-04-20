package eventos.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import eventos.controller.EventoController;
import eventos.controller.InscricaoController;
import eventos.controller.PalestranteController;
import eventos.controller.ParticipanteController;
import eventos.dao.EventoDAO;
import eventos.dao.InscricaoDAO;
import eventos.dao.PalestranteDAO;
import eventos.dao.ParticipanteDAO;
import eventos.util.ApiResponse;
import eventos.util.JsonUtil;
import eventos.util.ValidationException;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class Main {

    private static EventoController eventoController;
    private static PalestranteController palestranteController;
    private static ParticipanteController participanteController;
    private static InscricaoController inscricaoController;

    public static void main(String[] args) throws Exception {
        String basePath = "./storage";

        EventoDAO eventoDAO = new EventoDAO(basePath);
        ParticipanteDAO participanteDAO = new ParticipanteDAO(basePath);
        InscricaoDAO inscricaoDAO = new InscricaoDAO(basePath);
        PalestranteDAO palestranteDAO = new PalestranteDAO(basePath);

        eventoController = new EventoController(eventoDAO, palestranteDAO, inscricaoDAO);
        palestranteController = new PalestranteController(palestranteDAO, eventoDAO);
        participanteController = new ParticipanteController(participanteDAO, inscricaoDAO);
        inscricaoController = new InscricaoController(inscricaoDAO, eventoDAO, participanteDAO);

        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/eventos", Main::handleEventos);
        server.createContext("/palestrantes", Main::handlePalestrantes);
        server.createContext("/participantes", Main::handleParticipantes);
        server.createContext("/inscricoes", Main::handleInscricoes);
        server.createContext("/health", Main::handleHealth);
        server.setExecutor(null);
        server.start();
        System.out.println("Servidor rodando em http://localhost:8080");
    }

    private static void handleEventos(HttpExchange exchange) throws IOException {
        dispatch(exchange, () -> {
            int id = extractId(exchange);
            switch (exchange.getRequestMethod().toUpperCase()) {
                case "GET":
                    return id > 0 ? eventoController.get(id) : eventoController.list();
                case "POST":
                    return eventoController.create(readBody(exchange));
                case "PUT":
                    return eventoController.update(id, readBody(exchange));
                case "DELETE":
                    return eventoController.delete(id);
                default:
                    return methodNotAllowed();
            }
        });
    }

    private static void handlePalestrantes(HttpExchange exchange) throws IOException {
        dispatch(exchange, () -> {
            int id = extractId(exchange);
            switch (exchange.getRequestMethod().toUpperCase()) {
                case "GET":
                    return id > 0 ? palestranteController.get(id) : palestranteController.list(exchange.getRequestURI().getQuery());
                case "POST":
                    return palestranteController.create(readBody(exchange));
                case "PUT":
                    return palestranteController.update(id, readBody(exchange));
                case "DELETE":
                    return palestranteController.delete(id);
                default:
                    return methodNotAllowed();
            }
        });
    }

    private static void handleParticipantes(HttpExchange exchange) throws IOException {
        dispatch(exchange, () -> {
            int id = extractId(exchange);
            switch (exchange.getRequestMethod().toUpperCase()) {
                case "GET":
                    return id > 0 ? participanteController.get(id) : participanteController.list();
                case "POST":
                    return participanteController.create(readBody(exchange));
                case "PUT":
                    return participanteController.update(id, readBody(exchange));
                case "DELETE":
                    return participanteController.delete(id);
                default:
                    return methodNotAllowed();
            }
        });
    }

    private static void handleInscricoes(HttpExchange exchange) throws IOException {
        dispatch(exchange, () -> {
            int id = extractId(exchange);
            switch (exchange.getRequestMethod().toUpperCase()) {
                case "GET":
                    return id > 0 ? inscricaoController.get(id) : inscricaoController.list(exchange.getRequestURI().getQuery());
                case "POST":
                    return inscricaoController.create(readBody(exchange));
                case "PUT":
                    return inscricaoController.update(id, readBody(exchange));
                case "DELETE":
                    return inscricaoController.delete(id);
                default:
                    return methodNotAllowed();
            }
        });
    }

    private static void handleHealth(HttpExchange exchange) throws IOException {
        dispatch(exchange, () -> new ApiResponse(200, JsonUtil.stringify(Map.of("status", "ok"))));
    }

    private static void dispatch(HttpExchange exchange, Handler handler) throws IOException {
        addCors(exchange);
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        ApiResponse response;
        try {
            response = handler.handle();
        } catch (ValidationException e) {
            response = new ApiResponse(400, JsonUtil.stringify(Map.of("erro", e.getMessage())));
        } catch (NumberFormatException e) {
            response = new ApiResponse(400, JsonUtil.stringify(Map.of("erro", "Parametro invalido")));
        } catch (Exception e) {
            response = new ApiResponse(500, JsonUtil.stringify(Map.of("erro", e.getMessage() == null ? "Erro interno" : e.getMessage())));
        }

        byte[] bytes = response.getBody().getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(response.getStatus(), bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static void addCors(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
    }

    private static String readBody(HttpExchange exchange) throws IOException {
        return new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
    }

    private static int extractId(HttpExchange exchange) {
        String[] parts = exchange.getRequestURI().getPath().split("/");
        if (parts.length >= 3 && !parts[2].isBlank()) {
            return Integer.parseInt(parts[2]);
        }
        return -1;
    }

    private static ApiResponse methodNotAllowed() {
        return new ApiResponse(405, JsonUtil.stringify(Map.of("erro", "Metodo nao permitido")));
    }

    @FunctionalInterface
    private interface Handler {
        ApiResponse handle() throws Exception;
    }
}
