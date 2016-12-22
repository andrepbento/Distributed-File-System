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
import java.util.logging.Level;
import java.util.logging.Logger;

public class Client {    
    private DatagramSocket udpSocket;
    private DatagramPacket packet; //para receber os pedidos e enviar as respostas
    private String cmd;
    private ObjectInputStream oIn;
    private ObjectOutputStream oOut;
    private InetAddress directoryServiceIp;
    private int directoryServicePort;
    private MSG msg;
    private String currentPath;
    ChatThreadReceive chatThread;
    HeartbeatThreadSend heartBeatThread;
    
    Map<String, ServerConnection> serverList;
    List<ClientInfo> clientList;
    
    public Client(InetAddress directoryServiceIp, int directoryServicePort) throws SocketException{
        udpSocket = new DatagramSocket();
        packet = null;
        this.directoryServiceIp = directoryServiceIp;
        this.directoryServicePort = directoryServicePort;
        serverList = new HashMap<>();
        clientList = new ArrayList<>();
        currentPath = "DS";
    }
    
    public InetAddress getDirectoryServiceIp() { return directoryServiceIp; }

    public void setDirectoryServiceIp(InetAddress directoryServiceIp) { this.directoryServiceIp = directoryServiceIp; }

    public int getDirectoryServicePort() { return directoryServicePort; }

    public void setDirectoryServicePort(int directoryServicePort) { this.directoryServicePort = directoryServicePort; }
    
    private void fillMsg(String line){
        String [] lineSplit = line.split(" ");
        msg.getCMD().addAll(Arrays.asList(lineSplit));
    }
    
    public void sendRequestUdp(String cmd) throws IOException{
        msg = new MSG();
        String line = Constants.CLIENT+" "+cmd;
        fillMsg(line);
        //Implementar o resto
        ByteArrayOutputStream bOut = new ByteArrayOutputStream();
        oOut = new ObjectOutputStream(bOut);
        oOut.writeObject(msg);
        oOut.flush();
        
        packet = new DatagramPacket(bOut.toByteArray(), bOut.toByteArray().length, directoryServiceIp, directoryServicePort);
        udpSocket.send(packet);
    }
    
    public Object receiveResponseUdp() throws IOException, ClassNotFoundException{
        packet = new DatagramPacket(new byte[Constants.MAX_SIZE], Constants.MAX_SIZE);
        udpSocket.receive(packet);
        oIn = new ObjectInputStream(new ByteArrayInputStream(packet.getData(), 0, packet.getLength()));

        return oIn.readObject();
    }
    
    public void sendRequestTcp(String cmd) throws IOException{
        msg = new MSG();
        String line = Constants.CLIENT+" "+cmd;
        fillMsg(line);
        
        ServerConnection s = serverList.get(getWhereAmI());
        oOut = new ObjectOutputStream(s.getSocket().getOutputStream());
        oOut.writeObject(msg);
        oOut.flush();
    }
    
