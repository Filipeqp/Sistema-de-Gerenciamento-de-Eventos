package eventos.model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class Evento implements Record {

    private int id;
    private String nome;
    private String descricao;
    private String dataEvento;
    private float preco;
    private String tags;

    public Evento() {
        this(-1, "", "", "", 0F, "");
    }

    public Evento(String nome, String descricao, String dataEvento, float preco, String tags) {
        this(-1, nome, descricao, dataEvento, preco, tags);
    }

    public Evento(int id, String nome, String descricao, String dataEvento, float preco, String tags) {
        this.id = id;
        this.nome = nome;
        this.descricao = descricao;
        this.dataEvento = dataEvento;
        this.preco = preco;
        this.tags = tags;
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

    public String getDescricao() {
        return descricao;
    }

    public String getDataEvento() {
        return dataEvento;
    }

    public float getPreco() {
        return preco;
    }

    public String getTags() {
        return tags;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public void setDataEvento(String dataEvento) {
        this.dataEvento = dataEvento;
    }

    public void setPreco(float preco) {
        this.preco = preco;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    @Override
    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeInt(id);
        dos.writeUTF(nome);
        dos.writeUTF(descricao);
        dos.writeUTF(dataEvento);
        dos.writeFloat(preco);
        dos.writeUTF(tags);
        return baos.toByteArray();
    }

    @Override
    public void fromByteArray(byte[] data) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        DataInputStream dis = new DataInputStream(bais);
        id = dis.readInt();
        nome = dis.readUTF();
        descricao = dis.readUTF();
        dataEvento = dis.readUTF();
        preco = dis.readFloat();
        tags = dis.readUTF();
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", id);
        map.put("nome", nome);
        map.put("descricao", descricao);
        map.put("dataEvento", dataEvento);
        map.put("preco", preco);
        map.put("tags", tags);
        return map;
    }
}
