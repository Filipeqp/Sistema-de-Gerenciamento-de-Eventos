package eventos.util;

import eventos.model.Record;

import java.util.*;

/**
 * Ordenação externa por intercalação (External Merge Sort).
 *
 * Ordena uma lista de registros por um campo string sem carregar tudo na memória
 * de uma vez (simula o comportamento de arquivos temporários).
 *
 * Para a Fase 2, usamos a versão em memória com intercalação real de runs,
 * conforme exigido pelo trabalho.
 *
 * Como usar:
 *   List<Evento> ordenados = ExternalSorter.sort(lista, e -> e.getNome());
 */
public class ExternalSorter {

    // Tamanho de cada "bloco" lido na 1ª passagem (simula buffer de disco)
    private static final int RUN_SIZE = 3;

    /**
     * Ordena a lista usando intercalação de runs.
     *
     * @param items   lista de objetos a ordenar
     * @param keyFn   função que extrai a chave string do objeto
     * @return lista ordenada crescentemente pela chave
     */
    public static <T> List<T> sort(List<T> items, KeyExtractor<T> keyFn) {
        if (items == null || items.size() <= 1) {
            return items == null ? new ArrayList<>() : new ArrayList<>(items);
        }

        // Passagem 1: divide em runs ordenados internamente
        List<List<T>> runs = createRuns(items, keyFn);

        // Passagem 2+: intercala par a par até restar 1 run
        while (runs.size() > 1) {
            runs = mergeRounds(runs, keyFn);
        }

        return runs.get(0);
    }

    /**
     * Ordena decrescentemente.
     */
    public static <T> List<T> sortDesc(List<T> items, KeyExtractor<T> keyFn) {
        List<T> asc = sort(items, keyFn);
        Collections.reverse(asc);
        return asc;
    }

    // ============================================================
    // Passagem 1: criação dos runs
    // ============================================================

    private static <T> List<List<T>> createRuns(List<T> items, KeyExtractor<T> keyFn) {
        List<List<T>> runs = new ArrayList<>();
        int i = 0;
        while (i < items.size()) {
            int end = Math.min(i + RUN_SIZE, items.size());
            List<T> run = new ArrayList<>(items.subList(i, end));
            // Ordena internamente o bloco (simula ordenação interna do buffer)
            run.sort((a, b) -> keyFn.extract(a).compareToIgnoreCase(keyFn.extract(b)));
            runs.add(run);
            i = end;
        }
        return runs;
    }

    // ============================================================
    // Passagens de intercalação
    // ============================================================

    private static <T> List<List<T>> mergeRounds(List<List<T>> runs, KeyExtractor<T> keyFn) {
        List<List<T>> nextRound = new ArrayList<>();
        int i = 0;
        while (i < runs.size()) {
            if (i + 1 < runs.size()) {
                nextRound.add(mergeTwoRuns(runs.get(i), runs.get(i + 1), keyFn));
                i += 2;
            } else {
                nextRound.add(runs.get(i));
                i++;
            }
        }
        return nextRound;
    }

    /**
     * Intercala dois runs ordenados em um único run ordenado.
     * Este é o passo central da ordenação por intercalação.
     */
    private static <T> List<T> mergeTwoRuns(List<T> left, List<T> right, KeyExtractor<T> keyFn) {
        List<T> merged = new ArrayList<>(left.size() + right.size());
        int li = 0, ri = 0;
        while (li < left.size() && ri < right.size()) {
            String lk = keyFn.extract(left.get(li));
            String rk = keyFn.extract(right.get(ri));
            if (lk.compareToIgnoreCase(rk) <= 0) {
                merged.add(left.get(li++));
            } else {
                merged.add(right.get(ri++));
            }
        }
        while (li < left.size()) merged.add(left.get(li++));
        while (ri < right.size()) merged.add(right.get(ri++));
        return merged;
    }

    // ============================================================
    // Interface funcional para extração de chave
    // ============================================================

    @FunctionalInterface
    public interface KeyExtractor<T> {
        String extract(T item);
    }
}