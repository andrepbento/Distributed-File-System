
import java.net.InetAddress;

public class ServerInfo {
    private String name;
    private InetAddress ip;
    private int port;
    private boolean logged;

    public ServerInfo(String name, InetAddress ip, int port) {
        this.name = name;
        this.ip = ip;
        this.port = port;
        this.logged = true;
    }
    
    public ServerInfo(InetAddress ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    @Override
    public boolean equals(Object o) {
        ServerInfo si = (ServerInfo)o;
        if(this.ip.equals(si.getIp()) && this.port == si.getPort())
            return true;
        return false;
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

    public synchronized void setLogged(boolean logged) {
        this.logged = logged;
    }  
    
}
