
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 *
 * @author andre
 */
public class DistributedFileSystem implements ClientMethodsInterface {
    private boolean remote; // FALSE(local), TRUE(remote)
    private String workingDirectory;
    
    public DistributedFileSystem(String workingDirectory) {
        remote = false;
        this.workingDirectory = workingDirectory;
    }
    
    public void switchSystemType() {
        if(remote)
            remote = !remote;
        
        // VERIFICAR SE EXISTEM SERVIDORES LIGADOS
    }
    
    @Override
    public void register(String username, String password) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void login(String username, String password) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void logout() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public void connect(String serverName) {
        
    }
    
    @Override
    public void disconnect() {
        
    }

    @Override
    public void list(String type) {
        //MANDAR SEMPRE PARA O SD
        //if(remote){
        //}
    }
    
    @Override
    public void copyFile(String filename, String destinationPath) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void moveFile(String filename, String destinationPath) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void changeWorkingDirectory(String newWorkingDirectoryPath) {
        if(remote){
            throws Exceptions.CmdFailure();
        }else{
            File folder = new File(newWorkingDirectoryPath);
        }
    }

    @Override
    public void getWorkingDirContent(String workingDir) 
            throws Exceptions.CmdFailure {
        if(remote){
            // PEDIR "ls" ao Servidor em questão
            throw new Exceptions.CmdFailure();
        }else{
            File folder = new File(workingDir);
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
        if(remote){
            // PEDIR "cat" ao Servidor em questão
            throw new Exceptions.CmdFailure();
        }else{
            try{
                BufferedReader in = new BufferedReader(new FileReader(directoria+"\\"+filename));
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
    public void removeFile(String name) {
        if(remote){
            // PEDIR "cat" ao Servidor em questão
            throw new Exceptions.CmdFailure();
        }else{
            try{
                File file = new File(directoria+"\\"+filename));
                if(file.delete())
                    System.out.println("\nFile +"filename+" successfully deleted");
                else
                    System.out.println("\nFile +"filename+" not deleted");
            } catch(FileNotFoundException ex) {
                throw new Exceptions.CmdFailure();
            } catch(IOException ex) {
                throw new Exceptions.CmdFailure();
            }
        }
    }

    @Override
    public void makeDir(String directoryName) {
        if(remote){
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }else{
            File theDir = new File(directoryName);
            if (!theDir.exists()){
                System.out.println("creating directory: " + directoryName);
                boolean result = false;

                try{
                    theDir.mkdir();
                    result = true;
                }catch(SecurityException se){
                    //handle it
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
        if(remote){
            //SÓ SE ESTIVER EM MODO remote
        }
    }
    
    @Override
    public void uploadFile(String fileName) {
        if(!remote){
            //SÓ SE ESTIVER EM MODO local
        }
    }
}
