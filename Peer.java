import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * $ java Peer 1.0 1 peer_1 224.0.0.1 8081 224.0.0.2 8082 224.0.0.3 8083 $ java
 * Peer 1.0 2 peer_2 224.0.0.4 8084 224.0.0.5 8085 224.0.0.6 8086 $ java Peer
 * 1.0 3 peer_3 224.0.0.7 8087 224.0.0.8 8088 224.0.0.9 8089
 */
public class Peer implements RemoteInterface {

    private static ExecutorService threadpool;
    private static Channel mc, mdb, mdr;
    private static Storage localStorage;
    private static String protocolVersion, id, accessPoint;

    public Peer(String[] args) {
        parseArguments(args);
        localStorage = new Storage();
        threadpool = Executors.newFixedThreadPool(100);
        threadpool.execute(mc);
        threadpool.execute(mdb);
        threadpool.execute(mdr);
    }

    public static void main(String args[]) {
        if (args.length < 1 || args.length > 9) {
            System.out.println(
                    "Usage: java Peer <protocol_version> <peer_id> <access_point> <mc_addr> <mc_port> <mdb_addr> <mdb_port> <mdr_addr> <mdr_port>");
            return;
        }

        try {
            Peer peer = new Peer(args);
            RemoteInterface stub = (RemoteInterface) UnicastRemoteObject.exportObject(peer, 0);
            Registry registry = LocateRegistry.getRegistry();
            registry.rebind(accessPoint, stub);
            System.out.println("Peer ready to receive requests!");
        } catch (Exception e) {
            System.out.println("ERROR in main\n");
            e.printStackTrace();
        }
    }

    void parseArguments(String args[]) {
        protocolVersion = args[0];
        id = args[1];
        accessPoint = args[2];
        mc = new Channel(args[3], Integer.parseInt(args[4]));
        mdb = new Channel(args[5], Integer.parseInt(args[6]));
        mdr = new Channel(args[7], Integer.parseInt(args[8]));
    }

    public static Channel getChannel(String channel) {
        //TODO: Remove prints, just for testing
        if (channel == "MC"){
            //System.out.println("Got mc");
            return mc;
        }
            
        if (channel == "MDB"){
            //System.out.println("Got MDB");
            return mdb;
        }
            
        if (channel == "MDR"){
            //System.out.println("Got MDR");
            return mdr;
        }
            
        // default
        System.out.println("ERROR: Reached default in getChannel");
        return null;
    }

    public static ExecutorService getThreadPool(){
        return threadpool;
    }

    public static String getId() {
        return id;
    }

    public static Storage getLocalStorage() {
        return localStorage;
    }

    public byte[] getByteMessage(Chunk chunk) {
        String header = Message.mes_putchunk(protocolVersion, id, chunk.getFileId(), chunk.getNum(), chunk.getDesiredRepDgr());
        String headerData = Message.mes_addBody(header, chunk.getData());
        byte[] message = new byte[headerData.length()];
        System.arraycopy(headerData.getBytes(), 0, message, 0, headerData.length());
        return message;
    }

    // @Override
    public void backup(String filepath, int replicationDeg) {
        FileManager manager = new FileManager(filepath, replicationDeg);

        for (Chunk chunk : manager.getChunkList()) {
            byte[] message = getByteMessage(chunk);
            MessageSender sender = new MessageSender("MDB",message); //send message through MDB
            threadpool.execute(sender);
        }
    }

    // @Override
    public void restore(String filepath) {

    }

    // @Override
    public void delete(String pathname) {

    }

    // @Override
    public void reclaim(int maxDiskSpace) {

    }

    // @Override
    public void state() {

    }
}