    public Object receiveResponseTcp() throws IOException, ClassNotFoundException{
        ServerConnection s = serverList.get(getWhereAmI());
        oIn = new ObjectInputStream(s.getSocket().getInputStream());
        
        return oIn.readObject();
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

    private void updateClientList(List<ClientInfo> list){ 
        clientList.clear();
        clientList = list; 
        System.out.println("Client List Updated\n---------------------------------------");
        System.out.print(listClients());
        
    }
    
    private String listClients(){
        String list = "";
        for (ClientInfo c : clientList) {
            list += c.getUsername() + "\n";
        }
        
        list += "---------------------------------------\n";
        return list;
    } 
    
    private void updateServerList(List<ServerInfo> list){
        for (ServerInfo item : list) {
            if(!serverList.containsKey(item.getName())){
                serverList.put(item.getName(), new ServerConnection(item, null));
            }
        }
        
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
        
        System.out.println("\tServer List Updated\n---------------------------------------\n");
        System.out.print(listServers());
    }
    
    private String listServers(){
        String list = null;
        list = ("Server Name \tState (Connected/Not Connected\n");
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
    
    private boolean serverExists(String serverName){
        for (Map.Entry<String, ServerConnection> entry : serverList.entrySet()) {
            String key = entry.getKey();
            if(key.equals(serverName)){
               return true; 
            }
        }
        return false;
    }
    
    private String getWhereAmI(){
        String []s = currentPath.split("/");
        return s[0].trim();
    } 
    
    private int processClientCommand(String line){
        String [] lineSplit = line.split(" ");
        String cmd = lineSplit[0].trim();
        
        if(lineSplit.length <= 2){
            if(cmd.equalsIgnoreCase(Constants.CMD_CONNECT)){
                String serverName = lineSplit[1].trim();
                if(serverExists(serverName)){
                    if(!serverList.get(serverName).isConnected()){
                        ServerConnection server = serverList.get(serverName);
                        server.createSocket();
                        Object obj = null;
                        try {
                            obj = receiveResponseTcp();
                        } catch (IOException | ClassNotFoundException ex) {
                            ex.printStackTrace();
                        }
                        if(obj instanceof MSG){
                            msg = (MSG)obj;
                            
                        }
                        return Constants.CODE_CONNECT_OK; //1000 só por enquanto
                    }else
                        return Constants.CODE_SERVER_ALREADY_CONNECTED;
                }
                else
                    return Constants.CODE_SERVER_DOESNT_EXIST;
            }else
                return Constants.CODE_CMD_NOT_RECOGNIZED;
        }
        
        return Constants.CODE_CMD_FAILURE;
    }
    
    private void processServerCommand(MSG msg){
        switch(msg.getMSGCode()){
            case Constants.CODE_CONNECT_OK: 
                System.out.println("Connected to: " + getWhereAmI());
                
                break;
            default: break;
        }
    }
    
    private void processDirectoryServiceCommand(MSG msg){
        switch(msg.getMSGCode()){
            case Constants.CODE_LOGOUT_OK:
                if(heartBeatThread != null)
                    heartBeatThread.terminate();
                if(chatThread != null){
                    chatThread.terminate();
                    chatThread = null;
                }
                System.out.println("You logged out"); 
                break;
            case Constants.CODE_LOGIN_OK:
                heartBeatThread = new HeartbeatThreadSend(directoryServiceIp, 
                        udpSocket.getLocalPort());
                heartBeatThread.start();
                if(chatThread == null){
                    chatThread = new ChatThreadReceive();
                    chatThread.start();
                }
                System.out.println("Logged in"); 
                break;
            case Constants.CODE_REGISTER_OK:  System.out.println("You're now registered"); break;
            case Constants.CODE_CHAT_OK:  System.out.println("Chat ok"); break;
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
            default: break;
        }
    }
    
    private void processError(int code) throws Exceptions.ConnectFailure, 
            Exceptions.ListFailure, Exceptions.CmdFailure, Exceptions.CmdNotRecognized, 
            Exceptions.RegisterFailure, Exceptions.RegisterClientAlreadyExists, 
            Exceptions.NotLoggedIn, Exceptions.AlreadyLoggedIn, Exceptions.LoginFailure, 
            Exceptions.ChatFailure{
        switch(code){
            case Constants.CODE_CONNECT_FAILURE: 
                throw  new Exceptions.ConnectFailure();
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
    
    public void runClient(){
        while(true){
            try{
                System.out.print(currentPath+" >> ");
                cmd = new Scanner(System.in).nextLine();
                
                int code = processClientCommand(cmd);
                
                String where = getWhereAmI();
                if(where == null){
                    throw new Exceptions.CurrenthPath();
                }
                
                if(where.equalsIgnoreCase(Constants.DS)){
                    sendRequestUdp(cmd);
                    Object obj = receiveResponseUdp();

                    if(obj instanceof MSG){
                        msg = (MSG)obj;
                        processDirectoryServiceCommand(msg);
                    } 
                }else{
//                    sendRequestTcp(cmd);
                    Object obj = receiveResponseTcp();
                    if(obj instanceof MSG){
                        msg = (MSG)obj;
                        processServerCommand(msg);
                    }
                }
                processError(msg.getMSGCode());
                
            } catch(Exception ex) {
                System.out.println("\n"+ex);
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
            InetAddress clientIp = null;
            int clientPort = -1;
            
            clientIp = InetAddress.getByName(args[0]);
            clientPort = Integer.parseInt(args[1]);
            new Client(clientIp, clientPort).runClient();
            
        } catch (UnknownHostException | SocketException ex) {
            System.out.println(ex);
            ex.printStackTrace();
        }
    }
}
