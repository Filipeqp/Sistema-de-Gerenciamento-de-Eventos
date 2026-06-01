package eventos.compression;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.HashMap;
import java.util.Map;

public class LzwCodec implements CompressionCodec {

    @Override
    public String name() {
        return "lzw";
    }

    @Override
    public byte[] compress(byte[] input) throws Exception {
        Map<String, Integer> dictionary = new HashMap<>();
        for (int i = 0; i < 256; i++) {
            dictionary.put(String.valueOf((char) i), i);
        }

        int nextCode = 256;
        String current = "";
        ByteArrayOutputStream codes = new ByteArrayOutputStream();
        DataOutputStream codeWriter = new DataOutputStream(codes);

        // O LZW guarda codigos de sequencias repetidas; aqui cada codigo e persistido em 16 bits.
        for (byte value : input) {
            char symbol = (char) (value & 0xFF);
            String combined = current + symbol;
            if (dictionary.containsKey(combined)) {
                current = combined;
            } else {
                codeWriter.writeShort(dictionary.get(current));
                if (nextCode <= 0xFFFF) {
                    dictionary.put(combined, nextCode++);
                }
                current = String.valueOf(symbol);
            }
        }

        if (!current.isEmpty()) {
            codeWriter.writeShort(dictionary.get(current));
        }
        codeWriter.flush();

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (DataOutputStream data = new DataOutputStream(output)) {
            data.writeUTF("LZW");
            data.writeInt(input.length);
            data.writeInt(nextCode);
            data.write(codes.toByteArray());
        }
        return output.toByteArray();
    }
}
