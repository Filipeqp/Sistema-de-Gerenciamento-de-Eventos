package eventos.compression;

public interface CompressionCodec {
    String name();

    byte[] compress(byte[] input) throws Exception;
}
