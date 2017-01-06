
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author andre
 */
public class DirectoryService extends Thread {
    private DatagramSocket socket;
    private DatagramPacket packet;
    protected static List<ServerInfo> serversList;
    protected static List<ClientInfo> clientsList;
    
    private Chat clientsChat;
    
    private Registry r;
    
    public DirectoryService() throws SocketException{
        socket = null;
        packet = null;
        socket = new DatagramSocket(Constants.LISTENIGN_PORT);
        serversList = new ArrayList<>();
        clientsList = loadClientsList();
        logOutAllClients();
        clientsChat = new Chat();
        try {
            System.out.println("DirectoryService started...\n"
                    +"IP:"+InetAddress.getLocalHost().getHostAddress()
                    +"\tPort:"+Constants.LISTENIGN_PORT+"\n");
        } catch (UnknownHostException ex) {
            ex.printStackTrace();
        }
        
        new HeartbeatThreadReceive().start();
        
        try {
            if((r = launchRemoteService()) == null)
                r.unbind(Constants.SERVICE_SERVER_LIST);
        } catch(Exception e) {
            System.out.println("Launch remote service failure!");
            System.exit(0);
        }
        
        printClientsList();
        printServersList();
    }
    
    public MSG waitDatagram() throws IOException {
        if(socket == null)
            return null;
        
        packet = new DatagramPacket(new byte[Constants.MAX_SIZE], Constants.MAX_SIZE);
        socket.receive(packet);

        try{
            ByteArrayInputStream bin = new ByteArrayInputStream(packet.getData(), 0, packet.getLength());
            ObjectInputStream ois = new ObjectInputStream(bin);
            
            System.out.println("MSG received from "+packet.getAddress().getHostAddress() 
                    +":"+packet.getPort());
            return (MSG)ois.readObject();
        }catch(ClassNotFoundException e){
            System.out.println("Received data from: "+packet.getAddress().getHostAddress() 
                        +":"+packet.getPort()+" is not MSG object");
            return null;
        }
    }
    
    private void closeSocket(){
        if(socket != null)
            socket.close();
    }
    
    public void processRequests(){
        MSG receivedMsg;
         
        if(socket == null)
            return;
        
        try{
            while(true){
                receivedMsg = waitDatagram();

                if(receivedMsg == null)
                    continue;
                if(receivedMsg.getCMD().isEmpty())
                    continue;
                
                switch(receivedMsg.getCMDarg(0).toUpperCase()){
                    case Constants.SERVER:
                        processServerCommand(receivedMsg);
                        break;
                    case Constants.CLIENT:
                        processClientCommand(receivedMsg);
                        break;
                }
            }
        } catch(IOException | NumberFormatException e){
            e.printStackTrace();
        } finally {
            closeSocket();
        }
    }
    
    private void sendResponse(MSG msg){
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream(Constants.MAX_SIZE);
        try{
            ObjectOutputStream os = new ObjectOutputStream(new
                                    BufferedOutputStream(byteStream));
            os.flush();
            os.writeObject((MSG)msg);
            os.flush();

            byte[] sendBuf = byteStream.toByteArray();
        
            packet.setData(sendBuf);
            packet.setLength(sendBuf.length);
            socket.send(packet);
        } catch(IOException ex) {
            ex.printStackTrace();
        }
        System.out.println("Reply MSG sended!");
    }
    
    private void processServerCommand(MSG receivedMSG){
        System.out.println("\tContent: "+receivedMSG.getCMD().toString());
        try{
            String name = receivedMSG.getCMDarg(1);
            InetAddress ip = packet.getAddress();
            int port = packet.getPort();
            int serverSocketPort = Integer.parseInt(receivedMSG.getCMDarg(2));
            
            MSG serverResponse = new MSG(Constants.CODE_SERVER_REGISTER_FAILURE);

            if(!serverExists(name)){
                serversList.add(new ServerInfo(name, ip, port, serverSocketPort));
                System.out.println("Server connected: "+name+"\t"
                        +ip+":"+port);
                serverResponse.setMSGCode(Constants.CODE_SERVER_REGISTER_OK);
            }
            sendResponse(serverResponse);
        }catch(Exception e){
            e.printStackTrace();
        }
        printServersList();
    }
    
    private void printServersList(){
        System.out.println("---------- Servers List ----------");
        int i = 1;
        for(ServerInfo s : serversList){
            System.out.println(i+"-["+s.getName()+"]:["+s.isLogged()+"]:["
                    +s.getIp()+"]:["+s.getDatagramSocketPort()+"]");
            i++;
        }
        System.out.println("----------------------------------");
    }
    
    public synchronized List<ServerInfo> getServersList() {
        return serversList;
    }
    
