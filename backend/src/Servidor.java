package eventos.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import eventos.dao.EventoDAO;
import eventos.dao.InscricaoDAO;
import eventos.dao.PalestranteDAO;
import eventos.dao.ParticipanteDAO;
import eventos.model.Evento;
import eventos.model.Inscricao;
import eventos.model.Palestrante;
import eventos.model.Participante;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class Servidor {

    private static final String BASE_PATH = "./storage";

    private static EventoDAO eventoDAO;
    private static PalestranteDAO palestranteDAO;
    private static ParticipanteDAO participanteDAO;
    private static InscricaoDAO inscricaoDAO;

    public static void main(String[] args) throws Exception {
        eventoDAO       = new EventoDAO(BASE_PATH);
        palestranteDAO  = new PalestranteDAO(BASE_PATH);
        participanteDAO = new ParticipanteDAO(BASE_PATH);
        inscricaoDAO    = new InscricaoDAO(BASE_PATH);

        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/eventos",      Servidor::handleEventos);
        server.createContext("/palestrantes", Servidor::handlePalestrantes);
        server.createContext("/participantes",Servidor::handleParticipantes);
        server.createContext("/inscricoes",   Servidor::handleInscricoes);
        server.setExecutor(null);
        server.start();
        System.out.println("Servidor rodando em http://localhost:8080");
    }

    // ==================== CORS / HELPERS ====================

    private static void addCors(HttpExchange ex) {
        ex.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        ex.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        ex.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
    }

    private static void sendResponse(HttpExchange ex, int status, String body) throws IOException {
        ex.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        ex.sendResponseHeaders(status, bytes.length);
        OutputStream os = ex.getResponseBody();
        os.write(bytes);
        os.close();
    }

    private static String readBody(HttpExchange ex) throws IOException {
        return new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
    }

    private static int parseId(String[] parts) {
        return (parts.length == 3 && !parts[2].isEmpty()) ? Integer.parseInt(parts[2]) : -1;
    }

    // ==================== EVENTOS ====================

    private static void handleEventos(HttpExchange ex) throws IOException {
        addCors(ex);
        if (ex.getRequestMethod().equalsIgnoreCase("OPTIONS")) { ex.sendResponseHeaders(204, -1); return; }

        String[] parts = ex.getRequestURI().getPath().split("/");
        boolean hasId = parts.length == 3 && !parts[2].isEmpty();
        int id = hasId ? Integer.parseInt(parts[2]) : -1;
        String query = ex.getRequestURI().getQuery(); // ex: ordenar=nome&ordem=asc

        try {
            switch (ex.getRequestMethod().toUpperCase()) {
                case "GET":
                    if (hasId) {
                        Evento e = eventoDAO.findById(id);
                        if (e != null) sendResponse(ex, 200, eventoToJson(e));
                        else           sendResponse(ex, 404, "{\"erro\":\"Evento nao encontrado\"}");
                    } else {
                        List<Evento> lista;
                        // GET /eventos?ordenar=nome&ordem=desc  → B+ decrescente
                        // GET /eventos?ordenar=nome             → B+ crescente (default)
                        // GET /eventos?ordenar=externo          → Ordenação externa por intercalação
                        // GET /eventos                          → listagem sem ordem garantida
                        if (query != null && query.contains("ordenar=externo")) {
                            lista = eventoDAO.listAllExternalSorted();
                        } else if (query != null && query.contains("ordenar=nome")) {
                            boolean desc = query.contains("ordem=desc");
                            lista = desc ? eventoDAO.listAllOrderedDesc() : eventoDAO.listAllOrdered();
                        } else {
                            lista = eventoDAO.listAll();
                        }
                        sendResponse(ex, 200, eventosToJson(lista));
                    }
                    break;
                case "POST":
                    Evento novo = eventoFromJson(readBody(ex));
                    Evento criado = eventoDAO.create(novo);
                    if (criado != null) sendResponse(ex, 201, eventoToJson(criado));
                    else               sendResponse(ex, 500, "{\"erro\":\"Erro ao incluir\"}");
                    break;
                case "PUT":
                    Evento atualizar = eventoFromJson(readBody(ex));
                    atualizar.setId(id);
                    Evento atualizado = eventoDAO.update(id, atualizar);
                    if (atualizado != null) sendResponse(ex, 200, eventoToJson(atualizado));
                    else                   sendResponse(ex, 404, "{\"erro\":\"Evento nao encontrado\"}");
                    break;
                case "DELETE":
                    if (eventoDAO.delete(id)) sendResponse(ex, 200, "{\"mensagem\":\"Evento excluido\"}");
                    else                      sendResponse(ex, 404, "{\"erro\":\"Evento nao encontrado\"}");
                    break;
                default:
                    sendResponse(ex, 405, "{\"erro\":\"Metodo nao permitido\"}");
            }
        } catch (Exception e) {
            sendResponse(ex, 500, "{\"erro\":\"" + escape(e.getMessage()) + "\"}");
        }
    }

    // ==================== PALESTRANTES ====================

    private static void handlePalestrantes(HttpExchange ex) throws IOException {
        addCors(ex);
        if (ex.getRequestMethod().equalsIgnoreCase("OPTIONS")) { ex.sendResponseHeaders(204, -1); return; }

        String[] parts = ex.getRequestURI().getPath().split("/");
        boolean hasId = parts.length == 3 && !parts[2].isEmpty();
        int id = hasId ? Integer.parseInt(parts[2]) : -1;
        String query = ex.getRequestURI().getQuery();

        try {
            switch (ex.getRequestMethod().toUpperCase()) {
                case "GET":
                    if (hasId) {
                        Palestrante p = palestranteDAO.findById(id);
                        if (p != null) sendResponse(ex, 200, palestranteToJson(p));
                        else           sendResponse(ex, 404, "{\"erro\":\"Palestrante nao encontrado\"}");
                    } else if (query != null && query.startsWith("idEvento=")) {
                        // GET /palestrantes?idEvento=X  → usa LinkedEntityListIndex (1:N via índice)
                        int idEvento = Integer.parseInt(query.split("=")[1].split("&")[0]);
                        List<Palestrante> lista = palestranteDAO.listByEvento(idEvento);
                        sendResponse(ex, 200, palestrantesToJson(lista));
                    } else if (query != null && query.contains("ordenar=nome")) {
                        // GET /palestrantes?ordenar=nome  → B+ crescente
                        boolean desc = query.contains("ordem=desc");
                        List<Palestrante> lista = desc
                                ? palestranteDAO.listAllOrderedDesc()
                                : palestranteDAO.listAllOrdered();
                        sendResponse(ex, 200, palestrantesToJson(lista));
                    } else {
                        sendResponse(ex, 200, palestrantesToJson(palestranteDAO.listAll()));
                    }
                    break;
                case "POST":
                    Palestrante novoPal = palestranteFromJson(readBody(ex));
                    Palestrante criadoPal = palestranteDAO.create(novoPal);
                    if (criadoPal != null) sendResponse(ex, 201, palestranteToJson(criadoPal));
                    else                  sendResponse(ex, 500, "{\"erro\":\"Erro ao incluir\"}");
                    break;
                case "PUT":
                    Palestrante atualizarPal = palestranteFromJson(readBody(ex));
                    atualizarPal.setId(id);
                    Palestrante atualizadoPal = palestranteDAO.update(id, atualizarPal);
                    if (atualizadoPal != null) sendResponse(ex, 200, palestranteToJson(atualizadoPal));
                    else                      sendResponse(ex, 404, "{\"erro\":\"Palestrante nao encontrado\"}");
                    break;
                case "DELETE":
                    if (palestranteDAO.delete(id)) sendResponse(ex, 200, "{\"mensagem\":\"Palestrante excluido\"}");
                    else                           sendResponse(ex, 404, "{\"erro\":\"Palestrante nao encontrado\"}");
                    break;
                default:
                    sendResponse(ex, 405, "{\"erro\":\"Metodo nao permitido\"}");
            }
        } catch (Exception e) {
            sendResponse(ex, 500, "{\"erro\":\"" + escape(e.getMessage()) + "\"}");
        }
    }

    // ==================== PARTICIPANTES ====================

    private static void handleParticipantes(HttpExchange ex) throws IOException {
        addCors(ex);
        if (ex.getRequestMethod().equalsIgnoreCase("OPTIONS")) { ex.sendResponseHeaders(204, -1); return; }

        String[] parts = ex.getRequestURI().getPath().split("/");
        boolean hasId = parts.length == 3 && !parts[2].isEmpty();
        int id = hasId ? Integer.parseInt(parts[2]) : -1;
        String query = ex.getRequestURI().getQuery();

        try {
            switch (ex.getRequestMethod().toUpperCase()) {
                case "GET":
                    if (hasId) {
                        Participante p = participanteDAO.findById(id);
                        if (p != null) sendResponse(ex, 200, participanteToJson(p));
                        else           sendResponse(ex, 404, "{\"erro\":\"Participante nao encontrado\"}");
                    } else if (query != null && query.contains("ordenar=nome")) {
                        boolean desc = query.contains("ordem=desc");
                        List<Participante> lista = desc
                                ? participanteDAO.listAllOrderedDesc()
                                : participanteDAO.listAllOrdered();
                        sendResponse(ex, 200, participantesToJson(lista));
                    } else {
                        sendResponse(ex, 200, participantesToJson(participanteDAO.listAll()));
                    }
                    break;
                case "POST":
                    Participante novoPart = participanteFromJson(readBody(ex));
                    Participante criadoPart = participanteDAO.create(novoPart);
                    if (criadoPart != null) sendResponse(ex, 201, participanteToJson(criadoPart));
                    else                   sendResponse(ex, 500, "{\"erro\":\"Erro ao incluir\"}");
                    break;
                case "PUT":
                    Participante atualizarPart = participanteFromJson(readBody(ex));
                    atualizarPart.setId(id);
                    Participante atualizadoPart = participanteDAO.update(id, atualizarPart);
                    if (atualizadoPart != null) sendResponse(ex, 200, participanteToJson(atualizadoPart));
                    else                       sendResponse(ex, 404, "{\"erro\":\"Participante nao encontrado\"}");
                    break;
                case "DELETE":
                    if (participanteDAO.delete(id)) sendResponse(ex, 200, "{\"mensagem\":\"Participante excluido\"}");
                    else                            sendResponse(ex, 404, "{\"erro\":\"Participante nao encontrado\"}");
                    break;
                default:
                    sendResponse(ex, 405, "{\"erro\":\"Metodo nao permitido\"}");
            }
        } catch (Exception e) {
            sendResponse(ex, 500, "{\"erro\":\"" + escape(e.getMessage()) + "\"}");
        }
    }

    // ==================== INSCRICOES ====================

    private static void handleInscricoes(HttpExchange ex) throws IOException {
        addCors(ex);
        if (ex.getRequestMethod().equalsIgnoreCase("OPTIONS")) { ex.sendResponseHeaders(204, -1); return; }

        String[] parts = ex.getRequestURI().getPath().split("/");
        boolean hasId = parts.length == 3 && !parts[2].isEmpty();
        int id = hasId ? Integer.parseInt(parts[2]) : -1;
        String query = ex.getRequestURI().getQuery();

        try {
            switch (ex.getRequestMethod().toUpperCase()) {
                case "GET":
                    if (hasId) {
                        Inscricao i = inscricaoDAO.findById(id);
                        if (i != null) sendResponse(ex, 200, inscricaoToJson(i));
                        else           sendResponse(ex, 404, "{\"erro\":\"Inscricao nao encontrada\"}");
                    } else if (query != null && query.startsWith("idEvento=")) {
                        int idEvento = Integer.parseInt(query.split("=")[1].split("&")[0]);
                        sendResponse(ex, 200, inscricoesToJson(inscricaoDAO.listByEvento(idEvento)));
                    } else if (query != null && query.startsWith("idParticipante=")) {
                        int idPart = Integer.parseInt(query.split("=")[1].split("&")[0]);
                        sendResponse(ex, 200, inscricoesToJson(inscricaoDAO.listByParticipante(idPart)));
                    } else {
                        sendResponse(ex, 200, inscricoesToJson(inscricaoDAO.listAll()));
                    }
                    break;
                case "POST":
                    Inscricao nova = inscricaoFromJson(readBody(ex));
                    if (inscricaoDAO.existsByEventoAndParticipante(
                            nova.getIdEvento(), nova.getIdParticipante(), null)) {
                        sendResponse(ex, 400, "{\"erro\":\"Participante ja inscrito neste evento\"}");
                        return;
                    }
                    Inscricao criada = inscricaoDAO.create(nova);
                    if (criada != null) sendResponse(ex, 201, inscricaoToJson(criada));
                    else               sendResponse(ex, 500, "{\"erro\":\"Erro ao incluir\"}");
                    break;
                case "PUT":
                    Inscricao atualizarInsc = inscricaoFromJson(readBody(ex));
                    atualizarInsc.setId(id);
                    Inscricao atualizadaInsc = inscricaoDAO.update(id, atualizarInsc);
                    if (atualizadaInsc != null) sendResponse(ex, 200, inscricaoToJson(atualizadaInsc));
                    else                       sendResponse(ex, 404, "{\"erro\":\"Inscricao nao encontrada\"}");
                    break;
                case "DELETE":
                    if (inscricaoDAO.delete(id)) sendResponse(ex, 200, "{\"mensagem\":\"Inscricao cancelada\"}");
                    else                         sendResponse(ex, 404, "{\"erro\":\"Inscricao nao encontrada\"}");
                    break;
                default:
                    sendResponse(ex, 405, "{\"erro\":\"Metodo nao permitido\"}");
            }
        } catch (Exception e) {
            sendResponse(ex, 500, "{\"erro\":\"" + escape(e.getMessage()) + "\"}");
        }
    }

    // ==================== JSON HELPERS ====================

    private static String eventoToJson(Evento e) {
        return "{\"id\":" + e.getId()
                + ",\"nome\":\""        + escape(e.getNome())       + "\""
                + ",\"descricao\":\""   + escape(e.getDescricao())  + "\""
                + ",\"dataEvento\":\"" + escape(e.getDataEvento())  + "\""
                + ",\"preco\":"         + e.getPreco()
                + ",\"tags\":\""        + escape(e.getTags())       + "\"}";
    }

    private static String eventosToJson(List<Evento> lista) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < lista.size(); i++) {
            sb.append(eventoToJson(lista.get(i)));
            if (i < lista.size() - 1) sb.append(",");
        }
        return sb.append("]").toString();
    }

    private static Evento eventoFromJson(String json) {
        return new Evento(
                extractString(json, "nome"),
                extractString(json, "descricao"),
                extractString(json, "dataEvento"),
                Float.parseFloat(extractValue(json, "preco")),
                extractString(json, "tags"));
    }

    private static String palestranteToJson(Palestrante p) {
        return "{\"id\":" + p.getId()
                + ",\"nome\":\""            + escape(p.getNome())           + "\""
                + ",\"miniCurriculo\":\""   + escape(p.getMiniCurriculo())  + "\""
                + ",\"especialidades\":\""  + escape(p.getEspecialidades()) + "\""
                + ",\"idEvento\":"           + p.getIdEvento()              + "}";
    }

    private static String palestrantesToJson(List<Palestrante> lista) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < lista.size(); i++) {
            sb.append(palestranteToJson(lista.get(i)));
            if (i < lista.size() - 1) sb.append(",");
        }
        return sb.append("]").toString();
    }

    private static Palestrante palestranteFromJson(String json) {
        return new Palestrante(
                extractString(json, "nome"),
                extractString(json, "miniCurriculo"),
                extractString(json, "especialidades"),
                Integer.parseInt(extractValue(json, "idEvento")));
    }

    private static String participanteToJson(Participante p) {
        return "{\"id\":" + p.getId()
                + ",\"nome\":\""        + escape(p.getNome())       + "\""
                + ",\"email\":\""       + escape(p.getEmail())      + "\""
                + ",\"interesses\":\"" + escape(p.getInteresses())  + "\"}";
    }

    private static String participantesToJson(List<Participante> lista) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < lista.size(); i++) {
            sb.append(participanteToJson(lista.get(i)));
            if (i < lista.size() - 1) sb.append(",");
        }
        return sb.append("]").toString();
    }

    private static Participante participanteFromJson(String json) {
        return new Participante(
                extractString(json, "nome"),
                extractString(json, "email"),
                extractString(json, "interesses"));
    }

    private static String inscricaoToJson(Inscricao i) {
        return "{\"id\":" + i.getId()
                + ",\"idEvento\":"          + i.getIdEvento()
                + ",\"idParticipante\":"    + i.getIdParticipante()
                + ",\"dataInscricao\":\"" + escape(i.getDataInscricao()) + "\"}";
    }

    private static String inscricoesToJson(List<Inscricao> lista) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < lista.size(); i++) {
            sb.append(inscricaoToJson(lista.get(i)));
            if (i < lista.size() - 1) sb.append(",");
        }
        return sb.append("]").toString();
    }

    private static Inscricao inscricaoFromJson(String json) {
        return new Inscricao(
                Integer.parseInt(extractValue(json, "idEvento")),
                Integer.parseInt(extractValue(json, "idParticipante")),
                extractString(json, "dataInscricao"));
    }

    private static String extractString(String json, String key) {
        String search = "\"" + key + "\":\"";
        int start = json.indexOf(search);
        if (start == -1) return "";
        start += search.length();
        int end = json.indexOf("\"", start);
        return end == -1 ? "" : json.substring(start, end);
    }

    private static String extractValue(String json, String key) {
        String search = "\"" + key + "\":";
        int start = json.indexOf(search);
        if (start == -1) return "0";
        start += search.length();
        int end = json.indexOf(",", start);
        if (end == -1) end = json.indexOf("}", start);
        return end == -1 ? "0" : json.substring(start, end).trim().replace("\"", "");
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r");
    }
}