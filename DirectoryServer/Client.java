/**
 *
 * @author andre
 */
public class Client {
    
    private String username;
    private String password;
    
    private boolean logged;
    
    public Client(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() { return username; }

    public String getPassword() { return password; }

    public boolean isLogged() { return logged; }
    
    public void setLogged(boolean logged) { this.logged = logged; }
    
}