    private ServerInfo getServer(int serverIndex){
        serverIndex--;
        if(serversList.size() < serverIndex)
            return null;
        return serversList.get(serverIndex);
    }
    
    private ServerInfo getServer(String serverName){
        for(ServerInfo si : getServersList())
            if(si.getName().equals(serverName))
                return si;
        return null;
    }
    
    private boolean serverExists(String name){   
        for (ServerInfo s : serversList)
            if(s.getName().equalsIgnoreCase(name))
                return true;
        return false;
    }
    
    /* IMPLEMENTAR ISTO MAIS TARDE CASO SE QUEIRA FAZER SAVE/LOAD AOS SERVIDORES [REGISTO_SERVIDORES]
    public static final List<Server> loadServerList(){
        List<Server> serverList = new ArrayList<>();
        FileInputStream fins = null;
        try {
            fins = new FileInputStream(new File(Constants.SERVER_LIST_PATH));
            ObjectInputStream ois = null;
            ois = new ObjectInputStream(fins);
            System.out.println("ServerInfo list loaded!\n");
            return (List<Server>) ois.readObject();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException | IOException e){
            e.printStackTrace();
        }
        System.out.println("Clients list not loaded!\n");
        return serverList;
    }
    
    public final void saveServerList(){
        FileOutputStream fout = null;
        try {
            fout = new FileOutputStream(Constants.SERVER_LIST_PATH);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        ObjectOutputStream oos = null;
        try {
            System.out.println("ServerInfo list saved!\n");
            oos = new ObjectOutputStream(fout);
            oos.writeObject(serversList);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    */
    
