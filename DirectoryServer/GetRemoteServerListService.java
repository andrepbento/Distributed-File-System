
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

/**
 *
 * @author andre
 */
public class GetRemoteServerListService extends UnicastRemoteObject 
        implements GetRemoteServerListInterface {
    
    private List<ServerInfo> serverList;
    
    public GetRemoteServerListService(List<ServerInfo> serverList) throws RemoteException {
        this.serverList = serverList;
    }
    
    @Override
    public List<ServerInfo> getServerList() throws RemoteException {
        return serverList;
    }
    
}
