package eventos.controller;

import eventos.dao.InscricaoDAO;
import eventos.dao.ParticipanteDAO;
import eventos.model.Participante;
import eventos.util.ApiResponse;
import eventos.util.JsonUtil;
import eventos.util.ValidationException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ParticipanteController {

    private final ParticipanteDAO participanteDAO;
    private final InscricaoDAO inscricaoDAO;

    public ParticipanteController(ParticipanteDAO participanteDAO, InscricaoDAO inscricaoDAO) {
        this.participanteDAO = participanteDAO;
        this.inscricaoDAO = inscricaoDAO;
    }

    public ApiResponse list() throws Exception {
        List<Map<String, Object>> items = new ArrayList<>();
        for (Participante participante : participanteDAO.listAll()) {
            items.add(participante.toMap());
        }
        return new ApiResponse(200, JsonUtil.stringify(items));
    }

    public ApiResponse get(int id) throws Exception {
        Participante participante = participanteDAO.findById(id);
        if (participante == null) {
            return error(404, "Participante nao encontrado");
        }
        return new ApiResponse(200, JsonUtil.stringify(participante.toMap()));
    }

    public ApiResponse create(String body) throws Exception {
        Participante participante = fromPayload(JsonUtil.parseObject(body));
        return new ApiResponse(201, JsonUtil.stringify(participanteDAO.create(participante).toMap()));
    }

    public ApiResponse update(int id, String body) throws Exception {
        Participante participante = fromPayload(JsonUtil.parseObject(body));
        Participante updated = participanteDAO.update(id, participante);
        if (updated == null) {
            return error(404, "Participante nao encontrado");
        }
        return new ApiResponse(200, JsonUtil.stringify(updated.toMap()));
    }

    public ApiResponse delete(int id) throws Exception {
        if (inscricaoDAO.hasParticipante(id)) {
            return error(409, "Nao e possivel excluir participante com inscricoes ativas");
        }
        if (!participanteDAO.delete(id)) {
            return error(404, "Participante nao encontrado");
        }
        return new ApiResponse(200, JsonUtil.stringify(Map.of("mensagem", "Participante excluido")));
    }

    private Participante fromPayload(Map<String, String> payload) throws ValidationException {
        String nome = JsonUtil.getString(payload, "nome");
        String email = JsonUtil.getString(payload, "email");
        String interesses = JsonUtil.getString(payload, "interesses");

        if (nome.isBlank()) {
            throw new ValidationException("Nome do participante e obrigatorio");
        }
        if (email.isBlank() || !email.contains("@")) {
            throw new ValidationException("Email invalido");
        }

        return new Participante(nome, email, interesses);
    }

    private ApiResponse error(int status, String message) {
        return new ApiResponse(status, JsonUtil.stringify(Map.of("erro", message)));
    }
}
