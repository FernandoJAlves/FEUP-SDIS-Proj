import java.io.IOException;
import static java.lang.Math.pow;

public class Message {

    public enum MessageType {
        PUTCHUNK, STORED, GETCHUNK, CHUNK, DELETE, REMOVED
    }

    public static boolean containsMesType(String test) {

        for (MessageType m : MessageType.values()) {
            if (m.name().equals(test)) {
                return true;
            }
        }

        return false;
    }

    public Message() {
    }

    // <MessageType> <Version> <SenderId> <FileId> <ChunkNo> <ReplicationDeg> <CRLF>
    public static String mes_putchunk(String version, int id, String fileId, int chunkNo, int repDeg) {

        String finalVersion;
        if (Character.isDigit(version.charAt(0)) & Character.isDigit(version.charAt(2)) & version.charAt(1) == '.') {
            finalVersion = version;
        } else {
            return "ERROR";
        }
        if(chunkNo >= 1000000){ //Has to be smaller than 1.000.000
            return "ERROR";
        }

        return "PUTCHUNK " + finalVersion + " " + id + " " + fileId + " " + chunkNo + " " + repDeg + " \r\n\r\n";
    }

    // STORED <Version> <SenderId> <FileId> <ChunkNo> <CRLF><CRLF>
    public static String mes_stored(String version, int id, String fileId, int chunkNo) {

        String finalVersion;
        if (Character.isDigit(version.charAt(0)) & Character.isDigit(version.charAt(2)) & version.charAt(1) == '.') {
            finalVersion = version;
        } else {
            return "ERROR";
        }
        if(chunkNo >= 1000000){ //Has to be smaller than 1.000.000
            return "ERROR";
        }

        return "STORED " + finalVersion + " " + id + " " + fileId + " " + chunkNo + " \r\n\r\n";
    }

    // GETCHUNK <Version> <SenderId> <FileId> <ChunkNo> <CRLF><CRLF>
    public static String mes_getchunk(String version, int senderId, String fileId, int chunkNo) {

        String finalVersion;
        if (Character.isDigit(version.charAt(0)) & Character.isDigit(version.charAt(2)) & version.charAt(1) == '.') {
            finalVersion = version;
        } else {
            return "ERROR";
        }
        if(chunkNo >= 1000000){ //Has to be smaller than 1.000.000
            return "ERROR";
        }

        return "GETCHUNK " + finalVersion + " " + senderId + " " + fileId + " " + chunkNo
                + " \r\n\r\n";
    }

    // CHUNK <Version> <SenderId> <FileId> <ChunkNo> <CRLF><CRLF><Body>
    public static String mes_chunk(String version, int senderId, String fileId, int chunkNo) {

        String finalVersion;
        if (Character.isDigit(version.charAt(0)) & Character.isDigit(version.charAt(2)) & version.charAt(1) == '.') {
            finalVersion = version;
        } else {
            return "ERROR";
        }
        if(chunkNo >= 1000000){ //Has to be smaller than 1.000.000
            return "ERROR";
        }

        return "CHUNK " + finalVersion + " " + senderId + " " + fileId + " " + chunkNo + " \r\n\r\n";
    }

    // DELETE <Version> <SenderId> <FileId> <CRLF><CRLF>
    public static String mes_delete(String version, int senderId, String fileId) {

        String finalVersion;
        if (Character.isDigit(version.charAt(0)) & Character.isDigit(version.charAt(2)) & version.charAt(1) == '.') {
            finalVersion = version;
        } else {
            return "ERROR";
        }

        return "DELETE " + finalVersion + " " + senderId + " " + fileId + " \r\n\r\n";
    }

    // REMOVED <Version> <SenderId> <FileId> <ChunkNo> <CRLF><CRLF>
    public static String mes_removed(String version, int senderId, String fileId, int chunkNo) {

        String finalVersion;
        if (Character.isDigit(version.charAt(0)) & Character.isDigit(version.charAt(2)) & version.charAt(1) == '.') {
            finalVersion = version;
        } else {
            return "ERROR";
        }
        if(chunkNo >= 1000000){ //Has to be smaller than 1.000.000
            return "ERROR";
        }

        return "REMOVED " + finalVersion + " " + senderId + " " + fileId + " " + chunkNo + " \r\n\r\n";
    }

    // DELETED <Version> <SenderId> <FileId> <ChunkNo> <CRLF><CRLF>
    public static String mes_deleted(String version, int senderId, String fileId) {

        String finalVersion;
        if (Character.isDigit(version.charAt(0)) & Character.isDigit(version.charAt(2)) & version.charAt(1) == '.') {
            finalVersion = version;
        } else {
            return "ERROR";
        }

        return "DELETED " + finalVersion + " " + senderId + " " + fileId + " \r\n\r\n";
    }

    public static byte[] getPutchunkMessage(Chunk chunk) {
        String header = mes_putchunk(Peer.getVersion(), Peer.getId(), chunk.getFileId(), chunk.getNum(), chunk.getDesiredRepDgr()); 
        return mes_addBody(header, chunk.getData());
    }

    public static byte[] getChunkMessage(Chunk chunk) {
        String header = mes_chunk(Peer.getVersion(), Peer.getId(), chunk.getFileId(), chunk.getNum());
        return mes_addBody(header, chunk.read());
    }

    public static byte[] mes_addBody(String msg, byte[] body) {
        byte[] header = msg.getBytes();
        byte[] message;
        if(body == null){
            message = new byte[header.length];
            System.arraycopy(header, 0, message, 0, header.length);
        }
        else{
            message = new byte[header.length + body.length];
            System.arraycopy(header, 0, message, 0, header.length);
            System.arraycopy(body, 0, message, header.length, body.length);
        }

        return message;
    }
}
