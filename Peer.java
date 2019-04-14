import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;

public class Peer implements RemoteInterface {

    private static ScheduledExecutorService threadpool;
    private static Channel mc, mdb, mdr;
    private static Storage storage;
    private static String protocolVersion, accessPoint;
    private static int id;

    private boolean deleteFirstIteration = true;

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
        threadpool.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                Utils.saveStorage();
            }
        }, 30, 30, TimeUnit.SECONDS);
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
        String tempVersion = protocolVersion;
        protocolVersion = "1.0"; // since it is a vanilla protocol, the version will be 1.0

        FileManager file = new FileManager(filepath, replicationDeg);
        storage.addFile(file);

        for (Chunk chunk : file.getChunkList()) {
            byte[] message = Message.getPutchunkMessage(chunk);
            MessageSenderPutChunk sender = new MessageSenderPutChunk("MDB", message, chunk.getFileId(), chunk.getNum(),
                    replicationDeg);
            threadpool.execute(sender);
        }
        protocolVersion = tempVersion; // Reset the version
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

        String tempVersion = protocolVersion;
        protocolVersion = "1.0"; // since it is a vanilla protocol, the version will be 1.0

        for (Chunk chunk : fm.getChunkList()) {
            String message = Message.mes_getchunk(protocolVersion, id, chunk.getFileId(), chunk.getNum());
            MessageSender sender = new MessageSender("MC", message.getBytes());
            threadpool.execute(sender);
        }
        protocolVersion = tempVersion; // Reset the version

        // aggregate chunks after a second of delay
        threadpool.schedule(new Runnable() {
            @Override
            public void run() {
                // aggregate all restored chunks
                Utils.aggregateChunks(filepath, hashedFileId);
                // clear requested chunks
                storage.getRestoredChunks().clear();
            }
        }, 1, TimeUnit.SECONDS);
    }

    // @Override
    public void delete(String pathname) {
        FileManager file = new FileManager(pathname, 0);

        String tempVersion = protocolVersion;
        protocolVersion = "1.0"; // since it is a vanilla protocol, the version will be 1.0

        for (Chunk chunk : file.getChunkList()) {
            String message = Message.mes_delete(protocolVersion, id, chunk.getFileId());
            for (int i = 0; i < 5; i++) { // Sends delete 5 times, once every second
                MessageSender sender = new MessageSender("MC", message.getBytes()); // send message through MC
                threadpool.schedule(sender, i, TimeUnit.SECONDS);
            }
        }

        protocolVersion = tempVersion; // Reset the version

        // TODO: erase original file from filesystem ?
        file.delete();
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
            String message = Message.mes_removed("1.0", Peer.getId(), fileId, chunkNum); // Since this is a vanilla
                                                                                         // protocol, the version will
                                                                                         // always be 1.0
            MessageSender sender = new MessageSender("MC", message.getBytes()); // send message through MC
            threadpool.execute(sender);
        }

        // update available space
        storage.setAvailableSpace(maxDiskSpace);
    }

    // @Override
    public void state() {
        System.out.println("=================\nFiles Backed up:");

        for (FileManager f : storage.getLocalFiles()) {
            System.out.println();
            System.out.println("     - Filename: " + f.getPathname());
            System.out.println("     - Hashed Id: " + f.getHashedFileId());
            System.out.println("     - Desired Rep Degree: " + f.getRepDgr());
            System.out.println("     - Chunks: ");
            for (Chunk chunk : f.getChunkList()) {
                System.out.println("        - Id: " + chunk.getNum());
                System.out.println("        - Perceived Rep Degree: "
                        + storage.getReplicationHashmap().get(chunk.getName()).size());
            }
        }

        System.out.println("\n=================\nChunks Stored:");

        for (Chunk chunk : storage.getStoredChunks()) {
            System.out.println();
            System.out.println(" - Id: " + chunk.getNum());
            System.out.println(" - Size: " + chunk.getData().length / 1000.0 + " KBytes");
            System.out
                    .println(" - Perceived Rep Degree: " + storage.getReplicationHashmap().get(chunk.getName()).size());
        }

        System.out.println("\n=================");
        System.out.println(
                "Peer Max Storage: " + (storage.getAvailableSpace() + storage.getOccupiedSpace()) / 1000.0 + " KBytes");
        System.out.println("Peer Occupied Storage: " + (storage.getOccupiedSpace()) / 1000.0 + " KBytes");
        System.out.println("=================");
    }

    @Override
    public void restore_enh(String filepath) {
        // Check if the peer can run the protocol
        if (!protocolVersion.equals("2.0")) { // Se o protocolo do peer não for 2.0, não pode correr enhancements
            System.out.println("This Peer cannot run this protocol!");
            return;
        }

        File file = new File(filepath);
        String finalFileId = filepath + file.lastModified();
        String hashedFileId = Utils.bytesToHex(Utils.encodeSHA256(finalFileId));

        FileManager fm = storage.getLocalFile(hashedFileId);
        if (fm == null) {
            System.out.println("Error: file wasn't asked for backup!");
            return;
        }

        // initiate tcp/ip server and iterate over each needed chunk
        List<Chunk> chunks = fm.getChunkList();

        for (int i = 0; i <= chunks.size(); i++) {
            // send last chunk twice
            Chunk chunk;
            if (i < chunks.size()) {
                chunk = chunks.get(i);
            } else {
                chunk = chunks.get(i - 1);
            }

            // construct getchunk message
            String message = Message.mes_getchunk(protocolVersion, id, chunk.getFileId(), chunk.getNum());
            MessageSender sender = new MessageSender("MC", message.getBytes());
            threadpool.schedule(sender, 100, TimeUnit.MILLISECONDS);

            try {
                ServerSocket server = new ServerSocket(8090);

                System.out.println("Waiting for a client ...");

                Socket socket = server.accept();
                server.close();
                System.out.println("Client accepted");

                // takes input from the client socket
                DataInputStream in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));

                byte[] finalMessage = new byte[65000];
                byte[] buf = new byte[65000];

                int counter = 0;
                while (true) {
                    int readbytes = in.read(buf);
                    if (readbytes == -1)
                        break;
                    System.arraycopy(buf, 0, finalMessage, counter, readbytes);
                    counter += readbytes;
                }

                String headerStr = Utils.getHeader(finalMessage, counter);
                String[] args = Utils.makeArrayArgs(headerStr);
                byte[] body = Utils.getBody(finalMessage, counter);

                String fileId = args[3];
                int chunkNum = Integer.parseInt(args[4]);
                Chunk c = new Chunk(fileId, chunkNum, body, 0);

                Storage storage = Peer.getStorage();
                storage.addRestoredChunk(c);

                // close connection
                System.out.println("Closing connection");
                socket.close();
                in.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        // aggregate chunks after a second of delay
        threadpool.schedule(new Runnable() {
            @Override
            public void run() {
                // aggregate all restored chunks
                Utils.aggregateChunks(filepath, hashedFileId);
                // clear requested chunks
                storage.getRestoredChunks().clear();
            }
        }, 1, TimeUnit.SECONDS);
    }

    // @Override
    public void delete_enh(String pathname) {
        // Check if the peer can run the protocol
        if (!protocolVersion.equals("2.0")) { // Se o protocolo do peer não for 2.0, não pode correr enhancements
            System.out.println("This Peer cannot run this protocol!");
            return;
        }

        Storage storage = Peer.getStorage();

        FileManager file = storage.getLocalFileByPathname(pathname);
        if (file == null) {
            System.out.println("Error: file has not been backed up!");
            return;
        }
        storage.addDeletionFile(file);

        if (deleteFirstIteration) {
            MessageDeleteCycle sender = new MessageDeleteCycle();
            threadpool.scheduleAtFixedRate(sender, 0, 10, TimeUnit.SECONDS);
            deleteFirstIteration = false;
        }
        
        // TODO: erase original file from filesystem ?
        file.delete();
    }
}