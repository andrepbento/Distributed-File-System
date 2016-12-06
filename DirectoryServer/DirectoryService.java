
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
/**
 *
 * @author andre
 */
public class DirectoryService extends Thread {
    private DatagramSocket socket;
    private DatagramPacket packet;
    private List<ServerRegistry> activeServers;
    private boolean threadIsRunning = false;
    
    private List<Client> clientsList;
    
    public DirectoryService(int listeningPort) throws SocketException{
        socket = null;
        packet = null;       
        socket = new DatagramSocket(listeningPort);
        activeServers = new ArrayList<>();
        clientsList = new ArrayList<>();
    }

    public synchronized List<ServerRegistry> getActiveServers() {
        return activeServers;
    }
    
    public synchronized List<Client> getClientsList() {
        return clientsList;
    }
    
    public String waitDatagram() throws IOException {
        String request;
        
        if(socket == null)
            return null;
        
        packet = new DatagramPacket(new byte[Constants.MAX_SIZE], Constants.MAX_SIZE);
        socket.receive(packet);

        try{
            request = new String(packet.getData());
        }catch(ClassCastException e){
            System.out.println("Received data from: "+packet.getAddress().getHostAddress() 
                        +":"+packet.getPort()+" is not a String");
            return null;
        }

        System.out.println("Received \""+request+"\" from "+packet.getAddress().getHostAddress() 
                    +":"+packet.getPort());
        
        return request;
    }
    
    private boolean serverExists(String nome){   
        for (ServerRegistry s : activeServers)
            if(s.getNome().equalsIgnoreCase(nome))
                return true;
        return false;
    }
    
    private boolean clientExists(String username){
        for(Client c : clientsList)
            if(c.getUsername().equalsIgnoreCase(username))
                return true;
        return false;
    }
    
    public void processRequests(){
        String receivedMsg;
        ByteArrayOutputStream bOut;
        ObjectOutputStream out;
         
        if(socket == null)
            return;
        
        while(true){
            try{
                receivedMsg = waitDatagram();

                if(receivedMsg == null)
                    continue;

                System.out.println("ReceivedMSG: "+receivedMsg);
                
                String []comando= receivedMsg.split(" ");
                InetAddress ip;
                int porto;
                String name;
                System.out.println("CHEGUEI AQUI");              
                switch(comando[0].toUpperCase()){
                    case Constants.SERVER: 
                        name = comando[1];
                        ip = packet.getAddress();
                        porto = packet.getPort();
                        
                        if(!serverExists(name)){
                            activeServers.add(new ServerRegistry(name, ip, porto));
                            System.out.println("Server connected: "+name+"\t"
                                    +ip+":"+porto);
                            packet = new DatagramPacket("REGISTADO".getBytes(), Constants.REGISTADO.length(), ip, porto);
                            socket.send(packet);
                            if(!threadIsRunning){
                                new HeartbeatThreadReceive(activeServers).start();
                                threadIsRunning = true;
                            }
                        }
                        packet = new DatagramPacket("ERRO".getBytes(), Constants.ERRO.length(), ip, porto);
                        socket.send(packet);
                        break;
                    case Constants.CLIENT:
                        System.out.println("CHEGUEI AQUI CLIENTE");
                        processClientCommand(comando);
                        break;
                }
            }catch(IOException e){
                System.out.println(e);
            }catch(NumberFormatException e){
                System.out.println(e);
            }catch(Exception e){
                System.out.println(e);
            }finally{
                //closeSocket();
            }
        }
    }
    
