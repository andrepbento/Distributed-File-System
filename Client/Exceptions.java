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
        public RegisterFailure(){ super("You're missing some parameters"); }
    }
    
    static class RegisterClientAlreadyExists extends Exception{
        public RegisterClientAlreadyExists(){ super("The username you're trying to regist already exists"); }
    }
    
    //LOGIN    
    static class LoginFailure extends Exception{
        public LoginFailure(){ super("Check your username or password"); }
    }
    
    static class AlreadyLoggedIn extends Exception{
        public AlreadyLoggedIn(){ super("You're already logged in"); }
    }
    
    static class NotLoggedIn extends Exception{
        public NotLoggedIn(){ super("You need to login first"); }
    }
    
    //List
    static class ListFailure extends Exception{
        public ListFailure(){ super("List Failures"); }
    }
    
    //Chat
    static class ChatFailure extends Exception{
        public ChatFailure(){ super("Chat failure"); }
    }
    
    
    //CLIENT 
    static class CurrenthPath extends Exception{
        public CurrenthPath(){ super("Current Path not rigth"); }
    }
    
    static class ConnectFailure extends Exception{
        public ConnectFailure(){ super("Connect failure"); }
    }
    
    static class AlreadyConnected extends Exception{
        public AlreadyConnected(){ super("Already connected to this server"); }
    }
    
    static class ServerDoesntExist extends Exception{
        public ServerDoesntExist(){ super("There is no server with this name"); }
    }
}
