public class MessageSender implements Runnable {
    private String channel;
    private byte[] message;

    public MessageSender(String channel, byte[] message) {
        this.channel = channel;
        this.message = message;
    }

    public void run() {
        Peer.getChannel(channel).send(message);
    }
}