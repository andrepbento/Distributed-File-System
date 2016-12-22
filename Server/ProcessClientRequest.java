
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Jorge
 */
public class ProcessClientRequest extends Thread {
    
    private ServerSocket serverSocket;
    Socket toClientSocket;
    BufferedReader in;
    PrintWriter out;
    String directoryPath;
    private List<String> cmd;
    MSG msg;
    
    public ProcessClientRequest(ServerSocket serverSocket, Socket toClientSocket, String directoryPath) throws SocketException{
        this.serverSocket = serverSocket;
        this.toClientSocket = toClientSocket;
        this.directoryPath = directoryPath;
        cmd = new ArrayList<>();
    }
    @Override
    public void run() {
        ObjectInputStream in;
        ObjectOutputStream out;
        MSG request;
        ClientInfo requestClientInfo;
        boolean run = true;
        
        if(serverSocket == null){
            return;
        }
        /*
        FALTA COLOCAR AQUI UMA PASTA COM O NOME DO UTILIZADOR
        System.out.println("CRIEI UMA DIRECTORIA EM: " + directoryPath);
        new File(directoryPath).mkdir();
        */
        System.out.println("TCP conection started [PORT: " + serverSocket.getLocalPort()+"]");
        
        while (run) {

            try {
                out = new ObjectOutputStream(toClientSocket.getOutputStream());
                in = new ObjectInputStream(toClientSocket.getInputStream());
                
                requestClientInfo = (ClientInfo) (in.readObject());
                
                //if(requestClientInfo.getUsername())

                request = (MSG) (in.readObject());
                
                System.out.println("CRIEI UMA DIRECTORIA EM: " + directoryPath);
                directoryPath = directoryPath + File.separator + requestClientInfo.getUsername();
                new File(directoryPath).mkdir();
                
                msg = new MSG();
                cmd.clear();
                cmd.add("CLIENTE PATH");
                cmd.add(directoryPath);
                msg.setCMD(cmd);
                out.writeObject(msg);
                out.flush();

                if (request == null) {
                    toClientSocket.close();
                    run = false;
                    continue;
                }

                System.out.println("Recebido \"" + request.toStringCMD()
                        + "\" de " + toClientSocket.getInetAddress().getHostAddress()
                        + ":" + toClientSocket.getLocalPort()); //trim apaga os espaços brancos

                //TRATA DOS CLIENTES POSTERIORMENTE
                if (request.getCMD().size() < 2) {
                    msg = new MSG();
                    msg.setMSGCode(Constants.CODE_CMD_NOT_RECOGNIZED);
                    out.writeObject(msg);
                    out.flush();
                    return;
                }
                
                switch (request.getCMDarg(0)) {
                    case "CD":
                        System.out.println("RECEBI UM CD COMO PRIMEIRO ARGUMENTO");
                        
                        File localDirectory;
                        localDirectory = new File(request.getCMDarg(1));
                        
                        if(!localDirectory.exists()){
                            System.out.println("A directoria para a qual te estas a mover  [" + localDirectory + "] nao existe!");
                            return;
                        }

                        if(!localDirectory.isDirectory()){
                            System.out.println("O caminho que indicaste [" + localDirectory + "] nao se refere a uma directoria!");
                            return;
                        }

                        if(!localDirectory.canRead()){
                            System.out.println("Sem permissoes de leitura na directoria " + localDirectory + "!");
                            return;
                        }
                        processFileRequest();
                        break;
                    case "COPY":
                        System.out.println("RECEBI UM COPY COMO PRIMEIRO ARGUMENTO");
                        break;
                    case "CP":
                        System.out.println("RECEBI UM COPY COMO PRIMEIRO ARGUMENTO");
                        break;
                    default:
                        System.out.println("CALMA QUE EU CHEGUEI AO DEFAULT E NÃO DEVO TER ENCONTRADO NADA");
                }
                
            }catch(IOException e){
                System.out.println("Erro na comunicacao como o cliente " + 
                                toClientSocket.getInetAddress().getHostAddress() + ":" + 
                                    toClientSocket.getPort()+"\n\t" + e);
            }catch (ClassNotFoundException ex) {
                Logger.getLogger(ProcessClientRequest.class.getName()).log(Level.SEVERE, null, ex);
            }finally{
                try{
                  toClientSocket.close();
                }catch(IOException e){}
            }
            
        }
    }
    
     public void processFileRequest()
    {        
        BufferedReader in;
        OutputStream out;
        Socket socketToClient;        
        byte []fileChunck = new byte[1026];
        int nbytes;
        String requestedFileName, requestedCanonicalFilePath = null;
        FileInputStream requestedFileInputStream = null;
        
        if(serverSocket == null){
            return;
        }

        System.out.println("Servidor de carregamento de ficheiros iniciado...");
        
        try{
            
                
                try{
                    
                    in = new BufferedReader(new InputStreamReader(toClientSocket.getInputStream()));
                    out = toClientSocket.getOutputStream();
                    
                    requestedFileName = in.readLine();
                     
                    System.out.println("Recebido pedido para: " + requestedFileName);

                    requestedCanonicalFilePath = new File(directoryPath+File.separator+requestedFileName).getCanonicalPath();

                    if(!requestedCanonicalFilePath.startsWith(directoryPath+File.separator)){
                        System.out.println("Nao e' permitido aceder ao ficheiro " + requestedCanonicalFilePath + "!");
                        System.out.println("A directoria de base nao corresponde a " + directoryPath +"!");
                        return;
                    }
                    
                    requestedFileInputStream = new FileInputStream(requestedCanonicalFilePath);
                    System.out.println("Ficheiro " + requestedCanonicalFilePath + " aberto para leitura.");
                    
                    while((nbytes = requestedFileInputStream.read(fileChunck))>0){                        
                        
                        out.write(fileChunck, 0, nbytes);
                        out.flush();
                                                
                    }     
                    
                    System.out.println("Transferencia concluida");
                    
                }catch(FileNotFoundException e){   //Subclasse de IOException                 
                    System.out.println("Ocorreu a excepcao {" + e + "} ao tentar abrir o ficheiro " + requestedCanonicalFilePath + "!");                   
                }catch(IOException e){
                    System.out.println("Ocorreu a excepcao de E/S: \n\t" + e);                       
                }
                
                if(requestedFileInputStream != null){
                    try {
                        requestedFileInputStream.close();
                    } catch (IOException ex) {}
                }

                try{
                     toClientSocket.close();
                 } catch (IOException e) {}
                
           
        }finally{
            try {
                serverSocket.close();
            } catch (IOException e) {}
        }
    }
    

}