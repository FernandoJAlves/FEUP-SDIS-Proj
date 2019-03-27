import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;

public class Server implements RemoteInterface {

    private static HashMap<String, String> database = new HashMap<>();

    public Server() {}

    public static void main(String args[]) {

        if (args.length < 1) {
            System.out.println("Usage: java Server <remote_object_name>");
            return;
        }

        try {
            Server sv = new Server();
            RemoteInterface stub = (RemoteInterface) UnicastRemoteObject.exportObject(sv,0);
            Registry registry = LocateRegistry.getRegistry();
            registry.bind(args[0], stub);
            System.out.println("Server ready to receive requests!");
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Registers plates in the car database
     * 
     * @param plate Car plate
     * @param owner Car owner
     */
    public String register(String plate, String owner) {
        System.out.println("Register ");
        System.out.println(database);
        if (!database.containsKey(plate)) {
            database.put(plate, owner);
            String ret = Integer.toString(database.size());
            System.out.println(plate + " " + owner + " :: " + ret);
            return ret;
        } else {
            return Integer.toString(-1);
        }
    }

    /**
     * Looks for plates in the car database
     * 
     * @param plate Car plate
     * @return Car owner
     */
    public String lookup(String plate) {
        System.out.println("Lookup ");
        if (database.containsKey(plate)) {
            String ret = database.get(plate).toString();
            System.out.println(plate + " :: " + ret);
            return ret;
        } else {
            return "NOT_FOUND";
        }
    } 

}