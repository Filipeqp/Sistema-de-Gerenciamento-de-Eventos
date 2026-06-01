package eventos.compression;

import java.io.ByteArrayOutputStream;
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
        byte[] archive = buildSingleArchive(dataFiles);
        byte[] compressed = codec.compress(archive);

        Files.createDirectories(outputPath);
        Path outputFile = outputPath.resolve("dados-" + codec.name() + ".cmp");
        Files.write(outputFile, compressed);

        return new CompressionReport(codec.name(), outputFile, archive.length, compressed.length, dataFiles.size());
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
}
