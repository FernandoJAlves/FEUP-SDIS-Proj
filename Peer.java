import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import java.io.File;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.Random;
import java.util.concurrent.Executors;

/**
 * $ java Peer 1.0 1 peer_1 224.0.0.1 8081 224.0.0.2 8082 224.0.0.3 8083 $ java
 * Peer 1.0 2 peer_2 224.0.0.4 8084 224.0.0.5 8085 224.0.0.6 8086 $ java Peer
 * 1.0 3 peer_3 224.0.0.7 8087 224.0.0.8 8088 224.0.0.9 8089
 */
public class Peer implements RemoteInterface {

    private static ScheduledExecutorService threadpool;
    private static Channel mc, mdb, mdr;
    private static Storage storage;
    private static String protocolVersion, accessPoint;
    private static int id;

    public Peer(String[] args) {
        parseArguments(args);
        threadpool = Executors.newScheduledThreadPool(100);
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

        // load possible saved storage
        Utils.loadStorage();

        // schedule storage serialization
        SaveDataToFile saver = new SaveDataToFile(); // Saves map to file every 10 seconds //TODO: If class does not do
                                                     // anything else, remove it and just create a runnable
        threadpool.scheduleAtFixedRate(saver, 10, 10, TimeUnit.SECONDS);
    }

    void parseArguments(String args[]) {
        protocolVersion = args[0];
        id = Integer.parseInt(args[1]);
        accessPoint = args[2];
        mc = new Channel(args[3], Integer.parseInt(args[4]));
        mdb = new Channel(args[5], Integer.parseInt(args[6]));
        mdr = new Channel(args[7], Integer.parseInt(args[8]));
    }

    public static String getVersion() {
        return protocolVersion;
    }

    public static Channel getChannel(String channel) {
        // TODO: Remove prints, just for testing
        if (channel == "MC") {
            // System.out.println("Got mc");
            return mc;
        }

        if (channel == "MDB") {
            // System.out.println("Got MDB");
            return mdb;
        }

        if (channel == "MDR") {
            // System.out.println("Got MDR");
            return mdr;
        }

        // default
        System.out.println("ERROR: Reached default in getChannel");
        return null;
    }

    public static ScheduledExecutorService getThreadPool() {
        return threadpool;
    }

    public static int getId() {
        return id;
    }

    public static Storage getStorage() {
        return storage;
    }

    public static void setStorage(Storage s) {
        storage = s;
    }

    // @Override
    public void backup(String filepath, int replicationDeg) {
        FileManager file = new FileManager(filepath, replicationDeg);
        storage.addFile(file);

        for (Chunk chunk : file.getChunkList()) {
            byte[] message = Message.getPutchunkMessage(chunk);
            MessageSenderPutChunk sender = new MessageSenderPutChunk("MDB", message, chunk.getFileId(), chunk.getNum(),
                    replicationDeg);
            threadpool.execute(sender);
        }
    }

    // @Override
    public void restore(String filepath) {
        File file = new File(filepath);
        String finalFileId = filepath + file.lastModified();
        String hashedFileId = Utils.bytesToHex(Utils.encodeSHA256(finalFileId));

        FileManager fm = storage.getLocalFile(hashedFileId);
        if (fm == null) {
            System.out.println("Error: file wasn't asked for backup!");
            return;
        }

        for (Chunk chunk : fm.getChunkList()) {
            String message = Message.mes_getchunk(protocolVersion, id, chunk.getFileId(), chunk.getNum());
            MessageSender sender = new MessageSender("MC", message.getBytes());
            threadpool.execute(sender);
        }

        try {
            Thread.sleep(1000); // TODO: justify waiting time
            // aggregate all restored chunks 
            Utils.aggregateChunks(filepath, hashedFileId);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // clear requested chunks
        storage.getRestoredChunks().clear();
    }

    // @Override
    public void delete(String pathname) {
        FileManager file = new FileManager(pathname, 0);

        for (Chunk chunk : file.getChunkList()) {
            String message = Message.mes_delete(protocolVersion, id, chunk.getFileId());
            MessageSender sender = new MessageSender("MC", message.getBytes()); // send message through MC
            threadpool.execute(sender);
        }
    }

    // @Override
    public void reclaim(int maxDiskSpace) {
        Storage storage = Peer.getStorage();

        Random seed = new Random();
        while (storage.getOccupiedSpace() > maxDiskSpace) {
            int index = seed.nextInt(storage.getStoredChunks().size());
            String fileId = storage.getChunk(index).getFileId();
            int chunkNum = storage.getChunk(index).getNum();
            // remove chunk from stored chunks
            storage.removeChunk(index);
            // send removed message
            String message = Message.mes_removed(Peer.getVersion(), Peer.getId(), fileId, chunkNum);
            MessageSender sender = new MessageSender("MC", message.getBytes()); // send message through MC
            threadpool.execute(sender);
        }
    }

    // @Override
    public void state() {
        System.out.println(storage);
    }
}