import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Path;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author Jorge
 */
public class ProcessClientRequest extends Thread {
    
    private ServerSocket serverSocket;
    private Socket toClientSocket;
    private BufferedReader in;
    private PrintWriter out;
    private String directoryPath;
    private String serverDirectory;
    private List<String> cmd;
    private MSG msg, request;
    private MSG requestClientInfo;
    private ObjectInputStream inObj;
    private ObjectOutputStream outObj;
    
    public ProcessClientRequest(ServerSocket serverSocket, Socket toClientSocket, String directoryPath) throws SocketException{
        this.serverSocket = serverSocket;
        this.toClientSocket = toClientSocket;
        this.directoryPath = directoryPath;
        this.serverDirectory = directoryPath;
        cmd = new ArrayList<>();
    }
    
    @Override
    public void run() {
        boolean run = true;
        File localDirectory;
        
        System.out.println("<------- THREAD RUNNING AND CONECTED TO :" + toClientSocket.getInetAddress().toString() +"------->");
        
        if(serverSocket == null){
            return;
        }
        
        try {
            initiate();
        } catch (IOException ex) {
            Logger.getLogger(ProcessClientRequest.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        System.out.println("<------- TCP conection started [PORT: " + serverSocket.getLocalPort()+"] ------->");

        while (run) {
            try {
                outObj = new ObjectOutputStream(toClientSocket.getOutputStream());
                inObj = new ObjectInputStream(toClientSocket.getInputStream());

                request = (MSG) (inObj.readObject());
                
                if (request == null) {
                    toClientSocket.close();
                    run = false;
                    break;
                }

                System.out.println("Received \"" + request.getCMD().toString()
                        + "\" de " + toClientSocket.getInetAddress().getHostAddress()
                        + ":" + toClientSocket.getLocalPort()); //trim apaga os espaços brancos
                
                System.out.println("splited :" );      

                String pattern = Pattern.quote(System.getProperty("file.separator"));
                String[] array = directoryPath.split(pattern);
                
                for(int i = 0; i < array.length; i++)
                    System.out.println(array[i]);

                if (request.getCMDarg(0).toUpperCase() == "DISCONNECT") {
                    sendMSG(new MSG(Constants.CODE_DISCONNECT_OK));
                    toClientSocket.close();
                    run = false;
                    return;
                }
                                
                switch (request.getCMDarg(0).toUpperCase()) {
                    case Constants.CMD_DOWNLOAD_FILE:    //ESTE PRIMEIRO PODE SER MUDADO
                        System.out.println("RECEBI UM DOWNLOAD COMO PRIMEIRO ARGUMENTO");
                        
                        localDirectory = new File(request.getCMDarg(1));
                        
                        if(directoryExists(localDirectory)){
                            processFileRequest(request.getCMDarg(1));
                        }
                        break;
                    case Constants.CMD_CD_DIR:
                        System.out.println("RECEBI UM CD COMO PRIMEIRO ARGUMENTO");
                        //if(request.getCMDarg(1).contains(requestClientInfo.getClientList().get(0).getUsername())){
                        System.out.println("splited :" );      

                        String pathParts[] =  directoryPath.split(directoryPath);
                       
                            if(request.getCMDarg(1).equals("..")){
                               if(!(pathParts[pathParts.length].equals(requestClientInfo.getClientList().get(0).getUsername())))
                                   for(int i = 0; i < pathParts.length; i++)
                                       System.out.println("para compilar");
                            }       
                            localDirectory = new File(directoryPath + File.separator + File.separator + request.getCMDarg(1));
                            if(directoryExists(localDirectory)){
                                processCDRequest(localDirectory.getCanonicalPath());
                            }
                        //}
                        break;
                    case Constants.CMD_MK_DIR:
                        System.out.println("RECEBI UM MKDIR COMO PRIMEIRO ARGUMENTO");
                        
                        localDirectory = new File(directoryPath+File.separator + File.separator +request.getCMDarg(1));
                        
                        if(!directoryExists(localDirectory)){
                            processMKDIRequest(directoryPath+File.separator+File.separator+request.getCMDarg(1));
                        }
                        break;
                    case Constants.CMD_MOVE_FILE:
                        System.out.println("RECEBI UM MV COMO PRIMEIRO ARGUMENTO");
                                                                            //nome do ficheiro a move   //nome do destino
                        processMoveRequest(directoryPath + File.separator + File.separator + request.getCMDarg(1),directoryPath + File.separator + File.separator + request.getCMDarg(2));
                        
                        break;
                    case Constants.CMD_COPY_FILE:
                        System.out.println("RECEBI UM COPY COMO PRIMEIRO ARGUMENTO");
                            localDirectory = new File(directoryPath + File.separator + File.separator + request.getCMDarg(2));
                            if(directoryExists(localDirectory) || fileExists(localDirectory)){
                                                                                //nome do ficheiro a move   //nome do destino
                                processCopyRequest(request.getCMDarg(1),request.getCMDarg(2));
                            }

                        break;
                    case Constants.CMD_UPLOAD_FILE:
                        //AINDA NÃO ESTÁ IMPLEMENTADO
                        break;
                    case Constants.CMD_RM_FILE:
                        System.out.println("RECEBI UM RMDIR COMO PRIMEIRO ARGUMENTO");
                        
                        localDirectory = new File(directoryPath + File.separator + File.separator + request.getCMDarg(1));
                        if(directoryExists(localDirectory)|| fileExists(localDirectory)){                        //file name
                            processRMDIRequest(directoryPath + File.separator + File.separator + request.getCMDarg(1));
                        }
                        break;
                    case Constants.CMD_LS_DIR:
                        System.out.println("RECEBI UM LS COMO PRIMEIRO ARGUMENTO");
                        
                        localDirectory = new File(directoryPath);
                        
                        if(directoryExists(localDirectory)){
                            processLSRequest(directoryPath);
                        }
                        break;
                    default:
                        System.out.println("CALMA QUE EU CHEGUEI AO DEFAULT E NÃO DEVO TER ENCONTRADO NADA");
                }
            }catch (ClassNotFoundException ex) {
                ex.printStackTrace();
                return;
            } catch (IOException ex) {
                Logger.getLogger(ProcessClientRequest.class.getName()).log(Level.SEVERE, null, ex);
                run = false;
                try {
                    toClientSocket.close();
                    return;
                } catch (IOException ex1) {
                    Logger.getLogger(ProcessClientRequest.class.getName()).log(Level.SEVERE, null, ex1);
                   return;
                }
            }
        }
    }
    
    private boolean initiate() throws IOException {
       try{
            outObj = new ObjectOutputStream(toClientSocket.getOutputStream());
            
            sendMSG(new MSG(Constants.CODE_CONNECT_OK));
            
            inObj = new ObjectInputStream(toClientSocket.getInputStream());
            
            requestClientInfo = (MSG) (inObj.readObject());
            

        if (requestClientInfo == null) {
            toClientSocket.close();
            System.out.println("OCURREU ALGO DE ERRADO NO CLIENT INFO");
            return false;
        }
        System.out.println("CRIEI UMA DIRECTORIA EM: " + directoryPath);
        directoryPath = directoryPath + File.separator + File.separator + requestClientInfo.getClientList().get(0).getUsername();
        new File(directoryPath).mkdir();
                
                sendMSG(new MSG(Constants.CODE_CONNECT_OK, Arrays.asList(directoryPath)));
        
        }catch(IOException ex){
            System.out.println("NAO RECEBI CORRECTAMENTE OS COMANDOS CLIENTINFO");
             toClientSocket.close();
            return false;
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ProcessClientRequest.class.getName()).log(Level.SEVERE, null, ex);
             toClientSocket.close();
            System.out.println("NAO RECEBI CORRECTAMENTE OS COMANDOS CLIENTINFO");
            return false;
        }
       return true;
    }
    //VISTO
    public boolean directoryExists(File localDirectory){
        if(!localDirectory.exists()){
            System.out.println("ERROR : The directory that you try to move  [" + localDirectory + "] doesnt exists!");
            return false;
        }

        if(!localDirectory.isDirectory()){
            System.out.println("ERROR : The path inserted [" + localDirectory + "] doesnt refer any directory!");
            return false;
        }

        if(!localDirectory.canRead()){
            System.out.println("ERROR : Without permissions to read the directory " + localDirectory + "!");
            return false;
        }
        return true;
    }
    
    public boolean fileExists(File localDirectory){
        if(!localDirectory.exists()){
            System.out.println("ERROR : The file that you try to move  [" + localDirectory + "] doesnt exists!");
            return false;
        }

        if(!localDirectory.isFile()){
            System.out.println("ERROR : The path inserted [" + localDirectory + "] doesnt refer any file!");
            return false;
        }

        if(!localDirectory.canRead()){
            System.out.println("ERROR : Without permissions to read the directory " + localDirectory + "!");
            return false;
        }
        return true;
    }
    //VISTO
    public boolean processFileRequest(String filename)
    {        
        OutputStream out;    
        byte []fileChunck = new byte[1026];
        int nbytes;
        String requestedCanonicalFilePath = null;
        FileInputStream requestedFileInputStream = null;
        
        if(serverSocket == null){
            return false;
        }

        System.out.println("Sending files...");
                try{

                    requestedCanonicalFilePath = new File(directoryPath+File.separator + File.separator +filename).getCanonicalPath();

                    if(!requestedCanonicalFilePath.startsWith(directoryPath+File.separator + File.separator)){
                        sendMSG(new MSG(Constants.CODE_SERVER_DOWNLOAD_ERROR,Arrays.asList("The base directory doenst correspond " + directoryPath +"!")));
                        return false;
                    }
                    
                    requestedFileInputStream = new FileInputStream(requestedCanonicalFilePath);
                    System.out.println("File " + requestedCanonicalFilePath + " open to read.");
                    
                    out = toClientSocket.getOutputStream();
                    
                    while((nbytes = requestedFileInputStream.read(fileChunck))>0){                        
                        
                        out.write(fileChunck, 0, nbytes);
                        out.flush();
                                                
                    }     
                    System.out.println("Transfer finished!");
                    
                }catch(FileNotFoundException e){   //Subclasse de IOException                              
                    sendMSG(new MSG(Constants.CODE_SERVER_DOWNLOAD_ERROR,Arrays.asList("Ocurred an exeception {" + e + "} while trying to open the file " + requestedCanonicalFilePath + "!")));
                        return false;
                }catch(IOException e){
                    System.out.println("Ocorreu a excepcao de E/S: \n\t" + e); 
                    sendMSG(new MSG(Constants.CODE_SERVER_DOWNLOAD_ERROR,Arrays.asList("Ocurred an exception of I/O: \n\t" + e)));
                    return false;
                }
                
                if(requestedFileInputStream != null){
                    try {
                        requestedFileInputStream.close();
                    } catch (IOException ex) {}
                }
        
        return true;
    }
    //VISTO 
    public boolean processCDRequest(String canonicalPath)
    {          
        if(serverSocket == null){
            return false;
        }

        System.out.println("CHANGING DIRECTORY...");
        
        System.out.println("Received:  " + canonicalPath);
        
        //ACTUALIZA O PATH DESTA 
        directoryPath = canonicalPath;


        //Constroi a resposta terminando-a com uma mudanca de lina
        return sendMSG(new MSG(Constants.CODE_SERVER_CD_OK,Arrays.asList("Changed directory to ... " + directoryPath,  onlyClientDir(directoryPath))));
    }
    public boolean processCatRequest(String canonicalPath)
    {          
        if(serverSocket == null){
            return false;
        }

        System.out.println("CAT FILE...");
        List<String> text = new ArrayList<>();
        System.out.println("Received the file:  " + canonicalPath);
        try (BufferedReader br = new BufferedReader(new FileReader(canonicalPath))) {
            String line = null;
            while ((line = br.readLine()) != null) {
                text.add(line);
            }
         }catch(IOException ex){
             System.out.println("NÃO DEU PARA LER O FICHEIRO");
             return sendMSG(new MSG(Constants.CODE_SERVER_CAT_ERROR,Arrays.asList("Comand cat error in file" + directoryPath)));
         }
         return sendMSG(new MSG(Constants.CODE_SERVER_CAT_OK,text));

    }
    //VISTO
    public boolean processLSRequest(String canonicalPath)
    {                
        if(serverSocket == null){
            return false;
        }

        System.out.println("LS OF DIRECTORY...");
        
        System.out.println("Recebido \"" + canonicalPath);
        
        File file = new File(directoryPath);
        String listagem = listar(file);

        //Constroi a resposta terminando-a com uma mudanca de lina
        return sendMSG(new MSG(Constants.CODE_SERVER_LS_OK,Arrays.asList("LS OF DIRECTORY: " + listagem)));
    
            
    }
    //VISTO
    public boolean processMKDIRequest(String canonicalPath)
    {        
        if(serverSocket == null){
            return false;
        }
        
        System.out.println("MK DIRECTORY...");
        
        System.out.println("RECEIVED: " + canonicalPath);
        
        System.out.println("splited :" );      

                String array[] =  canonicalPath.split("\\\\");
                for(int i = 0; i < array.length; i++)
                    System.out.println(array[i]);

        new File(canonicalPath).mkdirs();
        
        return sendMSG(new MSG(Constants.CODE_SERVER_MKDIR_OK,Arrays.asList("Directory [" + canonicalPath + "] created.")));
    }
    //VISTO
    public boolean processRMDIRequest(String canonicalPath)
    {        
        boolean delete = false;
        
        if(serverSocket == null){
            return false;
        }

        System.out.println("DELETING DIRECTORY ...");
        
        System.out.println("Received " +canonicalPath);

        File file = new File(canonicalPath);
        delete = file.delete();
        
        if(delete){
            return sendMSG(new MSG(Constants.CODE_SERVER_RMDIR_OK,Arrays.asList("SUCSSEFULLY deleted the file [" + canonicalPath + "].")));
        }else{
            return sendMSG(new MSG(Constants.CODE_SERVER_RMDIR_ERROR,Arrays.asList("Ocurred an error deleting the file [" + canonicalPath + "]")));
        }
    }
    //VISTO
    public boolean processRenameRequest(String filename, String newFileName)
    {                
        if(serverSocket == null){
            return false;
        }

        System.out.println("CHANGING THE NAME OF DIRECTORY/FILE...");
        
        System.out.println("Received: OldName: " + filename + "   NewName:  " + 
                 newFileName);
        try{
        // File (or directory) with old name
        File oldFile = new File(directoryPath + File.separator + File.separator + filename);

        // File (or directory) with new name
        File newFile = new File(directoryPath + File.separator + File.separator + newFileName);

        if (newFile.exists())
           throw new java.io.IOException("You already have this name assigned-> " + newFileName);
        
        // Rename file (or directory)
        boolean success = oldFile.renameTo(newFile);

        if (!success) {
           throw new java.io.IOException("Ocurred an error renaming the File");
        }
        }catch(IOException ex){
            System.out.println(ex.toString());
            return sendMSG(new MSG(Constants.CODE_SERVER_RENAME_ERROR,Arrays.asList(ex.toString())));
        }
        //ESTE RETURN TRUE É SÓ PARA COMPILAR
        return sendMSG(new MSG(Constants.CODE_SERVER_RENAME_OK,Arrays.asList("SUCCESSFULLY RENAMED " + filename + "->" + newFileName)));
    }
    //VISTO
    public boolean processCopyRequest(String filename, String fileDestination)
    {        
        
        if(serverSocket == null){
            return false;
        }

        System.out.println("COPYING THE DIRECTORY/FILE...");
        
        System.out.println("Received filename: " + filename + 
                "    with the destination folder " + fileDestination);
        try{
            
            
        File file = new File( directoryPath + File.separator + File.separator + filename);
        new File(directoryPath + File.separator + File.separator + fileDestination + File.separator + File.separator + filename).mkdirs();
        File fileDest = new File(directoryPath + File.separator + File.separator + fileDestination + File.separator + File.separator + filename);
        FileUtils.copyDirectory(file, fileDest);
        
        return sendMSG(new MSG(Constants.CODE_SERVER_COPY_OK,Arrays.asList("SUCCESSFULY COPIED THE FILE [" + filename + "] to destination [" + fileDestination + filename + "")));
        }catch(IOException ex){
            System.out.println(ex.toString());
             return sendMSG(new MSG(Constants.CODE_SERVER_COPY_ERROR,Arrays.asList("OCURRED SOME ERROR IN COPY REQUEST :" + filename+ "]")));
        }
    }
    //VISTO
    public boolean processMoveRequest(String filename, String fileDestination)
    {                
        if(serverSocket == null){
            return false;
        }

        System.out.println("MOVING THE DIRECTORY/FILE...");
        
        System.out.println("RECEIVED:  " + filename + " FileDestination:  " + fileDestination);
        
        try{
        
        File fileOrigin = new File(filename);
        File fileDest = new File(fileDestination);
            if(directoryExists(fileOrigin) || fileExists(fileOrigin)){
                if(directoryExists(fileOrigin)){

                    Path source = fileOrigin.toPath();
                    Path newdir = fileDest.toPath();
                    Files.move(source, newdir.resolve(source.getFileName()), REPLACE_EXISTING);
                    System.out.println("ola");
                    return sendMSG(new MSG(Constants.CODE_SERVER_MOVE_OK,Arrays.asList("Successfuly moved the File [" 
                            + filename + "] to destination [" + fileDestination + "]")));
                }

            }
        }catch(IOException ex){
            System.out.println(ex.toString());
            return sendMSG(new MSG(Constants.CODE_SERVER_MOVE_ERROR,Arrays.asList("Ocurred an error moving the File :" + filename + "]   ->   " + fileDestination)));
        }
        return sendMSG(new MSG(Constants.CODE_SERVER_MOVE_ERROR,Arrays.asList("Ocurred an error moving the File :" + filename + "]   ->   " + fileDestination)));
    }
    //VISTO
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
    //VISTO
    public String listar(File directory) {
        String list = "";
        if(directory.isDirectory()) {
            System.out.println(directory.getPath());
            String[] subDirectory = directory.list();
            if(subDirectory != null) {
                for(String dir : subDirectory){
                    //listar(new File(directory + File.separator + File.separator + dir));
                    System.out.println(onlyClientDir(directory + File.separator + dir ));
                    list += onlyClientDir(directory + File.separator + File.separator + dir + "\n");
                }
            }
        }
        return list;
    }
    
    public String onlyClientDir(String directory) {
        String path ="";
        
        String pattern = Pattern.quote(System.getProperty("file.separator"));
        String[] array = directory.split(pattern);
        boolean copy = false;
        for(int i = 0; i < array.length; i++){
            if(!array[i].equals(""))
                if(array[i].equals(Server.name))
                    copy = true;
            if(copy)
                path += array[i].toString() + File.separator + File.separator;
        }
        
        return path;
    }
}