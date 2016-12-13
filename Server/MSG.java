
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author jorge
 */
public class MSG implements Serializable {
    private List<String> cmd;
    private int msgCode;
    static final long serialVersionUID = 1010L;
    
    public MSG() {
        cmd = new ArrayList<>();
        msgCode = 0;
    }
    
    public List<String> getCMD() {
        return cmd;
    }
    
    public String toStringCMD() {
        String aux = "";
        for(int i = 0; i < cmd.size(); i++)
            aux += cmd.get(i) + " ";
        return aux;
    }
    
    public void setCMD(List<String> cmd) {
        this.cmd = cmd;
    }
    
    public void setMSGCode(int msgCode) {
        this.msgCode = msgCode;
    }
    
    public int getMSGCode() {
        return this.msgCode;
    }
}
