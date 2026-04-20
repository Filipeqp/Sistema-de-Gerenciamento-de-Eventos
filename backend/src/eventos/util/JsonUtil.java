package eventos.util;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public final class JsonUtil {

    private JsonUtil() {
    }

    public static Map<String, String> parseObject(String json) throws ValidationException {
        String source = json == null ? "" : json.trim();
        if (source.isEmpty()) {
            return new LinkedHashMap<>();
        }
        if (!source.startsWith("{") || !source.endsWith("}")) {
            throw new ValidationException("JSON invalido");
        }

        Map<String, String> values = new LinkedHashMap<>();
        int index = 1;
        while (index < source.length() - 1) {
            index = skipWhitespace(source, index);
            if (index >= source.length() - 1) {
                break;
            }
            if (source.charAt(index) == ',') {
                index++;
                continue;
            }
            if (source.charAt(index) != '"') {
                throw new ValidationException("JSON invalido");
            }

            int keyEnd = findStringEnd(source, index + 1);
            String key = unescape(source.substring(index + 1, keyEnd));
            index = skipWhitespace(source, keyEnd + 1);
            if (source.charAt(index) != ':') {
                throw new ValidationException("JSON invalido");
            }

            index = skipWhitespace(source, index + 1);
            ParsedValue parsed = parseValue(source, index);
            values.put(key, parsed.value);
            index = parsed.nextIndex;
        }

        return values;
    }

    public static String stringify(Object value) {
        if (value == null) {
            return "null";
        }
        if (value instanceof String) {
            return "\"" + escape((String) value) + "\"";
        }
        if (value instanceof Number || value instanceof Boolean) {
            return value.toString();
        }
        if (value instanceof Map<?, ?>) {
            StringBuilder builder = new StringBuilder("{");
            boolean first = true;
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) value).entrySet()) {
                if (!first) {
                    builder.append(",");
                }
                first = false;
                builder.append("\"").append(escape(String.valueOf(entry.getKey()))).append("\":");
                builder.append(stringify(entry.getValue()));
            }
            return builder.append("}").toString();
        }
        if (value instanceof Collection<?>) {
            StringBuilder builder = new StringBuilder("[");
            boolean first = true;
            for (Object item : (Collection<?>) value) {
                if (!first) {
                    builder.append(",");
                }
                first = false;
                builder.append(stringify(item));
            }
            return builder.append("]").toString();
        }
        return "\"" + escape(String.valueOf(value)) + "\"";
    }

    public static String getString(Map<String, String> map, String key) {
        String value = map.get(key);
        return value == null ? "" : value.trim();
    }

    public static int getInt(Map<String, String> map, String key, int defaultValue) throws ValidationException {
        String value = map.get(key);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            throw new ValidationException("Campo " + key + " invalido");
        }
    }

    public static float getFloat(Map<String, String> map, String key, float defaultValue) throws ValidationException {
        String value = map.get(key);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            return Float.parseFloat(value.trim());
        } catch (NumberFormatException e) {
            throw new ValidationException("Campo " + key + " invalido");
        }
    }

    private static ParsedValue parseValue(String source, int index) throws ValidationException {
        if (index >= source.length()) {
            throw new ValidationException("JSON invalido");
        }

        char ch = source.charAt(index);
        if (ch == '"') {
            int end = findStringEnd(source, index + 1);
            return new ParsedValue(unescape(source.substring(index + 1, end)), end + 1);
        }

        int end = index;
        while (end < source.length()) {
            char current = source.charAt(end);
            if (current == ',' || current == '}') {
                break;
            }
            end++;
        }
        return new ParsedValue(source.substring(index, end).trim(), end);
    }

    private static int findStringEnd(String source, int start) throws ValidationException {
        boolean escaped = false;
        for (int i = start; i < source.length(); i++) {
            char ch = source.charAt(i);
            if (escaped) {
                escaped = false;
                continue;
            }
            if (ch == '\\') {
                escaped = true;
                continue;
            }
            if (ch == '"') {
                return i;
            }
        }
        throw new ValidationException("JSON invalido");
    }

    private static int skipWhitespace(String source, int index) {
        while (index < source.length() && Character.isWhitespace(source.charAt(index))) {
            index++;
        }
        return index;
    }

    private static String escape(String value) {
        return value
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t");
    }

    private static String unescape(String value) {
        StringBuilder builder = new StringBuilder();
        boolean escaped = false;
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            if (escaped) {
                switch (ch) {
                    case 'n':
                        builder.append('\n');
                        break;
                    case 'r':
                        builder.append('\r');
                        break;
                    case 't':
                        builder.append('\t');
                        break;
                    case '"':
                        builder.append('"');
                        break;
                    case '\\':
                        builder.append('\\');
                        break;
                    default:
                        builder.append(ch);
                        break;
                }
                escaped = false;
            } else if (ch == '\\') {
                escaped = true;
            } else {
                builder.append(ch);
            }
        }
        return builder.toString();
    }

    private static class ParsedValue {
        private final String value;
        private final int nextIndex;

        private ParsedValue(String value, int nextIndex) {
            this.value = value;
            this.nextIndex = nextIndex;
        }
    }
}
