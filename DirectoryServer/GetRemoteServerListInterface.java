
import java.rmi.Remote;
import java.util.List;


/**
 *
 * @author andre
 */
public interface GetRemoteServerListInterface extends Remote {

    public List<ServerInfo> getServerList() throws java.rmi.RemoteException;
    public List<ClientInfo> getClientList() throws java.rmi.RemoteException;

}
