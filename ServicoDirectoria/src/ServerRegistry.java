
import java.net.InetAddress;

public class ServerRegistry {
    private String nome;
    private InetAddress ip;
    private int porto;
    private boolean log;

    public ServerRegistry(String nome, InetAddress ip, int porto) {
        this.nome = nome;
        this.ip = ip;
        this.porto = porto;
        this.log = true;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public InetAddress getIp() {
        return ip;
    }

    public void setIp(InetAddress ip) {
        this.ip = ip;
    }

    public int getPorto() {
        return porto;
    }

    public void setPorto(int porto) {
        this.porto = porto;
    }

    public boolean getLog() {
        return log;
    }

    public void setLog(boolean log) {
        this.log = log;
    }  
    
}
