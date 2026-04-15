import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class Servidor {

    private static EventoDAO eventoDAO;
    private static PalestranteDAO palestranteDAO;
    private static ParticipanteDAO participanteDAO;
    private static InscricaoDAO inscricaoDAO;

    public static void main(String[] args) throws Exception {
        eventoDAO = new EventoDAO();
        palestranteDAO = new PalestranteDAO();
        participanteDAO = new ParticipanteDAO();
        inscricaoDAO = new InscricaoDAO();

        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/eventos", Servidor::handleEventos);
        server.createContext("/palestrantes", Servidor::handlePalestrantes);
        server.createContext("/participantes", Servidor::handleParticipantes);
        server.createContext("/inscricoes", Servidor::handleInscricoes);
        server.setExecutor(null);
        server.start();
        System.out.println("Servidor rodando em http://localhost:8080");
    }

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

    // ==================== EVENTOS ====================
    private static void handleEventos(HttpExchange ex) throws IOException {
        addCors(ex);
        if (ex.getRequestMethod().equalsIgnoreCase("OPTIONS")) { ex.sendResponseHeaders(204, -1); return; }
        String[] parts = ex.getRequestURI().getPath().split("/");
        boolean hasId = parts.length == 3 && !parts[2].isEmpty();
        int id = hasId ? Integer.parseInt(parts[2]) : -1;
        try {
            switch (ex.getRequestMethod().toUpperCase()) {
                case "GET":
                    if (hasId) {
                        Evento e = eventoDAO.buscarEvento(id);
                        if (e != null) sendResponse(ex, 200, eventoToJson(e));
                        else sendResponse(ex, 404, "{\"erro\":\"Evento nao encontrado\"}");
                    } else {
                        sendResponse(ex, 200, eventosToJson(eventoDAO.listarEventos()));
                    }
                    break;
                case "POST":
                    Evento novoEvento = eventoFromJson(readBody(ex));
                    if (eventoDAO.incluirEvento(novoEvento)) sendResponse(ex, 201, eventoToJson(novoEvento));
                    else sendResponse(ex, 500, "{\"erro\":\"Erro ao incluir\"}");
                    break;
                case "PUT":
                    Evento eventoAtualizado = eventoFromJson(readBody(ex));
                    eventoAtualizado.setId(id);
                    if (eventoDAO.alterarEvento(eventoAtualizado)) sendResponse(ex, 200, eventoToJson(eventoAtualizado));
                    else sendResponse(ex, 404, "{\"erro\":\"Evento nao encontrado\"}");
                    break;
                case "DELETE":
                    if (eventoDAO.excluirEvento(id)) sendResponse(ex, 200, "{\"mensagem\":\"Evento excluido\"}");
                    else sendResponse(ex, 404, "{\"erro\":\"Evento nao encontrado\"}");
                    break;
                default: sendResponse(ex, 405, "{\"erro\":\"Metodo nao permitido\"}");
            }
        } catch (Exception e) { sendResponse(ex, 500, "{\"erro\":\"" + escape(e.getMessage()) + "\"}"); }
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
                        Palestrante p = palestranteDAO.buscarPalestrante(id);
                        if (p != null) sendResponse(ex, 200, palestranteToJson(p));
                        else sendResponse(ex, 404, "{\"erro\":\"Palestrante nao encontrado\"}");
                    } else if (query != null && query.startsWith("idEvento=")) {
                        int idEvento = Integer.parseInt(query.split("=")[1]);
                        sendResponse(ex, 200, palestrantesToJson(palestranteDAO.listarPorEvento(idEvento)));
                    } else {
                        sendResponse(ex, 200, palestrantesToJson(palestranteDAO.listarPalestrantes()));
                    }
                    break;
                case "POST":
                    Palestrante novo = palestranteFromJson(readBody(ex));
                    if (palestranteDAO.incluirPalestrante(novo)) sendResponse(ex, 201, palestranteToJson(novo));
                    else sendResponse(ex, 500, "{\"erro\":\"Erro ao incluir\"}");
                    break;
                case "PUT":
                    Palestrante atualizado = palestranteFromJson(readBody(ex));
                    atualizado.setId(id);
                    if (palestranteDAO.alterarPalestrante(atualizado)) sendResponse(ex, 200, palestranteToJson(atualizado));
                    else sendResponse(ex, 404, "{\"erro\":\"Palestrante nao encontrado\"}");
                    break;
                case "DELETE":
                    if (palestranteDAO.excluirPalestrante(id)) sendResponse(ex, 200, "{\"mensagem\":\"Palestrante excluido\"}");
                    else sendResponse(ex, 404, "{\"erro\":\"Palestrante nao encontrado\"}");
                    break;
                default: sendResponse(ex, 405, "{\"erro\":\"Metodo nao permitido\"}");
            }
        } catch (Exception e) { sendResponse(ex, 500, "{\"erro\":\"" + escape(e.getMessage()) + "\"}"); }
    }

    // ==================== PARTICIPANTES ====================
    private static void handleParticipantes(HttpExchange ex) throws IOException {
        addCors(ex);
        if (ex.getRequestMethod().equalsIgnoreCase("OPTIONS")) { ex.sendResponseHeaders(204, -1); return; }
        String[] parts = ex.getRequestURI().getPath().split("/");
        boolean hasId = parts.length == 3 && !parts[2].isEmpty();
        int id = hasId ? Integer.parseInt(parts[2]) : -1;
        try {
            switch (ex.getRequestMethod().toUpperCase()) {
                case "GET":
                    if (hasId) {
                        Participante p = participanteDAO.buscarParticipante(id);
                        if (p != null) sendResponse(ex, 200, participanteToJson(p));
                        else sendResponse(ex, 404, "{\"erro\":\"Participante nao encontrado\"}");
                    } else {
                        sendResponse(ex, 200, participantesToJson(participanteDAO.listarParticipantes()));
                    }
                    break;
                case "POST":
                    Participante novo = participanteFromJson(readBody(ex));
                    if (participanteDAO.incluirParticipante(novo)) sendResponse(ex, 201, participanteToJson(novo));
                    else sendResponse(ex, 500, "{\"erro\":\"Erro ao incluir\"}");
                    break;
                case "PUT":
                    Participante atualizado = participanteFromJson(readBody(ex));
                    atualizado.setId(id);
                    if (participanteDAO.alterarParticipante(atualizado)) sendResponse(ex, 200, participanteToJson(atualizado));
                    else sendResponse(ex, 404, "{\"erro\":\"Participante nao encontrado\"}");
                    break;
                case "DELETE":
                    if (participanteDAO.excluirParticipante(id)) sendResponse(ex, 200, "{\"mensagem\":\"Participante excluido\"}");
                    else sendResponse(ex, 404, "{\"erro\":\"Participante nao encontrado\"}");
                    break;
                default: sendResponse(ex, 405, "{\"erro\":\"Metodo nao permitido\"}");
            }
        } catch (Exception e) { sendResponse(ex, 500, "{\"erro\":\"" + escape(e.getMessage()) + "\"}"); }
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
                        Inscricao i = inscricaoDAO.buscarInscricao(id);
                        if (i != null) sendResponse(ex, 200, inscricaoToJson(i));
                        else sendResponse(ex, 404, "{\"erro\":\"Inscricao nao encontrada\"}");
                    } else if (query != null && query.startsWith("idEvento=")) {
                        int idEvento = Integer.parseInt(query.split("=")[1]);
                        sendResponse(ex, 200, inscricoesToJson(inscricaoDAO.listarPorEvento(idEvento)));
                    } else if (query != null && query.startsWith("idParticipante=")) {
                        int idPart = Integer.parseInt(query.split("=")[1]);
                        sendResponse(ex, 200, inscricoesToJson(inscricaoDAO.listarPorParticipante(idPart)));
                    } else {
                        sendResponse(ex, 200, inscricoesToJson(inscricaoDAO.listarInscricoes()));
                    }
                    break;
                case "POST":
                    Inscricao nova = inscricaoFromJson(readBody(ex));
                    if (inscricaoDAO.jaInscrito(nova.getIdEvento(), nova.getIdParticipante())) {
                        sendResponse(ex, 400, "{\"erro\":\"Participante ja inscrito neste evento\"}");
                        return;
                    }
                    if (inscricaoDAO.incluirInscricao(nova)) sendResponse(ex, 201, inscricaoToJson(nova));
                    else sendResponse(ex, 500, "{\"erro\":\"Erro ao incluir\"}");
                    break;
                case "PUT":
                    Inscricao atualizada = inscricaoFromJson(readBody(ex));
                    atualizada.setId(id);
                    if (inscricaoDAO.alterarInscricao(atualizada)) sendResponse(ex, 200, inscricaoToJson(atualizada));
                    else sendResponse(ex, 404, "{\"erro\":\"Inscricao nao encontrada\"}");
                    break;
                case "DELETE":
                    if (inscricaoDAO.excluirInscricao(id)) sendResponse(ex, 200, "{\"mensagem\":\"Inscricao cancelada\"}");
                    else sendResponse(ex, 404, "{\"erro\":\"Inscricao nao encontrada\"}");
                    break;
                default: sendResponse(ex, 405, "{\"erro\":\"Metodo nao permitido\"}");
            }
        } catch (Exception e) { sendResponse(ex, 500, "{\"erro\":\"" + escape(e.getMessage()) + "\"}"); }
    }

    // ==================== JSON HELPERS ====================
    private static String eventoToJson(Evento e) {
        return "{\"id\":" + e.getId() + ",\"nome\":\"" + escape(e.getNome()) + "\",\"descricao\":\"" + escape(e.getDescricao()) + "\",\"dataEvento\":\"" + escape(e.getDataEvento()) + "\",\"preco\":" + e.getPreco() + ",\"tags\":\"" + escape(e.getTags()) + "\"}";
    }
    private static String eventosToJson(ArrayList<Evento> lista) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < lista.size(); i++) { sb.append(eventoToJson(lista.get(i))); if (i < lista.size()-1) sb.append(","); }
        return sb.append("]").toString();
    }
    private static Evento eventoFromJson(String json) {
        return new Evento(extractString(json,"nome"), extractString(json,"descricao"), extractString(json,"dataEvento"), Float.parseFloat(extractValue(json,"preco")), extractString(json,"tags"));
    }

    private static String palestranteToJson(Palestrante p) {
        return "{\"id\":" + p.getId() + ",\"nome\":\"" + escape(p.getNome()) + "\",\"miniCurriculo\":\"" + escape(p.getMiniCurriculo()) + "\",\"especialidades\":\"" + escape(p.getEspecialidades()) + "\",\"idEvento\":" + p.getIdEvento() + "}";
    }
    private static String palestrantesToJson(ArrayList<Palestrante> lista) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < lista.size(); i++) { sb.append(palestranteToJson(lista.get(i))); if (i < lista.size()-1) sb.append(","); }
        return sb.append("]").toString();
    }
    private static Palestrante palestranteFromJson(String json) {
        return new Palestrante(extractString(json,"nome"), extractString(json,"miniCurriculo"), extractString(json,"especialidades"), Integer.parseInt(extractValue(json,"idEvento")));
    }

    private static String participanteToJson(Participante p) {
        return "{\"id\":" + p.getId() + ",\"nome\":\"" + escape(p.getNome()) + "\",\"email\":\"" + escape(p.getEmail()) + "\",\"interesses\":\"" + escape(p.getInteresses()) + "\"}";
    }
    private static String participantesToJson(ArrayList<Participante> lista) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < lista.size(); i++) { sb.append(participanteToJson(lista.get(i))); if (i < lista.size()-1) sb.append(","); }
        return sb.append("]").toString();
    }
    private static Participante participanteFromJson(String json) {
        return new Participante(extractString(json,"nome"), extractString(json,"email"), extractString(json,"interesses"));
    }

    private static String inscricaoToJson(Inscricao i) {
        return "{\"id\":" + i.getId() + ",\"idEvento\":" + i.getIdEvento() + ",\"idParticipante\":" + i.getIdParticipante() + ",\"dataInscricao\":\"" + escape(i.getDataInscricao()) + "\"}";
    }
    private static String inscricoesToJson(ArrayList<Inscricao> lista) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < lista.size(); i++) { sb.append(inscricaoToJson(lista.get(i))); if (i < lista.size()-1) sb.append(","); }
        return sb.append("]").toString();
    }
    private static Inscricao inscricaoFromJson(String json) {
        return new Inscricao(Integer.parseInt(extractValue(json,"idEvento")), Integer.parseInt(extractValue(json,"idParticipante")), extractString(json,"dataInscricao"));
    }

    private static String extractString(String json, String key) {
        String search = "\"" + key + "\":\"";
        int start = json.indexOf(search);
        if (start == -1) return "";
        start += search.length();
        int end = json.indexOf("\"", start);
        return json.substring(start, end);
    }
    private static String extractValue(String json, String key) {
        String search = "\"" + key + "\":";
        int start = json.indexOf(search);
        if (start == -1) return "0";
        start += search.length();
        int end = json.indexOf(",", start);
        if (end == -1) end = json.indexOf("}", start);
        return json.substring(start, end).trim().replace("\"","");
    }
    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\","\\\\").replace("\"","\\\"").replace("\n","\\n").replace("\r","\\r");
    }
}
