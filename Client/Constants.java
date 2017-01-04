

public final class Constants {
    
    private Constants(){ }
    
    
    public static final String CLIENT = "CLIENT";
    
    //Client CMD List
    public static final String NO_USER = "NO_USER";
    public static final String CMD_REGISTER = "REGISTER";
    public static final String CMD_LOGIN = "LOGIN";
    public static final String CMD_LOGOUT = "LOGOUT";
    public static final String CMD_LIST = "LIST";
    public static final String CMD_LIST_C = "LIST -C";
    public static final String CMD_LIST_S = "LIST -S";
    
    public static final String CMD_CONNECT = "CONNECT";
    public static final String CMD_DISCONNECT = "DISCONNECT";
    public static final String CMD_SWITCH = "SWITCH";
    public static final String CMD_COPY_FILE = "CP";
    public static final String CMD_MOVE_FILE = "MV";
    public static final String CMD_CD_DIR = "CD";
    public static final String CMD_LS_DIR = "LS";
    public static final String CMD_CAT_FILE = "CAT";
    public static final String CMD_RM_FILE = "RM";
    public static final String CMD_MK_DIR = "MKDIR";
    public static final String CMD_DOWNLOAD_FILE = "DOWNLOAD";
    public static final String CMD_UPLOAD_FILE = "UPLOAD";
    public static final String CMD_RENAME = "RENAME";
    
    public static final String DS = "DS";
    public static final String LOCAL = "LOCAL";

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
    public static final int CODE_DISCONNECT_OK = 999;
    
    
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
