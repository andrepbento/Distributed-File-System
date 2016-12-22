
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Arrays;


/**
 *
 * @author andre
 */
public class Chat {
    
    private DatagramSocket socket;
    private DatagramPacket packet;
    
    public Chat() {
        try {
            this.socket = new DatagramSocket();
        } catch (SocketException ex) {
            ex.printStackTrace();
        }
        this.packet = null;
    }
    
    private void prepareChatMSG(String username, MSG msg) {
        MSG msgToSend = new MSG();
        msgToSend.setCMD(Arrays.asList(username, msg.getCMDarg(3)));
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream(Constants.MAX_SIZE);
        try{
            ObjectOutputStream os = new ObjectOutputStream(new
                                    BufferedOutputStream(byteStream));
            os.flush();
            os.writeObject((MSG)msgToSend);
            os.flush();

            byte[] sendBuf = byteStream.toByteArray();
        
            packet = new DatagramPacket(sendBuf, sendBuf.length);
        } catch(IOException ex) {
            ex.printStackTrace();
        }
        System.out.println("Reply MSG sended!");
    }
    
    public boolean sendChatMSGToAll(String username, MSG msg) {
        prepareChatMSG(username, msg);
        try {
            for(ClientInfo ci : DirectoryService.clientsList) {
                if(/*!ci.getUsername().equals(username) &&*/ ci.isLogged()) {
                    packet.setAddress(ci.getClientAddress());
                    packet.setPort(Constants.REC_CHAT_PORT);
                    socket.send(packet);
                }
            }
        } catch(IOException e) {
            return false;
        }
        return true;
    }
    
    public boolean sendChatMSGToDesignatedClients(String username, MSG msg) {
        prepareChatMSG(username, msg);
        String[] clientsUsernames = msg.getCMDarg(2).split(",");
        for(int i = 0; i < clientsUsernames.length; i++) {
            for(ClientInfo ci : DirectoryService.clientsList) {
                if(ci.isLogged() && ci.getUsername().equals(clientsUsernames[i])){
                    packet.setAddress(ci.getClientAddress());
                    packet.setPort(Constants.REC_CHAT_PORT);
                    try {
                        socket.send(packet);
                    } catch (SocketException ex) {
                        return false;
                    } catch (IOException ex) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
}
