
public final class Constants {
    
    private Constants() {}

    public static final String NO_USER = "NO_USER";
    
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
    // [300-399] LOGOUT
    // [400-499] LIST
    public static final int CODE_LIST_FAILURE = 401;

    //HeartbeatThreadReceive
    public static final long TIME = 30000;
    public static final int MAX_SIZE = 256;
    public static final String HEARTBEAT_SERVER = "HEARTBEAT_SERVER";
    public static final int LISTENING_PORT = 9801;
    
    //DirectoryService
    //public static final String TIME_REQUEST = "TIME";
}
