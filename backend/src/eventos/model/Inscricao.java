package eventos.model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class Inscricao implements Record {

    private int id;
    private int idEvento;
    private int idParticipante;
    private String dataInscricao;

    public Inscricao() {
        this(-1, -1, -1, "");
    }

    public Inscricao(int idEvento, int idParticipante, String dataInscricao) {
        this(-1, idEvento, idParticipante, dataInscricao);
    }

    public Inscricao(int id, int idEvento, int idParticipante, String dataInscricao) {
        this.id = id;
        this.idEvento = idEvento;
        this.idParticipante = idParticipante;
        this.dataInscricao = dataInscricao;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    @Override
    public int getId() {
        return id;
    }

    public int getIdEvento() {
        return idEvento;
    }

    public int getIdParticipante() {
        return idParticipante;
    }

    public String getDataInscricao() {
        return dataInscricao;
    }

    public void setIdEvento(int idEvento) {
        this.idEvento = idEvento;
    }

    public void setIdParticipante(int idParticipante) {
        this.idParticipante = idParticipante;
    }

    public void setDataInscricao(String dataInscricao) {
        this.dataInscricao = dataInscricao;
    }

    @Override
    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeInt(id);
        dos.writeInt(idEvento);
        dos.writeInt(idParticipante);
        dos.writeUTF(dataInscricao);
        return baos.toByteArray();
    }

    @Override
    public void fromByteArray(byte[] data) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        DataInputStream dis = new DataInputStream(bais);
        id = dis.readInt();
        idEvento = dis.readInt();
        idParticipante = dis.readInt();
        dataInscricao = dis.readUTF();
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", id);
        map.put("idEvento", idEvento);
        map.put("idParticipante", idParticipante);
        map.put("dataInscricao", dataInscricao);
        return map;
    }
}
