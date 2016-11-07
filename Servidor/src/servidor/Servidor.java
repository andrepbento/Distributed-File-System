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

/**
 *
 * @author Luis
 */
public class Servidor {
    
    public static final int MAX_SIZE = 256;
    
    private DatagramSocket socket;
    private DatagramPacket packet; //para receber os pedidos e enviar as respostas
    InetAddress ip;
   


    public Servidor(InetAddress ip, int listeningPort) throws SocketException 
    {
        socket = null;
        packet = null;
        socket = new DatagramSocket();
        this.ip = ip;
    }
    
    /*public String waitDatagram() throws IOException
    {
        String request;
        
        if(socket == null){
            return null;
        }
        
        packet = new DatagramPacket(new byte[MAX_SIZE], MAX_SIZE);
        socket.receive(packet);
        request = new String(packet.getData(), 0, packet.getLength());
        
        if(debug){
            System.out.println("Recebido \"" + request + "\" de " + 
                    packet.getAddress().getHostAddress() + ":" + packet.getPort());
        }
        
        return request;
    
    }*/
    
    public void processHeartBeat() throws IOException, InterruptedException
    {      
        
    }
    
    public void closeSocket()
    {
        if(socket != null){
            socket.close();
        }
    }
    
    public static void main(String[] args) {
        InetAddress ip;
        int porto;
        Servidor servidor = null;
        
        if(args.length != 1){
            System.out.println("Sintaxe: java Servidor ip listeningPort");
            return;
        }
        
        try{
            ip = InetAddress.getByName(args[0]);
            porto = Integer.parseInt(args[1]);
            
            servidor = new Servidor(ip, porto); 
            new HeartbeatThreadSend(ip, porto).start();
            
        }catch(NumberFormatException e){
            System.out.println("O porto de escuta deve ser um inteiro positivo.");
        }catch(SocketException e){
            System.out.println("Ocorreu um erro ao nível do socket UDP:\n\t"+e);
        }catch(IOException e){
            System.out.println("Ocorreu um erro no acesso ao socket:\n\t"+e);
        }finally{
            if(servidor != null){
                servidor.closeSocket();
            }
        }
    }
}

