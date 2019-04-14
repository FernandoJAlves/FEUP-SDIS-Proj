import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class MessageHandler implements Runnable {
    private byte[] body;
    private String[] args;
    private Map<String, ScheduledFuture<?>> scheduledPutchunks;

    public MessageHandler(byte[] message, int msg_size) {
        // header extraction
        String headerStr = Utils.getHeader(message,msg_size);
        this.args = Utils.makeArrayArgs(headerStr);

        // body extraction
        byte[] body = Utils.getBody(message,msg_size);
        if (body != null) {
            this.body = body;
        }
        else{
            this.body = new byte[0]; //Case of last chunk being size 0
        }

        this.scheduledPutchunks = new HashMap<String, ScheduledFuture<?>>();
    }

    public void run() {
        if (Integer.parseInt(args[2]) == Peer.getId()) { // To ignore own messages
            return;
        }

        switch (args[0]) {
        case "PUTCHUNK":
            handlePutChunk();
            return;
        case "STORED":
            handleStored();
            return;
        case "GETCHUNK":
            handleGetChunk();
            return;
        case "CHUNK":
            handleChunk();
            return;
        case "DELETE":
            handleDelete();
            return;
        case "REMOVED":
            handleRemoved();
            return;
        default:
            System.out.println("Error: Entered MessageHandler Switch Default");
            break;
        }
    }

    // PUTCHUNK, STORED, GETCHUNK, CHUNK, DELETE, REMOVED
    private void handlePutChunk() {
        System.out.println("Received Putchunk!");

        // Arguments
        String version = args[1];
        int senderId = Integer.parseInt(args[2]);
        String fileId = args[3];
        int chunkNum = Integer.parseInt(args[4]);
        int desiredRepDgr = Integer.parseInt(args[5]);

        // check if there is a scheduled putchunk task
        String chunkName = fileId + "_" + chunkNum;
        if (scheduledPutchunks.containsKey(chunkName)) {
            ScheduledFuture<?> future = scheduledPutchunks.get(chunkName);
            if (!future.isDone()) {
                future.cancel(true);
            }
            scheduledPutchunks.remove(chunkName);
        }

        // retrieve local storage
        Storage storage = Peer.getStorage();
        Chunk chunk = new Chunk(fileId, chunkNum, body, desiredRepDgr);

        if (storage.saveChunk(chunk, senderId)) {
            // send stored message
            String storedMsg = Message.mes_stored(version, Peer.getId(), fileId, chunkNum);
            MessageSender sender = new MessageSender("MC", storedMsg.getBytes()); // send message through MC
            int delay = ThreadLocalRandom.current().nextInt(0, 400 + 1); // random delay between 0 and 400ms
            Peer.getThreadPool().schedule(sender, delay, TimeUnit.MILLISECONDS);
        }
    }

    private void handleStored() {
        System.out.println("Received Stored!");

        // Arguments
        int senderId = Integer.parseInt(args[2]);
        String fileId = args[3];
        int chunkNum = Integer.parseInt(args[4]);

        // retrieve local storage
        Storage storage = Peer.getStorage();

        String chunkName = fileId + "_" + chunkNum;
        storage.insertInReplicationMap(chunkName, senderId);
    }

    private void handleGetChunk() {
        System.out.println("Received Getchunk!");

        // arguments
        String senderVersion = args[1];
        String fileId = args[3];
        int chunkNum = Integer.parseInt(args[4]);

        // retrieve stored chunk
        Storage storage = Peer.getStorage();
        Chunk chunk = storage.getChunk(fileId, chunkNum);

        // send chunk message
        byte[] message = Message.getChunkMessage(chunk);
        System.out.println("The chunk size = " + chunk.getData().length);

        String runningVer; //Version of the protocol that will be run

        if(senderVersion.equals(Peer.getVersion())){ //Same version
            System.out.println("Same Version");
            runningVer = senderVersion;
        }
        else if(senderVersion.equals("1.0") && Peer.getVersion().equals("2.0")){ //Diff version but sender is vanilla
            System.out.println("Diff Version but works");
            runningVer = senderVersion;
        }
        else{
            System.out.println("Error: Request's Version is greater than the Receiver's Version");
            return;
        }

        System.out.println("Running: " + runningVer);

        switch (runningVer) {
        case "1.0":
            MessageSender sender = new MessageSender("MDR", message); // send message through MDR
            int delay = ThreadLocalRandom.current().nextInt(0, 400 + 1); // random delay between 0 and 400ms
            Peer.getThreadPool().schedule(sender, delay, TimeUnit.MILLISECONDS);
            break;
        case "2.0":
            // create tcp/ip client
            Socket socket;
            DataOutputStream out;
            try {
                socket = new Socket("localhost", 8090);
                // sends output to the socket
                out = new DataOutputStream(socket.getOutputStream());
                // send chunk message
                out.write(message);
                // close connection
                out.close();
                socket.close();
            } catch(ConnectException e) {
                System.out.println("Could not connect!");
            } catch(SocketException e) {
                System.out.println("Could not connect to socket!");
            } catch (UnknownHostException e1) {
                // TODO Auto-generated catch b lock
                e1.printStackTrace();
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            break;
        default:
            break;
        }        
    }

    private void handleChunk() {
        System.out.println("Received Chunk!");

        // arguments
        String fileId = args[3];
        int chunkNum = Integer.parseInt(args[4]);

        Chunk chunk = new Chunk(fileId, chunkNum, body, 0);

        Storage storage = Peer.getStorage();
        storage.addRestoredChunk(chunk);   
    }

    private void handleDelete() {
        System.out.println("Received Delete!");

        // arguments
        String fileId = args[3];

        Storage storage = Peer.getStorage();
        storage.deleteChunks(fileId);
    }

    private void handleRemoved() {
        System.out.println("Received Removed!");

        // arguments
        int senderId = Integer.parseInt(args[2]);
        String fileId = args[3];
        int chunkNum = Integer.parseInt(args[4]);

        Storage storage = Peer.getStorage();

        // if peer has chunk
        String chunkName = fileId + "_" + chunkNum;

        Chunk chunk = storage.getChunk(chunkName);
        if (chunk == null) {
            return;
        }

        // remove peer from replication map
        storage.removeFromReplicationMap(chunkName, senderId);
        // if replication dgr dropped below desired, initiate putchunk protocol
        if (storage.getChunkRepDgr(chunkName) < chunk.getDesiredRepDgr()) {
            byte[] message = Message.getPutchunkMessage(chunk);
            MessageSenderPutChunk sender = new MessageSenderPutChunk("MDB", message, chunk.getFileId(), chunk.getNum(), chunk.getDesiredRepDgr());
            int delay = ThreadLocalRandom.current().nextInt(0, 400 + 1); // random delay between 0 and 400ms
            ScheduledFuture<?> putchunkTask = Peer.getThreadPool().schedule(sender, delay, TimeUnit.MILLISECONDS);
            scheduledPutchunks.put(chunkName, putchunkTask);
        }
    }
}