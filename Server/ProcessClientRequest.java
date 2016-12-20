
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
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
    
    public ProcessClientRequest(ServerSocket serverSocket, Socket toClientSocket) throws SocketException{
        this.serverSocket = serverSocket;
        this.toClientSocket = toClientSocket;
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
        
        System.out.println("TCP conection started [PORT: " + serverSocket.getLocalPort()+"]");
        
        while (run) {

            try {
                out = new ObjectOutputStream(toClientSocket.getOutputStream());
                in = new ObjectInputStream(toClientSocket.getInputStream());
                
                requestClientInfo = (ClientInfo) (in.readObject());
                
                //if(requestClientInfo.getUsername())

                request = (MSG) (in.readObject());

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
                    MSG msg = new MSG();
                    msg.setMSGCode(Constants.CODE_CMD_NOT_RECOGNIZED);
                    out.writeObject(msg);
                    out.flush();
                    return;
                }
                
                switch (request.getCMDarg(0)) {
                    case "CD":
                        System.out.println("RECEBI UM CD COMO PRIMEIRO ARGUMENTO");
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
    

}