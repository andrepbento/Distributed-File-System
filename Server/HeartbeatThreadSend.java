import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
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

    public HeartbeatThreadSend(InetAddress ip, DatagramSocket socketThread) throws SocketException{
        socketSend = socketThread;
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
            while(true){
                os.writeObject((MSG)mesageSend);
                os.flush();

            byte[] sendBuf = byteStream.toByteArray();
            
            packetSend = new DatagramPacket(sendBuf, sendBuf.length, ip, Constants.SENDING_PORT);
            socketSend.send(packetSend);
            sleep(32000);
            }
        } catch(IOException ex) {
            ex.printStackTrace();
        } catch (InterruptedException ex) {
            Logger.getLogger(HeartbeatThreadSend.class.getName()).log(Level.SEVERE, null, ex);
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
