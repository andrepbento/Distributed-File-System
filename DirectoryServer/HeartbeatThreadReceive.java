
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Iterator;

public class HeartbeatThreadReceive extends Thread {
    private DatagramSocket socket;
    private DatagramPacket packet;
    
    public HeartbeatThreadReceive() throws SocketException{
        socket = new DatagramSocket(Constants.HB_LISTENING_PORT);
        packet = null;
    }
    
    private void setServerHeartBeatStateOn(ServerInfo si, String serverName,
            int serverSocketPort) {
        for(ServerInfo s : DirectoryService.serversList)
            if(s.equals(si) && s.getName().equals(serverName)) { // UM BOCADO ESTRANHO... Alterar o equals do SI para que usufrua do serverName
                s.setHeartBeatState(true);
                s.setServerSocketPort(serverSocketPort);
            }
    }
    
    private void setClientHeartBeatStateOn(ClientInfo ci, int chatDatagramSocketPort) {
        for(ClientInfo c : DirectoryService.clientsList)
            if(c.equals(ci)) {
                c.setHeartBeatState(true);
                c.setChatDatagramSocketPort(chatDatagramSocketPort);
            }
    }
    
    private void printConnectedServers() {
        System.out.println("\n--- Connected servers: ---");
        int i = 1;
        for(ServerInfo s : DirectoryService.serversList){
            System.out.println(i+" - "+s.getName()+" - ["+s.isLogged()+"]");
            i++;
        }
        System.out.println("--------------------------");
    }
    
    private void printConnectedClients() {
        System.out.println("\n--- Connected clients: ---");
        int i = 1;
        for(ClientInfo c : DirectoryService.clientsList){
            System.out.println(i+" - "+c.getUsername()+" - ["+c.isLogged()+"]");
            i++;
        }
        System.out.println("--------------------------");
    }
    
    @Override
    public void run() {
        if(socket == null)
            return;
        
        new CheckIfServerIsOn().start();
        new CheckIfClientIsOn().start();
        
        while(true){
            try {
                packet = new DatagramPacket(new byte[Constants.MAX_SIZE], Constants.MAX_SIZE);
                socket.receive(packet);
                
                ByteArrayInputStream bais = new ByteArrayInputStream(packet.getData());
                ObjectInputStream oin = new ObjectInputStream(bais);
                MSG receivedMSG = (MSG)oin.readObject();
                
                if(receivedMSG.getCMDarg(0).equals(Constants.HEARTBEAT_SERVER)) {
                    System.out.println(Constants.HEARTBEAT_SERVER+" received!");
                    setServerHeartBeatStateOn(new ServerInfo(packet.getAddress(), 
                            packet.getPort()), receivedMSG.getCMDarg(1), // FALTA DAR USO AO serverName 
                            Integer.parseInt(receivedMSG.getCMDarg(2)));
                } else if(receivedMSG.getCMDarg(0).equals(Constants.HEARTBEAT_CLIENT)) {
                    System.out.println(Constants.HEARTBEAT_CLIENT+" received!");
                    setClientHeartBeatStateOn(new ClientInfo(packet.getAddress(),
                            Integer.parseInt(receivedMSG.getCMDarg(1))), 
                            Integer.parseInt(receivedMSG.getCMDarg(2)));
                } else
                    System.out.println("Hearth-Beat received NOT RECOGNIZED!");
                    // PODE SER UMA LIGACAO CONHECIDA... PENSAR EM TENTAR RE-ESTABLECER LIGACAO
            } catch (ClassNotFoundException | IOException ex) {
                ex.printStackTrace();
            }
        }
    }
    
    class CheckIfServerIsOn extends Thread {
        @Override
        public void run() {
            while(true) {
                try {
                    Thread.sleep(Constants.TIME + 500);
                    
                    Iterator<ServerInfo> it = DirectoryService.serversList.iterator();
                    while(it.hasNext()) {
                        ServerInfo si = it.next();
                        if(!si.isHeartBeatState()) {
                            si.setLogged(false);
                        } else
                            si.setHeartBeatState(false);
                    }
                    printConnectedServers();
                } catch(InterruptedException e) {
                    e.printStackTrace();
                }    
            }
        }
    }
    
    class CheckIfClientIsOn extends Thread {
        @Override
        public void run() {
            while(true) {
                try {
                    Thread.sleep(Constants.TIME + 500);

                    Iterator<ClientInfo> it = DirectoryService.clientsList.iterator();
                    while(it.hasNext()) {
                        ClientInfo ci = it.next();
                        if(!ci.isHeartBeatState()) {
                            ci.logOut();
                        } else
                            ci.setHeartBeatState(false);
                    }

                    printConnectedClients();
                } catch(InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

