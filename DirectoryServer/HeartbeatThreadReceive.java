
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.List;

public class HeartbeatThreadReceive extends Thread {
    private DatagramSocket socketReceive;
    private DatagramPacket packetReceive;
    protected List<ServerRegistry> activeServers;
    protected List<ServerRegistry> serverHistory;
    
    protected List<Client> activeClients;
    //protected List<Client
    
    public HeartbeatThreadReceive(List<ServerRegistry> activeServers) throws SocketException{
        socketReceive = new DatagramSocket(Constants.HD_LISTENING_PORT);
        socketReceive.setSoTimeout(31000);
        packetReceive = null;
        this.activeServers = activeServers;
    }
    
    private void setServerLogOn(InetAddress serverIp) {
        for(ServerRegistry sr : activeServers)
            if(sr.getIp().equals(serverIp))
                sr.setLog(true);
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
                
                setServerLogOn(packetReceive.getAddress());
                
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
            while(true) {
                try {
                Thread.sleep(Constants.TIME + 200);
                
                for(ServerRegistry s : activeServers)
                    if(!s.getLog())
                        activeServers.remove(s);
                    else
                        s.setLog(false);
                
                // TENTAR VERIFICAR SE CONSEGUIMOS REESTABELECER LIGAÇÃO
                
                System.out.print("Connected servers:  ");
                for(ServerRegistry s : activeServers)
                        System.out.print(s.getIp().toString()+ "\t");
                System.out.println();
                
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
                Thread.sleep(Constants.TIME + 200);
                
                for(Client c : activeClients)
                    if(!c.isLogged())
                        activeClients.remove(c);
                    //else
                        //c.setLog(false);
                
                // TENTAR VERIFICAR SE CONSEGUIMOS REESTABELECER LIGAÇÃO
                
                System.out.print("Clientes ligados:  ");
                for(Client c : activeClients)
                        System.out.print(c.getUsername()+ "   ");
                System.out.println("");
                
                } catch(InterruptedException e) {
                    e.printStackTrace();
                }
                
            }
        }
    }
}

