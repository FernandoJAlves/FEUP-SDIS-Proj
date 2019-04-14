import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteInterface extends Remote {
    void backup(String filepath, int replicationDeg) throws RemoteException;
    void restore(String filepath) throws RemoteException;
    void delete(String pathname) throws RemoteException;
    void reclaim(int maxDiskSpace) throws RemoteException;
    String state() throws RemoteException;
    void restore_enh(String filepath) throws RemoteException;
    void delete_enh(String filepath) throws RemoteException;
}