
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;


public class UploadThread extends Thread{
    private ServerConnection currentConnection;
    private String filePath;
    private String fileName;
    
    public UploadThread(ServerConnection currentConnection, String filePath, String fileName) {
        this.currentConnection = currentConnection;
        this.filePath = filePath;
        this.fileName = fileName;
    }
    
    @Override
    public void run() {
        File localDir = null;
        OutputStream outputStream = null;
        FileInputStream requestedFileInputStream = null;
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

            if(!localDir.canRead()){                        
                throw new Exceptions.ErrorReadingPermissions();
            }

            filePath = localDir.getCanonicalPath() + 
                    File.separator + fileName;
            fileChunck = new byte[Constants.MAX_SIZE];
            outputStream = currentConnection.getOutputStream();
            
            while((nbytes = requestedFileInputStream.read(fileChunck))>0){                        
                outputStream.write(fileChunck, 0, nbytes);
                outputStream.flush();
                System.out.println("Sent block n. " + ++blockCounter + " with " + nbytes + " bytes.");
                System.out.println("Removed " + nbytes + " bytes from file " + filePath+ ".");   
            }                    

            System.out.println("Upload done.");
        }catch(Exception ex){
            System.out.println(ex);
        }finally{
            
            if(outputStream != null){
                try{
                    outputStream.close();
                }catch(IOException e){}
            }
            
            if(requestedFileInputStream != null){
                try{
                    requestedFileInputStream.close();
                }catch(IOException e){}
            }
        }
    }
}
