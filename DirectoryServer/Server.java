
import java.net.InetAddress;

public class Server {
    private String name;
    private InetAddress ip;
    private int port;
    private boolean logged;

    public Server(String name, InetAddress ip, int port) {
        this.name = name;
        this.ip = ip;
        this.port = port;
        this.logged = true;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public InetAddress getIp() {
        return ip;
    }

    public void setIp(InetAddress ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPorto(int port) {
        this.port = port;
    }

    public boolean isLogged() {
        return logged;
    }

    public void setLogged(boolean logged) {
        this.logged = logged;
    }  
    
}
