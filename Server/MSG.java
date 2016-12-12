
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author jorge
 */
public class MSG implements Serializable {
    private List<String> cmd;
    
    public MSG() {
        cmd = new ArrayList<>();
    }
    
    public List<String> getCMD() {
        return cmd;
    }
    
    public void setCMD(List<String> cmd) {
        this.cmd = cmd;
    }
}
