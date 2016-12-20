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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Client {    
    private DatagramSocket udpSocket;
    private DatagramPacket packet; //para receber os pedidos e enviar as respostas
    private String cmd;
    private ObjectInputStream oIn;
    private ObjectOutputStream oOut;
    private InetAddress directoryServiceIp;
    private int directoryServicePort;
    private MSG msg;
    Map<String, ServerList> serverList;
    List<ClientInfo> clientList;
    
    public Client(InetAddress directoryServiceIp, int directoryServicePort) throws SocketException{
        udpSocket = new DatagramSocket();
        packet = null;
        this.directoryServiceIp = directoryServiceIp;
        this.directoryServicePort = directoryServicePort;
        serverList = new HashMap<>();
    }
    
    public InetAddress getDirectoryServiceIp() { return directoryServiceIp; }

    public void setDirectoryServiceIp(InetAddress directoryServiceIp) { this.directoryServiceIp = directoryServiceIp; }

    public int getDirectoryServicePort() { return directoryServicePort; }

    public void setDirectoryServicePort(int directoryServicePort) { this.directoryServicePort = directoryServicePort; }
    
    private void fillMsg(String line){
        String [] lineSplit = line.split(" ");
        msg.getCMD().addAll(Arrays.asList(lineSplit));
    }
    
    public void sendRequestUdp() throws IOException{
        msg = new MSG();
        cmd = new Scanner(System.in).nextLine();
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
    
    public void closeUdpSocket(){
        if(udpSocket != null)
            udpSocket.close();
    }
//        public void closeTcpSocket() throws IOException{
//        if(tcpSockets != null || !tcpSockets.isEmpty()){
//            for (Socket s : tcpSockets) {
//                s.close();
//            }  
//        }
//    }
//
    
//    public void sendRequestTcp(int ) throws IOException{
//        cmd = new Scanner(System.in).nextLine();
//        String send = "CLIENT " + cmd;
//        oOut = new ObjectOutputStream(tcpSockets.get(index).getOutputStream());
//        oOut.writeObject(send);
//        oOut.flush();
//    }
    
    private void updateClientList(List<ClientInfo> list){ 
        clientList = list; 
        System.out.println("Client List Updated\n"+listClients());
    }
    
    private String listClients(){
        String list = null;
        list = ("Client Username:\n");
        for (ClientInfo c : clientList) {
            list += c.getUsername() + "\n";
        }
        return list;
    } 
    
    private void updateServerList(List<ServerInfo> list){
        for (ServerInfo item : list) {
            if(!serverList.containsKey(item.getName())){
                serverList.put(item.getName(), new ServerList(item, null));
            }
        }
        
        for (Map.Entry<String, ServerList> entry : serverList.entrySet()) {
            String serverName = entry.getKey();
            ServerList serverL = entry.getValue();
            if(!list.contains(serverL.getServerInfo())){
                serverList.remove(serverName, serverL);
            }
        }
        
        System.out.println("\tServer List Updated\n"+listServers());
    }
    
    private String listServers(){
        String list = null;
        list = ("Server Name \tState (Connected/Not Connected\n");
        for (Map.Entry<String, ServerList> entry : serverList.entrySet()) {
            String serverName = entry.getKey();
            ServerList serverL = entry.getValue();
            list += (serverName);
            if(serverL.getSocket() == null) list += ("\tNot Connected");
            else list += ("\tConnected\n");
            
        }
        return list;
    } 
    
    private void connectToServer(){
        
    }
    
    private void processCommandOk(MSG msg){
        switch(msg.getMSGCode()){
            case Constants.CODE_LOGOUT_OK: System.out.println("You logged out"); break;
            case Constants.CODE_LOGIN_OK: System.out.println("Logged in"); break;
            case Constants.CODE_REGISTER_OK:  System.out.println("You're now registered"); break;
            case Constants.CODE_CONNECT_OK: System.out.println("Connected to "); break;
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
            //default: System.out.println("Code Error"); break;
        }
    }
    
    private void processError(MSG msg) throws Exceptions.ConnectFailure, 
            Exceptions.ListFailure, Exceptions.CmdFailure, Exceptions.CmdNotRecognized, 
            Exceptions.RegisterFailure, Exceptions.RegisterClientAlreadyExists, 
            Exceptions.NotLoggedIn, Exceptions.AlreadyLoggedIn, Exceptions.LoginFailure{
        switch(msg.getMSGCode()){
            case Constants.CODE_CONNECT_FAILURE: 
                throw  new Exceptions.ConnectFailure();
            case Constants.CODE_LIST_FAILURE: 
                throw new Exceptions.ListFailure();
            case Constants.CODE_CMD_FAILURE: 
                throw new Exceptions.CmdFailure();
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
            //default: System.out.println("Code Error"); break;
        }
    }
    
    public void runClient(){
        while(true){
            try{  
                System.out.print(">> ");
                sendRequestUdp();
                Object obj = receiveResponseUdp();

                    if(obj instanceof MSG){
                        MSG msg = (MSG)obj;
                        
                        processError(msg);
                        processCommandOk(msg);
                        
                    } 
            } catch(Exception ex) {
                System.out.println("\n"+ex);
            }
        //} finally {
            //client.closeUdpSocket();
        }
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
