package eventos.util;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

public final class QueryParams {

    private QueryParams() {
    }

    public static Map<String, String> parse(String query) {
        Map<String, String> params = new LinkedHashMap<>();
        if (query == null || query.isBlank()) {
            return params;
        }

        for (String pair : query.split("&")) {
            if (pair.isBlank()) {
                continue;
            }
            int separator = pair.indexOf('=');
            String key = separator >= 0 ? pair.substring(0, separator) : pair;
            String value = separator >= 0 ? pair.substring(separator + 1) : "";
            params.put(decode(key), decode(value));
        }
        return params;
    }

    public static boolean isBPlusOrdering(Map<String, String> params) {
        String value = params.getOrDefault("ordenacao", params.getOrDefault("order", ""));
        return "bplus".equalsIgnoreCase(value) || "arvoreb".equalsIgnoreCase(value);
    }

    public static boolean isDescending(Map<String, String> params) {
        String value = params.getOrDefault("direcao", params.getOrDefault("direction", "asc"));
        return "desc".equalsIgnoreCase(value) || "decrescente".equalsIgnoreCase(value);
    }

    public static int getInt(Map<String, String> params, String key, int defaultValue) {
        String value = params.get(key);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return Integer.parseInt(value.trim());
    }

    private static String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }
}
