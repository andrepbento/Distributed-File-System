<<<<<<< HEAD:Servidor/src/HeartbeatThreadSend.java
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


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
=======

public class HeartbeatThreadSend extends Thread implements Constantes{
>>>>>>> a16874484eedd3acc490eed34c97f2807e87f48e:Servidor/src/servidor/HeartbeatThreadSend.java
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
