
public final class Constants {
    
    private Constants() {}
    
    //DirectoryService
    public static final int LISTENIGN_PORT = 9800;
    public static final String CLIENT_LIST_PATH = "clientList.bin";
    public static final String SERVER_LIST_PATH = "serverList.bin";
    
    //Names
    public static final String SERVER = "SERVER";
    public static final String CLIENT = "CLIENT";
    
    //Client CMD List
    public static final String CMD_REGISTER = "REGISTER";
    public static final String CMD_LOGIN = "LOGIN";
    public static final String CMD_LOGOUT = "LOGOUT";
    public static final String CMD_LIST = "LIST";
    
    //Server CMD List
    public static final String REGISTADO = "REGISTADO";
    public static final String ERRO = "ERRO";
    
    //Client Codes
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

    //HeartbeatThreadReceive
    public static final long TIME = 30000;
    public static final int MAX_SIZE = 256;
    public static final String HEARTBEAT_CLIENT = "HEARTBEAT_CLIENT";
    public static final String HEARTBEAT_SERVER = "HEARTBEAT_SERVER";
    public static final int HD_LISTENING_PORT = 9801;
}
