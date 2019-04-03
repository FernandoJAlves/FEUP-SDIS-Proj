import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * $ java Peer 1.0 1 peer_1 224.0.0.1 8081 224.0.0.2 8082 224.0.0.3 8083
 * $ java Peer 1.0 2 peer_2 224.0.0.4 8084 224.0.0.5 8085 224.0.0.6 8086
 * $ java Peer 1.0 3 peer_3 224.0.0.7 8087 224.0.0.8 8088 224.0.0.9 8089
 */
public class Peer implements RemoteInterface {

    private static ExecutorService threadpool;
    private static Channel mdc, mdb, mdr;

    private int id;
    private String protocolVersion, accessPoint;

    public Peer(String[] args) {
        parseArguments(args);
        threadpool = Executors.newFixedThreadPool(5);
        threadpool.execute(mdc);
        threadpool.execute(mdb);
        threadpool.execute(mdr);
    }

    public static void main(String args[]) {
        if (args.length < 1 || args.length > 9) {
            System.out.println("Usage: java Peer <protocol_version> <peer_id> <access_point> <mdc_addr> <mdc_port> <mdb_addr> <mdb_port> <mdr_addr> <mdr_port>");
            return;
        }

        try {
            Peer peer = new Peer(args);
            RemoteInterface stub = (RemoteInterface) UnicastRemoteObject.exportObject(peer,0);
            Registry registry = LocateRegistry.getRegistry();
            registry.rebind(peer.accessPoint, stub);
            System.out.println("Peer ready to receive requests!");
        } catch(Exception e) {
            System.out.println("ERROR in main\n");
            e.printStackTrace();
        }
    }

    void parseArguments(String args[]) {
        this.protocolVersion = args[0];
        this.id = Integer.parseInt(args[1]);
        this.accessPoint = args[2];
        mdc = new Channel(args[3],Integer.parseInt(args[4]));
        mdb = new Channel(args[5],Integer.parseInt(args[6]));
        mdr = new Channel(args[7],Integer.parseInt(args[8]));
    }

    //@Override
    public void backup(String filepath, int replicationDeg) {
      
    }

    //@Override
    public void restore(String filepath) {

    }

    //@Override
    public void delete(String pathname) {

    }

    //@Override
    public void reclaim(int maxDiskSpace) {

    }

    //@Override
    public void state() {
        byte[] buf;
        String s = "adoihasdoiashdiashdoiasdoaskdasdasidjasoidjaoidjajdoasijd";
        buf = s.getBytes();
        System.out.println(buf);
        this.mdc.send(buf);
    }
}