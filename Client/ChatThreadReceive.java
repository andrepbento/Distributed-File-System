
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class ChatThreadReceive extends Thread{
    private boolean running;
    MSG msg;
    DatagramPacket packet;
    DatagramSocket socket;
    ObjectInputStream objectInputStream;
    
    public ChatThreadReceive(){
        running = true;
        try {
            //socket = new DatagramSocket(0);
            socket = new DatagramSocket(Constants.REC_CHAT_PORT);
        } catch (SocketException ex) {
            ex.printStackTrace();
        }
    }
    
    public int getDatagramSocketPort(){
        return socket.getPort();
    }
    
    public void terminate(){                
        running = false;
    }
    
    @Override
    public void run(){ 
        if(!running || socket == null){
            return;
        }
        
        try{
            while(running){
                packet = new DatagramPacket(new byte[Constants.MAX_SIZE], Constants.MAX_SIZE);  
                socket.receive(packet);
                System.out.println("(" + packet.getAddress().getHostAddress() + ":" + packet.getPort() + ") ");
                
                try{                              
                    objectInputStream = new ObjectInputStream(new ByteArrayInputStream(packet.getData()));
                    
                    Object obj = objectInputStream.readObject();
                    objectInputStream.close();
                    
                    if(obj instanceof MSG){
                        msg = (MSG) obj;
                        if(msg.getCmdSize() >= 2)
                            System.out.println(msg.getCMDarg(0)+": "+msg.getCMDarg(1)); 
                    }        
                }catch(ClassNotFoundException | IOException e){
                   e.printStackTrace();
                }
            }
            
        }catch (SocketException ex) {  
            ex.printStackTrace();
        }catch (IOException ex) {              
            if(running){
                ex.printStackTrace();
            }
            
            if(!socket.isClosed()){                
                socket.close();
            }
        }  
        
    }
  
}
