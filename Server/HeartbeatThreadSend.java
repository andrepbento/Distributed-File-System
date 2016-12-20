import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Jorge
 */
public class HeartbeatThreadSend extends Thread{
    
    
    private DatagramSocket socketSend;
    private DatagramPacket packetSend;
    InetAddress ip;

    public HeartbeatThreadSend(InetAddress ip) throws SocketException{
        socketSend = new DatagramSocket();
        this.ip = ip;
        //packetSend = new DatagramPacket(Constants.HEARTBEAT_SERVER.getBytes(),Constants.HEARTBEAT_SERVER.length(),ip, Constants.SENDING_PORT);
    }
    
    @Override
    public void run() {
        
         ByteArrayOutputStream byteStream = new ByteArrayOutputStream(Constants.MAX_SIZE);
        
        try{
            ObjectOutputStream os = new ObjectOutputStream(new
                                    BufferedOutputStream(byteStream));
            os.flush();
            
            MSG mesageSend = new MSG();
            List<String> listSend = new ArrayList<>();
            
            //ADICIONAR OS COMANDS A LIST
            listSend.add(Constants.HEARTBEAT_SERVER);
            
            //ADICIONAR A LIST A MSG
            mesageSend.setCMD(listSend);
            
            //ENVIAR
            os.writeObject((MSG)mesageSend);
            os.flush();

            byte[] sendBuf = byteStream.toByteArray();
            
            packetSend = new DatagramPacket(sendBuf, sendBuf.length, ip, Constants.SENDING_PORT);
            socketSend.send(packetSend);
            
        } catch(IOException ex) {
            ex.printStackTrace();
        }
        System.out.println("Registration submitted...");
        
    }
}
