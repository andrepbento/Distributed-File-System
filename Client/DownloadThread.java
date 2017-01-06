
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;


public class DownloadThread extends Thread{
    private ServerConnection currentConnection;
    private String filePath;
    private String fileName;
    
    public DownloadThread(ServerConnection currentConnection, String filePath, String fileName) {
        this.currentConnection = currentConnection;
        this.filePath = filePath;
        this.fileName = fileName;
    }
    
    @Override
    public void run() {
        File localDir = null;
        FileOutputStream localFileOutputStream = null;
        InputStream inputStream = null;
        byte [] fileChunck;
        int nbytes;  
        int blockCounter = 0;
        
        try{
            localDir = new File(filePath);
            
            if(!localDir.exists()){
                throw new Exceptions.DirectoryOrFileDoesntExist();
            }

            if(!localDir.isDirectory()){
                throw new Exceptions.NotADirectory();
            }

            if(!localDir.canWrite()){                        
                throw new Exceptions.ErrorWrittingPermissions();
            }

            filePath = localDir.getCanonicalPath() + 
                    File.separator + fileName;
            localFileOutputStream = new FileOutputStream(filePath);
            System.out.println("File " + filePath + " created.");

            fileChunck = new byte[Constants.MAX_SIZE];
            inputStream = currentConnection.getSocket().getInputStream();
            
            while((nbytes = inputStream.read(fileChunck)) > 0){                    
                localFileOutputStream.write(fileChunck, 0, nbytes);
                System.out.println("Received block n. " + ++blockCounter + " with " + nbytes + " bytes.");
                System.out.println("Added " + nbytes + " bytes to file " + filePath+ ".");                    
            } 

            System.out.println("Download done.");
        }catch(Exception ex){
            System.out.println(ex);
        }finally{
            
            if(localFileOutputStream != null){
                try{
                    localFileOutputStream.close();
                }catch(IOException e){}
            }
            
            if(inputStream != null){
                try{
                    inputStream.close();
                }catch(IOException e){}
            }
        }
    }  
}
