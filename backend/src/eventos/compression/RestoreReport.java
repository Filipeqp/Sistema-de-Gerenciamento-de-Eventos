package eventos.compression;

import java.nio.file.Path;

public class RestoreReport {
    private final String algorithm;
    private final Path inputPath;
    private final int restoredFiles;
    private final long restoredSize;

    public RestoreReport(String algorithm, Path inputPath, int restoredFiles, long restoredSize) {
        this.algorithm = algorithm;
        this.inputPath = inputPath;
        this.restoredFiles = restoredFiles;
        this.restoredSize = restoredSize;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public Path getInputPath() {
        return inputPath;
    }

    public int getRestoredFiles() {
        return restoredFiles;
    }

    public long getRestoredSize() {
        return restoredSize;
    }
}
