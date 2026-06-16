package eventos.controller;

import eventos.pattern.BoyerMooreSearch;
import eventos.pattern.KmpSearch;
import eventos.pattern.PatternMatchAlgorithm;
import eventos.pattern.PatternSearchService;
import eventos.util.ApiResponse;
import eventos.util.JsonUtil;
import eventos.util.QueryParams;
import eventos.util.ValidationException;

import java.util.Map;

public class PesquisaController {

    private final PatternSearchService patternSearchService;

    public PesquisaController(PatternSearchService patternSearchService) {
        this.patternSearchService = patternSearchService;
    }

    public ApiResponse search(String query) throws Exception {
        Map<String, String> params = QueryParams.parse(query);
        String algoritmo = params.getOrDefault("algoritmo", "bm");
        String entidade = params.getOrDefault("entidade", "todos");
        String padrao = params.getOrDefault("padrao", "");

        if (padrao.isBlank()) {
            throw new ValidationException("Informe um padrao para pesquisar");
        }

        // Resolve dinamicamente o algoritmo pedido pela interface e delega a busca ao servico central.
        PatternMatchAlgorithm implementation = resolve(algoritmo);
        return new ApiResponse(200, JsonUtil.stringify(patternSearchService.search(entidade, padrao, implementation)));
    }

    private PatternMatchAlgorithm resolve(String algoritmo) throws ValidationException {
        if ("bm".equalsIgnoreCase(algoritmo) || "boyermoore".equalsIgnoreCase(algoritmo)) {
            return new BoyerMooreSearch();
        }
        if ("kmp".equalsIgnoreCase(algoritmo)) {
            return new KmpSearch();
        }
        throw new ValidationException("Algoritmo invalido. Use bm ou kmp");
    }
}
