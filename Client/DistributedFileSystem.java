
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 * @author andre
 */
public class DistributedFileSystem implements ClientMethodsInterface {
    public static final int FS_DIRECTORY_SERVICE = 0;
    public static final int FS_SERVER = 1;
    public static final int FS_LOCAL = 2;
    
    private int fileSystem; // 0(DS), 1(Server), 2(local) ??????????????????????? TALVEZ SEJA MELHOR ASSIM TRUE|FALSE NAO CHEGA
    private String localDirectory;
    private Client client;
    
    public DistributedFileSystem(Client client) {
        fileSystem = FS_DIRECTORY_SERVICE;
        this.client = client;
        try {
            localDirectory = new File(".").getCanonicalPath();
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
        if(to.equals(Constants.DS)){ //MUDAR PARA DS
            fileSystem = FS_DIRECTORY_SERVICE;
        }else if(to.equals(Constants.LOCAL)){ //MUDAR PARA LOCAL
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
    }
    
    @Override
    public void register(String username, String password) {
        if(fileSystem == FS_DIRECTORY_SERVICE){
            sendRequestUdp(REGISTER+" "+username+" "+password) //***************
        }
    }

    @Override
    public void login(String username, String password) {
        if(fileSystem == FS_DIRECTORY_SERVICE){
            sendRequestUdp(LOGIN+" "+username+" "+password) //******************
        }
    }

    @Override
    public void logout() {
        if(fileSystem == FS_DIRECTORY_SERVICE){
            sendRequestUdp(LOGIN+" "+username+" "+password) //******************
        }
    }
    
    @Override
    public void connect(String serverName) {
        if(fileSystem == FS_DIRECTORY_SERVICE || fileSystem == FS_SERVER){ //AQUI, MÃO SE PODE CONNECTAR QUANDO ESTÁ EM LOCAL?
            if(client.getServerConnection(serverName) != null){
                if(!client.checkIfImConnected(serverName)){
                    client.getCurrentConnection().createSocket();
                    try {
                        Object obj = client.receiveResponseTcp();
                        if(obj instanceof MSG){
                            MSG msg = (MSG)obj;
                            if(msg.getMSGCode() == Constants.CODE_CONNECT_OK){
                                //Mandar ao Server o clientInfo
                                //Mandar ao DS que estou connectado com este servidor
                            }
                            else
                                throw new Exceptions.ConnectFailure();
                        }
                    } catch (IOException | ClassNotFoundException ex) {
                        ex.printStackTrace();
                    }
                    
                }
                else
                    throw new Exceptions.AlreadyConnected();
            }
            else
                throw new Exceptions.ServerDoesntExist();
        }
    }
    
    @Override
    public void disconnect() {
        if(fileSystem == FS_DIRECTORY_SERVICE||fileSystem==FS_SERVER){
            sendRequestTCP("NAO SEI O COMANDO") //******************************
        }
    }

    @Override
    public void list(String type) {
        if(fileSystem == FS_DIRECTORY_SERVICE||fileSystem==FS_SERVER){
            sendRequestUDP(LIST+" "+type) //************************************
        }
    }
    
    @Override
    public void copyFile(String fileName, String destinationPath) {
        if(fileSystem == FS_SERVER){
            sendRequestTCP(CP+" "+fileName+" "+destinationPath) //**************
        }else if(fileSystem==FS_LOCAL){
            try {
                InputStream input = new FileInputStream(localDirectory+"\\"+fileName);
                OutputStream output = new FileOutputStream(destinationPath);
                byte[] buf = new byte[1024];
                int bytesRead;
                while ((bytesRead = input.read(buf)) > 0)
                    output.write(buf, 0, bytesRead);
                input.close();
                output.close();
            } catch(FileNotFoundException ex) {
                ex.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    public void moveFile(String fileName, String destinationPath) {
        if(fileSystem==FS_SERVER){
            sendRequestTCP(CP+" "+fileName+" "+destinationPath) //**************
        }else if(fileSystem==FS_LOCAL){
            copyFile(fileName, destinationPath);
            new File(localDirectory+"\\"+fileName).delete();
        }
    }

    @Override
    public void changeWorkingDirectory(String newWorkingDirectoryPath) {
        if(fileSystem==FS_SERVER){
            sendRequestTCP(CD+" "+newWorkingDirectoryPath) //**************
        }else if(fileSystem==FS_LOCAL){
            if(newWorkingDirectoryPath.split("\\").length==1){
                if(newWorkingDirectoryPath.equals("..")){
                    String 
                    File folder = new File(newWorkingDirectoryPath);
                }else{
                    
                }
                //..
                //(ou)
                //pasta especifica na directoria corrente
            }else{
                //caminho completo
                try{
                    File folder = new File(newWorkingDirectoryPath);
                    localDirectory=folder.getCanonicalPath();
                } catch(IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    @Override
    public void getWorkingDirContent() 
            throws Exceptions.CmdFailure {
        if(fileSystem==FS_SERVER){
            sendRequestTCP(LS)
            // PEDIR "ls" AO SERVIDOR EM QUESTAO
        }else if(fileSystem==FS_LOCAL){
            File folder = new File(localDirectory);
            File[] listOfFiles = folder.listFiles();

            for (int i = 0; i < listOfFiles.length; i++) {
                if (listOfFiles[i].isFile())
                    System.out.println("f: " + listOfFiles[i].getName());
                else if (listOfFiles[i].isDirectory())
                    System.out.println("d: " + listOfFiles[i].getName());
            }
            throw new Exceptions.;
        }
    }

    @Override
    public void getFileContent(String fileName)
            throws Exceptions.CmdFailure {
        if(fileSystem==FS_SERVER){
            sendRequestTCP(CAT) //**********************************************
        }else if(fileSystem==FS_LOCAL){
            try{
                BufferedReader in = new BufferedReader(
                        new FileReader(localDirectory+"\\"+fileName));
                System.out.println("\nFile content:");
                String line;
                while((line = in.readLine()) != null)
                    System.out.println(line);
                in.close();
            } catch(FileNotFoundException ex) {
                throw new Exceptions.CmdFailure();
            } catch(IOException ex) {
                throw new Exceptions.CmdFailure();
            }
        }
    }

    @Override
    public void removeFile(String fileName) {
        if(fileSystem==FS_SERVER){
            sendRequestTCP(RM+" "+fileName) //**********************************
        }else if(fileSystem==FS_LOCAL){
            File file = new File(localDirectory+"\\"+fileName);
            if(file.delete())
                System.out.println("\nFile "+fileName+" successfully deleted");
            else
                System.out.println("\nFile "+fileName+" not deleted");
        }
    }

    @Override
    public void makeDir(String directoryName) {
        if(fileSystem==FS_SERVER){
            
        }else if(fileSystem==FS_LOCAL){
            File theDir = new File(directoryName);
            if (!theDir.exists()){
                System.out.println("creating directory: " + directoryName);
                boolean result = false;

                try{
                    theDir.mkdir();
                    result = true;
                }catch(SecurityException se){
                    se.printStackTrace();
                }
                if(result){    
                    System.out.println("DIR created");  
                }else{
                    
                }
            }
        }
    }
    
    @Override
    public void downloadFile(String fileName) {
        if(fileSystem==FS_SERVER){
            //FAZER DOWNLOAD DO FICHEIRO PARA localDirectory
        }
    }
    
    @Override
    public void uploadFile(String fileName) {
        if(fileSystem==FS_LOCAL){
            //FAZER UPLOAD DO FICHEIRO PARA remoteDirectory
        }
    }
}
