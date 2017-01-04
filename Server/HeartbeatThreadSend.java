import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;

/**
 *
 * @author Jorge
 */
public class HeartbeatThreadSend extends Thread {
    private InetAddress ip;
    private DatagramSocket socketSend;
    private DatagramPacket packetSend;
    private boolean running;

    public HeartbeatThreadSend(InetAddress ip, DatagramSocket socketSend,
            String serverName, int serverSocketPort) {
        this.ip = ip;
        this.socketSend = socketSend;
        MSG heartBeatMSG = new MSG();
        heartBeatMSG.setCMD(Arrays.asList(Constants.HEARTBEAT_SERVER, serverName,
                String.valueOf(serverSocketPort)));
        
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
            while(running) {
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
    /*
    public boolean receiveHeartBeatAnswer(){
        packetSend = new DatagramPacket(new byte[Constants.MAX_SIZE],Constants.MAX_SIZE);
        try{
            socketSend.receive(packetSend);
            

            ByteArrayInputStream bin = new ByteArrayInputStream(packet.getData(), 0, packet.getLength());
            ObjectInputStream ois = new ObjectInputStream(bin);

            MSG mesageReceived= (MSG)ois.readObject();
            
            int code = mesageReceived.getMSGCode();
            System.out.println("DirectoryService HEARBEAT answer: "
                    +mesageReceived.getMSGCode());

            switch(code){
                case Constants.CODE_SERVER_REGISTER_OK:
                    System.out.println("Register OK");
                    return true;
                case Constants.CODE_SERVER_REGISTER_FAILURE:
                    throw  new Exceptions.ConnectFailure("Register FAILURE");
            }
        }catch(ClassNotFoundException | IOException e){
            e.printStackTrace();
        }catch(Exceptions.ConnectFailure ex){
            System.out.println("\n"+ex);
        }finally{
            //this.closeSocket();
        }
        return false;
    }
    */ 
}
