
public final class Constants {
    
    private Constants(){}
    //HeadBeatThreadSend
    public static final long TIME = 30000;
    public static final long TIME_OUT = 30000;
    public static final String HEARTBEAT_SERVER = "HEARTBEAT_SERVER";
    public static final int SENDING_PORT_HB = 9801;
    
    //Names
    public static final String SERVER = "SERVER";
    public static final String CLIENT = "CLIENT";
    
    //Client CMD List
    public static final String CMD_REGISTER = "REGISTER";
    public static final String CMD_LOGIN = "LOGIN";
    public static final String CMD_LOGOUT = "LOGOUT";
    public static final String CMD_LIST = "LIST";

    
    public static final String CMD_COPY_FILE = "CP";
    public static final String CMD_MOVE_FILE = "MV";
    public static final String CMD_CD_DIR = "CD";
    public static final String CMD_LS_DIR = "LS";
    public static final String CMD_CAT_FILE = "CAT";
    public static final String CMD_RM_FILE = "RM";
    public static final String CMD_MK_DIR = "MKDIR";
    public static final String CMD_DOWNLOAD_FILE = "DOWNLOAD";
    public static final String CMD_UPLOAD_FILE = "UPLOAD";
    
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
    
     // [500-599] CONNECT
    public static final int CODE_CONNECT_FAILURE = 501;
    public static final int CODE_CONNECT_OK = 502;
    
    //Server
    public static final int MAX_SIZE = 256;
    
    public static final int CODE_DISCONNECT_OK = 999;
    
    public static final int CODE_SERVER_REGISTER_FAILURE = 1000;
    public static final int CODE_SERVER_REGISTER_OK = 1001;
    
    public static final int CODE_SERVER_COPY_OK = 1002;
    public static final int CODE_SERVER_COPY_ERROR = 1003;

    public static final int CODE_SERVER_MKDIR_OK = 1004;

    public static final int CODE_SERVER_RMDIR_OK = 1006;
    public static final int CODE_SERVER_RMDIR_ERROR = 1007;

    public static final int CODE_SERVER_RENAME_OK = 1008;
    public static final int CODE_SERVER_RENAME_ERROR = 1009;

    public static final int CODE_SERVER_MOVE_OK = 1010;
    public static final int CODE_SERVER_MOVE_ERROR = 1011;

    public static final int CODE_SERVER_DOWNLOAD_OK = 1012;
    public static final int CODE_SERVER_DOWNLOAD_ERROR = 1013;

    public static final int CODE_SERVER_CD_OK = 1014;
    public static final int CODE_SERVER_CD_ERROR = 1015;

    public static final int CODE_SERVER_LS_OK = 1016;
    public static final int CODE_SERVER_LS_ERROR = 1017;
    
    public static final int CODE_SERVER_CAT_OK = 1018;
    public static final int CODE_SERVER_CAT_ERROR = 1019;


    
}
