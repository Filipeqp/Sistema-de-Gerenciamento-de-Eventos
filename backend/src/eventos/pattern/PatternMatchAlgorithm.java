package eventos.pattern;

import java.util.List;

public interface PatternMatchAlgorithm {
    String name();
    List<Integer> search(String text, String pattern);
}
