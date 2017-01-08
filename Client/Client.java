import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Client {    
    private DatagramSocket udpSocket;
    private DatagramPacket packet; //para receber os pedidos e enviar as respostas
    private InetAddress directoryServiceIp;
    private int directoryServicePort;
    private String line;
    private MSG msg;
    private String username;
    private ChatThreadReceive chatThread;
    private HeartbeatThreadSend heartBeatThread;
    private DistributedFileSystem fileSystem;
    private Map<String, ServerConnection> serverList;
    private List<ClientInfo> clientList;
    private ServerConnection currentConnection;
    
    ObjectOutputStream oOut;
    ObjectInputStream oIn;
    
    public Client(InetAddress directoryServiceIp, int directoryServicePort) throws SocketException{
        try {
            System.out.println("Client running on ip: "+InetAddress.getLocalHost().getHostAddress());
        } catch (UnknownHostException ex) {
            ex.printStackTrace();
        }
        udpSocket = new DatagramSocket();
        packet = null;
        msg = null;
        this.directoryServiceIp = directoryServiceIp;
        this.directoryServicePort = directoryServicePort;
        serverList = new HashMap<>();
        clientList = new ArrayList<>();
        fileSystem = new DistributedFileSystem(this);
        username = "NoUser";
        currentConnection = null;
        heartBeatThread = null;
        chatThread = new ChatThreadReceive();
    }
    
    public InetAddress getDirectoryServiceIp() { 
        return directoryServiceIp; 
    }
    
    public MSG getMsg() {
        return msg;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
    
    public ServerConnection getCurrentConnection() {
        return currentConnection;
    }
    
    public void setCurrentConnection(ServerConnection currentConnection) {
        this.currentConnection = currentConnection;
    }
    
    public void setDirectoryServiceIp(InetAddress directoryServiceIp) { 
        this.directoryServiceIp = directoryServiceIp; 
    }

    public int getDirectoryServicePort() { 
        return directoryServicePort; 
    }

    public void setDirectoryServicePort(int directoryServicePort) { 
        this.directoryServicePort = directoryServicePort; 
    }
    
    private void fillMsg(String line){
        String [] lineSplit = line.split(" ");
        msg.getCMD().addAll(Arrays.asList(lineSplit));
    }
    
    public void sendRequestUdp(String cmd){
        try {
            msg = new MSG();
            fillMsg(Constants.CLIENT+" "+cmd);
            ByteArrayOutputStream bOut = new ByteArrayOutputStream();
            oOut = new ObjectOutputStream(bOut);
            oOut.writeObject(msg);
            oOut.flush();
            packet = new DatagramPacket(bOut.toByteArray(), bOut.toByteArray().length, directoryServiceIp, directoryServicePort);
            udpSocket.send(packet);
            oOut.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    public void sendRequestUdp(MSG msg){
        try {
            ByteArrayOutputStream bOut = new ByteArrayOutputStream();
            oOut = new ObjectOutputStream(bOut);
            oOut.writeObject(msg);
            oOut.flush();
            packet = new DatagramPacket(bOut.toByteArray(), bOut.toByteArray().length, directoryServiceIp, directoryServicePort);
            udpSocket.send(packet);
            oOut.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    public void receiveResponseUdp(){
        try {
            packet = new DatagramPacket(new byte[Constants.MAX_SIZE], Constants.MAX_SIZE);
            udpSocket.receive(packet);
            oIn = new ObjectInputStream(new ByteArrayInputStream(packet.getData(), 0, packet.getLength()));
            
            Object obj = oIn.readObject();
            if(obj instanceof MSG){
                this.msg = (MSG)obj;
            }
            oIn.close();
        } catch (IOException | ClassNotFoundException ex) {
            ex.printStackTrace();
        }
    }
    
    public void sendRequestTcp(MSG msg){
        try {
            oOut = currentConnection.getOutputStream();
            oOut.writeObject(msg);
            oOut.flush();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    public void receiveResponseTcp() {
        try{
            Object obj = getCurrentConnection().getInputStream().readObject();
            if(obj instanceof MSG) {
                this.msg = (MSG)obj;
            }
            else{
                System.out.println("Msg recebido mal");
            }
        }catch(Exception ex){
            System.out.println(ex);
        }
    }
    
    public void receiveResponseTcp(String serverName) {
        try{
            ObjectInputStream oIn = getServerConnection(serverName).getInputStream();
            Object obj = oIn.readObject();
            if(obj instanceof MSG){
                this.msg = (MSG)obj;
            }
            else{
                System.out.println("Msg recebido mal");
            }
        }catch(Exception ex){
            System.out.println(ex);
        }
    }
    
    public ServerConnection getServerConnection(String serverName){
        return serverList.get(serverName);
    }
    
    public boolean checkIfImConnected(String serverName){
        return getServerConnection(serverName) == null ? 
                false : getServerConnection(serverName).isConnected();
    }
    
    public void closeUdpSocket(){
        if(udpSocket != null)
            udpSocket.close();
    }
    
    public void closeTcpSocket() throws IOException{
        for (Map.Entry<String, ServerConnection> entry : serverList.entrySet()) {
            ServerConnection value = entry.getValue();
            if(value.getSocket() != null)
                value.getSocket().close();
        }
    }

    public void updateClientList(List<ClientInfo> list){ 
        clientList.clear();
        clientList = list;
        
        System.out.println(listClients());
    }
    
    private String listClients(){
        String list = "Logged Clients"+"\n---------------------------------------\n";
        for (ClientInfo c : clientList) 
            list += c.getUsername() + "\n";
        
        list += "\n---------------------------------------\n";
        return list;
    }
    
    public void updateServerList(List<ServerInfo> list){
        for (ServerInfo item : list) 
            if(!serverList.containsKey(item.getName()))
                serverList.put(item.getName(), new ServerConnection(item, null));
        
        for (Map.Entry<String, ServerConnection> entry : serverList.entrySet()) {
            String serverName = entry.getKey();
            ServerConnection server = entry.getValue();
            if(!list.contains(server.getServerInfo())){
                if(serverList.get(serverName).getSocket() != null){
                    try {
                        serverList.get(serverName).getSocket().close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
                serverList.remove(serverName, server);
            }
        }
        
        System.out.print(listServers());
    }
    
    private String listServers(){
        String list = "\tServer List\n";
        list += ("---------------------------------------\n"
                + "Server Name \tState (Connected/Not Connected\n");
        for (Map.Entry<String, ServerConnection> entry : serverList.entrySet()) {
            String serverName = entry.getKey();
            ServerConnection serverL = entry.getValue();
            list += (serverName);
            if(serverL.getSocket() == null) list += ("\tNot Connected\n");
            else list += ("\tConnected\n");
            
        }
        list += "\n---------------------------------------\n";
        return list;
    } 
      
    public ClientInfo getMyClientInfo() throws UnknownHostException{
        for (ClientInfo c : clientList) {
//            if(c.equals(new ClientInfo(InetAddress.getByName(InetAddress.getLocalHost().getHostAddress())
//                    , udpSocket.getLocalPort())))
            if(c.equals(new ClientInfo(InetAddress.getByName("10.65.128.149")
                    , udpSocket.getLocalPort())))
                    return c;
        }
        return null;
    }
    
    public void processDirectoryServiceCommand() throws Exception{
        if(msg == null)
            return;
        
        switch(msg.getMSGCode()){
            case Constants.CODE_LOGOUT_OK:
                username = "NoUser";
                if(heartBeatThread != null) {
                    heartBeatThread.terminate();
                    heartBeatThread = null;
                }
                chatThread.terminate();
                System.out.println("You logged out"); 
                break;
            case Constants.CODE_LOGIN_OK:
                username = msg.getCMD().get(0);
                if(heartBeatThread == null) {
                    heartBeatThread = new HeartbeatThreadSend(directoryServiceIp, 
                            udpSocket.getLocalPort(), chatThread.getDatagramSocketPort());
                    heartBeatThread.start();
                }
                chatThread.start();
                System.out.println("Logged in"); 
                break;
            case Constants.CODE_REGISTER_OK:  
                System.out.println("You're now registered"); 
                break;
            case Constants.CODE_CHAT_OK:  
                System.out.println("Chat ok"); 
                break;
                
            case Constants.CODE_LIST_OK:
                if(msg.getServersList()!= null){
                    if(!msg.getServersList().isEmpty())
                        updateServerList(msg.getServersList());
                } 
                else if(msg.getClientList()!= null) {
                    if(!msg.getClientList().isEmpty())
                        updateClientList(msg.getClientList());
                }
                break;
            case Constants.CODE_LIST_FAILURE: 
                throw new Exceptions.ListFailure();
                
            case Constants.CODE_CMD_NOT_RECOGNIZED:  
                throw new Exceptions.CmdNotRecognized();
            case Constants.CODE_REGISTER_FAILURE: 
                throw new Exceptions.RegisterFailure();
            case Constants.CODE_REGISTER_CLIENT_ALREADY_EXISTS: 
                throw new Exceptions.RegisterClientAlreadyExists();
            case Constants.CODE_LOGIN_NOT_LOGGED_IN:
                throw new Exceptions.NotLoggedIn();
            case Constants.CODE_LOGIN_ALREADY_LOGGED:
                throw new Exceptions.AlreadyLoggedIn();
            case Constants.CODE_LOGIN_FAILURE:
                throw new Exceptions.LoginFailure();
            case Constants.CODE_CHAT_FAILURE:
                throw new Exceptions.ChatFailure();
            case Constants.CODE_CMD_FAILURE: 
                throw new Exceptions.CmdFailure();
            default: break;
        }
    }
    
//    public void processError(int code) throws Exception{
//            switch(code){
//                case Constants.CODE_CONNECT_FAILURE: 
//                    throw  new Exceptions.ConnectFailure();
//                case Constants.CODE_LIST_FAILURE: 
//                    throw new Exceptions.ListFailure();
//                case Constants.CODE_CMD_NOT_RECOGNIZED:  
//                    throw new Exceptions.CmdNotRecognized();
//                case Constants.CODE_REGISTER_FAILURE: 
//                    throw new Exceptions.RegisterFailure();
//                case Constants.CODE_REGISTER_CLIENT_ALREADY_EXISTS: 
//                    throw new Exceptions.RegisterClientAlreadyExists();
//                case Constants.CODE_LOGIN_NOT_LOGGED_IN:
//                    throw new Exceptions.NotLoggedIn();
//                case Constants.CODE_LOGIN_ALREADY_LOGGED:
//                    throw new Exceptions.AlreadyLoggedIn();
//                case Constants.CODE_LOGIN_FAILURE:
//                    throw new Exceptions.LoginFailure();
//                case Constants.CODE_CHAT_FAILURE:
//                    throw new Exceptions.ChatFailure();
//                case Constants.CODE_CMD_FAILURE: 
//                    throw new Exceptions.CmdFailure();
//                default: break;
//            }
//    }
    
    private void processCommand(String line) throws Exceptions.CmdNotRecognized, 
            Exceptions.CmdFailure{
        String [] commands = line.split(" ");
        
        if(commands.length == 0)
            throw  new Exceptions.CmdFailure();
        
        String cmd1 = commands[0].toUpperCase().trim();
        
        switch(cmd1){
            case Constants.CMD_SWITCH:
                if(commands.length == 2) {
                    fileSystem.switchSystemType(commands[1].trim());
                    break;
                }
                throw new Exceptions.CmdFailure();
            case Constants.CMD_REGISTER:
                if(commands.length == 3) {
                    fileSystem.register(commands[1].trim(), commands[2].trim());
                    break;
                }
                throw new Exceptions.CmdFailure();
            case Constants.CMD_LOGIN:
                if(commands.length == 3) {
                    fileSystem.login(commands[1].trim(), commands[2].trim());
                    break;
                }
                throw new Exceptions.CmdFailure();
            case Constants.CMD_LOGOUT:
                if(commands.length == 1) {
                    fileSystem.logout();
                    break;
                }
                throw new Exceptions.CmdFailure();
           case Constants.CMD_LIST:
                if(commands.length == 2) {
                    fileSystem.list(commands[1].trim().toUpperCase());
                    break;
                }
                throw new Exceptions.CmdFailure();
            case Constants.CMD_CHAT:
                if(2 < commands.length) {
                    List<String> commandTmp = new ArrayList<>();
                    commandTmp.add(commands[1]);
                    int appearences = 0;
                    String msg = "";
                    for(String s : Arrays.copyOfRange(commands, 2, commands.length)) {
                        if(s.contains("\"")) {
                            appearences++;
                        }
                        msg += s+" ";
                    }
                    
                    if(appearences < 2)
                        throw new Exceptions.CmdFailure();
                    
                    commandTmp.add(msg);
                    
                    if(commandTmp.size() == 2)
                        fileSystem.chat(commandTmp.get(0).trim(), commandTmp.get(1));
                }
                throw new Exceptions.CmdFailure();
            case Constants.CMD_CONNECT:
                if(commands.length == 2) {
                    fileSystem.connect(commands[1].trim());
                    break;
                }
                throw new Exceptions.CmdFailure();
            case Constants.CMD_DISCONNECT:
                if(commands.length == 1) {
                    fileSystem.disconnect();
                    break;
                }
                throw new Exceptions.CmdFailure();
            case Constants.CMD_COPY_FILE:
                if(commands.length == 3) {
                    fileSystem.copyFile(commands[1].trim(), commands[2].trim());
                    break;
                }
                throw new Exceptions.CmdFailure();
            case Constants.CMD_MOVE_FILE:
                if(commands.length == 3) {
                    fileSystem.moveFile(commands[1].trim(), commands[2].trim());
                    break;
                }
                throw new Exceptions.CmdFailure();
            case Constants.CMD_CD_DIR:
                if(commands.length == 2) {
                    fileSystem.changeWorkingDirectory(commands[1].trim());
                    break;
                }
                throw new Exceptions.CmdFailure();
            case Constants.CMD_LS_DIR:
                if(commands.length == 1){
                    fileSystem.getWorkingDirContent();
                    break;
                }
                throw new Exceptions.CmdFailure();
            case Constants.CMD_CAT_FILE:
                if(commands.length == 2){
                    fileSystem.getFileContent(commands[1].trim());
                    break;
                }
                throw new Exceptions.CmdFailure();
            case Constants.CMD_RM_FILE:
                if(commands.length == 2){
                    fileSystem.removeFile(commands[1].trim());
                    break;
                }
                throw new Exceptions.CmdFailure();
            case Constants.CMD_MK_DIR:
                if(commands.length == 2) {
                    fileSystem.makeDir(commands[1].trim());
                    break;
                }
                throw new Exceptions.CmdFailure();
            case Constants.CMD_DOWNLOAD_FILE:
                if(commands.length == 2) {
                    fileSystem.downloadFile(commands[1].trim());
                    break;
                }
                throw new Exceptions.CmdFailure();
            case Constants.CMD_UPLOAD_FILE:
                if(commands.length == 2) {
                    fileSystem.uploadFile(commands[1].trim());
                    break;
                }
                throw new Exceptions.CmdFailure();
            case Constants.CMD_RENAME_FILE:
                if(commands.length == 3) {
                    fileSystem.renameFile(commands[1].trim(), commands[2].trim());
                    break;
                }
                throw new Exceptions.CmdFailure();
            default: throw new Exceptions.CmdNotRecognized();
        }
    }
    
    public void runClient(){
        while(true){
            try{
                System.out.print(username + "@" + fileSystem.getCurrentPath() + ">> ");
                line = new Scanner(System.in).nextLine();
                processCommand(line);
//                processError(msg.getMSGCode());
                
            } catch(Exception ex) {
                System.out.println(ex);
            }
        }
        //client.closeUdpSocket();
    }
    
    public static void main(String[] args) {        
             
        if(args.length != 2){
            System.out.println("Number of arguments invalid: java IP PORT");
            return;
        }
        
        try {
            InetAddress clientIp = InetAddress.getByName(args[0]);
            int clientPort = Integer.parseInt(args[1]);
            new Client(clientIp, clientPort).runClient();
            
        } catch (UnknownHostException | SocketException ex) {
            System.out.println(ex);
            ex.printStackTrace();
        }
    }
}
