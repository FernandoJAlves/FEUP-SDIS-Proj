import java.rmi.Remote;

public interface RemoteInterface extends Remote {
    void backup(String filepath, int replicationDeg);
    void restore(String filepath);
    void delete(String pathname);
    void reclaim(int maxDiskSpace);
    void state();
}