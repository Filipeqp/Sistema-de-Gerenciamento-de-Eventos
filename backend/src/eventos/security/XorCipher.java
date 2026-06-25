package eventos.security;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public final class XorCipher {

    // Chave fixa usada para transformar a senha. O mesmo XOR criptografa e descriptografa.
    private static final byte[] KEY = "GestEvent-AED3-XOR".getBytes(StandardCharsets.UTF_8);

    private XorCipher() {
    }

    public static String encrypt(String plainText) {
        byte[] input = normalize(plainText).getBytes(StandardCharsets.UTF_8);
        // Aplica XOR e converte o resultado para Base64 para armazenar a senha de forma legivel e persistente.
        byte[] output = apply(input);
        return Base64.getEncoder().encodeToString(output);
    }

    public static String decrypt(String cipherText) {
        String source = normalize(cipherText);
        if (source.isBlank()) {
            return "";
        }
        // O arquivo armazena Base64; primeiro voltamos para bytes e depois aplicamos XOR novamente.
        byte[] decoded = Base64.getDecoder().decode(source);
        return new String(apply(decoded), StandardCharsets.UTF_8);
    }

    public static boolean matches(String plainText, String encrypted) {
        // Compara a senha digitada com a senha armazenada ja criptografada no arquivo.
        return normalize(plainText).equals(decrypt(encrypted));
    }

    private static byte[] apply(byte[] input) {
        byte[] output = new byte[input.length];
        // O XOR usa uma chave fixa e percorre os bytes da senha um a um.
        for (int i = 0; i < input.length; i++) {
            output[i] = (byte) (input[i] ^ KEY[i % KEY.length]);
        }
        return output;
    }

    private static String normalize(String value) {
        return value == null ? "" : value;
    }
}
