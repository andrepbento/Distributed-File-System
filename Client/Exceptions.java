/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Luis
 */
public class Exceptions{
    //CMD
    static class CmdFailure extends Exception{
        public CmdFailure(){ super("Invalid arguments"); }
    }
    
    static class CmdNotRecognized extends Exception{
        public CmdNotRecognized(){ super("Command not recognized"); }
    }
    
    //REGISTER
    static class RegisterFailure extends Exception{
        public RegisterFailure(){ 
            super("You're missing some parameters"); 
        }
    }
    
    static class RegisterClientAlreadyExists extends Exception{
        public RegisterClientAlreadyExists(){ 
            super("The username you're trying to regist already exists"); 
        }
    }
    
    //LOGIN    
    static class LoginFailure extends Exception{
        public LoginFailure(){ 
            super("Check your username or password"); 
        }
    }
    
    static class AlreadyLoggedIn extends Exception{
        public AlreadyLoggedIn(){ 
            super("You're already logged in"); 
        }
    }
    
    static class NotLoggedIn extends Exception{
        public NotLoggedIn(){ 
            super("You need to login first"); 
        }
    }
    
    //List
    static class ListFailure extends Exception{
        public ListFailure(){ 
            super("List Failures"); 
        }
    }
    
    //Chat
    static class ChatFailure extends Exception{
        public ChatFailure(){ 
            super("Chat failure"); 
        }
    }
    
    //CLIENT 
    static class CurrenthPath extends Exception{
        public CurrenthPath(){ 
            super("Current Path not rigth"); 
        }
    }
    
    static class ConnectFailure extends Exception{
        public ConnectFailure(){ 
            super("Connect failure"); 
        }
    }
    
    static class AlreadyConnected extends Exception{
        public AlreadyConnected(){ 
            super("Already connected to this server"); 
        }
    }
    
    static class ServerDoesntExist extends Exception{
        public ServerDoesntExist(){ 
            super("There is no server with this name"); 
        }
    }
    
    static class NotConnectedToServer extends Exception{
        public NotConnectedToServer(){ 
            super("You're not connected to this server"); 
        }
    }
    
    static class SwitchingLocalNotPossible extends Exception{
        public SwitchingLocalNotPossible(){ 
            super("You can't change to local because you're not connected to any server"); 
        }
    }
    
    static class MyClientInfoNotFound extends Exception{
        public MyClientInfoNotFound(){ 
            super("Couldn't find my client info"); 
        }
    }
    
    static class ErrorCreatingDirectory extends Exception{
        public ErrorCreatingDirectory(){ 
            super("Couldn't create the directory"); 
        }
    }
    
    static class ErrorRemovingFileOrDirectory extends Exception{
        public ErrorRemovingFileOrDirectory(){ 
            super("Couldn't remove the file"); 
        }
    }
    
    static class ErrorCopyingFile extends Exception{
        public ErrorCopyingFile(){ 
            super("Couldn't copy the file"); 
        }
    }
    
    static class ErrorMovingFile extends Exception{
        public ErrorMovingFile(){ 
            super("Couldn't moving the file"); 
        }
    }
    
    static class ErrorRenamingFile extends Exception{
        public ErrorRenamingFile(){ 
            super("Couldn't rename the file"); 
        }
    }
    
    static class DirectoryOrFileDoesntExist extends Exception{
        public DirectoryOrFileDoesntExist(){ 
            super("The directory or file you're trying to use doesn't exist"); 
        }
    }
     
    static class ErrorDownloadingFile extends Exception{
        public ErrorDownloadingFile(){ 
            super("Couldn't download file"); 
        }
    }
    
    static class ErrorUploadingFile extends Exception{
        public ErrorUploadingFile(){ 
            super("Couldn't upload file"); 
        }
    }
    
    static class ErrorListingDirectory extends Exception{
        public ErrorListingDirectory(){ 
            super("Couldn't list the current directory"); 
        }
    }
    
    static class ErrorShowingFileContent extends Exception{
        public ErrorShowingFileContent(){ 
            super("Couldn't show file content"); 
        }
    }
    
    static class ErrorChangingDirectory extends Exception{
        public ErrorChangingDirectory(){ 
            super("Couldn't change directory"); 
        }
    }
    
    static class ErrorDisconnecting extends Exception{
        public ErrorDisconnecting(){ 
            super("Couldn't disconnect from server"); 
        }
    }

    static class NotADirectory extends Exception{
        public NotADirectory(){ 
            super("Not a directory"); 
        }
    }
    
    static class NotAfile extends Exception{
        public NotAfile(){ 
            super("Not a file"); 
        }
    }
    
    static class ErrorReadingFileOrDirectory extends Exception{
        public ErrorReadingFileOrDirectory(){ 
            super("No reading permissions"); 
        }
    }
}
