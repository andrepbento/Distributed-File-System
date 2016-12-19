import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
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
    
    public void updateServerList(List<ServerInfo> list){
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
    
    public String listServers(){
        StringBuilder list = null;
        list.append("Server Name").append("\tState (Connected/Not Connected\n\n");
        for (Map.Entry<String, ServerList> entry : serverList.entrySet()) {
            String serverName = entry.getKey();
            ServerList serverL = entry.getValue();
            list.append(serverName);
            if(serverL.getSocket() == null) list.append("Not Connected");
            else list.append("Connected\n");
            
        }
        return list.toString();
    } 
    
    private void connectToServer(){
        
    }
    
    private void processCommandOk(int code){
        //process commands ok
    }
    
    private void processError(int code){
        //Process errors
    }
    
    public void runClient(){
        while(true){
            try{  
                System.out.print(">>");
                sendRequestUdp();
                Object obj = receiveResponseUdp();

                    if(obj instanceof MSG){
                        MSG msg = (MSG)obj;
                        switch(msg.getMSGCode()){
                            case Constants.CODE_LOGOUT_OK:
                                System.out.println("Logout Ok!");
                                break;
                            case Constants.CODE_CONNECT_FAILURE:
                                throw  new Exceptions.ConnectFailure("Connect failure");
                            case Constants.CODE_CONNECT_OK:
                                System.out.println("Connected!");                              
                                break;
                            case Constants.CODE_LIST_FAILURE:
                                throw new Exceptions.ListFailure("List Failures");
                            case Constants.CODE_LIST_OK:
                                if(!msg.getServersList().isEmpty()){
                                    updateServerList(msg.getServersList());
                                } 
                                // PARA IMPLEMENTAR POSTERIORMENTE [LISTA_CLIENTES]
                                //else if(!msg.getClientList().isEmpty()) {
                                //    updateClientList(/*...*/);
                                //}
                                break;
                            case Constants.CODE_CMD_FAILURE: 
                                throw new Exceptions.CmdFailure(Constants.MSG_CMD_FAILURE);
                            case Constants.CODE_CMD_NOT_RECOGNIZED: 
                                throw new Exceptions.CmdNotRecognized(Constants.MSG_CMD_NOT_RECODNIZED);
                            case Constants.CODE_REGISTER_OK: 
                                System.out.println(Constants.MSG_REGISTER_OK);
                                break;
                            case Constants.CODE_REGISTER_FAILURE:
                                throw new Exceptions.RegisterFailure(Constants.MSG_REGISTER_FAILURE);
                            case Constants.CODE_LOGIN_OK:
                                System.out.println("Login Ok");
                                break;
                            case Constants.CODE_REGISTER_CLIENT_ALREADY_EXISTS:
                                throw new Exceptions.RegisterClientAlreadyExists(Constants.MSG_REGISTER_CLIENT_ALREADY_EXISTS);
                            case Constants.CODE_LOGIN_FAILURE:
                                throw new Exceptions.LoginFailure(Constants.MSG_LOGIN_FAILURE);
                            case Constants.CODE_LOGIN_ALREADY_LOGGED:
                                throw new Exceptions.AlreadyLoggedIn(Constants.MSG_LOGIN_ALREADY_LOGGED);
                            case Constants.CODE_LOGIN_NOT_LOGGED_IN:
                                throw new Exceptions.NotLoggedIn(Constants.MSG_LOGIN_NOT_LOGGED_IN);
                            default:
                                System.out.println(Constants.MSG_CODE_ERROR); break;
                        }
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
