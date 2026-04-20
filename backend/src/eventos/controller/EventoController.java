package eventos.controller;

import eventos.dao.EventoDAO;
import eventos.dao.InscricaoDAO;
import eventos.dao.PalestranteDAO;
import eventos.model.Evento;
import eventos.util.ApiResponse;
import eventos.util.JsonUtil;
import eventos.util.ValidationException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EventoController {

    private final EventoDAO eventoDAO;
    private final PalestranteDAO palestranteDAO;
    private final InscricaoDAO inscricaoDAO;

    public EventoController(EventoDAO eventoDAO, PalestranteDAO palestranteDAO, InscricaoDAO inscricaoDAO) {
        this.eventoDAO = eventoDAO;
        this.palestranteDAO = palestranteDAO;
        this.inscricaoDAO = inscricaoDAO;
    }

    public ApiResponse list() throws Exception {
        List<Map<String, Object>> items = new ArrayList<>();
        for (Evento evento : eventoDAO.listAll()) {
            items.add(evento.toMap());
        }
        return new ApiResponse(200, JsonUtil.stringify(items));
    }

    public ApiResponse get(int id) throws Exception {
        Evento evento = eventoDAO.findById(id);
        if (evento == null) {
            return error(404, "Evento nao encontrado");
        }
        return new ApiResponse(200, JsonUtil.stringify(evento.toMap()));
    }

    public ApiResponse create(String body) throws Exception {
        Evento evento = fromPayload(JsonUtil.parseObject(body));
        return new ApiResponse(201, JsonUtil.stringify(eventoDAO.create(evento).toMap()));
    }

    public ApiResponse update(int id, String body) throws Exception {
        Evento evento = fromPayload(JsonUtil.parseObject(body));
        Evento updated = eventoDAO.update(id, evento);
        if (updated == null) {
            return error(404, "Evento nao encontrado");
        }
        return new ApiResponse(200, JsonUtil.stringify(updated.toMap()));
    }

    public ApiResponse delete(int id) throws Exception {
        if (!palestranteDAO.listByEvento(id).isEmpty()) {
            return error(409, "Nao e possivel excluir evento com palestrantes ativos");
        }
        if (inscricaoDAO.hasEvento(id)) {
            return error(409, "Nao e possivel excluir evento com inscricoes ativas");
        }
        if (!eventoDAO.delete(id)) {
            return error(404, "Evento nao encontrado");
        }
        return new ApiResponse(200, JsonUtil.stringify(Map.of("mensagem", "Evento excluido")));
    }

    private Evento fromPayload(Map<String, String> payload) throws ValidationException {
        String nome = JsonUtil.getString(payload, "nome");
        String descricao = JsonUtil.getString(payload, "descricao");
        String dataEvento = JsonUtil.getString(payload, "dataEvento");
        float preco = JsonUtil.getFloat(payload, "preco", 0F);
        String tags = JsonUtil.getString(payload, "tags");

        if (nome.isBlank()) {
            throw new ValidationException("Nome do evento e obrigatorio");
        }
        if (dataEvento.isBlank()) {
            throw new ValidationException("Data do evento e obrigatoria");
        }
        if (preco < 0) {
            throw new ValidationException("Preco invalido");
        }

        return new Evento(nome, descricao, dataEvento, preco, tags);
    }

    private ApiResponse error(int status, String message) {
        return new ApiResponse(status, JsonUtil.stringify(Map.of("erro", message)));
    }
}
