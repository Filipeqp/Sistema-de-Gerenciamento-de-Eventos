package eventos.controller;

import eventos.dao.EventoDAO;
import eventos.dao.PalestranteDAO;
import eventos.model.Palestrante;
import eventos.util.ApiResponse;
import eventos.util.JsonUtil;
import eventos.util.ValidationException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PalestranteController {

    private final PalestranteDAO palestranteDAO;
    private final EventoDAO eventoDAO;

    public PalestranteController(PalestranteDAO palestranteDAO, EventoDAO eventoDAO) {
        this.palestranteDAO = palestranteDAO;
        this.eventoDAO = eventoDAO;
    }

    public ApiResponse list(String query) throws Exception {
        List<Map<String, Object>> items = new ArrayList<>();
        if (query != null && query.startsWith("idEvento=")) {
            int idEvento = Integer.parseInt(query.substring("idEvento=".length()));
            for (Palestrante palestrante : palestranteDAO.listByEvento(idEvento)) {
                items.add(palestrante.toMap());
            }
        } else {
            for (Palestrante palestrante : palestranteDAO.listAll()) {
                items.add(palestrante.toMap());
            }
        }
        return new ApiResponse(200, JsonUtil.stringify(items));
    }

    public ApiResponse get(int id) throws Exception {
        Palestrante palestrante = palestranteDAO.findById(id);
        if (palestrante == null) {
            return error(404, "Palestrante nao encontrado");
        }
        return new ApiResponse(200, JsonUtil.stringify(palestrante.toMap()));
    }

    public ApiResponse create(String body) throws Exception {
        Palestrante palestrante = fromPayload(JsonUtil.parseObject(body));
        return new ApiResponse(201, JsonUtil.stringify(palestranteDAO.create(palestrante).toMap()));
    }

    public ApiResponse update(int id, String body) throws Exception {
        Palestrante palestrante = fromPayload(JsonUtil.parseObject(body));
        Palestrante updated = palestranteDAO.update(id, palestrante);
        if (updated == null) {
            return error(404, "Palestrante nao encontrado");
        }
        return new ApiResponse(200, JsonUtil.stringify(updated.toMap()));
    }

    public ApiResponse delete(int id) throws Exception {
        if (!palestranteDAO.delete(id)) {
            return error(404, "Palestrante nao encontrado");
        }
        return new ApiResponse(200, JsonUtil.stringify(Map.of("mensagem", "Palestrante excluido")));
    }

    private Palestrante fromPayload(Map<String, String> payload) throws Exception {
        String nome = JsonUtil.getString(payload, "nome");
        String miniCurriculo = JsonUtil.getString(payload, "miniCurriculo");
        String especialidades = JsonUtil.getString(payload, "especialidades");
        int idEvento = JsonUtil.getInt(payload, "idEvento", -1);

        if (nome.isBlank()) {
            throw new ValidationException("Nome do palestrante e obrigatorio");
        }
        if (idEvento <= 0) {
            throw new ValidationException("Evento invalido");
        }
        if (eventoDAO.findById(idEvento) == null) {
            throw new ValidationException("Evento informado nao existe");
        }

        return new Palestrante(nome, miniCurriculo, especialidades, idEvento);
    }

    private ApiResponse error(int status, String message) {
        return new ApiResponse(status, JsonUtil.stringify(Map.of("erro", message)));
    }
}
