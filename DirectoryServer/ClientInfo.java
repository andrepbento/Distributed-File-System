
import java.io.Serializable;
import java.net.InetAddress;

/**
 *
 * @author andre
 */
public class ClientInfo implements Serializable {
    private String username;
    private String password;
    private InetAddress clientAddress;
    private int chatDatagramSocketPort;
    private boolean logged;
    
    public ClientInfo(String username){
        this.username = username;
    }
    
    public ClientInfo(InetAddress clientAddress){
        this.clientAddress = clientAddress;
    }
    
    public ClientInfo(String username, String password, InetAddress clientAddress) {
        this.username = username;
        this.password = password;
        this.clientAddress = clientAddress;
    }

    @Override
    public boolean equals(Object o) {
        ClientInfo c = (ClientInfo)o;
        if(this.clientAddress == c.getClientAddress())
            return true;
        return false;
    }

    public String getUsername() { return username; }

    public String getPassword() { return password; }

    public InetAddress getClientAddress() { return clientAddress; }

    public int getChatDatagramSocketPort() {
        return chatDatagramSocketPort;
    }

    public void setChatDatagramSocketPort(int chatDatagramSocketPort) {
        this.chatDatagramSocketPort = chatDatagramSocketPort;
    }
    
    public boolean isLogged() { return logged; }
    
    public void setLogged(boolean logged) { this.logged = logged; }
    
    public void setClientAddress(InetAddress clientAddress) {
        this.clientAddress = clientAddress;
    }
    
}
