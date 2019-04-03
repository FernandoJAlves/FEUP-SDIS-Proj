import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Client {
    public Client() {}

    public static void main(String args[]) {
       
       if (args.length < 4 || args.length > 5) {
            System.out.println("Usage: java Client <host_name> <remote_object_name> <oper> <opnd> *");
            return;
       }

        try {
            Registry registry = LocateRegistry.getRegistry(args[0]);
            RemoteInterface stub = (RemoteInterface) registry.lookup(args[1]);
            
            switch (args.length) {
                case 4:
                    //stub.lookup(args[3]);
                    break;
                case 5:
                    //stub.register(args[3],args[4]);
                    break;
                default:
                    break;
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
