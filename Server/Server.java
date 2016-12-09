/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
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
    private int listeningPort;
    static List<HeartbeatThreadSend> heartBeatList;
    private ServerSocket serverSocket;
    
    public Server(String name, InetAddress ip, int listeningPort) throws SocketException 
    {
        this.name = name;
        this.ip = ip;
        this.listeningPort = listeningPort;
        socket = null;
        packet = null;
        socket = new DatagramSocket();
        heartBeatList =new ArrayList<>();
    }
    
    public String waitDatagram() throws IOException
    {
        String request;
        
        if(socket == null){
            return null;
        }
        
        packet = new DatagramPacket(new byte[Constants.MAX_SIZE], Constants.MAX_SIZE);
        socket.receive(packet);
        request = new String(packet.getData(), 0, packet.getLength());
        
        if(true){
            System.out.println("Recebido \"" + request + "\" de " + 
                    packet.getAddress().getHostAddress() + ":" + packet.getPort());
        }
        
        return request;
    
    }
    
    //Regista-se mas se já ouver um servidor com o mesmo nome não regista
    public boolean sendRegister() throws IOException{
        Constants.ME
        String msg = "SERVER " + this.name + " ";
        String resultado;
        System.out.println("CHEGUEI AQUI");
        packet = new DatagramPacket(msg.getBytes(), msg.length(), ip, listeningPort);
        socket.send(packet);
                System.out.println("CHEGUEI AQUI");

        packet = new DatagramPacket(new byte[Constants.MAX_SIZE],Constants.MAX_SIZE);
        socket.receive(packet);        
        System.out.println("CHEGUEI AQUI");
        resultado = new String(packet.getData(),0,packet.getLength());
        System.out.println("O registo foi enviado para o Serviço de directoria"
                            + " e o resultado foi o seguinte: [" 
                            + resultado + "]");
        if(resultado.equalsIgnoreCase("REGISTADO")==true)
            return true;
        if(resultado.equalsIgnoreCase("ERRO")==true)
            return false;
        return false;
    }
    
    public boolean inicializeTCP(int listPort) throws IOException{
        int listeningPort;
        
        serverSocket = null;
        
        try{
            listeningPort = listPort;
            //Coloca o serversocket a ouvir no porto de escuta para onde os clietnes irão mandar
            serverSocket = new ServerSocket(listeningPort);
            
        }catch(NumberFormatException e){
            System.out.println("O porto de escuta deve ser um inteiro positivo.");
        }catch(IOException e){
            System.out.println("Ocorreu um erro no acesso ao socket:\n\t"+e);
            serverSocket = null;
        }
        return true;
    }
    
    public final void processRequestsClient() throws IOException{
        Socket toClientSocket;
        BufferedReader in;
        PrintWriter out;
        String request;
        
        if(serverSocket == null){
            return;
        }
        
        System.out.println("TCP conection started [PORT: " + socket.getLocalPort()+"]");
        
        while(true){
            try{
                toClientSocket = serverSocket.accept();
            }catch(IOException e){
                System.out.println("ERROR while waiting for a conection request:\n\t");
                return;
            }
            try{
                out = new PrintWriter(toClientSocket.getOutputStream(),true);
                in = new BufferedReader(new InputStreamReader(toClientSocket.getInputStream()));
                
                request = in.readLine();
                
                if(request == null){
                    toClientSocket.close();
                    continue;
                }
                
                System.out.println("Recebido \"" + request.trim() + 
                "\" de " + toClientSocket.getInetAddress().getHostAddress() 
                + ":" + toClientSocket.getLocalPort()); //trim apaga os espaços brancos
                
                //TRATA DOS CLIENTES POSTERIORMENTE
                //           ( . . . )
            }catch(IOException e){
                System.out.println("Erro na comunicacao como o cliente " + 
                                toClientSocket.getInetAddress().getHostAddress() + ":" + 
                                    toClientSocket.getPort()+"\n\t" + e);
                }finally{
                        try{
                            toClientSocket.close();
                        }catch(IOException e){}
                }
            }
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
            //Preencher os campos de registo
            serverName = args[1];
            ip = InetAddress.getByName(args[2]);
            porto = Integer.parseInt(args[3]);
            
            //Registar o Servidor
            servidor = new Server(serverName, ip, porto); 
            if(servidor.sendRegister()==true)
                System.out.println("Servidor Registado com sucesso.");
            else{
                System.out.println("NAO ME REGISTEI");
                throw new IOException();
            }
            HeartbeatThreadSend novaHeartBeat = new HeartbeatThreadSend(ip);
            heartBeatList.add(novaHeartBeat);
            novaHeartBeat.start();
            
            servidor.inicializeTCP(porto);
            
            while(true) {
                System.out.printf("z");
                
                //trata clientes
                
                Thread.sleep(1000);
            }
            
        }catch(NumberFormatException e){
            System.out.println("O porto de escuta deve ser um inteiro positivo.");
        }catch(NullPointerException e){
            System.out.println("Null pointer exception apanho do hertbeat." + e);
        }catch(SocketException e){
            System.out.println("Ocorreu um erro ao nível do socket UDP:\n\t"+e);
        }catch(IOException e){
            System.out.println("Ocorreu um erro no registo do servidor:\n\t"+e);
        }finally{
            if(servidor != null){
                servidor.closeSocket();
            }
        }
    }
}

