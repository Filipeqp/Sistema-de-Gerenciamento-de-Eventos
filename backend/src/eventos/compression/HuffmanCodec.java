package eventos.compression;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.PriorityQueue;

public class HuffmanCodec implements CompressionCodec {

    @Override
    public String name() {
        return "huffman";
    }

    @Override
    public byte[] compress(byte[] input) throws Exception {
        int[] frequencies = new int[256];
        for (byte value : input) {
            frequencies[value & 0xFF]++;
        }

        Node root = buildTree(frequencies);
        String[] codes = new String[256];
        if (root != null) {
            buildCodes(root, "", codes);
        }

        ByteArrayOutputStream payload = new ByteArrayOutputStream();
        int currentByte = 0;
        int bitCount = 0;
        int validBitsInLastByte = 0;

        // Escreve a sequencia compactada bit a bit usando os codigos gerados pela arvore de Huffman.
        for (byte value : input) {
            String code = codes[value & 0xFF];
            for (int i = 0; i < code.length(); i++) {
                currentByte = (currentByte << 1) | (code.charAt(i) == '1' ? 1 : 0);
                bitCount++;
                if (bitCount == 8) {
                    payload.write(currentByte);
                    currentByte = 0;
                    bitCount = 0;
                    validBitsInLastByte = 8;
                }
            }
        }

        if (bitCount > 0) {
            currentByte <<= (8 - bitCount);
            payload.write(currentByte);
            validBitsInLastByte = bitCount;
        }

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (DataOutputStream data = new DataOutputStream(output)) {
            data.writeUTF("HUFF");
            data.writeInt(input.length);
            data.writeInt(validBitsInLastByte);
            for (int frequency : frequencies) {
                data.writeInt(frequency);
            }
            data.write(payload.toByteArray());
        }
        return output.toByteArray();
    }

    private Node buildTree(int[] frequencies) {
        PriorityQueue<Node> queue = new PriorityQueue<>();
        for (int i = 0; i < frequencies.length; i++) {
            if (frequencies[i] > 0) {
                queue.add(new Node((byte) i, frequencies[i], null, null));
            }
        }

        if (queue.isEmpty()) {
            return null;
        }

        while (queue.size() > 1) {
            Node left = queue.poll();
            Node right = queue.poll();
            queue.add(new Node((byte) 0, left.frequency + right.frequency, left, right));
        }
        return queue.poll();
    }

    private void buildCodes(Node node, String prefix, String[] codes) {
        if (node.isLeaf()) {
            // Caso exista apenas um byte diferente no arquivo, usa "0" para ainda haver um codigo valido.
            codes[node.value & 0xFF] = prefix.isEmpty() ? "0" : prefix;
            return;
        }
        buildCodes(node.left, prefix + "0", codes);
        buildCodes(node.right, prefix + "1", codes);
    }

    private static class Node implements Comparable<Node> {
        private final byte value;
        private final int frequency;
        private final Node left;
        private final Node right;

        private Node(byte value, int frequency, Node left, Node right) {
            this.value = value;
            this.frequency = frequency;
            this.left = left;
            this.right = right;
        }

        private boolean isLeaf() {
            return left == null && right == null;
        }

        @Override
        public int compareTo(Node other) {
            return Integer.compare(this.frequency, other.frequency);
        }
    }
}
