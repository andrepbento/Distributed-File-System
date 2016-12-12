

public final class Constants {
    
    private Constants(){ }
    
    //Client CMD List
    public static final String NO_USER = "NO_USER";
    public static final String CMD_REGISTER = "REGISTER";
    public static final String CMD_LOGIN = "LOGIN";
    public static final String CMD_LOGOUT = "LOGOUT";
    public static final String CMD_LIST = "LIST";
    
        //Client Codes
    public static final String MSG_CODE_ERROR = "Code Error";
    // [0 - 99] CMD
    public static final int CODE_CMD_FAILURE = 0;
    public static final String MSG_CMD_FAILURE = "Invalid arguments";
    
    public static final int CODE_CMD_NOT_RECOGNIZED = 1;
    public static final String MSG_CMD_NOT_RECODNIZED = "Command not recognized";
    // [100 - 199] REGISTER
    public static final int CODE_REGISTER_FAILURE = 100;
    public static final String MSG_REGISTER_FAILURE = "You're missing some parameters";
    
    public static final int CODE_REGISTER_OK = 101;
    public static final String MSG_REGISTER_OK = "You're now registered";
    
    public static final int CODE_REGISTER_CLIENT_ALREADY_EXISTS = 102;
    public static final String MSG_REGISTER_CLIENT_ALREADY_EXISTS = "The username you're trying to regist already exists";
    // [200-299] LOGIN
    public static final int CODE_LOGIN_OK = 203;
    public static final String MSG_LOGIN_OK = "You logged in";
    
    public static final int CODE_LOGIN_FAILURE = 200;
    public static final String MSG_LOGIN_FAILURE = "Check your username or password";
    
    public static final int CODE_LOGIN_ALREADY_LOGGED = 201;
    public static final String MSG_LOGIN_ALREADY_LOGGED = "You're already logged in";
    
    public static final int CODE_LOGIN_NOT_LOGGED_IN = 202;
    public static final String MSG_LOGIN_NOT_LOGGED_IN = "You need to login first";
    // [300-399] LOGOUT
    
    // [400-499] LIST
    
    
    //DirectoryService
    public static final int SENDING_PORT_SD = 9801;
    public static final int MAX_SIZE = 256;
    
    
    //HeadBeatThreadSend
    public static final long TIME = 30 * 1000;
    public static final String HEARTBEAT_CLIENT = "HEARTBEAT_CLIENTE";
    public static final int SENDING_PORT_HB = 9801;
    
  

}
