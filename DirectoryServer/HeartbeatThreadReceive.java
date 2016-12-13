
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.List;

public class HeartbeatThreadReceive extends Thread {
    private DatagramSocket socketReceive;
    private DatagramPacket packetReceive;
    protected List<Server> activeServers;
    protected List<Server> serverHistory;
    
    protected List<Client> activeClients;
    //protected List<Client
    
    public HeartbeatThreadReceive(List<Server> activeServers) throws SocketException{
        socketReceive = new DatagramSocket(Constants.HD_LISTENING_PORT);
        socketReceive.setSoTimeout(31000);
        packetReceive = null;
        this.activeServers = activeServers;
    }
    
    private void setServerLogOn(InetAddress serverIp) {
        for(Server sr : activeServers)
            if(sr.getIp().equals(serverIp))
                sr.setLogged(true);
    }
    
    @Override
    public void run() {
        
        if(socketReceive == null){
                return;
        }
                
        new CheckIfServerIsOn().start();
        new CheckIfClientIsOn().start();
        while(true){
            /*
            try {
                packetReceive = new DatagramPacket(new byte[Constants.MAX_SIZE], Constants.MAX_SIZE);
                socketReceive.receive(packetReceive);
                
                setServerLogOn(packetReceive.getAddress());
                
                // VERIFICAR SE EXISTE NA LISTA O IP DE ONDE FOI RECEBIDO O HB POSTERIORMENTE (HISTÓRICO)
                
                System.out.println("Recebi heartbeat de " + packetReceive.getAddress());
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            */
        }
    }
    
    class CheckIfServerIsOn extends Thread {

        @Override
        public void run() {
            while(true) {
                /*
                try {
                    
                Thread.sleep(Constants.TIME + 200);
                
                for(Server s : activeServers)
                    if(!s.isLogged())
                        activeServers.remove(s);
                    else
                        s.setLogged(false);
                
                // TENTAR VERIFICAR SE CONSEGUIMOS REESTABELECER LIGAÇÃO
                
                System.out.print("Connected servers:  ");
                for(Server s : activeServers)
                        System.out.print(s.getIp().toString()+ "\t");
                System.out.println();
                
                } catch(InterruptedException e) {
                    e.printStackTrace();
                }
                */
            }
        }
    }
    
    class CheckIfClientIsOn extends Thread {

        @Override
        public void run() {
            while(true) {
                try {
                Thread.sleep(Constants.TIME + 200);
                
                /*
                for(Client c : activeClients)
                    if(!c.isLogged())
                        activeClients.remove(c);
                */
                    //else
                        //c.setLog(false);
                
                // TENTAR VERIFICAR SE CONSEGUIMOS REESTABELECER LIGAÇÃO
                
                /*
                System.out.print("Clientes ligados:  ");
                for(Client c : activeClients)
                        System.out.print(c.getUsername()+ "   ");
                System.out.println("");
                */
                } catch(InterruptedException e) {
                    e.printStackTrace();
                }
                
            }
        }
    }
}

