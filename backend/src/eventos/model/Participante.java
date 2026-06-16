package eventos.model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class Participante implements Record {

    private int id;
    private String nome;
    private String email;
    private String interesses;
    private String senha;

    public Participante() {
        this(-1, "", "", "", "");
    }

    public Participante(String nome, String email, String interesses) {
        this(-1, nome, email, interesses, "");
    }

    public Participante(String nome, String email, String interesses, String senha) {
        this(-1, nome, email, interesses, senha);
    }

    public Participante(int id, String nome, String email, String interesses) {
        this(id, nome, email, interesses, "");
    }

    public Participante(int id, String nome, String email, String interesses, String senha) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.interesses = interesses;
        this.senha = senha;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    @Override
    public int getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getEmail() {
        return email;
    }

    public String getInteresses() {
        return interesses;
    }

    public String getSenha() {
        return senha;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setInteresses(String interesses) {
        this.interesses = interesses;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    @Override
    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeInt(id);
        dos.writeUTF(nome);
        dos.writeUTF(email);
        dos.writeUTF(interesses);
        dos.writeUTF(senha == null ? "" : senha);
        return baos.toByteArray();
    }

    @Override
    public void fromByteArray(byte[] data) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        DataInputStream dis = new DataInputStream(bais);
        id = dis.readInt();
        nome = dis.readUTF();
        email = dis.readUTF();
        interesses = dis.readUTF();
        senha = dis.available() > 0 ? dis.readUTF() : "";
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", id);
        map.put("nome", nome);
        map.put("email", email);
        map.put("interesses", interesses);
        return map;
    }
}
