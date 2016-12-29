
import java.io.Serializable;
import java.net.InetAddress;

public class ServerInfo implements Serializable {
    
    static final long serialVersionUID = 1010L;
    
    private String name;
    private InetAddress ip;
    private int datagramSocketPort;
    private int serverSocketPort;
    private boolean logged;

    public ServerInfo(String name, InetAddress ip, int datagramSocketPort
            , int serverSocketPort) {
        this.name = name;
        this.ip = ip;
        this.datagramSocketPort = datagramSocketPort;
        this.serverSocketPort = serverSocketPort;
        this.logged = true;
    }
    
    public ServerInfo(InetAddress ip, int port) {
        this.ip = ip;
        this.datagramSocketPort = port;
    }

    @Override
    public boolean equals(Object o) {
        ServerInfo si = (ServerInfo)o;
        if(this.ip.equals(si.getIp()) && this.datagramSocketPort == si.getDatagramSocketPort())
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

    public int getDatagramSocketPort() {
        return datagramSocketPort;
    }

    public void setDatagramSocketPort(int datagramSocketPort) {
        this.datagramSocketPort = datagramSocketPort;
    }
    
    public int getServerSocketPort() {
        return serverSocketPort;
    }
    
    public void setServerSocketPort(int severSocketPort) {
        this.serverSocketPort = severSocketPort;
    }

    public boolean isLogged() {
        return logged;
    }

    public synchronized void setLogged(boolean logged) {
        this.logged = logged;
    }  
    
}
