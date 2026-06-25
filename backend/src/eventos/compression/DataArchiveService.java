package eventos.compression;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DataArchiveService {
    private final Path storagePath;
    private final Path outputPath;

    public DataArchiveService(String basePath) {
        this.storagePath = Path.of(basePath);
        this.outputPath = storagePath.resolve("compactados");
    }

    public CompressionReport compressDataFiles(CompressionCodec codec) throws Exception {
        List<Path> dataFiles = listDataFiles();
        // Primeiro criamos um pacote unico com todos os .db; depois aplicamos Huffman ou LZW sobre esse pacote.
        byte[] archive = buildSingleArchive(dataFiles);
        byte[] compressed = codec.compress(archive);

        Files.createDirectories(outputPath);
        Path outputFile = outputPath.resolve("dados-" + codec.name() + ".cmp");
        Files.write(outputFile, compressed);

        return new CompressionReport(codec.name(), outputFile, archive.length, compressed.length, dataFiles.size());
    }

    public RestoreReport restoreDataFiles(CompressionCodec codec) throws Exception {
        Path inputFile = outputPath.resolve("dados-" + codec.name() + ".cmp");
        if (!Files.exists(inputFile)) {
            throw new IllegalArgumentException("Arquivo compactado nao encontrado: " + inputFile);
        }

        // Fluxo da apresentacao da Fase IV: ler .cmp, descompactar e regravar os arquivos .db originais.
        byte[] compressed = Files.readAllBytes(inputFile);
        byte[] archive = codec.decompress(compressed);
        int restoredFiles = restoreSingleArchive(archive);
        // Os indices dependem dos dados; apos restaurar os .db, eles sao apagados e reconstruidos pelos DAOs.
        deleteIndexFiles();

        return new RestoreReport(codec.name(), inputFile, restoredFiles, archive.length);
    }

    private List<Path> listDataFiles() throws Exception {
        Path dataPath = storagePath.resolve("dados");
        if (!Files.exists(dataPath)) {
            return List.of();
        }

        try (Stream<Path> stream = Files.walk(dataPath)) {
            return stream
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(".db"))
                    .sorted(Comparator.comparing(path -> dataPath.relativize(path).toString()))
                    .collect(Collectors.toList());
        }
    }

    private byte[] buildSingleArchive(List<Path> dataFiles) throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (DataOutputStream data = new DataOutputStream(output)) {
            data.writeUTF("GESTEVENT-DATA-ARCHIVE");
            data.writeInt(dataFiles.size());

            for (Path file : dataFiles) {
                byte[] bytes = Files.readAllBytes(file);
                Path relativePath = storagePath.resolve("dados").relativize(file);

                // Cada entrada guarda caminho relativo, tamanho e conteudo; assim o pacote unico funciona como backup completo.
                data.writeUTF(relativePath.toString().replace('\\', '/'));
                data.writeLong(bytes.length);
                data.write(bytes);
            }
        }
        return output.toByteArray();
    }

    private int restoreSingleArchive(byte[] archive) throws Exception {
        Path dataPath = storagePath.resolve("dados");
        Files.createDirectories(dataPath);

        try (DataInputStream data = new DataInputStream(new ByteArrayInputStream(archive))) {
            String magic = data.readUTF();
            if (!"GESTEVENT-DATA-ARCHIVE".equals(magic)) {
                throw new IllegalArgumentException("Pacote de dados invalido");
            }

            int count = data.readInt();
            for (int i = 0; i < count; i++) {
                String relative = data.readUTF();
                long size = data.readLong();
                if (size < 0 || size > Integer.MAX_VALUE) {
                    throw new IllegalArgumentException("Tamanho invalido no pacote: " + relative);
                }

                Path target = dataPath.resolve(relative).normalize();
                if (!target.startsWith(dataPath.normalize())) {
                    throw new IllegalArgumentException("Caminho invalido no pacote: " + relative);
                }

                byte[] bytes = new byte[(int) size];
                data.readFully(bytes);
                Files.createDirectories(target.getParent());
                // Aqui acontece a restauracao fisica: o .db salvo no backup volta para a pasta de dados.
                Files.write(target, bytes);
            }
            return count;
        }
    }

    private void deleteIndexFiles() throws Exception {
        Path indexPath = storagePath.resolve("indices");
        if (!Files.exists(indexPath)) {
            return;
        }

        try (Stream<Path> stream = Files.walk(indexPath)) {
            List<Path> files = stream
                    .filter(Files::isRegularFile)
                    .collect(Collectors.toList());
            for (Path file : files) {
                Files.deleteIfExists(file);
            }
        }
    }
}
