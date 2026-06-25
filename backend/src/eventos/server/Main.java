package eventos.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import eventos.compression.CompressionReport;
import eventos.compression.DataArchiveService;
import eventos.compression.HuffmanCodec;
import eventos.compression.LzwCodec;
import eventos.compression.RestoreReport;
import eventos.controller.EventoController;
import eventos.controller.InscricaoController;
import eventos.controller.PalestranteController;
import eventos.controller.ParticipanteController;
import eventos.controller.AuthController;
import eventos.controller.PesquisaController;
import eventos.dao.EventoDAO;
import eventos.dao.InscricaoDAO;
import eventos.dao.PalestranteDAO;
import eventos.dao.ParticipanteDAO;
import eventos.pattern.PatternSearchService;
import eventos.security.AuthService;
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
    private static DataArchiveService dataArchiveService;
    private static PesquisaController pesquisaController;
    private static AuthController authController;
    private static EventoDAO eventoDAO;
    private static PalestranteDAO palestranteDAO;
    private static ParticipanteDAO participanteDAO;
    private static InscricaoDAO inscricaoDAO;
    private static final String BASE_PATH = "./storage";

    public static void main(String[] args) throws Exception {
        initializeApplication();

        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/eventos", Main::handleEventos);
        server.createContext("/palestrantes", Main::handlePalestrantes);
        server.createContext("/participantes", Main::handleParticipantes);
        server.createContext("/inscricoes", Main::handleInscricoes);
        server.createContext("/compressao", Main::handleCompressao);
        server.createContext("/pesquisa", Main::handlePesquisa);
        server.createContext("/auth", Main::handleAuth);
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
                    return id > 0 ? eventoController.get(id) : eventoController.list(exchange.getRequestURI().getQuery());
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
                    return id > 0 ? participanteController.get(id) : participanteController.list(exchange.getRequestURI().getQuery());
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

    private static void handlePesquisa(HttpExchange exchange) throws IOException {
        dispatch(exchange, () -> {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                return methodNotAllowed();
            }
            return pesquisaController.search(exchange.getRequestURI().getQuery());
        });
    }

    private static void handleAuth(HttpExchange exchange) throws IOException {
        dispatch(exchange, () -> {
            String action = extractIdOrName(exchange);
            if ("status".equalsIgnoreCase(action)) {
                if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                    return methodNotAllowed();
                }
                return authController.status();
            }
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                return methodNotAllowed();
            }
            if ("login".equalsIgnoreCase(action)) {
                return authController.login(readBody(exchange));
            }
            if ("primeiro-acesso".equalsIgnoreCase(action)) {
                return authController.registerFirstAccess(readBody(exchange));
            }
            if ("register".equalsIgnoreCase(action)) {
                return authController.register(readBody(exchange));
            }
            return new ApiResponse(404, JsonUtil.stringify(Map.of("erro", "Rota de autenticacao nao encontrada")));
        });
    }

    private static void handleCompressao(HttpExchange exchange) throws IOException {
        dispatch(exchange, () -> {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                return methodNotAllowed();
            }

            String action = pathPart(exchange, 2);
            String algorithm = "restaurar".equalsIgnoreCase(action) ? pathPart(exchange, 3) : action;
            boolean restore = "restaurar".equalsIgnoreCase(action);
            CompressionReport report;

            if (restore) {
                RestoreReport restoreReport;
                closeDaos();
                try {
                    if ("huffman".equalsIgnoreCase(algorithm)) {
                        restoreReport = dataArchiveService.restoreDataFiles(new HuffmanCodec());
                    } else if ("lzw".equalsIgnoreCase(algorithm)) {
                        restoreReport = dataArchiveService.restoreDataFiles(new LzwCodec());
                    } else {
                        return new ApiResponse(400, JsonUtil.stringify(Map.of(
                                "erro", "Algoritmo invalido. Use /compressao/restaurar/huffman ou /compressao/restaurar/lzw")));
                    }
                } finally {
                    initializeApplication();
                }

                return new ApiResponse(200, JsonUtil.stringify(Map.of(
                        "algoritmo", restoreReport.getAlgorithm(),
                        "arquivo", restoreReport.getInputPath().toString(),
                        "arquivosRestaurados", restoreReport.getRestoredFiles(),
                        "tamanhoRestaurado", restoreReport.getRestoredSize(),
                        "mensagem", "Dados restaurados e indices reconstruidos")));
            }

            // A Fase IV exige Huffman e LZW; cada rota gera um unico arquivo compactado com todos os .db.
            if ("huffman".equalsIgnoreCase(algorithm)) {
                report = dataArchiveService.compressDataFiles(new HuffmanCodec());
            } else if ("lzw".equalsIgnoreCase(algorithm)) {
                report = dataArchiveService.compressDataFiles(new LzwCodec());
            } else {
                return new ApiResponse(400, JsonUtil.stringify(Map.of(
                        "erro", "Algoritmo invalido. Use /compressao/huffman ou /compressao/lzw")));
            }

            return new ApiResponse(200, JsonUtil.stringify(Map.of(
                    "algoritmo", report.getAlgorithm(),
                    "arquivo", report.getOutputPath().toString(),
                    "arquivosCompactados", report.getFileCount(),
                    "tamanhoOriginal", report.getOriginalSize(),
                    "tamanhoCompactado", report.getCompressedSize(),
                    "taxaCompressao", String.format("%.2f%%", report.getCompressionRate()))));
        });
    }

    private static void initializeApplication() throws Exception {
        eventoDAO = new EventoDAO(BASE_PATH);
        participanteDAO = new ParticipanteDAO(BASE_PATH);
        inscricaoDAO = new InscricaoDAO(BASE_PATH);
        palestranteDAO = new PalestranteDAO(BASE_PATH);

        eventoController = new EventoController(eventoDAO, palestranteDAO, inscricaoDAO);
        palestranteController = new PalestranteController(palestranteDAO, eventoDAO);
        participanteController = new ParticipanteController(participanteDAO, inscricaoDAO);
        inscricaoController = new InscricaoController(inscricaoDAO, eventoDAO, participanteDAO);
        dataArchiveService = new DataArchiveService(BASE_PATH);
        pesquisaController = new PesquisaController(new PatternSearchService(
                eventoDAO, palestranteDAO, participanteDAO, inscricaoDAO));
        authController = new AuthController(new AuthService(participanteDAO));
    }

    private static void closeDaos() throws Exception {
        if (eventoDAO != null) eventoDAO.close();
        if (palestranteDAO != null) palestranteDAO.close();
        if (participanteDAO != null) participanteDAO.close();
        if (inscricaoDAO != null) inscricaoDAO.close();
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

    private static String extractIdOrName(HttpExchange exchange) {
        String[] parts = exchange.getRequestURI().getPath().split("/");
        if (parts.length >= 3) {
            return parts[2];
        }
        return "";
    }

    private static String pathPart(HttpExchange exchange, int index) {
        String[] parts = exchange.getRequestURI().getPath().split("/");
        return parts.length > index ? parts[index] : "";
    }

    private static ApiResponse methodNotAllowed() {
        return new ApiResponse(405, JsonUtil.stringify(Map.of("erro", "Metodo nao permitido")));
    }

    @FunctionalInterface
    private interface Handler {
        ApiResponse handle() throws Exception;
    }
}
