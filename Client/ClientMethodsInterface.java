
/**
 *
 * @author andre
 * METHOD -> CMD
 * register() -> register username password
 * login() -> login usename password 
 * logout() -> logout
 * list() -> list -s || list -c                             [SERVERS || CLIENTS]
 * copyFile() -> cp fileXPTO /cliente1/pasta2/pasta21                   [COPIAR]
 * moveFile() -> mv fileXPTO /cliente1/pasta2/pasta21                   [CORTAR]
 * changeWorkingDirectory() -> cd ..   || cd pasta2                 [BACK || TO]
 * getWorkingDirContent() -> ls
 * getFileContent() -> cat fileXPTO
 * removeFile() -> rm fileXPTO  || rm directoryXPTO (ONLY IF EMPTY)
 * makeDir() -> mkdir directoryXPTO
 * download() -> fileXPTO
 * upload() -> fileXPTO
 * 
 */
public interface ClientMethodsInterface {
    public void register(String username, String password);
    public void login(String username, String password); 
    public void logout();
    public void connect(String serverName);
    public void disconnect();
    public void list(String type);
    public void copyFile(String fileName, String destinationPath);
    public void moveFile(String fileName, String destinationPath);
    public void changeWorkingDirectory(String newWorkingDirectoryPath);
    public void getWorkingDirContent();
    public void getFileContent(String fileName) throws Exceptions.CmdFailure;
    public void renameFile(String oldName, String newName);
    public void removeFile(String fileName);
    public void makeDir(String directoryName);
    public void downloadFile(String fileName);
    public void uploadFile(String fileName);
}
