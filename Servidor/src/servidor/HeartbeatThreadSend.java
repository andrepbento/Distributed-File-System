/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servidor;

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
    DatagramSocket socketSend;
    DatagramPacket packetSend;
    public static final long TIME = 30000;
    public static final String HEARTBEAT = "HEARTBEAT";
    

    public HeartbeatThreadSend(InetAddress ip, int sendingPort) throws SocketException{
        socketSend = new DatagramSocket();
        packetSend = new DatagramPacket(HEARTBEAT.getBytes(),HEARTBEAT.length()
                                        ,ip, sendingPort);
    }
    
    
    @Override
    public void run() {
    if(socketSend == null){
            return;
        }
        
        while(true){
            try {
                //Thread.sleep(TIME);

                //O ip e porto de destino ja' se encontram definidos em packet
                socketSend.send(packetSend);

            } catch (IOException ex) {
                Logger.getLogger(HeartbeatThreadSend.class.getName()).log(Level.SEVERE, null, ex);
            //} catch (InterruptedException ex) {
                //Logger.getLogger(HeartbeatThreadSend.class.getName()).log(Level.SEVERE, null, ex);
        }
            
        }
    }
    
    
    
}
