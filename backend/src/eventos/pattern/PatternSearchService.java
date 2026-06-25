package eventos.pattern;

import eventos.dao.EventoDAO;
import eventos.dao.InscricaoDAO;
import eventos.dao.PalestranteDAO;
import eventos.dao.ParticipanteDAO;
import eventos.model.Evento;
import eventos.model.Inscricao;
import eventos.model.Palestrante;
import eventos.model.Participante;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PatternSearchService {

    private final EventoDAO eventoDAO;
    private final PalestranteDAO palestranteDAO;
    private final ParticipanteDAO participanteDAO;
    private final InscricaoDAO inscricaoDAO;

    public PatternSearchService(
            EventoDAO eventoDAO,
            PalestranteDAO palestranteDAO,
            ParticipanteDAO participanteDAO,
            InscricaoDAO inscricaoDAO) {
        this.eventoDAO = eventoDAO;
        this.palestranteDAO = palestranteDAO;
        this.participanteDAO = participanteDAO;
        this.inscricaoDAO = inscricaoDAO;
    }

    public Map<String, Object> search(String entidade, String padrao, PatternMatchAlgorithm algoritmo) throws Exception {
        String pattern = normalize(padrao);
        String entity = normalize(entidade);
        List<Map<String, Object>> resultados = new ArrayList<>();

        // Percorre apenas as entidades selecionadas e transforma cada registro em texto pesquisavel.
        if (accepts(entity, "eventos")) {
            for (Evento evento : eventoDAO.listAll()) {
                addIfMatched(resultados, "eventos", evento.getId(), evento.getNome(),
                        describeEvento(evento), pattern, algoritmo);
            }
        }
        if (accepts(entity, "palestrantes")) {
            for (Palestrante palestrante : palestranteDAO.listAll()) {
                addIfMatched(resultados, "palestrantes", palestrante.getId(), palestrante.getNome(),
                        describePalestrante(palestrante), pattern, algoritmo);
            }
        }
        if (accepts(entity, "participantes")) {
            for (Participante participante : participanteDAO.listAll()) {
                addIfMatched(resultados, "participantes", participante.getId(), participante.getNome(),
                        describeParticipante(participante), pattern, algoritmo);
            }
        }
        if (accepts(entity, "inscricoes")) {
            for (Inscricao inscricao : inscricaoDAO.listAll()) {
                addIfMatched(resultados, "inscricoes", inscricao.getId(), "Inscricao #" + inscricao.getId(),
                        describeInscricao(inscricao), pattern, algoritmo);
            }
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("algoritmo", algoritmo.name());
        response.put("entidade", entity.isBlank() ? "todos" : entity);
        response.put("padrao", padrao);
        response.put("quantidade", resultados.size());
        response.put("resultados", resultados);
        return response;
    }

    private void addIfMatched(
            List<Map<String, Object>> resultados,
            String entidade,
            int id,
            String titulo,
            String textoOriginal,
            String pattern,
            PatternMatchAlgorithm algoritmo) {
        String searchable = normalize(textoOriginal);
        // O algoritmo devolve todas as posicoes em que o padrao aparece dentro do texto do registro.
        // Essa lista e usada para provar todas as ocorrencias encontradas por BM ou KMP.
        List<Integer> posicoes = algoritmo.search(searchable, pattern);
        if (posicoes.isEmpty()) {
            return;
        }

        Map<String, Object> item = new LinkedHashMap<>();
        item.put("entidade", entidade);
        item.put("id", id);
        item.put("titulo", titulo);
        // totalOcorrencias e o numero de vezes que o padrao apareceu no mesmo registro.
        item.put("totalOcorrencias", posicoes.size());
        // Posicoes guarda todos os indices onde o padrao apareceu dentro do texto pesquisavel.
        item.put("posicoes", posicoes);
        // O trecho e um recorte do texto original ao redor da primeira ocorrencia para facilitar a exibicao.
        // As demais ocorrencias continuam registradas em "posicoes"; o trecho mostra contexto visual da primeira.
        item.put("trecho", buildSnippet(textoOriginal, posicoes.get(0), pattern.length()));
        resultados.add(item);
    }

    private boolean accepts(String requested, String current) {
        return requested.isBlank()
                || "todos".equalsIgnoreCase(requested)
                || "all".equalsIgnoreCase(requested)
                || current.equalsIgnoreCase(requested);
    }

    private String describeEvento(Evento evento) {
        return String.join(" | ",
                fallback(evento.getNome()),
                fallback(evento.getDescricao()),
                fallback(evento.getDataEvento()),
                fallback(evento.getTags()));
    }

    private String describePalestrante(Palestrante palestrante) {
        return String.join(" | ",
                fallback(palestrante.getNome()),
                fallback(palestrante.getMiniCurriculo()),
                fallback(palestrante.getEspecialidades()),
                "evento " + palestrante.getIdEvento());
    }

    private String describeParticipante(Participante participante) {
        // Junta os campos relevantes em uma unica string para permitir busca textual no registro.
        return String.join(" | ",
                fallback(participante.getNome()),
                fallback(participante.getEmail()),
                fallback(participante.getInteresses()));
    }

    private String describeInscricao(Inscricao inscricao) {
        return "evento " + inscricao.getIdEvento()
                + " | participante " + inscricao.getIdParticipante()
                + " | " + fallback(inscricao.getDataInscricao());
    }

    private String buildSnippet(String text, int start, int patternLength) {
        if (text == null || text.isBlank()) {
            return "";
        }
        // Mantem contexto antes e depois do match para mostrar ao usuario onde o padrao foi encontrado.
        // Aqui usamos 20 caracteres antes e 20 depois da primeira ocorrencia.
        int from = Math.max(0, start - 20);
        int to = Math.min(text.length(), start + patternLength + 20);
        return text.substring(from, to).trim();
    }

    private String normalize(String value) {
        return fallback(value).toLowerCase(Locale.ROOT);
    }

    private String fallback(String value) {
        return value == null ? "" : value;
    }
}
