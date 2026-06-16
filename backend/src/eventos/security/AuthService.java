package eventos.security;

import eventos.dao.ParticipanteDAO;
import eventos.model.Participante;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AuthService {

    private final ParticipanteDAO participanteDAO;

    public AuthService(ParticipanteDAO participanteDAO) {
        this.participanteDAO = participanteDAO;
    }

    public Map<String, Object> login(String email, String senha) throws Exception {
        for (Participante participante : participanteDAO.listAll()) {
            if (!participante.getEmail().equalsIgnoreCase(email)) {
                continue;
            }
            // Valida a senha informada contra a senha criptografada do participante.
            if (!XorCipher.matches(senha, participante.getSenha())) {
                return null;
            }
            Map<String, Object> session = new LinkedHashMap<>();
            session.put("id", participante.getId());
            session.put("nome", participante.getNome());
            session.put("email", participante.getEmail());
            session.put("interesses", participante.getInteresses());
            session.put("autenticado", true);
            return session;
        }
        return null;
    }

    public boolean hasAnyUserWithPassword() throws Exception {
        List<Participante> participantes = participanteDAO.listAll();
        for (Participante participante : participantes) {
            if (participante.getSenha() != null && !participante.getSenha().isBlank()) {
                return true;
            }
        }
        return false;
    }

    public boolean emailAlreadyExists(String email) throws Exception {
        List<Participante> participantes = participanteDAO.listAll();
        // Impede contas duplicadas usando o email como identificador de login.
        for (Participante participante : participantes) {
            if (participante.getEmail() != null && participante.getEmail().equalsIgnoreCase(email)) {
                return true;
            }
        }
        return false;
    }

    public Map<String, Object> registerFirstAccess(String nome, String email, String interesses, String senha) throws Exception {
        if (hasAnyUserWithPassword()) {
            return null;
        }

        if (emailAlreadyExists(email)) {
            return null;
        }

        // Cria o primeiro usuario da base e ja persiste a senha no formato criptografado.
        Participante participante = new Participante(
                nome,
                email,
                interesses,
                XorCipher.encrypt(senha));
        Participante created = participanteDAO.create(participante);

        Map<String, Object> session = new LinkedHashMap<>();
        session.put("id", created.getId());
        session.put("nome", created.getNome());
        session.put("email", created.getEmail());
        session.put("interesses", created.getInteresses());
        session.put("autenticado", true);
        session.put("primeiroAcesso", true);
        return session;
    }

    public Map<String, Object> registerUser(String nome, String email, String interesses, String senha) throws Exception {
        boolean firstAccess = !hasAnyUserWithPassword();
        if (emailAlreadyExists(email)) {
            return null;
        }

        // Permite novos cadastros publicos pela tela inicial sem exigir login previo.
        Participante participante = new Participante(
                nome,
                email,
                interesses,
                XorCipher.encrypt(senha));
        Participante created = participanteDAO.create(participante);

        Map<String, Object> session = new LinkedHashMap<>();
        session.put("id", created.getId());
        session.put("nome", created.getNome());
        session.put("email", created.getEmail());
        session.put("interesses", created.getInteresses());
        session.put("autenticado", true);
        session.put("primeiroAcesso", firstAccess);
        return session;
    }
}
