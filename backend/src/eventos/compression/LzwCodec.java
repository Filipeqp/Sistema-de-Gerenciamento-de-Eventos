package eventos.compression;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
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
        // O dicionario inicia com todos os bytes simples; depois ganha sequencias encontradas no arquivo.
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

    @Override
    public byte[] decompress(byte[] input) throws Exception {
        try (DataInputStream data = new DataInputStream(new ByteArrayInputStream(input))) {
            String magic = data.readUTF();
            if (!"LZW".equals(magic)) {
                throw new IllegalArgumentException("Arquivo LZW invalido");
            }

            int originalLength = data.readInt();
            data.readInt(); // Mantido no arquivo para auditoria do tamanho do dicionario usado na compactacao.
            Map<Integer, String> dictionary = new HashMap<>();
            // Na descompactacao, o dicionario e reconstruido na mesma ordem da compactacao.
            for (int i = 0; i < 256; i++) {
                dictionary.put(i, String.valueOf((char) i));
            }

            if (data.available() <= 0 || originalLength == 0) {
                return new byte[0];
            }

            int nextCode = 256;
            int previousCode = data.readUnsignedShort();
            String previous = dictionary.get(previousCode);
            ByteArrayOutputStream output = new ByteArrayOutputStream(originalLength);
            writeStringBytes(output, previous, originalLength);

            while (data.available() > 0 && output.size() < originalLength) {
                int code = data.readUnsignedShort();
                String entry = dictionary.get(code);
                if (entry == null && code == nextCode) {
                    entry = previous + previous.charAt(0);
                }
                if (entry == null) {
                    throw new IllegalArgumentException("Codigo LZW invalido: " + code);
                }

                writeStringBytes(output, entry, originalLength);
                if (nextCode <= 0xFFFF) {
                    // Cada nova sequencia recuperada entra no dicionario para decodificar os proximos codigos.
                    dictionary.put(nextCode++, previous + entry.charAt(0));
                }
                previous = entry;
            }
            return output.toByteArray();
        }
    }

    private void writeStringBytes(ByteArrayOutputStream output, String value, int limit) {
        for (int i = 0; i < value.length() && output.size() < limit; i++) {
            output.write((byte) value.charAt(i));
        }
    }
}
