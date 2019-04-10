import java.util.concurrent.TimeUnit;

public class MessageSenderPutChunk implements Runnable {
    private String channel;
    private byte[] message;
    private String fileID;
    private int chunkNumber;
    private int wantedRepDeg;
    private double waitInt;
    private int attemptCount;

    public MessageSenderPutChunk(String channel, byte[] message, String fileID, int chunkNumber, int wantedRepDeg) {
        this.channel = channel;
        this.message = message;
        this.fileID = fileID;
        this.chunkNumber = chunkNumber;
        this.wantedRepDeg = wantedRepDeg;
        this.waitInt = 0.5;
        this.attemptCount = 0;
    }

    public void run() {
        
        int knownRepDeg = 0; //TODO: Ir buscar ao ficheiro

        if(knownRepDeg < wantedRepDeg){ //if wanted repDeg has not been reached
            if(attemptCount >= 5){
                System.out.println("Could not backup!");
            }
            else{
                Peer.getChannel(channel).send(message);
                attemptCount++; //next attempt
                waitInt *= 2;
                //System.out.println("Attempt Number: " + attemptCount);
                System.out.println(waitInt);
                Peer.getThreadPool().schedule(this, (int)waitInt, TimeUnit.SECONDS);
            }
        }
    }
}