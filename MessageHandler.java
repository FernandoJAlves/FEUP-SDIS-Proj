public class MessageHandler implements Runnable {
    private String message;
    private String[] args;

    public MessageHandler(byte[] message) {
        this.message = new String(message, 0, message.length);
        this.args = makeArrayArgs(this.message);
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

        String[] headerBody = splitHeaderBody(this.message);
        String fileId = this.args[3];
        int chunkNum = Integer.parseInt(this.args[4]);
        byte[] data = headerBody[1].getBytes();
        int desiredRepDgr = Integer.parseInt(this.args[4]);
        
        Chunk chunk = new Chunk(fileId, chunkNum, data, desiredRepDgr);
        storage.saveChunk(chunk);
        
        // TODO: No final, comunicar um STORED se der store
    }

    private void handleStored() {
        // TODO: Storage logic
    }

    private void handleGetChunk() {
        // TODO: Storage logic


        // TODO: No final, comunicar um CHUNK se tiver o chunk
    }

    private void handleChunk() {
        System.out.println("Received Chunk!");
    }

    private void handleDelete() {
        // TODO: Storage logic
    }

    private void handleRemoved() {
        // TODO: Storage logic
    }
}