

public final class Constants {
    
    private Constants(){}
    //HeadBeatThreadSend
    public static final long TIME = 30000;
    public static final String HEARTBEAT_SERVER = "HEARTBEAT_SERVER";
    public static final int SENDING_PORT = 9801;
    
    //Server
    public static final int MAX_SIZE = 256;
    
    //Names
    public static final String SERVER = "SERVER";
    public static final String CLIENT = "CLIENT";
    
    //Client CMD List
    public static final String CMD_REGISTER = "REGISTER";
    public static final String CMD_LOGIN = "LOGIN";
    public static final String CMD_LOGOUT = "LOGOUT";
    public static final String CMD_LIST = "LIST";
    
    //Client Codes
    // [0 - 99] CMD
    public static final int CODE_CMD_FAILURE = 0;
    public static final int CODE_CMD_NOT_RECOGNIZED = 1;
    // [100 - 199] REGISTER
    public static final int CODE_REGISTER_FAILURE = 100;
    public static final int CODE_REGISTER_OK = 101;
    // [200-299] LOGIN
    // [300-399] LOGOUT
    // [400-499] LIST

    //HeartbeatThreadReceive
    public static final int LISTENING_PORT = 9801;
    
    //DirectoryService
    //public static final String TIME_REQUEST = "TIME";
}
