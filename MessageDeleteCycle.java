public class MessageDeleteCycle implements Runnable {

    public MessageDeleteCycle() { }

    public void run() {
        Storage storage = Peer.getStorage();

        for (FileManager file : storage.getDeletionFiles()) {
            for (Chunk chunk : file.getChunkList()) {
                String message = Message.mes_delete(Peer.getVersion(), Peer.getId(), chunk.getFileId());
                MessageSender sender = new MessageSender("MC", message.getBytes()); // send message through MC
                Peer.getThreadPool().execute(sender); 
            }
        }
    }
}