package eventos.model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class Palestrante implements Record {

    private int id;
    private String nome;
    private String miniCurriculo;
    private String especialidades;
    private int idEvento;

    public Palestrante() {
        this(-1, "", "", "", -1);
    }

    public Palestrante(String nome, String miniCurriculo, String especialidades, int idEvento) {
        this(-1, nome, miniCurriculo, especialidades, idEvento);
    }

    public Palestrante(int id, String nome, String miniCurriculo, String especialidades, int idEvento) {
        this.id = id;
        this.nome = nome;
        this.miniCurriculo = miniCurriculo;
        this.especialidades = especialidades;
        this.idEvento = idEvento;
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

    public String getMiniCurriculo() {
        return miniCurriculo;
    }

    public String getEspecialidades() {
        return especialidades;
    }

    public int getIdEvento() {
        return idEvento;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public void setMiniCurriculo(String miniCurriculo) {
        this.miniCurriculo = miniCurriculo;
    }

    public void setEspecialidades(String especialidades) {
        this.especialidades = especialidades;
    }

    public void setIdEvento(int idEvento) {
        this.idEvento = idEvento;
    }

    @Override
    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeInt(id);
        dos.writeUTF(nome);
        dos.writeUTF(miniCurriculo);
        dos.writeUTF(especialidades);
        dos.writeInt(idEvento);
        return baos.toByteArray();
    }

    @Override
    public void fromByteArray(byte[] data) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        DataInputStream dis = new DataInputStream(bais);
        id = dis.readInt();
        nome = dis.readUTF();
        miniCurriculo = dis.readUTF();
        especialidades = dis.readUTF();
        idEvento = dis.readInt();
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", id);
        map.put("nome", nome);
        map.put("miniCurriculo", miniCurriculo);
        map.put("especialidades", especialidades);
        map.put("idEvento", idEvento);
        return map;
    }
}
