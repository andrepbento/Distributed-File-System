
import java.net.InetAddress;
import java.net.Socket;


public class ServerList {
    ServerInfo serverInfo;
    Socket socket;
    String currentPath;

    public ServerList(ServerInfo serverInfo, Socket socket) {
        this.serverInfo = serverInfo;
        this.socket = socket;
        this.currentPath = null;
    }

    public ServerInfo getServerInfo() {
        return serverInfo;
    }
    
     public String getServerName() {
        return serverInfo.getName();
    }

    public InetAddress getServerIp() {
        return serverInfo.getIp();
    }

    public int getServerPort() {
        return serverInfo.getPort();
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public String getCurrentPath() {
        return currentPath;
    }

    public void setCurrentPath(String currentPath) {
        this.currentPath = currentPath;
    }
    
    
}
