
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import java.util.ArrayList;
import java.util.Arrays;
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
    
    public DistributedFileSystem(Client client) {
        fileSystem = FS_DIRECTORY_SERVICE;
        this.client = client;
        try {
            localDirectory = new File(".").getCanonicalPath();
            System.out.println("Folder created:" + localDirectory);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
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
                if(client.getCurrentConnection().getServerName().equals(to)){
                    if(client.checkIfImConnected(to)){
                        fileSystem = FS_SERVER;
                        client.setCurrentConnection(client.getServerConnection(to));
                    }else
                        throw new Exceptions.NotConnectedToServer();
                }else
                    throw new Exceptions.ServerDoesntExist();
            }
        }catch(Exception ex){
            ex.printStackTrace();
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
                            fileSystem = FS_SERVER;
                        }else
                            throw new Exceptions.MyClientInfoNotFound();
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
            MSG msg = new MSG(0, Arrays.asList(Constants.CMD_DISCONNECT));
            client.sendRequestTcp(msg);
        }
    }

    @Override
    public void list(String type) {
        if(fileSystem == FS_DIRECTORY_SERVICE || fileSystem == FS_SERVER){

            client.sendRequestUdp(Constants.CMD_LIST + " " + type);
            client.receiveResponseUdp();
            try{
                client.processDirectoryServiceCommand();
            }catch(Exception ex){
                System.out.println(ex);
            }

            /*
            if(type.equals(Constants.CMD_LIST_S)){
                try {
                    String objectUrl = client.getDirectoryServiceIp().getHostAddress();
                    String registration = "rmi://"+objectUrl+"/"+Constants.SERVICE_SERVER_LIST;
                    Remote remote = Naming.lookup(registration);
                    GetRemoteServerListInterface fileService = (GetRemoteServerListInterface) remote;
                    
                    client.updateServerList(fileService.getServerList());
                } catch (NotBoundException ex) {
                    ex.printStackTrace();
                } catch (MalformedURLException ex) {
                    ex.printStackTrace();
                } catch (RemoteException ex) {
                    ex.printStackTrace();
                }
            } else {*/
            //}
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

    // cd ..  || cd download || cd c:\download\java
    @Override
    public void changeWorkingDirectory(String newWorkingDirectoryPath) {
                   
        try{
            if(fileSystem == FS_SERVER){
                MSG msg = new MSG(0, Arrays.asList(Constants.CMD_CD_DIR, 
                    newWorkingDirectoryPath));
                client.sendRequestTcp(msg);
                client.receiveResponseTcp();
            }else if(fileSystem == FS_LOCAL){

                String pattern = Pattern.quote(System.getProperty("file.separator"));
                String pathParts[] =  localDirectory.split(pattern);
                String newPath = "";
                
                if(newWorkingDirectoryPath.equals("..")){
                    if(localDirectory.split(File.separator).length == 1){
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
                        throw new Exceptions.ErrorReadingFileOrDirectory();
                    
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
                    text.add(line);
                }
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
            }else if(fileSystem == FS_LOCAL){
                File theDir = new File(localDirectory + File.separator + directoryName);
                if (theDir.exists()){
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
            }
        } catch(Exception ex) {
            System.out.println(ex);
        }
    }
}
