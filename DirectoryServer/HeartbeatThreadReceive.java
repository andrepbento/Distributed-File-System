
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.List;

public class HeartbeatThreadReceive extends Thread {
    private DatagramSocket socket;
    private DatagramPacket packet;
    
    // ISTO É A CÓPIA DO QUE É PASSADO DA FUNCAO proccessReques(), MESMA REFERENCIA, APENAS COPIA DE REFERENCIA
    protected List<ServerInfo> activeServers;
    protected List<ServerInfo> serverHistory;
    
    protected List<ClientInfo> activeClients;
    
    public HeartbeatThreadReceive(List<ServerInfo> activeServers) throws SocketException{
        socket = new DatagramSocket(Constants.HB_LISTENING_PORT);
        packet = null;
        this.activeServers = activeServers;
    }
    
    public synchronized List<ServerInfo> getActiveServers() {
        return activeServers;
    }
    
    public synchronized void removeServer(ServerInfo serverInfo) {
        activeServers.remove(serverInfo);
    }
    
    private void setServerLoggedOn(ServerInfo si) {
        for(ServerInfo s : getActiveServers())
            if(s.equals(si))
                s.setLogged(true);
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
                    setServerLoggedOn(new ServerInfo(packet.getAddress(), packet.getPort()));
                } else if(receivedMSG.getCMDarg(0).equals(Constants.HEARTBEAT_CLIENT)) {
                    System.out.println(Constants.HEARTBEAT_CLIENT+" received!");
                    System.out.println("IMPLEMENTAR setClientLoggedOn(/*...*/)");
                    //setClientLoggedOn(new ClientInfo(/*...*/));
                } else
                    System.out.println("Hearth-Beat received NOT RECOGNIZED!");
                
                // VERIFICAR SE EXISTE NA LISTA O IP DE ONDE FOI RECEBIDO O HB POSTERIORMENTE (HISTÓRICO)
                
            } catch (ClassNotFoundException | IOException ex) {
                ex.printStackTrace();
            }
        }
    }
    
    class CheckIfServerIsOn extends Thread {
        @Override
        public void run() {
            try {
                while(true) {
                    Thread.sleep(Constants.TIME + 200);

                    for(ServerInfo s : getActiveServers())
                        if(!s.isLogged()) {
                            System.out.println("Server "+s.getName()+" removed!");
                            removeServer(s);
                        } else
                            s.setLogged(false);

                    // TENTAR VERIFICAR SE CONSEGUIMOS REESTABELECER LIGAÇÃO

                    System.out.print("Connected servers:  ");
                    for(ServerInfo s : getActiveServers())
                            System.out.print(s.getName()+ "\t");
                    System.out.println();
                }
            } catch(InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    
    class CheckIfClientIsOn extends Thread {
        @Override
        public void run() {
            try {
                while(true) {
                    Thread.sleep(Constants.TIME + 200);
                /*
                for(ClientInfo c : activeClients)
                    if(!c.isLogged())
                        activeClients.remove(c);
                */
                    //else
                        //c.setLog(false);
                
                // TENTAR VERIFICAR SE CONSEGUIMOS REESTABELECER LIGAÇÃO
                
                /*
                System.out.print("Clientes ligados:  ");
                for(ClientInfo c : activeClients)
                        System.out.print(c.getUsername()+ "   ");
                System.out.println("");
                */
                }
            } catch(InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

