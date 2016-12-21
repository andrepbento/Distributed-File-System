
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Arrays;
import java.util.List;


/**
 *
 * @author andre
 */
public class Chat {
    
    private DatagramSocket socket;
    private DatagramPacket packet;
    
    public Chat() {
        this.socket = null;
        this.packet = null;
    }
    
    private void prepareChatMSG(String username, MSG msg) {
        MSG msgToSend = new MSG();
        msgToSend.setCMD(Arrays.asList(username, msg.getCMDarg(2)));
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream(Constants.MAX_SIZE);
        try{
            ObjectOutputStream os = new ObjectOutputStream(new
                                    BufferedOutputStream(byteStream));
            os.flush();
            os.writeObject((MSG)msg);
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
                if(!ci.equals(new ClientInfo(username)) && ci.isLogged()) {
                    packet.setAddress(ci.getClientAddress());
                    packet.setPort(Constants.REC_CHAT_PORT);
                    socket = new DatagramSocket();
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
        String[] clientsUsernames = msg.getCMDarg(0).split(",");
        for(int i = 0; i < clientsUsernames.length; i++) {
            for(ClientInfo ci : DirectoryService.clientsList) {
                if(ci.isLogged() && ci.equals(new ClientInfo(clientsUsernames[i]))){
                    packet.setAddress(ci.getClientAddress());
                    packet.setPort(Constants.REC_CHAT_PORT);
                    try {
                        socket = new DatagramSocket();
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
