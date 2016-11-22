/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servicodirectoria;

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

/**
 *
 * @author andre
 */
//OLA LUIS
//RECEBI O OLA!


//Tudo ok?
public class DirectoryService extends Thread implements Constantes{
    private DatagramSocket socket;
    private DatagramPacket packet; //para receber os pedidos e enviar as respostas
    private List<ServerRegistry> activeServers;
    private boolean threadIsRunning = false;

    public DirectoryService(int listeningPort) throws SocketException
    {
        socket = null;
        packet = null;       
        socket = new DatagramSocket(listeningPort);
        activeServers = new ArrayList<>();
    }

    public synchronized List<ServerRegistry> getActiveServers() {
            return activeServers;
    }
    
    public String waitDatagram() throws IOException
    {
        String request;
        
        if(socket == null){
            return null;
        }
        
        packet = new DatagramPacket(new byte[MAX_SIZE], MAX_SIZE);
        socket.receive(packet);

        try{
            request = new String(packet.getData());
        }catch(ClassCastException e){
             System.out.println("Recebido objecto diferente de String " + 
                    packet.getAddress().getHostAddress() + ":" + packet.getPort());
            return null;
        }

        System.out.println("Recebido \"" + request + "\" de " + 
                packet.getAddress().getHostAddress() + ":" + packet.getPort());
        
        return request;
    
    }
    
    public boolean serverExists(String nome){   
        for (ServerRegistry sr : activeServers) {
            if(sr.getNome().equalsIgnoreCase(nome))
                return true;
        }
        return false;
    }
    
    public void processRequests()
    {
        String receivedMsg;
        ByteArrayOutputStream bOut;
        ObjectOutputStream out;
         
        if(socket == null){
            return;
        }
        
        while(true){
                
            try{                    

                receivedMsg = waitDatagram();

                if(receivedMsg == null){
                    continue;
                }

                //Dados
                String []comando= receivedMsg.split(" ");
                InetAddress ip;
                int porto;
                String name;
                                
                switch(comando[0].toUpperCase()){
                    case "SERVIDOR": 
                        name = comando[1];
                        ip = packet.getAddress();
                        porto = packet.getPort();
                        
                        if(!serverExists(name)) {
                            activeServers.add(new ServerRegistry(name, ip, porto));
                            System.out.println("Ligou-se o servidor: " + ip + " porto: " + porto);
                            if(!threadIsRunning){ //Só corre uma vez
                                new HeartbeatThreadReceive(activeServers).start();
                                threadIsRunning = true;
                            }
                        }
                        break;
                    case "CLIENTE": 
                        
                        break;
                    default: 
                        continue;
                }
                
                /*
                bOut = new ByteArrayOutputStream(MAX_SIZE);            
                out = new ObjectOutputStream(bOut);

                packet.setData(bOut.toByteArray());
                packet.setLength(bOut.size());

                System.out.println("Tamanho da resposta serializada: "+bOut.size());
                //O ip e porto de destino já se encontram definidos em packet
                //socket.send(packet);
                */
                
            }catch(IOException e){
                System.out.println(e);
            }catch(NumberFormatException e){
                System.out.println(e);
            }catch(Exception e){
                System.out.println(e);
            }
        }
            
        
    }
    
    public void closeSocket()
    {
        if(socket != null){
            socket.close();
        }
    }
    
    public static void main(String[] args) {
        if(args[0] == null) {
            System.out.println("Syntax error: ServicoDirectoria <listeningPort>");
            return;
        }
        
        try {
            DirectoryService sd = new DirectoryService(Integer.parseInt(args[0]));
            sd.processRequests();
        } catch(SocketException e) {
            e.printStackTrace();
        }
    }
    
}
