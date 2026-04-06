import java.io.*;

public class Participante implements Registro {

    private int id;
    private String nome;
    private String email;
    private String interesses; // multivalorado, separado por vírgula

    public Participante() {
        this(-1, "", "", "");
    }

    public Participante(String nome, String email, String interesses) {
        this(-1, nome, email, interesses);
    }

    public Participante(int id, String nome, String email, String interesses) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.interesses = interesses;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNome() { return nome; }
    public String getEmail() { return email; }
    public String getInteresses() { return interesses; }

    public void setNome(String nome) { this.nome = nome; }
    public void setEmail(String email) { this.email = email; }
    public void setInteresses(String interesses) { this.interesses = interesses; }

    @Override
    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeInt(id);
        dos.writeUTF(nome);
        dos.writeUTF(email);
        dos.writeUTF(interesses);
        return baos.toByteArray();
    }

    @Override
    public void fromByteArray(byte[] ba) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(ba);
        DataInputStream dis = new DataInputStream(bais);
        id = dis.readInt();
        nome = dis.readUTF();
        email = dis.readUTF();
        interesses = dis.readUTF();
    }

    @Override
    public String toString() {
        return "\nID: " + id +
               "\nNome: " + nome +
               "\nEmail: " + email +
               "\nInteresses: " + interesses;
    }
}