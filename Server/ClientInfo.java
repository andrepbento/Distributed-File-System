
import java.io.Serializable;
import java.net.InetAddress;

/**
 *
 * @author andre
 */

public class ClientInfo implements Serializable {
    static final long serialVersionUID = 1010L;
    private String username;
    private String password;
    private InetAddress clientAddress;
    private int clientPort;
    private int chatDatagramSocketPort;
    private boolean logged;
    
    public ClientInfo(String username){
        this.username = username;
    }
    
    public ClientInfo(InetAddress clientAddress, int clientPort){
        this.clientAddress = clientAddress;
        this.clientPort = clientPort;
    }
    
    public ClientInfo(String username, String password, InetAddress clientAddress
            , int clientPort){
        this.username = username;
        this.password = password;
        this.clientAddress = clientAddress;
        this.clientPort = clientPort;
    }

    @Override
    public boolean equals(Object o){
        ClientInfo c = (ClientInfo)o;
        if(this.clientAddress != null && c.getClientAddress() != null)
            if(this.clientAddress.equals(c.getClientAddress()) && this.clientPort == c.clientPort)
                return true;
        return false;
    }

    public String getUsername() { return username; }

    public String getPassword() { return password; }

    public InetAddress getClientAddress() { return clientAddress; }
    
    public void setClientAddress(InetAddress clientAddress) {
        this.clientAddress = clientAddress;
    }

    public int getChatDatagramSocketPort() {
        return chatDatagramSocketPort;
    }

    public void setChatDatagramSocketPort(int chatDatagramSocketPort) {
        this.chatDatagramSocketPort = chatDatagramSocketPort;
    }
    
    public boolean isLogged() { return logged; }
    
    public void setLogged(boolean logged) { this.logged = logged; }

    public int getClientPort() {
        return clientPort;
    }

    public void setClientPort(int clientPort) {
        this.clientPort = clientPort;
    }
    
    public boolean logIn(String password, InetAddress clientAddress, int clientPort){
        if(this.getPassword().equals(password)){
            this.setLogged(true);
            this.setClientAddress(clientAddress);
            this.setClientPort(clientPort);
            return true;
        }
        return false;
    }
    
    public void logOut(){
        this.setClientAddress(null);
        this.setClientPort(-1);
        this.setChatDatagramSocketPort(-1);
        this.setLogged(false);
    }
    
}
