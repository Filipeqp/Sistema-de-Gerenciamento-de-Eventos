package eventos.model;

import java.io.IOException;

public interface Record {
    void setId(int id);
    int getId();
    byte[] toByteArray() throws IOException;
    void fromByteArray(byte[] data) throws IOException;
}
