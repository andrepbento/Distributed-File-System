import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Arrays;

public class HeartbeatThreadSend extends Thread{
    private DatagramSocket socketSend;
    private DatagramPacket packetSend;
    private boolean running;

    public HeartbeatThreadSend(InetAddress ip, int udpSocketPort) {
        try {
            socketSend = new DatagramSocket();
        } catch(SocketException e) {
            e.printStackTrace();
        }
        MSG heartBeatMSG = new MSG();
        heartBeatMSG.setCMD(Arrays.asList(Constants.HEARTBEAT_CLIENT, String.valueOf(udpSocketPort)));
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream(Constants.MAX_SIZE);
        try {
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(heartBeatMSG);
            oos.flush();
        } catch(IOException e) {
            e.printStackTrace();
        }
        byte[] sendBuf = baos.toByteArray();
        packetSend = new DatagramPacket(sendBuf, sendBuf.length, ip, Constants.SENDING_PORT_HB);
        running = true;
    }
    
    public void terminate() {
        running = false;
    }
    
    @Override
    public void run() {
        if(socketSend == null) 
            return;
        
        try {
            while(running){
                socketSend.send(packetSend);
                Thread.sleep(Constants.TIME);
            }
        } catch (IOException | InterruptedException ex) {
            ex.printStackTrace();
        } finally {
            terminate();
            if(socketSend != null)
                socketSend.close();
        }
    }
}
