import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class MessageHandler implements Runnable {
    private byte[] body;
    private String[] args;

    public MessageHandler(byte[] message, int msg_size) {

        String messageStr = new String(message, 0, msg_size);

        int indexCRLF = messageStr.indexOf("\r\n\r\n");

        byte[] header = new byte[indexCRLF];
        int bodySize = msg_size-(indexCRLF+4);
        byte[] body = new byte[bodySize]; //Plus 4 to count all the chars in CRLF
        System.arraycopy(message, 0, header, 0, indexCRLF);
        System.arraycopy(message, indexCRLF+4, body, 0, bodySize);

        String headerStr = new String(header);

        this.args = makeArrayArgs(headerStr);

        if (bodySize > 0) {
            this.body = body;
        }
    }

    public String[] makeArrayArgs(String m) {
        String aux = m.trim(); // remove whitespace
        return aux.split(" ");
    }

    public String[] splitHeaderBody(String m) {
        String aux = m.trim();
        return aux.split("\r\n\r\n");
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

        // retrieve local storage
        Storage storage = Peer.getStorage();
        Chunk chunk = new Chunk(fileId, chunkNum, body, desiredRepDgr);

        if (storage.saveChunk(chunk,senderId)) {
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
        storage.updateHashmap(chunkName, senderId);
    }

    private void handleGetChunk() {
        System.out.println("Received Getchunk!");
        
        // arguments
        String fileId = args[3];
        int chunkNum = Integer.parseInt(args[4]);
                
        // retrieve stored chunk
        Storage storage = Peer.getStorage();
        Chunk chunk = storage.getChunk(fileId, chunkNum);
    
        // send chunk message
        byte[] message = Message.getChunkMessage(chunk);
        
        MessageSender sender = new MessageSender("MDR", message); //send message through MDR
        int delay = ThreadLocalRandom.current().nextInt(0, 400 + 1); //random delay between 0 and 400ms
        Peer.getThreadPool().schedule(sender, delay, TimeUnit.MILLISECONDS);
    }

    private void handleChunk() {
        System.out.println("Received Chunk!");

        // arguments
        String fileId = args[3];
        int chunkNum = Integer.parseInt(args[4]);

        Chunk chunk = new Chunk(fileId,chunkNum,body,0);
        
        Storage storage = Peer.getStorage();
        storage.addRestoredChunk(chunk);
    }

    private void handleDelete() {
        System.out.println("Received Delete!");

        // Arguments
        String version = args[1];
        String senderId = args[2];
        String fileId = args[3];

        Storage storage = Peer.getStorage();
        storage.deleteChunks(fileId);
    }

    private void handleRemoved() {
        System.out.println("Received Removed!");

        // Arguments
        String version = args[1];
        String senderId = args[2];
        String fileId = args[3];
        String chunkNo = args[4];

        // TODO: Storage logic
    }
}