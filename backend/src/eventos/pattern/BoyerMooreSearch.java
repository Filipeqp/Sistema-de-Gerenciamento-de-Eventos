package eventos.pattern;

import java.util.ArrayList;
import java.util.List;

public class BoyerMooreSearch implements PatternMatchAlgorithm {

    private static final int ALPHABET_SIZE = 65536;

    @Override
    public String name() {
        return "bm";
    }

    @Override
    public List<Integer> search(String text, String pattern) {
        List<Integer> matches = new ArrayList<>();
        if (text == null || pattern == null || pattern.isEmpty() || text.length() < pattern.length()) {
            return matches;
        }

        // A tabela de bad character permite saltar trechos do texto quando ha incompatibilidade.
        int[] badChar = buildBadCharacterTable(pattern);
        int shift = 0;

        while (shift <= text.length() - pattern.length()) {
            int j = pattern.length() - 1;

            while (j >= 0 && pattern.charAt(j) == text.charAt(shift + j)) {
                j--;
            }

            if (j < 0) {
                // Quando j fica negativo, significa que o padrao inteiro casou nessa posicao.
                matches.add(shift);
                shift += shift + pattern.length() < text.length()
                        ? pattern.length() - badChar[text.charAt(shift + pattern.length())]
                        : 1;
            } else {
                int offset = j - badChar[text.charAt(shift + j)];
                shift += Math.max(1, offset);
            }
        }

        return matches;
    }

    private int[] buildBadCharacterTable(String pattern) {
        int[] table = new int[ALPHABET_SIZE];
        for (int i = 0; i < table.length; i++) {
            table[i] = -1;
        }
        // Guarda a ultima posicao de cada caractere no padrao para orientar os saltos do algoritmo.
        for (int i = 0; i < pattern.length(); i++) {
            table[pattern.charAt(i)] = i;
        }
        return table;
    }
}
