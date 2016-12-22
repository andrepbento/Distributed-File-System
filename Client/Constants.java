

public final class Constants {
    
    private Constants(){ }
    
    
    public static final String CLIENT = "CLIENT";
    
    //Client CMD List
    public static final String NO_USER = "NO_USER";
    public static final String CMD_REGISTER = "REGISTER";
    public static final String CMD_LOGIN = "LOGIN";
    public static final String CMD_LOGOUT = "LOGOUT";
    public static final String CMD_LIST = "LIST";
    
    public static final String CMD_CONNECT = "CONNECT";
    public static final String DS = "DS";
    
        //Directory Service Codes
    // [0 - 99] CMD
    public static final int CODE_CMD_FAILURE = 0;
    public static final int CODE_CMD_NOT_RECOGNIZED = 1;
    
    // [100 - 199] REGISTER
    public static final int CODE_REGISTER_FAILURE = 100; 
    public static final int CODE_REGISTER_OK = 101; 
    public static final int CODE_REGISTER_CLIENT_ALREADY_EXISTS = 102;
    
    // [200-299] LOGIN
    public static final int CODE_LOGIN_FAILURE = 200;
    public static final int CODE_LOGIN_ALREADY_LOGGED = 201;
    public static final int CODE_LOGIN_NOT_LOGGED_IN = 202;
    public static final int CODE_LOGIN_OK = 203;
    
    // [300-399] LOGOUT
    public static final int CODE_LOGOUT_OK = 300;
    
    // [400-499] LIST
    public static final int CODE_LIST_FAILURE = 401;
    public static final int CODE_LIST_OK = 402;
    
    
    // [600-699] CHAT
    public static final int CODE_CHAT_FAILURE = 601;
    public static final int CODE_CHAT_OK = 602;
    
        //Server
    // [500-599] CONNECT
    public static final int CODE_CONNECT_FAILURE = 501;
    public static final int CODE_CONNECT_OK = 502;
    
        //Client
    public static final int CODE_SERVER_DOESNT_EXIST = 701;
    public static final int CODE_SERVER_ALREADY_CONNECTED = 702;
    
    
        //DirectoryService
    public static final int SENDING_PORT_SD = 9801;
    public static final int MAX_SIZE = 2048;
    
    
    //HeadBeatThreadSend
    public static final long TIME = 30000;
    public static final String HEARTBEAT_CLIENT = "HEARTBEAT_CLIENT";
    public static final int SENDING_PORT_HB = 9801;
    
    //ChatThreadSend
    public static final int REC_CHAT_PORT = 9802;
}
