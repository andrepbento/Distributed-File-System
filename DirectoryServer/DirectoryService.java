
import java.io.BufferedOutputStream;
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
    
    public DirectoryService() throws SocketException{
        socket = null;
        packet = null;       
        socket = new DatagramSocket(Constants.LISTENIGN_PORT);
        activeServers = new ArrayList<>();
        clientsList = loadClientsList();
        try {
            System.out.println("DirectoryService started...\n"
                    +"IP:"+InetAddress.getLocalHost().getHostAddress()
                    +"\tPort"+Constants.LISTENIGN_PORT);
        } catch (UnknownHostException ex) {
            ex.printStackTrace();
        }
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
    
    private void closeSocket(){
        if(socket != null)
            socket.close();
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
                
                String []comando= receivedMsg.split(" ");
                InetAddress ip;
                int porto;
                String name;
        
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
    
    public synchronized List<ServerRegistry> getActiveServers() {
        return activeServers;
    }
    
    private boolean serverExists(String nome){   
        for (ServerRegistry s : activeServers)
            if(s.getNome().equalsIgnoreCase(nome))
                return true;
        return false;
    }
    
    private void processClientCommand(String[] cmd) {
        if(cmd.length <= 2) {
            sendClientResponse(Constants.CODE_CMD_FAILURE);
            return;
        }

        switch(cmd[1].toUpperCase()) {
            case Constants.CMD_REGISTER:
                System.out.print("Received " + Constants.CMD_REGISTER);
                if(cmd.length < 4) {
                    sendClientResponse(Constants.CODE_REGISTER_FAILURE);
                    System.out.print("\tRegister_Failure\n");
                }else{
                    if(!clientExists(packet.getAddress())) {
                        clientsList.add(new Client(cmd[2], cmd[3]));
                        saveClientsList();
                        sendClientResponse(Constants.CODE_REGISTER_OK);
                        System.out.println("\tRegister Client OK\t"+cmd[2]+","+cmd[3]+"\n");
                    }else{
                        sendClientResponse(Constants.CODE_REGISTER_CLIENT_ALREADY_EXISTS);
                        System.out.println("\tRegister Client FAIL\t"+cmd[2]+"\tAlready exists\n");
                    }
                }
                break;
            case Constants.CMD_LOGIN:
                System.out.println("Received " + Constants.CMD_LOGIN);
                if(clientIsLogged(packet.getAddress())){
                    sendClientResponse(Constants.CODE_LOGIN_ALREADY_LOGGED);
                    break;
                }
                if(cmd.length < 4){
                    sendClientResponse(Constants.CODE_LOGIN_FAILURE);
                    System.out.print("\tLogin_Failure\n");
                }else{
                    if(!clientExists(packet.getAddress())) { // CHECK IF USER IS ALREADY LOGGED
                        sendClientResponse(Constants.CODE_LOGIN_OK);
                        System.out.println("\tLogin Client OK\t"+cmd[2]+","+cmd[3]+"\n");
                    }else{
                        sendClientResponse(Constants.CODE_LOGIN_FAILURE);
                        System.out.println("\tLogin Client FAIL\t"+cmd[2]+"\tAlready exists\n");
                    }
                }
                break;
            case Constants.CMD_LOGOUT:
                System.out.println("Received " + Constants.CMD_LOGOUT);
                if(!clientIsLogged(packet.getAddress())){
                    sendClientResponse(Constants.CODE_LOGIN_NOT_LOGGED_IN);
                    break;
                } else {
                    Client c = getClient(packet.getAddress());
                    if(c != null)
                        c.setLogged(false);
                    System.out.println("");
                    //sendClientResponse(Constants.CODE); LOGOUT OK
                }
                break;
            case Constants.CMD_LIST: 
                if(!clientIsLogged(packet.getAddress())){
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
    
    public synchronized List<Client> getClientsList() {
        return clientsList;
    }
    
    private Client getClient(InetAddress clientAddress){
        for(Client c : clientsList)
            if(c.equals(new Client(clientAddress)))
                return c;
        return null;
    }
    
    private boolean clientExists(InetAddress clientAddress){
        for(Client c : clientsList)
            if(c.equals(new Client(clientAddress)))
                return true;
        return false;
    }
    
    private boolean clientIsLogged(InetAddress clientAddress){
        for(Client c : clientsList)
            if(c.equals(new Client(clientAddress)) && c.isLogged())
                return true;
        return false;
    }
    
    private void sendClientResponse(int responseCode) {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream(Constants.MAX_SIZE);
        try{
            ObjectOutputStream os = new ObjectOutputStream(new
                                    BufferedOutputStream(byteStream));
            os.flush();
            os.writeObject((Integer)responseCode);
            os.flush();

            byte[] sendBuf = byteStream.toByteArray();
        
            packet.setData(sendBuf);
            packet.setLength(sendBuf.length);
            socket.send(packet);
        } catch(IOException ex) {
            ex.printStackTrace();
        }
    }
    
    private void sendClientResponse(String response){
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream(Constants.MAX_SIZE);
        try{
            ObjectOutputStream os = new ObjectOutputStream(new
                                    BufferedOutputStream(byteStream));
            os.flush();
            os.writeObject((String)response);
            os.flush();

            byte[] sendBuf = byteStream.toByteArray();
        
            packet.setData(sendBuf);
            packet.setLength(sendBuf.length);
            socket.send(packet);
        } catch(IOException ex) {
            ex.printStackTrace();
        }
    }
    
    public static final List<Client> loadClientsList(){
        List<Client> clientList = new ArrayList<>();
        FileInputStream fins = null;
        try {
            fins = new FileInputStream(new File(Constants.CLIENT_LIST_PATH));
            ObjectInputStream ois = null;
            ois = new ObjectInputStream(fins);
            System.out.println("Clients list loaded!\n");
            return (List<Client>) ois.readObject();
        } catch (FileNotFoundException e) {} 
        catch (ClassNotFoundException e){
        } catch (IOException e) {}
        System.out.println("Clients list not loaded!\n");
        return clientList;
    }
    
    public final void saveClientsList(){
        FileOutputStream fout = null;
        try {
            fout = new FileOutputStream(Constants.CLIENT_LIST_PATH);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        ObjectOutputStream oos = null;
        try {
            System.out.println("Clients list saved!\n");
            oos = new ObjectOutputStream(fout);
            oos.writeObject(clientsList);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        try {
            DirectoryService sd = new DirectoryService();
            sd.processRequests();
        } catch(SocketException e) {
            e.printStackTrace();
        }
    }
}
