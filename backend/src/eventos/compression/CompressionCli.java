package eventos.compression;

public class CompressionCli {
    public static void main(String[] args) throws Exception {
        String basePath = args.length > 0 ? args[0] : "./storage";
        DataArchiveService service = new DataArchiveService(basePath);

        print(service.compressDataFiles(new HuffmanCodec()));
        print(service.compressDataFiles(new LzwCodec()));
    }

    private static void print(CompressionReport report) {
        // Saida pensada para o formulario tecnico: tamanho original, tamanho compactado e taxa.
        System.out.printf(
                "%s: arquivos=%d, original=%d bytes, compactado=%d bytes, taxa=%.2f%%, saida=%s%n",
                report.getAlgorithm(),
                report.getFileCount(),
                report.getOriginalSize(),
                report.getCompressedSize(),
                report.getCompressionRate(),
                report.getOutputPath());
    }
}
