
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

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
    MSG msg, request;
    ClientInfo requestClientInfo;
    ObjectInputStream inObj;
    ObjectOutputStream outObj;
    
    public ProcessClientRequest(ServerSocket serverSocket, Socket toClientSocket, String directoryPath) throws SocketException{
        this.serverSocket = serverSocket;
        this.toClientSocket = toClientSocket;
        this.directoryPath = directoryPath;
        cmd = new ArrayList<>();
    }
    
    @Override
    public void run() {
        boolean run = true;
        File localDirectory;
        
        System.out.println("THREAD A COMEAÇAR");
        
        if(serverSocket == null){
            return;
        }
        
        initiate();
        
                System.out.println("THREAD A PASSAR A INICIALIZAÇÃO.");

        
        System.out.println("TCP conection started [PORT: " + serverSocket.getLocalPort()+"]");
        
        
        while (run) {
            try {
                outObj = new ObjectOutputStream(toClientSocket.getOutputStream());
                inObj = new ObjectInputStream(toClientSocket.getInputStream());

                request = (MSG) (inObj.readObject());
                
                if (request == null) {
                    
                    toClientSocket.close();
                    run = false;
                    continue;
                }

                System.out.println("Recebido \"" + request.getCMD().toString()
                        + "\" de " + toClientSocket.getInetAddress().getHostAddress()
                        + ":" + toClientSocket.getLocalPort()); //trim apaga os espaços brancos

                //TRATA DOS CLIENTES POSTERIORMENTE
                if (request.getCMD().size() < 2) {
                    sendMSG(new MSG(Constants.CODE_CMD_NOT_RECOGNIZED));
                    return;
                }
                
                switch (request.getCMDarg(0).toLowerCase()) {
                    case "DOWNLOAD":    //ESTE PRIMEIRO PODE SER MUDADO
                        System.out.println("RECEBI UM DOWNLOAD COMO PRIMEIRO ARGUMENTO");
                        
                        localDirectory = new File(request.getCMDarg(1));
                        
                        if(directoryExists(localDirectory)){
                            processFileRequest();
                        }
                        break;
                    case "CD":
                        System.out.println("RECEBI UM CD COMO PRIMEIRO ARGUMENTO");
                        if(request.getCMDarg(0).contains(requestClientInfo.getUsername())){
                            localDirectory = new File(request.getCMDarg(1)+request.getCMDarg(2));
                            if(directoryExists(localDirectory)){
                                processCDRequest(localDirectory.getCanonicalPath());
                            }
                        }
                        break;
                    case "MKDIR":
                        System.out.println("RECEBI UM MKDIR COMO PRIMEIRO ARGUMENTO");
                        
                        localDirectory = new File(request.getCMDarg(1));
                        
                        if(directoryExists(localDirectory)){
                            File mkdirFile = new File(request.getCMDarg(1) + request.getCMDarg(2));
                            processMKDIRequest(mkdirFile.getCanonicalPath());
                        }
                        break;
                    default:
                        System.out.println("CALMA QUE EU CHEGUEI AO DEFAULT E NÃO DEVO TER ENCONTRADO NADA");
                }
            }catch (ClassNotFoundException ex) {
                ex.printStackTrace();
            }
        }
    }
    
    private boolean initiate() {
       try{
            outObj = new ObjectOutputStream(toClientSocket.getOutputStream());
            
            sendMSG(new MSG(Constants.CODE_CONNECT_OK));
            
            inObj = new ObjectInputStream(toClientSocket.getInputStream());
            
            requestClientInfo = (ClientInfo) (inObj.readObject());
        
        if (requestClientInfo == null) {
            toClientSocket.close();
            System.out.println("OCURREU ALGO DE ERRADO NO CLIENT INFO");
            return false;
        }
        System.out.println("CRIEI UMA DIRECTORIA EM: " + directoryPath);
        directoryPath = directoryPath + File.separator + requestClientInfo.getUsername();
        new File(directoryPath).mkdir();
                
                sendMSG(new MSG(Constants.CODE_CONNECT_OK, Arrays.asList(directoryPath)));
        
        }catch(IOException ex){
            System.out.println("NAO RECEBI CORRECTAMENTE OS COMANDOS CLIENTINFO");
            return false;
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ProcessClientRequest.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("NAO RECEBI CORRECTAMENTE OS COMANDOS CLIENTINFO");
            return false;
        }
       return true;
    }
    
    public boolean directoryExists(File localDirectory){
        if(!localDirectory.exists()){
            System.out.println("A directoria para a qual te estas a mover  [" + localDirectory + "] nao existe!");
            return false;
        }

        if(!localDirectory.isDirectory()){
            System.out.println("O caminho que indicaste [" + localDirectory + "] nao se refere a uma directoria!");
            return false;
        }

        if(!localDirectory.canRead()){
            System.out.println("Sem permissoes de leitura na directoria " + localDirectory + "!");
            return false;
        }
        return true;
    }
     
    public boolean processFileRequest()
    {        
        OutputStream out;    
        byte []fileChunck = new byte[1026];
        int nbytes;
        String requestedCanonicalFilePath = null;
        FileInputStream requestedFileInputStream = null;
        
        if(serverSocket == null){
            return false;
        }

        System.out.println("Servidor de carregamento de ficheiros iniciado...");
                try{
                    
                    System.out.println("Recebido pedido para: " + msg.getCMDarg(0));erro

                    requestedCanonicalFilePath = new File(directoryPath+File.separator+msg.getCMDarg(0)).getCanonicalPath();

                    if(!requestedCanonicalFilePath.startsWith(directoryPath+File.separator)){
                        System.out.println("Nao e' permitido aceder ao ficheiro " + requestedCanonicalFilePath + "!");
                        System.out.println("A directoria de base nao corresponde a " + directoryPath +"!");
                        return false;
                    }
                    
                    requestedFileInputStream = new FileInputStream(requestedCanonicalFilePath);
                    System.out.println("Ficheiro " + requestedCanonicalFilePath + " aberto para leitura.");
                    
                    out = toClientSocket.getOutputStream();
                    
                    while((nbytes = requestedFileInputStream.read(fileChunck))>0){                        
                        
                        out.write(fileChunck, 0, nbytes);
                        out.flush();
                                                
                    }     
                    System.out.println("Transferencia concluida");
                    
                }catch(FileNotFoundException e){   //Subclasse de IOException                 
                    System.out.println("Ocorreu a excepcao {" + e + "} ao tentar abrir o ficheiro " + requestedCanonicalFilePath + "!");                   
                    return false;
                }catch(IOException e){
                    System.out.println("Ocorreu a excepcao de E/S: \n\t" + e); 
                    return false;
                }
                
                if(requestedFileInputStream != null){
                    try {
                        requestedFileInputStream.close();
                    } catch (IOException ex) {}
                }
        
        return true;
    }
     
    public boolean processCDRequest(String canonicalPath)
    {        
        Socket socketToClient;        
        byte []fileChunck = new byte[1026];
        int nbytes;
        String requestedFileName, requestedCanonicalFilePath = null;
        FileInputStream requestedFileInputStream = null;
        
        if(serverSocket == null){
            return false;
        }

        System.out.println("MUDAR DE DIRECTORIA...");
        
        System.out.println("Recebido \"" + msg.getCMDarg(0) + "\"  " + 
                msg.getCMDarg(1) + "\"  "+ msg.getCMDarg(2));

        //Constroi a resposta terminando-a com uma mudanca de lina
        return sendMSG(new MSG(0,Arrays.asList(canonicalPath)));erro
            
    }
      
    public boolean processMKDIRequest(String canonicalPath)
    {        
        ObjectInputStream in;
        ObjectOutputStream out;
        Socket socketToClient;        
        byte []fileChunck = new byte[1026];
        int nbytes;
        String requestedFileName, requestedCanonicalFilePath = null;
        FileInputStream requestedFileInputStream = null;
        
        if(serverSocket == null){
            return false;
        }

        System.out.println("MUDAR DE DIRECTORIA...");
        
        System.out.println("Recebido \"" + msg.getCMDarg(0) + "\"  " + 
                 msg.getCMDarg(1) + "\"  "+ msg.getCMDarg(2));

        new File(canonicalPath).mkdirs();
        
        //Constroi a resposta terminando-a com uma mudanca de lina
        return sendMSG(new MSG(0,Arrays.asList("A directoria [" + canonicalPath + "] criada.")));erro
    }
    
    public boolean sendMSG(MSG msg)
    {
        try{
            outObj.writeObject(msg);
            outObj.flush();
        }catch(IOException e){
            System.out.println("Erro na comunicação como o cliente " + 
                toClientSocket.getInetAddress().getHostAddress() + ":" + 
                toClientSocket.getPort()+"\n\t" + e);
            return false;
        }
        return true;
    }
}