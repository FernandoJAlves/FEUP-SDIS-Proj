import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteInterface extends Remote {
    void backup(String filepath, int replicationDeg);
    void restore(String filepath);
    void delete(String pathname);
    void reclaim(int maxDiskSpace);
    void state();

    String register(String plate, String owner) throws RemoteException;
    String lookup(String plate) throws RemoteException;   
}