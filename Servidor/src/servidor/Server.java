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
public class Server implements Constantes{
    private String name;
    private DatagramSocket socket;
    private DatagramPacket packet; //para receber os pedidos e enviar as respostas
    private InetAddress ip;
    private int listeningPort;
    
    public Server(String name, InetAddress ip, int listeningPort) throws SocketException 
    {
        this.name = name;
        this.ip = ip;
        this.listeningPort = listeningPort;
        socket = null;
        packet = null;
        socket = new DatagramSocket();
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
    
    public void sendRegister() throws IOException{
        String msg = "SERVIDOR " + this.name + " ";
        packet = new DatagramPacket(msg.getBytes(), msg.length(), ip, listeningPort);
        socket.send(packet);
    }
    
    public void closeSocket()
    {
        if(socket != null){
            socket.close();
        }
    }
    
    public static void main(String[] args) throws InterruptedException {
        InetAddress ip;
        int porto;
        Server servidor = null;
        String serverName;
        
        if(args.length != 4){
            System.out.println("Sintaxe: java Servidor Nome ip listeningPort");
            return;
        }
        
        try{
            serverName = args[1];
            ip = InetAddress.getByName(args[2]);
            porto = Integer.parseInt(args[3]);
            
            servidor = new Server(serverName, ip, porto); 
            servidor.sendRegister();
            new HeartbeatThreadSend(ip).start();
            
            while(true) {
                System.out.printf("z");
                Thread.sleep(1000);
            }
            
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

