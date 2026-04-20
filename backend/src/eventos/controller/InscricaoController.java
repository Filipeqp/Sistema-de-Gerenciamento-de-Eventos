package eventos.controller;

import eventos.dao.EventoDAO;
import eventos.dao.InscricaoDAO;
import eventos.dao.ParticipanteDAO;
import eventos.model.Inscricao;
import eventos.util.ApiResponse;
import eventos.util.JsonUtil;
import eventos.util.ValidationException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class InscricaoController {

    private final InscricaoDAO inscricaoDAO;
    private final EventoDAO eventoDAO;
    private final ParticipanteDAO participanteDAO;

    public InscricaoController(InscricaoDAO inscricaoDAO, EventoDAO eventoDAO, ParticipanteDAO participanteDAO) {
        this.inscricaoDAO = inscricaoDAO;
        this.eventoDAO = eventoDAO;
        this.participanteDAO = participanteDAO;
    }

    public ApiResponse list(String query) throws Exception {
        List<Map<String, Object>> items = new ArrayList<>();
        if (query != null && query.startsWith("idEvento=")) {
            int idEvento = Integer.parseInt(query.substring("idEvento=".length()));
            for (Inscricao inscricao : inscricaoDAO.listByEvento(idEvento)) {
                items.add(inscricao.toMap());
            }
        } else if (query != null && query.startsWith("idParticipante=")) {
            int idParticipante = Integer.parseInt(query.substring("idParticipante=".length()));
            for (Inscricao inscricao : inscricaoDAO.listByParticipante(idParticipante)) {
                items.add(inscricao.toMap());
            }
        } else {
            for (Inscricao inscricao : inscricaoDAO.listAll()) {
                items.add(inscricao.toMap());
            }
        }
        return new ApiResponse(200, JsonUtil.stringify(items));
    }

    public ApiResponse get(int id) throws Exception {
        Inscricao inscricao = inscricaoDAO.findById(id);
        if (inscricao == null) {
            return error(404, "Inscricao nao encontrada");
        }
        return new ApiResponse(200, JsonUtil.stringify(inscricao.toMap()));
    }

    public ApiResponse create(String body) throws Exception {
        Inscricao inscricao = fromPayload(JsonUtil.parseObject(body), null);
        return new ApiResponse(201, JsonUtil.stringify(inscricaoDAO.create(inscricao).toMap()));
    }

    public ApiResponse update(int id, String body) throws Exception {
        Inscricao inscricao = fromPayload(JsonUtil.parseObject(body), id);
        Inscricao updated = inscricaoDAO.update(id, inscricao);
        if (updated == null) {
            return error(404, "Inscricao nao encontrada");
        }
        return new ApiResponse(200, JsonUtil.stringify(updated.toMap()));
    }

    public ApiResponse delete(int id) throws Exception {
        if (!inscricaoDAO.delete(id)) {
            return error(404, "Inscricao nao encontrada");
        }
        return new ApiResponse(200, JsonUtil.stringify(Map.of("mensagem", "Inscricao cancelada")));
    }

    private Inscricao fromPayload(Map<String, String> payload, Integer ignoredId) throws Exception {
        int idEvento = JsonUtil.getInt(payload, "idEvento", -1);
        int idParticipante = JsonUtil.getInt(payload, "idParticipante", -1);
        String dataInscricao = JsonUtil.getString(payload, "dataInscricao");

        if (idEvento <= 0 || eventoDAO.findById(idEvento) == null) {
            throw new ValidationException("Evento informado nao existe");
        }
        if (idParticipante <= 0 || participanteDAO.findById(idParticipante) == null) {
            throw new ValidationException("Participante informado nao existe");
        }
        if (dataInscricao.isBlank()) {
            throw new ValidationException("Data da inscricao e obrigatoria");
        }
        if (inscricaoDAO.existsByEventoAndParticipante(idEvento, idParticipante, ignoredId)) {
            throw new ValidationException("Participante ja inscrito neste evento");
        }

        return new Inscricao(idEvento, idParticipante, dataInscricao);
    }

    private ApiResponse error(int status, String message) {
        return new ApiResponse(status, JsonUtil.stringify(Map.of("erro", message)));
    }
}
