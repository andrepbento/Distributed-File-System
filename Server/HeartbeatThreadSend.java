import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Jorge
 */
public class HeartbeatThreadSend extends Thread{
    
    
    private DatagramSocket socketSend;
    private DatagramPacket packetSend;

    public HeartbeatThreadSend(InetAddress ip) throws SocketException{
        socketSend = new DatagramSocket();
        packetSend = new DatagramPacket(Constants.HEARTBEAT_SERVER.getBytes(),Constants.HEARTBEAT_SERVER.length()
                                        ,ip, Constants.SENDING_PORT);
    }
    
    @Override
    public void run() {
        
        if(socketSend == null){
                return;
        }
        
        while(true){
            try {
                System.out.println("THREAD A FUNCIONAR ");
                socketSend.send(packetSend);
                Thread.sleep(Constants.TIME);
            } catch (IOException ex) {
                Logger.getLogger(HeartbeatThreadSend.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InterruptedException ex) {
                Logger.getLogger(HeartbeatThreadSend.class.getName()).log(Level.SEVERE, null, ex);
            }  
        }
    }
}
