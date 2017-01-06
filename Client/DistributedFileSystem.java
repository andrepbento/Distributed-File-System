
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.FileUtils;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.regex.Pattern;

/**
 *
 * @author andre
 */
public class DistributedFileSystem implements ClientMethodsInterface {
    public static final int FS_DIRECTORY_SERVICE = 0;
    public static final int FS_SERVER = 1;
    public static final int FS_LOCAL = 2;
    
    private int fileSystem; // 0(DS), 1(Server), 2(local)
    private String localDirectory;
    private Client client;
    
    private String pattern;
    
    public DistributedFileSystem(Client client) {
        fileSystem = FS_DIRECTORY_SERVICE;
        this.client = client;
        try {
            localDirectory = new File(".").getCanonicalPath();
            System.out.println("Folder created:" + localDirectory);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        pattern = Pattern.quote(System.getProperty("file.separator"));
    }

    public String getCurrentPath() {
        switch (fileSystem) {
            case FS_DIRECTORY_SERVICE:
                return Constants.DS;
            case FS_SERVER:
                return //client.getCurrentConnection().getServerName() +
                        client.getCurrentConnection().getCurrentPath();
            case FS_LOCAL:
                return localDirectory;
        }
        return null;
    }
    
    // CHAMAR ESTA FUNCAO SEMPRE QUE O CMD TENHA lenght=1 [Comando interno]
    public void switchSystemType(String to) {
        try{
            if(to.equalsIgnoreCase(Constants.DS)){ //MUDAR PARA DS
                fileSystem = FS_DIRECTORY_SERVICE;
            }else if(to.equalsIgnoreCase(Constants.LOCAL)){ //MUDAR PARA LOCAL
                if(client.getCurrentConnection() != null){
                    fileSystem = FS_LOCAL;
                }
                else
                    throw new Exceptions.SwitchingLocalNotPossible();
            }else{ //MUDAR PARA "SERVER_X"
                if(client.getServerConnection(to) != null){
                    if(client.checkIfImConnected(to)){
                        fileSystem = FS_SERVER;
                        client.setCurrentConnection(client.getServerConnection(to));
                    }else
                        throw new Exceptions.NotConnectedToServer();
                }else
                    throw new Exceptions.ServerDoesntExist();
            }
        }catch(Exception ex){
            System.out.println(ex);
        }
    }
    
    @Override
    public void register(String username, String password) {
        if(fileSystem == FS_DIRECTORY_SERVICE){
            client.sendRequestUdp(Constants.CMD_REGISTER+" "+username+" "+password);
            client.receiveResponseUdp();
            try{
                client.processDirectoryServiceCommand();
            }catch(Exception ex){
                System.out.println(ex);
            }
        }
    }

    @Override
    public void login(String username, String password) {
        if(fileSystem == FS_DIRECTORY_SERVICE){
            client.sendRequestUdp(Constants.CMD_LOGIN+" "+username+" "+password);
            client.receiveResponseUdp();
            try{
                client.processDirectoryServiceCommand();
            }catch(Exception ex){
                System.out.println(ex);
            }
        }
    }

    @Override
    public void logout() {
        if(fileSystem == FS_DIRECTORY_SERVICE){
            client.sendRequestUdp(Constants.CMD_LOGOUT);
            client.receiveResponseUdp();
            try{
                client.processDirectoryServiceCommand();
            }catch(Exception ex){
                System.out.println(ex);
            }
        }
    }
        
    @Override
    public void connect(String serverName) {
        if(fileSystem == FS_DIRECTORY_SERVICE || fileSystem == FS_SERVER){ //AQUI, MÃO SE PODE CONNECTAR QUANDO ESTÁ EM LOCAL?
            try{
                if(client.getServerConnection(serverName) != null){
                    if(!client.checkIfImConnected(serverName)){
                        client.getServerConnection(serverName).createSocket();
                        client.receiveResponseTcp(serverName);  
                        if(client.getMsg().getMSGCode() == Constants.CODE_CONNECT_OK){
                            MSG msg = new MSG();
                            list(Constants.CMD_LIST_C);
                            if(client.getMyClientInfo() != null){
                                msg.setClientList(Arrays.asList(client.getMyClientInfo()));
                                client.sendRequestUdp(Constants.CMD_CONNECT + " " + 
                                        client.getUsername() + " " + serverName);
                                ServerConnection serverC = client.getServerConnection(serverName);
                                client.setCurrentConnection(serverC);
                                client.getCurrentConnection().setConnected(true);
                                client.sendRequestTcp(msg);
                                client.receiveResponseTcp();
                                client.getCurrentConnection().setCurrentPath(client.getMsg().getCMDarg(0));
                                fileSystem = FS_SERVER;
                                System.out.println("Connected to " + serverName);
                            }
                            else 
                                throw new Exceptions.MyClientInfoNotFound();
                        }
                        else if (client.getMsg().getMSGCode() == Constants.CODE_CONNECT_FAILURE)
                            throw new Exceptions.ConnectFailure();
                    }
                    else
                        throw new Exceptions.AlreadyConnected();
                }
                else
                    throw new Exceptions.ServerDoesntExist();
            }catch(Exception ex){
                System.out.println(ex);
            }
        }
    }
    
    @Override
    public void disconnect() {
        if(fileSystem == FS_DIRECTORY_SERVICE || fileSystem == FS_SERVER){
            try{
                MSG msg = new MSG(0, Arrays.asList(Constants.CMD_DISCONNECT));
                client.sendRequestTcp(msg);
                client.receiveResponseTcp();
                if(client.getMsg().getMSGCode() == Constants.CODE_DISCONNECT_OK){
                    client.sendRequestUdp(Constants.CMD_DISCONNECT + " " + 
                            client.getUsername() + " " +  client.getCurrentConnection().getServerName());
                }
                else if(client.getMsg().getMSGCode() == Constants.CODE_DISCONNECT_ERROR)
                    throw new Exceptions.ErrorDisconnecting();
            }catch(Exception ex){
                System.out.println(ex);
            }
        }
    }

    @Override
    public void list(String type) {
        try {
            if(fileSystem == FS_DIRECTORY_SERVICE || fileSystem == FS_SERVER){

                String objectUrl = client.getDirectoryServiceIp().getHostAddress();
                String registration = "rmi://"+objectUrl+"/"+Constants.SERVICE_SERVER_LIST;
                Remote remote = Naming.lookup(registration);
                GetRemoteServerListInterface listService = (GetRemoteServerListInterface) remote;

                if(type.equals(Constants.CMD_LIST_S))
                    client.updateServerList(listService.getServerList());
                else if(type.equals(Constants.CMD_LIST_C))
                    client.updateClientList(listService.getClientList());
                else
                    throw new Exceptions.CmdNotRecognized();
            } else
                throw new Exceptions.CmdFailure();
        } catch (NotBoundException | MalformedURLException | RemoteException ex) {
            ex.printStackTrace();
        } catch (Exceptions.CmdFailure | Exceptions.CmdNotRecognized ex) {
            System.out.println(ex);
        }
    }
    
    @Override
    public void chat(String to, String msg) {
        if(fileSystem == FS_SERVER || fileSystem == FS_DIRECTORY_SERVICE) {
            MSG msgToSend = new MSG();
            msgToSend.getCMD().addAll(Arrays.asList(Constants.CLIENT, Constants.CMD_CHAT, to,
                    msg));
            client.sendRequestUdp(msgToSend);
            client.receiveResponseUdp();
            try{
                client.processDirectoryServiceCommand();
            }catch(Exception ex){
                System.out.println(ex);
            }
        }
    }
    
    @Override
    public void copyFile(String fileName, String destination) {
        try {
            if(fileSystem == FS_SERVER){
                MSG msg = new MSG(0, Arrays.asList(Constants.CMD_COPY_FILE, 
                        fileName, destination));
                client.sendRequestTcp(msg);
                client.receiveResponseTcp();
                if(client.getMsg().getMSGCode() == Constants.CODE_SERVER_COPY_OK)
                    System.out.println(client.getMsg().getCMDarg(0));
                else if(client.getMsg().getMSGCode() == Constants.CODE_SERVER_COPY_ERROR)
                    throw new Exceptions.ErrorCopyingFile();
                
            }else if(fileSystem == FS_LOCAL){
                File origin = new File(localDirectory + File.separator + fileName);
                File dest = new File(localDirectory + File.separator + destination);
                
                if(!origin.exists())
                    throw new Exceptions.DirectoryOrFileDoesntExist();
                
                if(!dest.exists())
                    throw  new Exceptions.DirectoryOrFileDoesntExist();
                
                if(!dest.isDirectory())
                    throw new Exceptions.NotADirectory();
                                                    
                File file = new File(localDirectory + File.separator + fileName);
                new File(localDirectory + File.separator + destination + File.separator + fileName).mkdirs();
                File fileDest = new File(localDirectory + File.separator + destination + File.separator + fileName);
                FileUtils.copyDirectory(file, fileDest);
            }
        } catch(Exception ex) {
             System.out.println(ex);
        }
    }

    @Override
    public void moveFile(String fileName, String destination) {
        try{
            if(fileSystem == FS_SERVER){
                MSG msg = new MSG(0, Arrays.asList(Constants.CMD_MOVE_FILE, 
                            fileName, destination));
                client.sendRequestTcp(msg);
                client.receiveResponseTcp();
                
                if(client.getMsg().getMSGCode() == Constants.CODE_SERVER_MOVE_OK)
                    System.out.println(client.getMsg().getCMDarg(0));
                else if(client.getMsg().getMSGCode() == Constants.CODE_SERVER_MOVE_ERROR)
                    throw  new Exceptions.ErrorMovingFile();
                
            }else if(fileSystem == FS_LOCAL){
                File origin = new File(localDirectory + File.separator + fileName);
                File dest = new File(localDirectory + File.separator + destination);
                
                if(!origin.exists())
                    throw new Exceptions.DirectoryOrFileDoesntExist();
                
                if(!dest.exists())
                    throw  new Exceptions.DirectoryOrFileDoesntExist();
                
                if(!dest.isDirectory())
                    throw new Exceptions.NotADirectory();
                
                File fileOrigin = new File(fileName);
                File fileDest = new File(destination);
                Path source = fileOrigin.toPath();
                Path newdir = fileDest.toPath();
                Files.move(source, newdir.resolve(source.getFileName()), REPLACE_EXISTING);
            }
        } catch(Exception ex) {
            System.out.println(ex);
        }
    }

    @Override
    public void changeWorkingDirectory(String newWorkingDirectoryPath) {
                   
        try{
            if(fileSystem == FS_SERVER){
                MSG msg = new MSG(0, Arrays.asList(Constants.CMD_CD_DIR, 
                    newWorkingDirectoryPath));
                client.sendRequestTcp(msg);
                client.receiveResponseTcp();
                
                if(client.getMsg().getMSGCode() == Constants.CODE_SERVER_CD_OK){
                    System.out.println(client.getMsg().getCMDarg(0));
                    client.getCurrentConnection().setCurrentPath(client.getMsg().getCMDarg(1));
                }
                else if(client.getMsg().getMSGCode() == Constants.CODE_SERVER_CD_ERROR)
                    throw  new Exceptions.ErrorChangingDirectory();
                
            }else if(fileSystem == FS_LOCAL){
                String pathParts[] =  localDirectory.split(pattern);
                String newPath = "";
                
                if(newWorkingDirectoryPath.equals("..")){
                    if(localDirectory.split(pattern).length == 1){
                        System.out.println("Can't go back...");
                    }else{
                        for(int i = 0; i < pathParts.length - 1; i++)
                            newPath += pathParts[i] + File.separator;    
                        localDirectory = newPath;
                    }
                 }
                 else{
                    File dir = new File(localDirectory + File.separator + newWorkingDirectoryPath);
                    
                    if(!dir.exists())
                        throw new Exceptions.DirectoryOrFileDoesntExist();

                    if(!dir.canRead())
                        throw new Exceptions.ErrorReadingPermissions();
                    
                    localDirectory = localDirectory + File.separator + newWorkingDirectoryPath;
                 }
            }
        } catch(Exception ex) {
            System.out.println(ex);
        }
    }

    @Override
    public void getWorkingDirContent() {
        try{
            if(fileSystem == FS_SERVER){
                MSG msg = new MSG(0, Arrays.asList(Constants.CMD_LS_DIR));
                client.sendRequestTcp(msg);
                client.receiveResponseTcp();
                
                if(client.getMsg().getMSGCode() == Constants.CODE_SERVER_LS_OK){
                    System.out.println(client.getMsg().getCMDarg(0));
                }
                else if(client.getMsg().getMSGCode() == Constants.CODE_SERVER_LS_ERROR)
                    throw  new Exceptions.ErrorListingDirectory();
                
            }else if(fileSystem == FS_LOCAL){
                File folder = new File(localDirectory);
                if(!folder.exists())
                    throw new Exceptions.DirectoryOrFileDoesntExist();
                
                File[] listOfFiles = folder.listFiles();

                if(listOfFiles.length == 0)
                    System.out.println("No files or directories...");
                
                for (int i = 0; i < listOfFiles.length; i++) {
                    if (listOfFiles[i].isFile())
                        System.out.println("f: " + listOfFiles[i].getName());
                    else if (listOfFiles[i].isDirectory())
                        System.out.println("d: " + listOfFiles[i].getName());
                }
            }
        } catch(Exception ex) {
            System.out.println(ex);
        }
    }

    @Override
    public void getFileContent(String fileName) {
        try{
            if(fileSystem == FS_SERVER){
                MSG msg = new MSG(0, Arrays.asList(Constants.CMD_CAT_FILE, fileName));
                client.sendRequestTcp(msg);
                client.receiveResponseTcp();
                
                if(client.getMsg().getMSGCode() == Constants.CODE_SERVER_CAT_OK){
                    for(String line : msg.getCMD())
                        System.out.print(line);
                }
                else if(client.getMsg().getMSGCode() == Constants.CODE_SERVER_CAT_ERROR)
                    throw  new Exceptions.ErrorShowingFileContent();
                
            }else if(fileSystem==FS_LOCAL){
                File file = new File(localDirectory + File.separator + fileName);

                if(!file.exists())
                    throw new Exceptions.DirectoryOrFileDoesntExist();

                if(!file.isFile())
                    throw  new Exceptions.NotAfile();

                List<String> text = new ArrayList<>();
                BufferedReader br = new BufferedReader(
                        new FileReader(localDirectory + File.separator + fileName));
                String line;
                while ((line = br.readLine()) != null) {
                    text.add(line+"\n");
                }
                System.out.println("File content:\n");
                for(String lineTmp : text)
                    System.out.println(lineTmp);
            }
        } catch(Exception ex) {
            System.out.println(ex);
        }
    }
    
    @Override
    public void renameFile(String oldName, String newName) {
        try{
            if(fileSystem == FS_SERVER){
                MSG msg = new MSG(0, Arrays.asList(Constants.CMD_RENAME_FILE, 
                            oldName, newName));
                client.sendRequestTcp(msg);
                client.receiveResponseTcp();
                
                if(client.getMsg().getMSGCode() == Constants.CODE_SERVER_RENAME_OK){
                    System.out.println(client.getMsg().getCMDarg(0));
                }
                else if(client.getMsg().getMSGCode() == Constants.CODE_SERVER_RENAME_ERROR)
                    throw  new Exceptions.ErrorRenamingFile();
                
            }else if(fileSystem==FS_LOCAL){
                File oldFile = new File(localDirectory + File.separator + oldName);
                File newFile = new File(localDirectory + File.separator + newName);

                if (!oldFile.exists())
                   throw new Exceptions.DirectoryOrFileDoesntExist();

                if (!oldFile.renameTo(newFile)) {
                    throw new Exceptions.ErrorRenamingFile();
                }
                
                System.out.println("File renamed to: " + newName);
            }
        } catch(Exception ex) {
            System.out.println(ex);
        }
    }

    @Override
    public void removeFile(String fileName) {
        try{
            if(fileSystem == FS_SERVER){
                MSG msg = new MSG(0, Arrays.asList(Constants.CMD_RM_FILE, fileName));
                client.sendRequestTcp(msg);
                client.receiveResponseTcp();
                
                if(client.getMsg().getMSGCode() == Constants.CODE_SERVER_RMDIR_OK){
                    System.out.println(client.getMsg().getCMDarg(0));
                }
                else if(client.getMsg().getMSGCode() == Constants.CODE_SERVER_RMDIR_ERROR)
                    throw  new Exceptions.ErrorRemovingFileOrDirectory();
                
            }else if(fileSystem == FS_LOCAL){
                File file = new File(localDirectory + File.separator + fileName);
                if(file.delete())
                    System.out.println("\nFile "+fileName+" successfully deleted");
                else
                    throw new Exceptions.ErrorRemovingFileOrDirectory();
            }
        } catch(Exception ex) {
            System.out.println(ex);
        }
    }

    @Override
    public void makeDir(String directoryName) {
        try{
            if(fileSystem == FS_SERVER){
                MSG msg = new MSG(0, Arrays.asList(Constants.CMD_MK_DIR, directoryName));
                client.sendRequestTcp(msg);
                client.receiveResponseTcp();
                
                if(client.getMsg().getMSGCode() == Constants.CODE_SERVER_MKDIR_OK){
                    System.out.println(client.getMsg().getCMDarg(0));
                }
                else if(client.getMsg().getMSGCode() == Constants.CODE_SERVER_MKDIR_ERROR)
                    throw  new Exceptions.ErrorCreatingDirectory();
                
            }else if(fileSystem == FS_LOCAL){
                File theDir = new File(localDirectory + File.separator + directoryName);
                if (!theDir.exists()){
                    System.out.println("creating directory: " + directoryName);
                    if(theDir.mkdir()){    
                        System.out.println("DIR " + directoryName + " created");  
                    }else{
                         throw new Exceptions.ErrorCreatingDirectory();
                    }
                }else{
                    throw new Exceptions.ErrorCreatingDirectory();
                }
            }
        } catch(Exception ex) {
            System.out.println(ex);
        }
    }
    
    @Override
    public void downloadFile(String fileName) {
        try {
            if(fileSystem == FS_SERVER){
                MSG msg = new MSG(0, Arrays.asList(Constants.CMD_DOWNLOAD_FILE, fileName));
                client.sendRequestTcp(msg);
                client.receiveResponseTcp();
                
                if(client.getMsg().getMSGCode() == Constants.CODE_SERVER_DOWNLOAD_OK){
                    new DownloadThread(client.getCurrentConnection(), 
                            localDirectory, fileName).start();
                }
                else if(client.getMsg().getMSGCode() == Constants.CODE_SERVER_DOWNLOAD_ERROR)
                    throw  new Exceptions.ErrorDownloadingFile();
            }
        } catch(Exception ex) {
            System.out.println(ex);
        }
    }
    
    @Override
    public void uploadFile(String fileName) {
        try{
            if(fileSystem == FS_SERVER){
                MSG msg = new MSG(0, Arrays.asList(Constants.CMD_UPLOAD_FILE, fileName));
                client.sendRequestTcp(msg);
                client.receiveResponseTcp();
                
                if(client.getMsg().getMSGCode() == Constants.CODE_SERVER_UPLOAD_OK){
                    new UploadThread(client.getCurrentConnection(), 
                            localDirectory, fileName).start();
                }
                else if(client.getMsg().getMSGCode() == Constants.CODE_SERVER_UPLOAD_ERROR)
                    throw  new Exceptions.ErrorUploadingFile();
            }
        } catch(Exception ex) {
            System.out.println(ex);
        }
    }
}
