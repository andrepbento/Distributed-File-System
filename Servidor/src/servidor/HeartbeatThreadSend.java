
public class HeartbeatThreadSend extends Thread implements Constantes{
    private DatagramSocket socketSend;
    private DatagramPacket packetSend;

    public HeartbeatThreadSend(InetAddress ip) throws SocketException{
        socketSend = new DatagramSocket();
        packetSend = new DatagramPacket(HEARTBEAT.getBytes(),HEARTBEAT.length()
                                        ,ip, SENDING_PORT);
    }
    
    @Override
    public void run() {
        
        if(socketSend == null){
                return;
        }
        
        while(true){
            try {
                socketSend.send(packetSend);
                Thread.sleep(TIME);
            } catch (IOException ex) {
                Logger.getLogger(HeartbeatThreadSend.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InterruptedException ex) {
                Logger.getLogger(HeartbeatThreadSend.class.getName()).log(Level.SEVERE, null, ex);
            }  
        }
    }
    
    
    
}
