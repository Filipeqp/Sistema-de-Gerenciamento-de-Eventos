package eventos.controller;

import eventos.security.AuthService;
import eventos.util.ApiResponse;
import eventos.util.JsonUtil;
import eventos.util.ValidationException;

import java.util.Map;

public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    public ApiResponse login(String body) throws Exception {
        Map<String, String> payload = JsonUtil.parseObject(body);
        String email = JsonUtil.getString(payload, "email");
        String senha = JsonUtil.getString(payload, "senha");

        if (email.isBlank() || senha.isBlank()) {
            throw new ValidationException("Email e senha sao obrigatorios");
        }

        // Encaminha a autenticacao para o servico e devolve a sessao usada pelo frontend.
        Map<String, Object> session = authService.login(email, senha);
        if (session == null) {
            return new ApiResponse(401, JsonUtil.stringify(Map.of("erro", "Credenciais invalidas")));
        }

        return new ApiResponse(200, JsonUtil.stringify(session));
    }

    public ApiResponse status() throws Exception {
        // Informa ao frontend se ja existe pelo menos uma conta com senha cadastrada.
        return new ApiResponse(200, JsonUtil.stringify(Map.of(
                "possuiUsuarios", authService.hasAnyUserWithPassword())));
    }

    public ApiResponse registerFirstAccess(String body) throws Exception {
        Map<String, String> payload = JsonUtil.parseObject(body);
        String nome = validateNome(payload);
        String email = validateEmail(payload);
        String interesses = JsonUtil.getString(payload, "interesses");
        String senha = validateSenha(payload);

        Map<String, Object> session = authService.registerFirstAccess(nome, email, interesses, senha);
        if (session == null) {
            return new ApiResponse(409, JsonUtil.stringify(Map.of(
                    "erro", "Primeiro acesso ja foi configurado. Use a tela de login.")));
        }

        return new ApiResponse(201, JsonUtil.stringify(session));
    }

    public ApiResponse register(String body) throws Exception {
        Map<String, String> payload = JsonUtil.parseObject(body);
        String nome = validateNome(payload);
        String email = validateEmail(payload);
        String interesses = JsonUtil.getString(payload, "interesses");
        String senha = validateSenha(payload);

        // Cadastra uma nova conta diretamente da tela inicial e ja retorna a sessao autenticada.
        Map<String, Object> session = authService.registerUser(nome, email, interesses, senha);
        if (session == null) {
            return new ApiResponse(409, JsonUtil.stringify(Map.of(
                    "erro", "Ja existe um participante cadastrado com esse email")));
        }

        return new ApiResponse(201, JsonUtil.stringify(session));
    }

    private String validateNome(Map<String, String> payload) throws ValidationException {
        String nome = JsonUtil.getString(payload, "nome");
        if (nome.isBlank()) {
            throw new ValidationException("Nome e obrigatorio");
        }
        return nome;
    }

    private String validateEmail(Map<String, String> payload) throws ValidationException {
        String email = JsonUtil.getString(payload, "email");
        if (email.isBlank() || !email.contains("@")) {
            throw new ValidationException("Email invalido");
        }
        return email;
    }

    private String validateSenha(Map<String, String> payload) throws ValidationException {
        String senha = JsonUtil.getString(payload, "senha");
        if (senha.isBlank() || senha.length() < 4) {
            throw new ValidationException("Senha deve ter pelo menos 4 caracteres");
        }
        return senha;
    }
}
