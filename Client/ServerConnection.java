
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;


public class ServerConnection {
    private ServerInfo serverInfo;
    private Socket socket;
    private String currentPath;
    private boolean connected;

    public ServerConnection(ServerInfo serverInfo, Socket socket) {
        this.serverInfo = serverInfo;
        this.socket = socket;
        this.currentPath = null;
        this.connected = false;
    }

    public void createSocket(){
        try {
            socket = new Socket(serverInfo.getIp(), serverInfo.getServerSocketPort());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
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

    public int getDatagramPort() {
        return serverInfo.getDatagramSocketPort();
    }
    
    public int getServerPort() {
        return serverInfo.getServerSocketPort();
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
