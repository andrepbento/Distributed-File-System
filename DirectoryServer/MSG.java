
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author andre
 */
public class MSG implements Serializable {
    static final long serialVersionUID = 1010L;
    private List<String> cmd;
    private int msgCode;
    
    public MSG() {
        cmd = new ArrayList<>();
        msgCode = 0;
    }
    
    public List<String> getCMD() {
        return cmd;
    }
    
    public String getCMDarg(int argIndex) {
        if(argIndex < cmd.size())
            return cmd.get(argIndex);
        return null;
    } 
    
    public void setCMD(List<String> cmd) {
        this.cmd = cmd;
    }
    
    public int getMSGCode() {
        return msgCode;
    }
    
    public void setMSGCode(int msgCode) {
        this.msgCode = msgCode;
    }
}
