import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class HeartbeatThreadSend extends Thread{
    private DatagramSocket socketSend;
    private DatagramPacket packetSend;

    public HeartbeatThreadSend(InetAddress ip) throws SocketException{
        socketSend = new DatagramSocket();
        packetSend = new DatagramPacket(Constants.HEARTBEAT_CLIENT.getBytes(), 
                Constants.HEARTBEAT_CLIENT.length(), ip, Constants.SENDING_PORT_HB);
    }
    
    @Override
    public void run() {
        
        if(socketSend == null){
                return;
        }
        
        while(true){
            try {
                socketSend.send(packetSend);
                Thread.sleep(Constants.TIME);
            } catch (IOException ex) {
                System.out.println("Thread interrupted (Sleep)\n\t"+ex);
            } catch (InterruptedException ex) {
                System.out.println("Thread interrupted (Sleep)\n\t"+ex);
            }finally{
                socketSend.close();
            }  
        }
    }
}
