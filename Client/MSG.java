
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author luis
 */
public class MSG implements Serializable {
    private static final long serialVersionUID = 1010L;
    private List<String> cmd;
    private int msgCode;
    
    public MSG() {
        cmd = new ArrayList<>();
        msgCode = 0;
    }
    
    public List<String> getCMD() {
        return cmd;
    }
    
    public void setCMD(List<String> cmd) {
        this.cmd = cmd;
    }

    public int getMsgCode() {
        return msgCode;
    }

    public void setMsgCode(int msgCode) {
        this.msgCode = msgCode;
    }
}
