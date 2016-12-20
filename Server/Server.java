/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.File;
import java.io.FileOutputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Luis
 */
public class Server{
    private String name;
    private DatagramSocket socket;
    private DatagramPacket packet; //para receber os pedidos e enviar as respostas
    private InetAddress ip;
    private int sendingPort;
    //static List<HeartbeatThreadSend> heartBeatList;
    private HeartbeatThreadSend heartBeat;
    private ServerSocket serverSocket;
    private List<processClientRequest> listClientsPRequest;
    private List<Socket> listClientsSockets;
    private MSG msgReceived;
    
    public Server(String name, InetAddress ip, int sendingPort) throws SocketException 
    {
        this.name = name;
        this.ip = ip;
        this.sendingPort = sendingPort;
        packet = null;
        socket = new DatagramSocket();
        listClientsSockets = new ArrayList<>();
    }

    //Regista-se mas se já ouver um server com o mesmo nome não regista
    public void sendRegister(){
        
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream(Constants.MAX_SIZE);
        
        try{
            ObjectOutputStream os = new ObjectOutputStream(new
                                    BufferedOutputStream(byteStream));
            os.flush();
            
            MSG mesageSend = new MSG();
            List<String> listSend = new ArrayList<>();
            
            //ADICIONAR OS COMANDS A LIST
            listSend.add(Constants.SERVER);
            listSend.add(this.name);
            
            //ADICIONAR A LIST A MSG
            mesageSend.setCMD(listSend);
            
            //ENVIAR
            os.writeObject((MSG)mesageSend);
            os.flush();

            byte[] sendBuf = byteStream.toByteArray();
            
            packet = new DatagramPacket(sendBuf, sendBuf.length, ip, sendingPort);
            socket.send(packet);
            
        } catch(IOException ex) {
            ex.printStackTrace();
        }
        System.out.println("Registration submitted...");
    }
    
    public boolean receiveRegisterAnswer(){
        packet = new DatagramPacket(new byte[Constants.MAX_SIZE],Constants.MAX_SIZE);
        try{
            socket.receive(packet);

            ByteArrayInputStream bin = new ByteArrayInputStream(packet.getData(), 0, packet.getLength());
            ObjectInputStream ois = new ObjectInputStream(bin);

            msgReceived = (MSG)ois.readObject();
            
            int code = msgReceived.getMSGCode();
            System.out.println("DirectoryService receiveRegisterAnswer(): "
                    +msgReceived.getMSGCode());

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
            this.closeSocket();
        }
        return false;
    }
    
    public void processClientConnections(){
        serverSocket = null;
        
        try{
            serverSocket = new ServerSocket(0);
            
            System.out.println("Waiting client connections: "
                    +InetAddress.getLocalHost().getHostAddress()+":"
                    +serverSocket.getLocalPort());

            Socket clientSocket = serverSocket.accept();
            
            listClientsSockets.add(clientSocket);
            
            // LANCAR A THREAD PARA TRATAR DO CLIENTE X
            //PRIMEIRO CRIA A FUNÇÃO QUE VAI ATENDER ESSE CLIENTE
            processClientRequest newClient = new processClientRequest(serverSocket, clientSocket);
            
            //GUARDA A FUNÇÃO E O SOCKET PARA ESSE CLIENTE
            this.listClientsPRequest.add(newClient);
            this.listClientsSockets.add(clientSocket);
            
            //INICIA A THREAD
            newClient.start();
        
        }catch(IOException e){
            e.printStackTrace();
        }
    }
    
    public void closeSocket(){
        try {
            if(socket != null)
                socket.close();
            if(serverSocket != null)
                serverSocket.close();
            for(Socket s : listClientsSockets)
                if(s != null)
                    s.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }
    
    public void stopServer(){
        closeSocket();
        System.out.println("Server stoped!");
        System.exit(0);
    }
    
    public static void main(String[] args) {
        InetAddress ip;
        int porto;
        Server server = null;
        String serverName;
        
        if(args.length != 3){
            System.out.println("Sintaxe: java Servidor Nome ip sendingPort");
            return;
        }
        
        try{
            //Preencher os campos de registo
            serverName = args[0];
            ip = InetAddress.getByName(args[1]);
            porto = Integer.parseInt(args[2]);
            
            //Registar o Servidor
            server = new Server(serverName, ip, porto); 
            System.out.println("Server "+serverName+" started...\n");
            server.sendRegister();
            if(!server.receiveRegisterAnswer())
                server.stopServer();
            
            //INICIALIZAR A HEARTBEAT THREAD
            server.heartBeat = new HeartbeatThreadSend(ip);
            server.heartBeat.start();
            
            //INICIALIZA COMUNICAÇÃO TCP
            server.processClientConnections();
            
        }catch(NumberFormatException e){
            System.out.println("O porto de escuta deve ser um inteiro positivo.");
        }catch(NullPointerException e){
            System.out.println("Null pointer exception apanho do hertbeat." + e);
            e.printStackTrace();
        }catch(IOException e){
            e.printStackTrace();
        }catch(Exception e){
            System.out.println(e);
        }
        //System.out.println("Ocorreu um erro no registo do server:\n\t"+e);
        finally{
            if(server != null){
                server.closeSocket();
            }
        }
    }
}

