import java.rmi.*;

public interface RemoteInterface extends Remote {
    String register(String plate, String owner) throws RemoteException;
    String lookup(String plate) throws RemoteException;
}