    private void processClientCommand(MSG receivedMSG) {
        System.out.println("\tContent: "+receivedMSG.getCMD().toString());
        if(receivedMSG.getCMD().size() < 2) {
            sendResponse(new MSG(Constants.CODE_CMD_FAILURE));
            return;
        }

        switch(receivedMSG.getCMDarg(1).toUpperCase()) {
            case Constants.CMD_REGISTER:
                System.out.println("Received " + Constants.CMD_REGISTER);
                if(receivedMSG.getCMD().size() < 4) {
                    System.out.println("\tRegister Client FAIL");
                    sendResponse(new MSG(Constants.CODE_REGISTER_FAILURE));
                }else{
                    if(!clientExists(receivedMSG.getCMDarg(2))) {
                        clientsList.add(new ClientInfo(receivedMSG.getCMDarg(2), receivedMSG.getCMDarg(3), null, -1));
                        saveClientsList();
                        System.out.println("\tRegister Client OK\t"+receivedMSG.getCMDarg(2)+","+receivedMSG.getCMDarg(3)+"\n");
                        sendResponse(new MSG(Constants.CODE_REGISTER_OK));
                    }else{
                        System.out.println("\tRegister Client FAIL\t"+receivedMSG.getCMDarg(2)+"\tAlready exists\n");
                        sendResponse(new MSG(Constants.CODE_REGISTER_CLIENT_ALREADY_EXISTS));
                    }
                }
                break;
            case Constants.CMD_LOGIN:
                System.out.println("Received " + Constants.CMD_LOGIN);
                if(clientIsLogged(receivedMSG.getCMDarg(2))){
                    System.out.println("\tLogin FAIL\t"+receivedMSG.getCMDarg(2)+"\tAlready logged");
                    sendResponse(new MSG(Constants.CODE_LOGIN_ALREADY_LOGGED));
                    break;
                }
                if(receivedMSG.getCMD().size() < 4){
                    System.out.println("\tLogin FAIL");
                    sendResponse(new MSG(Constants.CODE_LOGIN_FAILURE));
                }else{
                    if(logInClient(receivedMSG.getCMDarg(2), receivedMSG.getCMDarg(3),
                            packet.getAddress(), packet.getPort())) {
                        System.out.println("\tLogin Client OK\t"+receivedMSG.getCMDarg(2)+","+receivedMSG.getCMDarg(3));
                        sendResponse(new MSG(Constants.CODE_LOGIN_OK, Arrays.asList(receivedMSG.getCMDarg(2))));
                    }else{
                        System.out.println("\tLogin Client FAIL\t"+receivedMSG.getCMDarg(2));
                        sendResponse(new MSG(Constants.CODE_LOGIN_FAILURE));
                    }
                }
                break;
            case Constants.CMD_LOGOUT:
                System.out.println("Received " + Constants.CMD_LOGOUT);
                if(!clientIsLogged(packet.getAddress(), packet.getPort())){
                    System.out.println("\tLogout FAIL\tNOT LOGGED IN");
                    sendResponse(new MSG(Constants.CODE_LOGIN_NOT_LOGGED_IN));
                    break;
                } else {
                    System.out.println("\tLogout OK\tClient: "
                            +getClient(packet.getAddress(), packet.getPort()).getUsername());
                    logOutClient(packet.getAddress(), packet.getPort());
                    sendResponse(new MSG(Constants.CODE_LOGOUT_OK));
                }
                break;
            case Constants.CMD_LIST: 
                if(!clientIsLogged(packet.getAddress(), packet.getPort())){
                    System.out.println("\tList FAIL\tNOT LOGGED IN");
                    sendResponse(new MSG(Constants.CODE_LOGIN_NOT_LOGGED_IN));
                    break;
                }
                if(receivedMSG.getCMD().size() <= 2){
                    System.out.println("\tList FAIL\tCMD WRONG");
                    sendResponse(new MSG(Constants.CODE_LOGIN_FAILURE));
                }else {
                    if(receivedMSG.getCMDarg(2).equalsIgnoreCase("-s")){
                        System.out.println("\tList Servers OK\tList sended!");
                        MSG serverMSG = new MSG(Constants.CODE_LIST_OK);
                        serverMSG.setServersList(getLoggedServers());
                        sendResponse(serverMSG);
                    }
                    else if(receivedMSG.getCMDarg(2).equalsIgnoreCase("-c")){
                        System.out.println("\tList Clients OK\tList sended!");
                        MSG clientMSG = new MSG(Constants.CODE_LIST_OK);
                        clientMSG.setClientList(getLoggedClients());
                        sendResponse(clientMSG);
                    }else
                        sendResponse(new MSG(Constants.CODE_LIST_FAILURE));
                }
                break;
            case Constants.CMD_CHAT:
                if(!clientIsLogged(packet.getAddress(), packet.getPort())){
                    System.out.println("\tChat FAIL\tNOT LOGGED IN");
                    sendResponse(new MSG(Constants.CODE_LOGIN_NOT_LOGGED_IN));
                    break;
                }
                if(receivedMSG.getCMD().size() < 4){
                    System.out.println("\tChat FAIL\tCMD WRONG");
                    sendResponse(new MSG(Constants.CODE_CMD_FAILURE));
                } else {
                    if(receivedMSG.getCMDarg(2).equalsIgnoreCase("-all")){
                        if(clientsChat.sendChatMSGToAll(
                                getClient(packet.getAddress(), packet.getPort()),
                                receivedMSG.getCMDarg(3)))
                            sendResponse(new MSG(Constants.CODE_CHAT_OK));
                        else
                            sendResponse(new MSG(Constants.CODE_CHAT_FAILURE));
                    } else {
                        if(clientsChat.sendChatMSGToDesignatedClients(
                                getClient(packet.getAddress(), packet.getPort()),
                                receivedMSG.getCMDarg(2),
                                receivedMSG.getCMDarg(3)))
                            sendResponse(new MSG(Constants.CODE_CHAT_OK));
                        else
                            sendResponse(new MSG(Constants.CODE_CHAT_FAILURE));
                    }
                }
                break;
            case Constants.CONNECTED:
                ServerInfo serverInfo = getServer(receivedMSG.getCMDarg(3));
                ClientInfo clientInfo = getClient(receivedMSG.getCMDarg(2));
                if(serverInfo != null && clientInfo != null)
                    serverInfo.addConnectedClient(clientInfo);
                else
                    System.out.println("getServer() ERROR!");
                break;
            case Constants.DISCONNECT:
                ServerInfo serverInfo2 = getServer(receivedMSG.getCMDarg(3));
                ClientInfo clientInfo2 = getClient(receivedMSG.getCMDarg(2));
                if(serverInfo2 != null && clientInfo2 != null)
                    serverInfo2.removeConnectedClient(clientInfo2);
                else
                    System.out.println("getServer() ERROR!");
                break;
            default:
                sendResponse(new MSG(Constants.CODE_CMD_NOT_RECOGNIZED));
        }
        
        printClientsList(); 
    }
    
    // ?????????????????????????????????????????????????????????????????????????
    private List<ServerInfo> getLoggedServers() {
        List<ServerInfo> serversLogged = new ArrayList<>();
            for(ServerInfo si : serversList)
                if(si.isLogged())
                    serversLogged.add(si);
        return serversLogged;
    }
    private List<ClientInfo> getLoggedClients() {
        List<ClientInfo> clientsLogged = new ArrayList<>();
        synchronized(clientsList) {
            for(ClientInfo ci : clientsList)
                if(ci.isLogged())
                    clientsLogged.add(ci);
        }
        return clientsLogged;
    }
    // ?????????????????????????????????????????????????????????????????????????
    
    
    private void printClientsList(){
        System.out.println("---------- Clients List ----------");
        int i = 1;
        for(ClientInfo c : clientsList){
            System.out.println(i+"-["+c.getUsername()+"]:["+c.getPassword()+"]:["
                    +c.isLogged()+"]");
            i++;
        }
        System.out.println("----------------------------------");
    }
    
