
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
    private List<Server> serversList;
    private boolean hbThreadIsRunning = false;
    private List<Client> clientsList;
    
    public DirectoryService() throws SocketException{
        socket = null;
        packet = null;       
        socket = new DatagramSocket(Constants.LISTENIGN_PORT);
        serversList = new ArrayList<>();
        clientsList = loadClientsList();
        try {
            System.out.println("DirectoryService started...\n"
                    +"IP:"+InetAddress.getLocalHost().getHostAddress()
                    +"\tPort"+Constants.LISTENIGN_PORT+"\n");
        } catch (UnknownHostException ex) {
            ex.printStackTrace();
        }
        
        printClientsList();
        printServersList();
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
        
        try{
            while(true){
                receivedMsg = waitDatagram();

                if(receivedMsg == null)
                    continue;

                String []command= receivedMsg.split(" ");
                
                switch(command[0].toUpperCase()){
                    case Constants.SERVER: 
                        String name = command[1];
                        InetAddress ip = packet.getAddress();
                        int port = packet.getPort();

                        if(!serverExists(name)){
                            serversList.add(new Server(name, ip, port));
                            System.out.println("Server connected: "+name+"\t"
                                    +ip+":"+port);
                            packet = new DatagramPacket("REGISTADO".getBytes(), Constants.REGISTADO.length(), ip, port);
                            socket.send(packet);
                            if(!hbThreadIsRunning){
                                new HeartbeatThreadReceive(serversList).start();
                                hbThreadIsRunning = true;
                            }
                        }
                        packet = new DatagramPacket("ERRO".getBytes(), Constants.ERRO.length(), ip, port);
                        socket.send(packet);
                        break;
                    case Constants.CLIENT:
                        processClientCommand(command);
                        break;
                }
            }
        }catch(IOException e){
                System.out.println(e);
            }catch(NumberFormatException e){
                System.out.println(e);
            }catch(Exception e){
                System.out.println(e);
            }finally{
                closeSocket();
            }
    }
    
    private void printServersList(){
        System.out.println("---------- Servers List ----------");
        int i = 1;
        for(Server s : serversList){
            System.out.println(i+"-["+s.getName()+"]:["+s.isLogged()+"]:["
                    +s.getIp()+"]:["+s.getPort()+"]");
            i++;
        }
        System.out.println("----------------------------------");
    }
    
    public synchronized List<Server> getServersList() {
        return serversList;
    }
    
    private String getServerListSTR(){
        String list = "---------- SERVERS LIST ----------\n"
                +"-- INDEX:SERVERNAME:LOGGED --\n";
        int i = 1;
        for(Server s : serversList){
            list += i+":"+s.getName()+":"+s.isLogged()+"\n";
            i++;
        }
        list+="----------------------------------\n";
        return list;
    }
    
    private boolean serverExists(String name){   
        for (Server s : serversList)
            if(s.getName().equalsIgnoreCase(name))
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
                System.out.println("Received " + Constants.CMD_REGISTER);
                if(cmd.length < 4) {
                    System.out.println("\tRegister Client FAIL");
                    sendClientResponse(Constants.CODE_REGISTER_FAILURE);
                }else{
                    if(!clientExists(cmd[2])) {
                        clientsList.add(new Client(cmd[2], cmd[3], null));
                        saveClientsList();
                        System.out.println("\tRegister Client OK\t"+cmd[2]+","+cmd[3]+"\n");
                        sendClientResponse(Constants.CODE_REGISTER_OK);
                    }else{
                        System.out.println("\tRegister Client FAIL\t"+cmd[2]+"\tAlready exists\n");
                        sendClientResponse(Constants.CODE_REGISTER_CLIENT_ALREADY_EXISTS);
                    }
                }
                break;
            case Constants.CMD_LOGIN:
                System.out.println("Received " + Constants.CMD_LOGIN);
                if(clientIsLogged(cmd[2])){
                    System.out.println("\tLogin FAIL\t"+cmd[2]+"\tAlready logged");
                    sendClientResponse(Constants.CODE_LOGIN_ALREADY_LOGGED);
                    break;
                }
                if(cmd.length < 4){
                    System.out.println("\tLogin FAIL");
                    sendClientResponse(Constants.CODE_LOGIN_FAILURE);
                }else{
                    if(logInClient(cmd[2], cmd[3], packet.getAddress())) {
                        System.out.println("\tLogin Client OK\t"+cmd[2]+","+cmd[3]);
                        sendClientResponse(Constants.CODE_LOGIN_OK);
                    }else{
                        System.out.println("\tLogin Client FAIL\t"+cmd[2]);
                        sendClientResponse(Constants.CODE_LOGIN_FAILURE);
                    }
                }
                break;
            case Constants.CMD_LOGOUT:
                System.out.println("Received " + Constants.CMD_LOGOUT);
                if(!clientIsLogged(packet.getAddress())){
                    System.out.println("\tLogout FAIL\tNOT LOGGED IN");
                    sendClientResponse(Constants.CODE_LOGIN_NOT_LOGGED_IN);
                    break;
                } else {
                    System.out.println("\tLogout OK\tClient: "
                            +getClient(packet.getAddress()).getUsername());
                    logOutClient(packet.getAddress());
                    sendClientResponse(Constants.CODE_LOGOUT_OK);
                }
                break;
            case Constants.CMD_LIST: 
                if(!clientIsLogged(packet.getAddress())){
                    System.out.println("\tList FAIL\tNOT LOGGED IN");
                    sendClientResponse(Constants.CODE_LOGIN_NOT_LOGGED_IN);
                    break;
                } 
                if(cmd.length <= 2){
                    System.out.println("\tList FAIL\tCMD WRONG");
                    sendClientResponse(Constants.CODE_LOGIN_FAILURE);
                }else {
                    if(cmd[2].equalsIgnoreCase("-s")){
                        System.out.println("\tList Servers OK\tList sended!");
                        sendClientResponse(getServerListSTR());
                    }
                    else if(cmd[2].equalsIgnoreCase("-c")){
                        System.out.println("\tList Clients OK\tList sended!");
                        sendClientResponse(getClientsListSTR());
                    }else
                        sendClientResponse(Constants.CODE_LIST_FAILURE);
                }
                break;
            /*
                connect
            case Constants.CMD_CONNECT:
                if(!clientIsLogged(packet.getAddress())){
                    System.out.println("\tList FAIL\tNOT LOGGED IN");
                    sendClientResponse(Constants.CODE_LOGIN_NOT_LOGGED_IN);
                    break;
                } else {
                    if()
                    //
                    
                }
                break;
            */
            default:
                sendClientResponse(Constants.CODE_CMD_NOT_RECOGNIZED);
        }
        
        printClientsList();
    }
    
    private void printClientsList(){
        System.out.println("---------- Clients List ----------");
        int i = 1;
        for(Client c : clientsList){
            System.out.println(i+"-["+c.getUsername()+"]:["+c.getPassword()+"]:["
                    +c.isLogged()+"]");
            i++;
        }
        System.out.println("----------------------------------");
    }
    
    public synchronized List<Client> getClientsList() {
        return clientsList;
    }
    
    private String getClientsListSTR(){
        String list = "---------- CLIENTS LIST ----------\n";
        int i = 1;
        for(Client c : clientsList){
            list += i+":"+ c.getUsername()+"\n";
            i++;
        }
        list+="----------------------------------\n";
        return list;
    }
    
    private Client getClient(String username){
        for(Client c : clientsList)
            if(c.getUsername().equals(username))
                return c;
        return null;
    }
    
    private Client getClient(InetAddress clientAddress){
        for(Client c : clientsList)
            if(c.getClientAddress().equals(clientAddress))
                return c;
        return null;
    }
    
    private boolean clientExists(String username){
        Client c = getClient(username);
        if(c != null)
            return true;
        return false;
    }
    
    private boolean clientIsLogged(String username){
        Client c = getClient(username);
        if(c != null)
            if(c.isLogged())
                return true;
        return false;
    }
    
    private boolean clientIsLogged(InetAddress clientAddress){
        Client c = getClient(clientAddress);
        if(c != null)
            if(c.isLogged())
                return true;
        return false;
    }
    
    private boolean logInClient(String username, String password, 
            InetAddress clientAddress){
        Client c = getClient(username);
        if(c != null){
            if(c.getPassword().equals(password)){
                c.setLogged(true);
                c.setClientAddress(clientAddress);
                return true;
            }
        }
        return false;
    }
    
    private void logOutClient(InetAddress clientAddress){
        Client c = getClient(clientAddress);
        if(c != null){
            c.setLogged(false);
            c.setClientAddress(null);
        }
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
        System.out.println("Mandei INTEGER!");
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
        System.out.println("Mandei STRING!");
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
