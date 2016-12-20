
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
public class processClientRequest extends Thread {
    
    private ServerSocket serverSocket;
    Socket toClientSocket;
    BufferedReader in;
    PrintWriter out;
    String request;
    
    public processClientRequest(ServerSocket serverSocket, Socket toClientSocket) throws SocketException{
        this.serverSocket = serverSocket;
        this.toClientSocket = toClientSocket;
    }
    @Override
    public void run() {
        ObjectInputStream in;
        ObjectOutputStream out;
        MSG request;
        
        if(serverSocket == null){
            return;
        }
        
        System.out.println("TCP conection started [PORT: " + serverSocket.getLocalPort()+"]");
        
        while (true) {

            try {
                out = new ObjectOutputStream(toClientSocket.getOutputStream());
                in = new ObjectInputStream(toClientSocket.getInputStream());

                request = (MSG) (in.readObject());

                if (request == null) {
                    toClientSocket.close();
                    continue;
                }

                System.out.println("Recebido \"" + request.toStringCMD()
                        + "\" de " + toClientSocket.getInetAddress().getHostAddress()
                        + ":" + toClientSocket.getLocalPort()); //trim apaga os espaços brancos

                //TRATA DOS CLIENTES POSTERIORMENTE
                /*if (request.getCMD().size() < 2) {
                    
                    out.writeObject(msg);
                    out.flush();
                    return;
                }

                switch () {
                    case :
                        
                        break;
                    case :
                        
                        break;
                    case :
                        
                        break;
                    case :
                        
                        break;
                    default:
                        System.out.println("CALMA QUE EU CHEGUEI AO DEFAULT");
                }
                */
            }catch(IOException e){
                System.out.println("Erro na comunicacao como o cliente " + 
                                toClientSocket.getInetAddress().getHostAddress() + ":" + 
                                    toClientSocket.getPort()+"\n\t" + e);
            }catch (ClassNotFoundException ex) {
                Logger.getLogger(processClientRequest.class.getName()).log(Level.SEVERE, null, ex);
            }finally{
                try{
                  toClientSocket.close();
                }catch(IOException e){}
            }
            
        }
    }
    

}