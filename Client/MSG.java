
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MSG implements Serializable {
    static final long serialVersionUID = 1010L;
    private int msgCode;
    private List<String> cmd;
    
    private List<ServerInfo> serversList = null;
    private List<ClientInfo> clientList = null;
    
    public MSG() {
        msgCode = 0;
        cmd = new ArrayList<>();
    }
    
    public MSG(int msgCode) {
        this.msgCode = msgCode;
        cmd = new ArrayList<>();
    }
    
    public MSG(int msgCode, List<String> cmd) {
        this.msgCode = msgCode;
        this.cmd = cmd;
    }
    
    public int getMSGCode() {
        return msgCode;
    }
    
    public void setMSGCode(int msgCode) {
        this.msgCode = msgCode;
    }
    
    public List<String> getCMD() {
        return cmd;
    }
    
    public String getCMDarg(int argIndex) {
        if(argIndex < cmd.size())
            return cmd.get(argIndex);
        return null;
    } 
    
    public int getCmdSize(){
        return cmd.size();
    }
    
    public void setCMD(List<String> cmd) {
        this.cmd = cmd;
    }

    public List<ServerInfo> getServersList() {
        return serversList;
    }

    public void setServersList(List<ServerInfo> serversList) {
        this.serversList = serversList;
    }

    public List<ClientInfo> getClientList() {
        return clientList;
    }

    public void setClientList(List<ClientInfo> clientList) {
        this.clientList = clientList;
    }
    
}
