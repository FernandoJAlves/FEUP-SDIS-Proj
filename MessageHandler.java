import java.util.Arrays;

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

        // retrieve local storage
        Storage storage = Peer.getLocalStorage();
    
        String fileId = this.args[3];
        int chunkNum = Integer.parseInt(this.args[4]);
        byte[] data = body.getBytes();       
        int desiredRepDgr = Integer.parseInt(this.args[5]);

        System.out.println(Arrays.toString(this.args));

        Chunk chunk = new Chunk(fileId, chunkNum, data, desiredRepDgr);
        
        if (storage.saveChunk(chunk)) {
            // send stored message
            String storedMsg = Message.mes_stored(this.args[1], this.args[2], fileId, chunkNum);
            MessageSender sender = new MessageSender("MC",storedMsg.getBytes()); //send message through MC
            Peer.getThreadPool().execute(sender);
        }
    }

    private void handleStored() {
        System.out.println("Received Stored!");
        
        // retrieve local storage
        Storage storage = Peer.getLocalStorage();

        String fileId = this.args[3];
        int chunkNum = Integer.parseInt(this.args[4]);
        String chunkName = fileId + "_" + chunkNum;
        
        storage.updateHashmap(chunkName,1);
    }

    private void handleGetChunk() {
        // TODO: Storage logic


        // TODO: No final, comunicar um CHUNK se tiver o chunk
    }

    private void handleChunk() {
        // TODO: Storage logic
    }

    private void handleDelete() {
        // TODO: Storage logic
    }

    private void handleRemoved() {
        // TODO: Storage logic
    }
}