    private void processClientCommand(String[] cmd) {
        //if(cmd.length <= 3) {
        //    sendClientResponse(Constants.CODE_CMD_FAILURE);
        //    return;
        //}
                    System.out.println("SWITCH");
        switch(cmd[2].toUpperCase()) { // TIPO DE COMANDO
            case Constants.CMD_REGISTER:
                System.out.print("Received " + Constants.CMD_REGISTER);
                if(cmd[1].equals(Constants.NO_USER)){
                    if(cmd.length < 4) {
                        sendClientResponse(Constants.CODE_REGISTER_FAILURE);
                        System.out.print("\tRegister_Failure\n");
                    }else{
                        if(!clientExists(cmd[2])) {
                            clientsList.add(new Client(cmd[2], cmd[3]));
                            sendClientResponse(Constants.CODE_REGISTER_OK);
                            System.out.println("\tRegister Client OK\t"+cmd[2]+","+cmd[3]+"\n");
                        }else{
                            sendClientResponse(Constants.CODE_REGISTER_CLIENT_ALREADY_EXISTS);
                            System.out.println("\tRegister Client FAIL\t"+cmd[2]+"\tAlready exists\n");
                        }
                    }
                }
                break;
            case Constants.CMD_LOGIN:
                System.out.println("Received " + Constants.CMD_LOGIN);
                if(clientIsLogged(cmd[3])){
                    sendClientResponse(Constants.CODE_LOGIN_ALREADY_LOGGED);
                    break;
                }
                if(cmd.length < 4){
                    sendClientResponse(Constants.CODE_LOGIN_FAILURE);
                    System.out.print("\tLogin_Failure\n");
                }else{
                    if(!clientExists(cmd[2])) {// | Client.logged() true | false
                        clientsList.add(new Client(cmd[2], cmd[3]));
                        sendClientResponse(Constants.CODE_REGISTER_OK);
                        System.out.println("\tRegister Client OK\t"+cmd[2]+","+cmd[3]+"\n");
                    }else{
                        sendClientResponse(Constants.CODE_REGISTER_CLIENT_ALREADY_EXISTS);
                        System.out.println("\tRegister Client FAIL\t"+cmd[2]+"\tAlready exists\n");
                    }
                }
                break;
            case Constants.CMD_LOGOUT:
                System.out.println("Received " + Constants.CMD_LOGOUT);
                if(cmd[1].equals(Constants.NO_USER)){
                    sendClientResponse(Constants.CODE_LOGIN_NOT_LOGGED_IN);
                    break;
                }
                for(Client c : clientsList)
                    if(c.getUsername().compareTo(cmd[1]) == 0)
                        c.setLogged(false);
                break;
            case Constants.CMD_LIST: 
                if(!clientIsLogged(cmd[1])){
                    sendClientResponse(Constants.CODE_LOGIN_NOT_LOGGED_IN);
                    break;
                } else {
                    String list = "";
                    if(cmd[3].equalsIgnoreCase("-s")){
                        list+="SERVERS LIST\n";
                        for(ServerRegistry s : activeServers)
                            list += s.getNome()+"\n";
                        list+="--------------";
                    }else if(cmd[3].equalsIgnoreCase("-c")){
                        list+="CLIENTS LIST\n";
                        for(Client c : clientsList)
                            list += c.getUsername()+"\n";
                        list+="--------------";
                    }else{
                        sendClientResponse(Constants.CODE_LIST_FAILURE);
                        break;
                    }
                    System.out.println("Lista:"+list.toString());
                    sendClientResponse(list);
                }
                break;
            default:
                sendClientResponse(Constants.CODE_CMD_NOT_RECOGNIZED);
        }
    }
    
    private void sendClientResponse(int responseCode) {
        byte[] response = ByteBuffer.allocate(4).putInt(responseCode).array();
        
        try{
            packet.setData(response);
            packet.setLength(response.length);
            socket.send(packet);
        } catch(IOException ex) {
            ex.printStackTrace();
        }
    }
    
    private void sendClientResponse(String response){
        
    }
    
    private void closeSocket(){
        if(socket != null)
            socket.close();
    }
    
    private boolean clientIsLogged(String username){
        for(Client c : clientsList)
            if(c.isLogged() && username.compareToIgnoreCase(c.getUsername()) == 0)
                return true;
        return false;
    }
    
    public static void main(String[] args) {
        if(args[0] == null){
            System.out.println("Syntax error: ServicoDirectoria <listeningPort>");
            return;
        }
        
        try {
            DirectoryService sd = new DirectoryService(Integer.parseInt(args[0]));
            sd.processRequests();
        } catch(SocketException e) {
            e.printStackTrace();
        }
    }
    
}
