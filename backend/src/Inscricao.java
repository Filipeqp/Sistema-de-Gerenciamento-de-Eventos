import java.io.*;

public class Inscricao implements Registro {

    private int id;
    private int idEvento;       // FK -> Evento
    private int idParticipante; // FK -> Participante
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

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getIdEvento() { return idEvento; }
    public int getIdParticipante() { return idParticipante; }
    public String getDataInscricao() { return dataInscricao; }

    public void setIdEvento(int idEvento) { this.idEvento = idEvento; }
    public void setIdParticipante(int idParticipante) { this.idParticipante = idParticipante; }
    public void setDataInscricao(String dataInscricao) { this.dataInscricao = dataInscricao; }

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
    public void fromByteArray(byte[] ba) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(ba);
        DataInputStream dis = new DataInputStream(bais);
        id = dis.readInt();
        idEvento = dis.readInt();
        idParticipante = dis.readInt();
        dataInscricao = dis.readUTF();
    }

    @Override
    public String toString() {
        return "\nID: " + id +
               "\nID do Evento: " + idEvento +
               "\nID do Participante: " + idParticipante +
               "\nData de Inscrição: " + dataInscricao;
    }
}