package servicodirectoria;

import java.net.InetAddress;

public class Server_Registry {
    String nome;
    InetAddress ip;
    int porto;

    public Server_Registry(String nome, InetAddress ip, int porto) {
        this.nome = nome;
        this.ip = ip;
        this.porto = porto;
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
    
    
    
}
