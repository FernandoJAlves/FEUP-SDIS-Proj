import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class MessageHandler implements Runnable {
    private String body;
    private String[] args;

    public MessageHandler(byte[] message) {
        String messageStr = new String(message, 0, message.length);
        
        String[] headerBody = splitHeaderBody(messageStr);
        this.args = makeArrayArgs(headerBody[0]);

        if (headerBody.length > 1) {
            this.body = headerBody[1];
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
        //System.out.println("TEST");  //TODO: Remove later, only for tests
        if (this.args[2].equals(Peer.getId())) { // To ignore own messages
            return;
        }

        switch (this.args[0]) {
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
            System.out.println("ERROR: Entered MessageHandler Switch Default");
            break;
        }
    }

    // PUTCHUNK, STORED, GETCHUNK, CHUNK, DELETE, REMOVED
    private void handlePutChunk() {
        System.out.println("Received Putchunk!");

        //Arguments
        String version = args[1];
        String senderId = args[2];
        String fileId = this.args[3];
        int chunkNum = Integer.parseInt(this.args[4]);
        int desiredRepDgr = Integer.parseInt(this.args[5]);

        // retrieve local storage
        Storage storage = Peer.getStorage();

        byte[] data = body.getBytes();    

        System.out.println(Arrays.toString(this.args));

        Chunk chunk = new Chunk(fileId, chunkNum, data, desiredRepDgr);
        
        if (storage.saveChunk(chunk)) {
            // send stored message
            String storedMsg = Message.mes_stored(version, Peer.getId(), fileId, chunkNum);
            MessageSender sender = new MessageSender("MC",storedMsg.getBytes()); //send message through MC
            int delay = ThreadLocalRandom.current().nextInt(0, 400 + 1); //random delay between 0 and 400ms
            Peer.getThreadPool().schedule(sender, delay, TimeUnit.MILLISECONDS);
        }
    }

    private void handleStored() {
        System.out.println("Received Stored!");
        
        //Arguments
        String fileId = this.args[3];
        int chunkNum = Integer.parseInt(this.args[4]);

        // retrieve local storage
        Storage storage = Peer.getStorage();

        String chunkName = fileId + "_" + chunkNum;
        System.out.println("chunkName = " + chunkName);
        
        storage.updateHashmap(chunkName,1);
    }

    private void handleGetChunk() {
        System.out.println("Received Getchunk!");
        
        //Arguments
        String version = args[1];
        String senderId = args[2];
        String fileId = args[3];
        String chunkNo = args[4];
        
        // TODO: Storage logic


        // TODO: No final, comunicar um CHUNK se tiver o chunk
    }

    private void handleChunk() {
        System.out.println("Received Chunk!");

        //Arguments
        String version = args[1];
        String senderId = args[2];
        String fileId = args[3];
        String chunkNo = args[4];
        //TODO: Ver qual é o index do body, 5? Ou mais por causa do CRLFs?

        // TODO: Storage logic
    }

    private void handleDelete() {
        System.out.println("Received Delete!");

        //Arguments
        String version = args[1];
        String senderId = args[2];
        String fileId = args[3];
        String chunkNo = args[4];
        // TODO: Storage logic
    }

    private void handleRemoved() {
        System.out.println("Received Removed!");

        //Arguments
        String version = args[1];
        String senderId = args[2];
        String fileId = args[3];
        String chunkNo = args[4];
        
        // TODO: Storage logic
    }
}