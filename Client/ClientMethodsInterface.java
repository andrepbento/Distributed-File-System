
/**
 *
 * @author andre
 * METHOD -> CMD
 * register() -> register username password
 * login() -> login 
 * logout() -> logout
 * copyFile() -> cp fileXPTO /cliente1/pasta2/pasta21                   [COPIAR]
 * moveFile() -> mv fileXPTO /cliente1/pasta2/pasta21                   [CORTAR]
 * changeWorkingDirectory() -> cd ..   || cd pasta2                 [BACK || TO]
 * getWorkingDirContent() -> ls
 * getFileContent() -> cat fileXPTO
 * removeFile() -> rm fileXPTO  || rm directoryXPTO (ONLY IF EMPTY)
 * makeDir() -> mkdir directoryXPTO
 * 
 */
public interface ClientMethodsInterface {
    public boolean register(String username, String password);
    public boolean login(String username, String password); 
    public boolean logout();
    public boolean copyFile(String filename, String destinationPath);
    public boolean moveFile(String filename, String destinationPath);
    public boolean changeWorkingDirectory(String newWorkingDirectoryPath);
    public boolean getWorkingDirContent();
    public boolean getFileContent(String filename);
    public boolean removeFile(String name);
    public boolean makeDir(String directoryName);
}
