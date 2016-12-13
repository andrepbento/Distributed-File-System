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
    private int listeningPort;
    //static List<HeartbeatThreadSend> heartBeatList;
    private HeartbeatThreadSend heartBeat;
    private ServerSocket serverSocket;
    private Socket socketToClient;   
    MSG msgReceived;
    
    public Server(String name, InetAddress ip, int listeningPort) throws SocketException 
    {
        this.name = name;
        this.ip = ip;
        this.listeningPort = listeningPort;
        socket = null;
        packet = null;
        socket = new DatagramSocket();
    }
    
    public void waitDatagram() throws IOException, ClassNotFoundException
    {
        String request;
        
        if(socket == null){
            throw new SocketException();
        }
        
        packet = new DatagramPacket(new byte[Constants.MAX_SIZE], Constants.MAX_SIZE);
        socket.receive(packet);
        
        ByteArrayInputStream bin = new ByteArrayInputStream(packet.getData(), 0, packet.getLength());
        ObjectInputStream ois = new ObjectInputStream(bin);
        
        try{
            System.out.println("VOU TRATAR DA MENSAGEM");
            
        MSG msgReceived = (MSG)ois.readObject();

        System.out.println(msgReceived.toStringCMD());

        }catch(ClassNotFoundException e){
            System.err.println("CLASS NOT FOUND EXCEPTION " + e);
        }catch(SocketException e){
            System.err.println("SOCKET NOT FOUND EXCEPTION " + e);
        }
    
    }
    
    //Regista-se mas se já ouver um servidor com o mesmo nome não regista
    public boolean sendRegister() throws IOException, ClassNotFoundException{
        
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream(Constants.MAX_SIZE);
        
        try{
            ObjectOutputStream os = new ObjectOutputStream(new
                                    BufferedOutputStream(byteStream));
            os.flush();
            MSG mesageSend = new MSG();
            List<String> listSend = new ArrayList<>();
            listSend.add("SERVER");
            listSend.add(this.name);
            mesageSend.setCMD(listSend);
            os.writeObject((MSG)mesageSend);
            os.flush();

            byte[] sendBuf = byteStream.toByteArray();
            
            packet = new DatagramPacket(sendBuf, sendBuf.length, ip, listeningPort);
            socket.send(packet);
            
        } catch(IOException ex) {
            ex.printStackTrace();
        }
        System.out.println("MANDEI O REGISTO!");
        
        packet = new DatagramPacket(new byte[Constants.MAX_SIZE],Constants.MAX_SIZE);
        /*socket.receive(packet);
        
        ByteArrayInputStream bin = new ByteArrayInputStream(packet.getData(), 0, packet.getLength());
        ObjectInputStream ois = new ObjectInputStream(bin);
        */
        //try{
            
        //msgReceived = (MSG)ois.readObject();
        
        //System.out.println(msgReceived.toStringCMD());
        
            System.out.println("FALTA TRATAR DOS COMANDOS QUE O SD ENVIA");
        /*if(!msgReceived.getCMD().isEmpty()){
            if(msgReceived.getCMD().get(0).equals("REGISTERED")==true)
                return true;
            if(msgReceived.getCMD().get(0).equals("ERRO")==true)
                return false;
        }*/
        /*}catch(ClassNotFoundException excp){
            System.err.println("Class Not Found Exception :[" + excp + "]");;
        }*/
        
        
        
        /*
        //MANEIRA ANTIGA DE ENVIAR REGISTO
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
            return false;*/
        
        return false;
    }
    /*
    public boolean inicializeTCP(int listPort) throws IOException{
        int listeningPort;
        
        serverSocket = null;
        
        
            listeningPort = listPort;
            //Coloca o serversocket a ouvir no porto de escuta para onde os clietnes irão mandar
            serverSocket = new ServerSocket(listeningPort);
            
            try{
                        
                socketToClient = serverSocket.accept();
            
            }catch(IOException e){
                System.out.println("Ocorreu uma excepcao no socket enquanto aguardava por um pedido de ligacao: \n\t" + e);
                System.out.println("O servidor vai terminar...");
                return false;
            }
            
            
            
            
            
            
            
            
            
            
            
            //Receber Comando para ficheiro
            
            File localDirectory;
            String fileName, localFilePath = null;
            FileOutputStream localFileOutputStream = null;
            Socket socketToClient = null;
            PrintWriter pout;
            InputStream in;
            byte []fileChunck = new byte[Constants.MAX_SIZE];
            int nbytes;      

            if(msgReceived.getCMD().size() != 4){
                System.out.println("Sintaxe: CLIENT FILE_NAME FILE_PATH_LOCATION");
                return false;
            }        

            fileName = msgReceived.getCMD().get(2).trim();
            localDirectory = new File(msgReceived.getCMD().get(3).trim());

            if(!localDirectory.exists()){
                System.out.println("A directoria " + localDirectory + " nao existe!");
                return false;
            }

            if(!localDirectory.isDirectory()){
                System.out.println("O caminho " + localDirectory + " nao se refere a uma directoria!");
                return false;
            }

            if(!localDirectory.canWrite()){
                System.out.println("Sem permissoes de escrita na directoria " + localDirectory);
                return false;
            }

            try{
                try{

                    localFilePath = localDirectory.getCanonicalPath()+File.separator+fileName;
                    localFileOutputStream = new FileOutputStream(localFilePath);
                    System.out.println("Ficheiro " + localFilePath + " criado.");

                }catch(IOException e){

                    if(localFilePath == null){
                        System.out.println("Ocorreu a excepcao {" + e +"} ao obter o caminho canonico para o ficheiro local!");   
                    }else{
                        System.out.println("Ocorreu a excepcao {" + e +"} ao tentar criar o ficheiro " + localFilePath + "!");
                    }

                    return false;
                }

                try{

                    //FALTA PREENCHER OS CAMPOS DO IP E PORTO
                    socketToClient = new Socket(args[0], serverPort);

                    socketToClient.setSoTimeout((int) (Constants.TIME_OUT*1000));

                    in = socketToClient.getInputStream();
                    pout = new PrintWriter(socketToClient.getOutputStream(), true);

                    pout.println(fileName);
                    pout.flush();

                    while((nbytes = in.read(fileChunck)) > 0){                    
                        //System.out.println("Recebido o bloco n. " + ++contador + " com " + nbytes + " bytes.");
                        localFileOutputStream.write(fileChunck, 0, nbytes);
                        //System.out.println("Acrescentados " + nbytes + " bytes ao ficheiro " + localFilePath+ ".");                    
                    }                    

                    System.out.println("Transferencia concluida.");

                }catch(UnknownHostException e){
                     System.out.println("Destino desconhecido:\n\t"+e);
                } 

        return true;
        }catch(IOException e){
                System.err.println("ERRO");
        }
            
            
            
            
            
            
        return true;
    }
    */
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
    
    public static void main(String[] args) throws InterruptedException, ClassNotFoundException {
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
            /*if(*/servidor.sendRegister();//==true)
                System.out.println("Servidor Registado com sucesso.");
            /*else{
                System.out.println("NAO ME REGISTEI");
                throw new IOException();
            }*/
            
            //INICIALIZAR A HEARTBEAT THREAD
            //servidor.heartBeat = new HeartbeatThreadSend(ip);
            //servidor.heartBeat.start();
            
            //INICIALIZA COMUNICAÇÃO TCP
            //servidor.inicializeTCP(porto);
            
            while(true) {
                System.out.printf("z");
                
                //trata clientes
                
                Thread.sleep(1000);
            }
            
        }catch(NumberFormatException e){
            System.out.println("O porto de escuta deve ser um inteiro positivo.");
        }catch(NullPointerException e){
            System.out.println("Null pointer exception apanho do hertbeat." + e);
            e.printStackTrace();
        }catch(SocketException e){
            System.out.println("Ocorreu um erro ao nível do socket UDP:\n\t"+e);
        }catch(IOException e){
            //System.out.println("Ocorreu um erro no registo do servidor:\n\t"+e);
            e.printStackTrace();
        }finally{
            if(servidor != null){
                servidor.closeSocket();
            }
        }
    }
}