    public synchronized List<ClientInfo> getClientsList() {
        return clientsList;
    }
    
    private ClientInfo getClient(String username){
        for(ClientInfo c : clientsList)
            if(c.getUsername().equals(username))
                return c;
        return null;
    }
    
    private ClientInfo getClient(InetAddress clientAddress, int clientPort){
        for(ClientInfo c : clientsList)
            if(c.equals(new ClientInfo(clientAddress, clientPort)))
                return c;
        return null;
    }
    
    private boolean clientExists(String username){
        ClientInfo c = getClient(username);
        if(c != null)
            return true;
        return false;
    }
    
    private boolean clientIsLogged(String username){
        ClientInfo c = getClient(username);
        if(c != null)
            if(c.isLogged())
                return true;
        return false;
    }
    
    private boolean clientIsLogged(InetAddress clientAddress, int clientPort){
        ClientInfo c = getClient(clientAddress, clientPort);
        if(c != null)
            if(c.isLogged())
                return true;
        return false;
    }

    private boolean logInClient(String username, String password, 
            InetAddress clientAddress, int clientPort){
        ClientInfo c = getClient(username);
        if(c != null)
            return c.logIn(password, clientAddress, clientPort);
        return false;
    }
    
    private void logOutClient(InetAddress clientAddress, int clientPort){
        ClientInfo c = getClient(clientAddress, clientPort);
        if(c != null)
            c.logOut();
    }
    
    private void logOutAllClients() {
        for(ClientInfo ci : clientsList)
            ci.logOut();
    }
    
    public static final List<ClientInfo> loadClientsList() {
        List<ClientInfo> clientList = new ArrayList<>();
        FileInputStream fins = null;
        try {
            fins = new FileInputStream(new File(Constants.CLIENT_LIST_PATH));
            ObjectInputStream ois = null;
            ois = new ObjectInputStream(fins);
            System.out.println("Clients list loaded!\n");
            return (List<ClientInfo>) ois.readObject();
        } catch (FileNotFoundException e) {
            System.out.println("Clients list does not exists!");
        } catch (ClassNotFoundException | IOException ex) {
            ex.printStackTrace();
        }
        System.out.println("Clients list not loaded!\n");
        return clientList;
    }
    
    public final boolean saveClientsList(){
        FileOutputStream fout = null;
        try {
            fout = new FileOutputStream(Constants.CLIENT_LIST_PATH);
            ObjectOutputStream oos = null;
            System.out.println("Clients list saved!\n");
            oos = new ObjectOutputStream(fout);
            oos.writeObject(clientsList);
        } catch(FileNotFoundException e) {
            return false;
        } catch(IOException e) {
            return false;
        }
        return true;
    }
    
    // JAVA RMI
    
    Registry launchRemoteService() throws RemoteException {
        Registry r = null;
        
        try{
            r = LocateRegistry.createRegistry(1099); //Se o servico nao estiver lancado ainda
        }catch(RemoteException e){
            System.out.println("Excepcao: " + e);
            r = LocateRegistry.getRegistry(1099); //caso esteja, faz get do servico existente
        }
        
        /*
        * Cria o servico
        */
        GetRemoteServerListService listService = new GetRemoteServerListService(serversList, clientsList);
        
        System.out.println("Servico "+Constants.SERVICE_SERVER_LIST
                +" criado e em execucao ("+listService.getRef().remoteToString()+"...");
        
        /*
        * Regista o servico no rmiregistry local para que os clientes possam localiza'-lo, ou seja,
        * obter a sua referencia remota (endereco IP, porto de escuta, etc.).
        */
        try{
            r.bind(Constants.SERVICE_SERVER_LIST, listService);
        }catch(AlreadyBoundException ex){
            System.out.println("Excepcao: " + ex);
        }                    
        
        System.out.println("Servico "+Constants.SERVICE_SERVER_LIST
                +" registado no registry...");
        
        return r;
    }
    
    /*
    private void detectServerExit() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
              public void run() {
                    logOutAllClients();
                    if(saveClientsList())
                        System.out.println("Clients list saved!");
                    else
                        System.out.println("Clients list NOT saved!");
                  }
              });
    }
    */
    
    public static void main(String[] args) {
        try {
            DirectoryService sd = new DirectoryService();
            //sd.detectServerExit();
            sd.processRequests();
        } catch(SocketException e) {
            e.printStackTrace();
        }
    }
}
