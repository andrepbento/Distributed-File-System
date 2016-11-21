package servicodirectoria;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HeartbeatThreadReceive extends Thread implements Constantes{
    private DatagramSocket socketReceive;
    private DatagramPacket packetReceive;
    protected List<ServerRegistry> activeServers;
    protected List<ServerRegistry> serverHistory;
    
    public HeartbeatThreadReceive(List<ServerRegistry> activeServers) throws SocketException{
        socketReceive = new DatagramSocket(LISTENING_PORT);
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
        while(true){
            try {
                packetReceive = new DatagramPacket(new byte[MAX_SIZE], MAX_SIZE);
                socketReceive.receive(packetReceive);
                
                setServerLogOn(packetReceive.getAddress());
                
                // VERIFICAR SE EXISTE NA LISTA O IP DE ONDE FOI RECEBIDO O HB POSTERIORMENTE (HISTÓRICO)
                
                System.out.println("Recebi heartbeat de " + packetReceive.getAddress());
            } catch (IOException ex) {
                Logger.getLogger(HeartbeatThreadReceive.class.getName()).log(Level.SEVERE, null, ex);
            } 
            
        }
    }
    
    class CheckIfServerIsOn extends Thread {

        @Override
        public void run() {
            while(true) {
                try {
                Thread.sleep(TIME + 200);
                
                for(ServerRegistry sr : activeServers)
                    if(sr.getLog() == false)
                        activeServers.remove(sr);
                    else
                        sr.setLog(false);
                
                // TENTAR VERIFICAR SE CONSEGUIMOS REESTABELECER LIGAÇÃO
                
                System.out.print("Servidores ligados:  ");
                for(ServerRegistry sr : activeServers)
                        System.out.print(sr.getIp().toString()+ "   ");
                System.out.println("");
                
                } catch(InterruptedException e) {
                    e.printStackTrace();
                }
                
            }
        }
    }
}

