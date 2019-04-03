import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteInterface extends Remote {
    void backup(String filepath, int replicationDeg) throws RemoteException;
    void restore(String filepath) throws RemoteException;
    void delete(String pathname) throws RemoteException;
    void reclaim(int maxDiskSpace) throws RemoteException;
    void state() throws RemoteException;

    String register(String plate, String owner) throws RemoteException;
    String lookup(String plate) throws RemoteException;   
}