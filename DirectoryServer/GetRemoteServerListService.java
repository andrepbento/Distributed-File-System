
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author andre
 */
public class GetRemoteServerListService extends UnicastRemoteObject 
        implements GetRemoteServerListInterface {
    
    private List<ServerInfo> serverList;
    private List<ClientInfo> clientList;
    
    public GetRemoteServerListService(List<ServerInfo> serverList, 
            List<ClientInfo> clientList) throws RemoteException {
        this.serverList = serverList;
        this.clientList = clientList;
    }
    
    @Override
    public List<ServerInfo> getServerList() throws RemoteException {
        List<ServerInfo> serversLogged = new ArrayList<>();
        synchronized(serverList) {
            for(ServerInfo si : serverList)
                if(si.isLogged())
                    serversLogged.add(si);
        }
        return serversLogged;
    }
    
    @Override
    public List<ClientInfo> getClientList() throws RemoteException {
        List<ClientInfo> clientsLogged = new ArrayList<>();
        synchronized(clientList) {
            for(ClientInfo ci : clientList)
                if(ci.isLogged())
                    clientsLogged.add(ci);
        }
        return clientsLogged;
    }
    
}
