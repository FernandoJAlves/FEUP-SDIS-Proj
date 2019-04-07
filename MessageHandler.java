public class MessageHandler implements Runnable {
    private byte[] message;

    public MessageHandler(byte[] message) {
        this.message = message;
    }

    public String[] makeArrayArgs(String m) {
        String aux = m.trim(); // remove whitespace
        return aux.split(" ");
    }

    public void run() {
        String messageString = new String(this.message, 0, this.message.length);
        String[] arrayArgs = makeArrayArgs(messageString);

        if (Integer.parseInt(arrayArgs[2]) == Peer.getId()) { // To ignore own messages
            return;
        }

        switch (arrayArgs[0]) {
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
        // TODO: Storage logic

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
        // TODO: Storage logic
    }

    private void handleDelete() {
        // TODO: Storage logic
    }

    private void handleRemoved() {
        // TODO: Storage logic
    }
}