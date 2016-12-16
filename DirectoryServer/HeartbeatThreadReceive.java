
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.List;

public class HeartbeatThreadReceive extends Thread {
    private DatagramSocket socketReceive;
    private DatagramPacket packetReceive;
    
    // ISTO É A CÓPIA DO QUE É PASSADO DA FUNCAO proccessReques(), MESMA REFERENCIA, APENAS COPIA DE REFERENCIA
    protected List<ServerInfo> activeServers;
    protected List<ServerInfo> serverHistory;
    
    protected List<ClientInfo> activeClients;
    
    public HeartbeatThreadReceive(List<ServerInfo> activeServers) throws SocketException{
        socketReceive = new DatagramSocket(Constants.HB_LISTENING_PORT);
        socketReceive.setSoTimeout(31000);
        packetReceive = null;
        this.activeServers = activeServers;
    }
    
    private void setServerLoggedOn(ServerInfo si) {
        for(ServerInfo s : activeServers)
            if(s.equals(si))
                s.setLogged(true);
    }
    
    @Override
    public void run() {
        if(socketReceive == null){
                return;
        }
        
        new CheckIfServerIsOn().start();
        new CheckIfClientIsOn().start();
        
        while(true){
            try {
                packetReceive = new DatagramPacket(new byte[Constants.MAX_SIZE], Constants.MAX_SIZE);
                socketReceive.receive(packetReceive);
                
                String hbReceived = new String(packetReceive.getData());
                
                if(hbReceived.equals(Constants.HEARTBEAT_SERVER)) {
                    System.out.println(Constants.HEARTBEAT_SERVER+" received!");
                    setServerLoggedOn(new ServerInfo(packetReceive.getAddress(), packetReceive.getPort()));
                } else if(hbReceived.equals(Constants.HEARTBEAT_CLIENT)) {
                    System.out.println(Constants.HEARTBEAT_CLIENT+" received!");
                    System.out.println("IMPLEMENTAR setClientLoggedOn(/*...*/)");
                    //setClientLoggedOn(new ClientInfo(/*...*/));
                } else
                    System.out.println("Hearth-Beat received NOT RECOGNIZED!");
                
                // VERIFICAR SE EXISTE NA LISTA O IP DE ONDE FOI RECEBIDO O HB POSTERIORMENTE (HISTÓRICO)
                
                System.out.println("Recebi heartbeat de " + packetReceive.getAddress());
            } catch (IOException ex) {
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

                    synchronized(activeServers) {
                        for(ServerInfo s : activeServers)
                            if(!s.isLogged()) {
                                System.out.println("Server "+s.getName()+" removed!");
                                activeServers.remove(s);
                            } else
                                s.setLogged(false);

                        // TENTAR VERIFICAR SE CONSEGUIMOS REESTABELECER LIGAÇÃO

                        System.out.print("Connected servers:  ");
                        for(ServerInfo s : activeServers)
                                System.out.print(s.getIp().toString()+ "\t");
                        System.out.println();
                    }
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

