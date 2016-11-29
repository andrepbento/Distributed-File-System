import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Scanner;


public class Client {
    private DatagramSocket udpSocket;
    private DatagramPacket packet; //para receber os pedidos e enviar as respostas
    private Socket tcpSocket;
    private String cmd;
    private String name;
    private ByteArrayInputStream bIn;
    private ObjectInputStream oIn;
    private ObjectOutputStream oOut;
    private InetAddress directoryServiceIp;
    private int directoryServicePort;
    
    public Client(InetAddress directoryServiceIp, int directoryServicePort) throws SocketException{
        udpSocket = new DatagramSocket();
        packet = null;
        tcpSocket = null;
        this.directoryServiceIp = directoryServiceIp;
        this.directoryServicePort = directoryServicePort;
    }
    
    public void sendRequestUdp() throws IOException{
        cmd = new Scanner(System.in).nextLine();
        packet = new DatagramPacket(cmd.getBytes(), cmd.length(), directoryServiceIp, directoryServicePort);
        udpSocket.send(packet);
    }
    
    public Object receiveResponseUdp() throws IOException, ClassNotFoundException{
        byte[] response = new byte[Constants.MAX_SIZE];
        packet = new DatagramPacket(response, Constants.MAX_SIZE);
        udpSocket.receive(packet);
        bIn = new ByteArrayInputStream(packet.getData());
        oIn = new ObjectInputStream(bIn);
        
        return oIn.readObject();
    }
    
    public void sendRequestTcp() throws IOException{
        cmd = new Scanner(System.in).nextLine();
        oOut = new ObjectOutputStream(tcpSocket.getOutputStream());
        oOut.writeObject(cmd);
        oOut.flush();
    }
    
    public static void main(String[] args) {        
            
        InetAddress ip = null;
        int port = -1;
        
        try {
            
            if(args.length != 2){
                System.out.println("Number of arguments invalid: java Client IP PORT");
                return;
            }
            
            ip = InetAddress.getByName(args[0]);
            port = Integer.parseInt(args[1]);
            Client c = new Client(ip, port);
            
            while(true){
                System.out.println(">>");
                c.sendRequestUdp();
//                Object obj = c.receiveResponseUdp();
//                
//                if(obj instanceof Integer){
//                    Integer code = (Integer)obj;
//                    switch(code){
//                        case Constants.CODE_REGISTER_OK:
//                            System.out.println("You're now registered!\n");
//                            break;
//                        case Constants.CODE_REGISTER_FAILURE:
//                            System.out.println("You're missing some parameters!\n");
//                            break;
//                        case Constants.CODE_REGISTER_CLIENT_ALREADY_EXISTS:
//                            System.out.println("The username you're trying to regist already exists!\n");
//                            break;
//                    }
//                }else if(obj instanceof String){
//                    
//                }
            }
            
        } catch (UnknownHostException ex) {
            System.out.println("Erro converting InetAddress\n\t"+ex);
        } catch (SocketException ex) {
            System.out.println("Socket exception\n\t"+ex);
        } catch (IOException ex) {
            System.out.println("IOException\n\t"+ex);
//        } catch (ClassNotFoundException ex) {
//            System.out.println("Class not found\n\t"+ex);
        }
        
    }
}
