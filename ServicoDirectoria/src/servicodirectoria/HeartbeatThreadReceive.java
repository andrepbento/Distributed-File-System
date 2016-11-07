package servicodirectoria;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HeartbeatThreadReceive extends Thread {
    DatagramSocket socketReceive;
    DatagramPacket packetReceive;
    public static final long TIME = 30000;
    public static final int MAX_SIZE = 256;
    public static final String HEARTBEAT = "HEARTBEAT";
    

    public HeartbeatThreadReceive(InetAddress ip, int receivingPort) throws SocketException{
        socketReceive = new DatagramSocket(receivingPort);
        socketReceive.setSoTimeout(31000);
        packetReceive =null;
    }
    
    
    @Override
    public void run() {
    if(socketReceive == null){
            return;
    }
        
        while(true){
            try {
                packetReceive = new DatagramPacket(new byte[MAX_SIZE], MAX_SIZE);

                //O ip e porto de destino ja' se encontram definidos em packet
                socketReceive.receive(packetReceive);
                

            } catch (IOException ex) {
                Logger.getLogger(HeartbeatThreadReceive.class.getName()).log(Level.SEVERE, null, ex);
            } 
            
        }
    }
    
    
    
}

