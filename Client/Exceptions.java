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

        public CmdFailure(String message){
           super(message);
        }
    }
    
    static class CmdNotRecognized extends Exception{

          public CmdNotRecognized(String message){
             super(message);
          }
    }
    
    //REGISTER
    static class RegisterFailure extends Exception{

          public RegisterFailure(String message){
             super(message);
          }
    }
    
    static class RegisterClientAlreadyExists extends Exception{

          public RegisterClientAlreadyExists(String message){
             super(message);
          }
    }
    
    //LOGIN
    static class LoginOk extends Exception{

          public LoginOk(String message){
             super(message);
          }
    }
    
    static class LoginFailure extends Exception{

          public LoginFailure(String message){
             super(message);
          }
    }
    
    static class AlreadyLoggedIn extends Exception{

          public AlreadyLoggedIn(String message){
             super(message);
          }
    }
    
    static class NotLoggedIn extends Exception{

          public NotLoggedIn(String message){
             super(message);
          }
    }
}
