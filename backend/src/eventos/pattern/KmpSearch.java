package eventos.pattern;

import java.util.ArrayList;
import java.util.List;

public class KmpSearch implements PatternMatchAlgorithm {

    @Override
    public String name() {
        return "kmp";
    }

    @Override
    public List<Integer> search(String text, String pattern) {
        List<Integer> matches = new ArrayList<>();
        if (text == null || pattern == null || pattern.isEmpty() || text.length() < pattern.length()) {
            return matches;
        }

        // A tabela LPS permite reaproveitar comparacoes ja feitas quando ocorre desencontro.
        int[] lps = buildLps(pattern);
        int i = 0;
        int j = 0;

        while (i < text.length()) {
            if (text.charAt(i) == pattern.charAt(j)) {
                i++;
                j++;
                if (j == pattern.length()) {
                    // Registra a posicao inicial exata onde o padrao foi encontrado no texto.
                    matches.add(i - j);
                    j = lps[j - 1];
                }
            } else if (j > 0) {
                j = lps[j - 1];
            } else {
                i++;
            }
        }

        return matches;
    }

    private int[] buildLps(String pattern) {
        int[] lps = new int[pattern.length()];
        int len = 0;
        int i = 1;

        // Pre-processa o padrao para saber quanto podemos avancar sem recomecar do zero.
        while (i < pattern.length()) {
            if (pattern.charAt(i) == pattern.charAt(len)) {
                lps[i++] = ++len;
            } else if (len > 0) {
                len = lps[len - 1];
            } else {
                lps[i++] = 0;
            }
        }

        return lps;
    }
}
