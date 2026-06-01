package eventos.compression;

import java.nio.file.Path;

public class CompressionReport {
    private final String algorithm;
    private final Path outputPath;
    private final long originalSize;
    private final long compressedSize;
    private final int fileCount;

    public CompressionReport(String algorithm, Path outputPath, long originalSize, long compressedSize, int fileCount) {
        this.algorithm = algorithm;
        this.outputPath = outputPath;
        this.originalSize = originalSize;
        this.compressedSize = compressedSize;
        this.fileCount = fileCount;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public Path getOutputPath() {
        return outputPath;
    }

    public long getOriginalSize() {
        return originalSize;
    }

    public long getCompressedSize() {
        return compressedSize;
    }

    public int getFileCount() {
        return fileCount;
    }

    public double getCompressionRate() {
        if (originalSize == 0) {
            return 0.0;
        }
        return (1.0 - ((double) compressedSize / (double) originalSize)) * 100.0;
    }
}
