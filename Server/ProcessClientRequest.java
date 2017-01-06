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
    String pattern, nome;
    
    public ProcessClientRequest(ServerSocket serverSocket, Socket toClientSocket, String directoryPath, String nome) throws SocketException{
        this.serverSocket = serverSocket;
        this.toClientSocket = toClientSocket;
        this.directoryPath = directoryPath;
        this.serverDirectory = directoryPath;
        cmd = new ArrayList<>();
        pattern = Pattern.quote(System.getProperty("file.separator"));
        this.nome = nome;
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
                System.out.println("<------- [ Waiting: " + nome + "] ------->");
                
                request = (MSG) (inObj.readObject());
                
                if (request == null) {
                    toClientSocket.close();
                    run = false;
                    break;
                }

                System.out.println("Received \"" + request.getCMD().toString()
                        + "\" de " + toClientSocket.getInetAddress().getHostAddress()
                        + ":" + toClientSocket.getLocalPort());
                
                System.out.println("splited :" );      
                
                String[] array = directoryPath.split(pattern);
                
                for(int i = 0; i < array.length; i++)
                    System.out.println(array[i]);
                                
                switch (request.getCMDarg(0).toUpperCase()) {
                    case Constants.CMD_DOWNLOAD_FILE:
                        System.out.println("RECEIVED A [DOWNLOAD] IN FIRST ARGUMENT");
                        
                        localDirectory = new File(request.getCMDarg(1));
                        
                        //if(directoryExists(localDirectory)){
                            processFileRequest(request.getCMDarg(1));
                        //}
                        break;
                    case Constants.CMD_CD_DIR:
                        System.out.println("RECEIVED A [CD] IN FIRST ARGUMENT");
                        String pathParts[] =  directoryPath.split(pattern);
                        String newPath = "";
                       
                            if(request.getCMDarg(1).equals("..")){
                               if(!(pathParts[pathParts.length-1].equals(requestClientInfo.getClientList().get(0).getUsername()))){
                                   for(int i = 0; i < pathParts.length - 1; i++)
                                       newPath += pathParts[i] + File.separator;
                                   sendMSG(new MSG(Constants.CODE_SERVER_CD_OK,Arrays.asList("Changed directory to ... " + directoryPath,  onlyClientDir(newPath))));
                               }
                            }
                            else{
                            localDirectory = new File(directoryPath+File.separator + File.separator +request.getCMDarg(1));
                                if(directoryExists(localDirectory)){
                                    processCDRequest(directoryPath+File.separator+File.separator+request.getCMDarg(1));
                                }
                            }
                               sendMSG(new MSG(Constants.CODE_SERVER_CD_ERROR,Arrays.asList("DIDNT CHANGE THE DIRECTORY TO ..." + directoryPath,  onlyClientDir(newPath))));
                            System.out.println(newPath);
                        break;
                        case Constants.CMD_DISCONNECT:
                        System.out.println("RECEIVED A [DISCONNECT] IN FIRST ARGUMENT");
                            sendMSG(new MSG(Constants.CODE_DISCONNECT_OK));
                            toClientSocket.close();
                            run = false;
                        break;
                    case Constants.CMD_MK_DIR:
                        System.out.println("RECEIVED A [MKDIR] IN FIRST ARGUMENT");
                        
                        localDirectory = new File(directoryPath+File.separator + File.separator +request.getCMDarg(1));
                        
                        if(!directoryExists(localDirectory)){
                            processMKDIRequest(directoryPath+File.separator+File.separator+request.getCMDarg(1));
                        }
                        break;
                    case Constants.CMD_MOVE_FILE:
                        System.out.println("RECEIVED A [MV] IN FIRST ARGUMENT");
                                                                            //nome do ficheiro a move   //nome do destino
                        processMoveRequest(directoryPath + File.separator + File.separator + request.getCMDarg(1),directoryPath + File.separator + File.separator + request.getCMDarg(2));
                        
                        break;
                    case Constants.CMD_COPY_FILE:
                        System.out.println("RECEIVED A [COPY] IN FIRST ARGUMENT");
                            localDirectory = new File(directoryPath + File.separator + File.separator + request.getCMDarg(2));
                            if(directoryExists(localDirectory) || fileExists(localDirectory)){
                                                  //nome do ficheiro a move   //nome do destino
                                processCopyRequest(request.getCMDarg(1),request.getCMDarg(2));
                            }

                        break;
                    case Constants.CMD_UPLOAD_FILE:
                        System.out.println("AINDA NÃO ESTÁ IMPLEMENTADO");
                        break;
                    case Constants.CMD_RM_FILE:
                        System.out.println("RECEIVED A [RMDIR] IN FIRST ARGUMENT");
                        
                        localDirectory = new File(directoryPath + File.separator + File.separator + request.getCMDarg(1));
                        
                        if(directoryExists(localDirectory)|| fileExists(localDirectory)){                        
                            processRMDIRequest(directoryPath + File.separator + File.separator + request.getCMDarg(1));
                        }
                        
                        break;
                    case Constants.CMD_LS_DIR:
                        System.out.println("RECEIVED A [LS] IN FIRST ARGUMENT");
                        
                        localDirectory = new File(directoryPath);
                        
                        if(directoryExists(localDirectory)){
                            processLSRequest(directoryPath);
                        }
                        break;
                    case Constants.CMD_CAT_FILE:
                        System.out.println("RECEIVED A [CAT] IN FIRST ARGUMENT");
                        
                        localDirectory = new File(directoryPath);
                        
                        File aux =  new File(directoryPath+File.separator+request.getCMDarg(1).toString());
                        
                        if(fileExists(aux)){
                            processCatRequest(directoryPath+File.separator+request.getCMDarg(1).toString());
                        }
                        break;
                    case Constants.CMD_RENAME_FILE:
                        System.out.println("RECEIVED A [RENAME] IN FIRST ARGUMENT");
                        
                        localDirectory = new File(directoryPath);
                        
                        if(fileExists(new File(directoryPath+File.separator+request.getCMDarg(1).toString()))
                                && !fileExists(new File(directoryPath+File.separator+request.getCMDarg(2).toString()))){
                            processRenameRequest(directoryPath + File.separator + File.separator + request.getCMDarg(1),directoryPath + File.separator + File.separator + request.getCMDarg(2));
                        }
                        if(directoryExists(new File(directoryPath+File.separator+request.getCMDarg(1).toString()))
                                && !directoryExists(new File(directoryPath+File.separator+request.getCMDarg(2).toString()))){
                            processRenameRequest(directoryPath + File.separator + File.separator + request.getCMDarg(1),directoryPath + File.separator + File.separator + request.getCMDarg(2));
                        }
                        break;
                    default:
                        System.out.println("***************COMAND NOT RECOGNIZED********************");
                        sendMSG(new MSG(Constants.CODE_CMD_FAILURE));
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
            
            outObj.flush();
            
            sendMSG(new MSG(Constants.CODE_CONNECT_OK));
            
            inObj = new ObjectInputStream(toClientSocket.getInputStream());
            
            requestClientInfo = (MSG) (inObj.readObject());
            

        if (requestClientInfo == null) {
            toClientSocket.close();
            System.out.println("OCURRED AN ERROR IN CLIENT INFO");
            return false;
        }
        System.out.println("CREATED AN DIRECTORY IN " + directoryPath);
        directoryPath = directoryPath + File.separator + File.separator + requestClientInfo.getClientList().get(0).getUsername();
        new File(directoryPath).mkdir();
                
        sendMSG(new MSG(Constants.CODE_CONNECT_OK, Arrays.asList(onlyClientDir(directoryPath))));
        
        }catch(IOException ex){
            System.out.println("DIDNT RECEIVE THE CORRECT COMANDS CLIENTINFO");
            sendMSG(new MSG(Constants.CODE_CONNECT_OK));
            ex.printStackTrace();
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
        byte []fileChunck = new byte[2048];
        int nbytes;
        String requestedCanonicalFilePath = null;
        FileInputStream requestedFileInputStream = null;
        
        if(serverSocket == null){
            return false;
        }

        System.out.println("Sending files...");
                try{

                    requestedCanonicalFilePath = new File(directoryPath+File.separator+filename).getCanonicalPath();
                    
                    sendMSG(new MSG(Constants.CODE_SERVER_DOWNLOAD_OK,Arrays.asList("The base directory will be downloaded -> " + directoryPath +"!")));
                        
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

        System.out.println("Changing Directory...");
        
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
                text.add(line + "\n");
            }
         }catch(IOException ex){
             System.out.println("DIDNT READ THE FILE");
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

        System.out.println("LS Of directory...");
        
        System.out.println("Received \"" + canonicalPath);
        
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
        
        System.out.println("MKDIR...");
        
        System.out.println("Received: " + canonicalPath);

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

        System.out.println("Deleting Directory ...");
        
        System.out.println("Received " +canonicalPath);

        File file = new File(canonicalPath);
        delete = file.delete();
        
        if(delete){
            return sendMSG(new MSG(Constants.CODE_SERVER_RMDIR_OK,Arrays.asList("SUCSSESS -> Deleted the file [" + canonicalPath + "].")));
        }else{
            return sendMSG(new MSG(Constants.CODE_SERVER_RMDIR_ERROR,Arrays.asList("ERROR -> Ocurred an error deleting the file [" + canonicalPath + "]")));
        }
    }
    //VISTO
    public boolean processRenameRequest(String filename, String newFileName)
    {                
        if(serverSocket == null){
            return false;
        }

        System.out.println("Changind the name of the Directory/File...");
        try{
        // File (or directory) with old name
        File oldFile = new File(filename);

        // File (or directory) with new name
        File newFile = new File(newFileName);

        if (oldFile.exists())
            System.out.println("DIDNT OCURRED ANY ERROR");
        
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

        System.out.println("Copying the Directory/File...");
        
        System.out.println("Received filename: " + filename + 
                "    with the destination folder " + fileDestination);
        try{
            
            
        File file = new File( directoryPath + File.separator + File.separator + filename);
        new File(directoryPath + File.separator + File.separator + fileDestination + File.separator + File.separator + filename).mkdirs();
        File fileDest = new File(directoryPath + File.separator + File.separator + fileDestination + File.separator + File.separator + filename);
        FileUtils.copyDirectory(file, fileDest);
        
        return sendMSG(new MSG(Constants.CODE_SERVER_COPY_OK,Arrays.asList("SUCCESS -> Copied the file [" + filename + "] to destination [" + fileDestination + filename + "")));
        }catch(IOException ex){
            System.out.println(ex.toString());
             return sendMSG(new MSG(Constants.CODE_SERVER_COPY_ERROR,Arrays.asList("ERROR -> Ocurred some erro in copy request :" + filename+ "]")));
        }
    }
    //VISTO
    public boolean processMoveRequest(String filename, String fileDestination)
    {                
        if(serverSocket == null){
            return false;
        }

        System.out.println("Moving the Directory/File...");
        
        System.out.println("Received comand to move :  " + filename + " to de fileDestination:  " + fileDestination);
        
        try{
        
        File fileOrigin = new File(filename);
        File fileDest = new File(fileDestination);
            if(directoryExists(fileOrigin) || fileExists(fileOrigin)){
                if(directoryExists(fileOrigin)){

                    Path source = fileOrigin.toPath();
                    Path newdir = fileDest.toPath();
                    Files.move(source, newdir.resolve(source.getFileName()), REPLACE_EXISTING);
                    System.out.println("ola");
                    return sendMSG(new MSG(Constants.CODE_SERVER_MOVE_OK,Arrays.asList("SUCCESS -> moved the File [" 
                            + filename + "] to destination [" + fileDestination + "]")));
                }

            }
        }catch(IOException ex){
            System.out.println(ex.toString());
            return sendMSG(new MSG(Constants.CODE_SERVER_MOVE_ERROR,Arrays.asList("ERROR -> Ocurred an error moving the File :" + filename + "]   ->   " + fileDestination)));
        }
        return sendMSG(new MSG(Constants.CODE_SERVER_MOVE_ERROR,Arrays.asList("ERROR -> Ocurred an error moving the File :" + filename + "]   ->   " + fileDestination)));
    }
    //VISTO
    public boolean sendMSG(MSG msg)
    {
        try{
            outObj.writeObject(msg);
            outObj.flush();
        }catch(IOException e){
            System.out.println("ERROR -> in comunication to the client " + 
                toClientSocket.getInetAddress().getHostAddress() + ":" + 
                toClientSocket.getPort()+"\n\t" + e);
            return false;
        }
        return true;
    }
    //VISTO
    public String listar(File directory) {
        String list = "\n";
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