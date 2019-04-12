import java.io.IOException;
import java.net.*;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import static java.lang.Math.pow;

public class Message {

    private static final char[] crlf = { 0xD, 0xA };

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

    public static void main(String args[]) throws IOException {

        // Example: java message STORED 1.0 8000 333 999999 7

        // <MessageType> <Version> <SenderId> <FileId>
        // <ChunkNo> <ReplicationDeg> <CRLF>
        /*
         * String messageType = "CHUNK"; String version = "1.0"; int senderId = 8000;
         * int fileId = 333; int chunkNo = 3; int replicationDeg = 3;
         */

        String messageType = args[0];
        String version = args[1];
        int senderId = Integer.parseInt(args[2]);
        String fileId = args[3];
        int chunkNo = Integer.parseInt(args[4]);
        int replicationDeg = Integer.parseInt(args[5]);

        char[] finalMesType = {};
        char[] finalVersion = {};
        char[] finalSenderId = {};
        char[] finalChunkNo = {};
        char[] finalRepDeg = {};

        // Message Type
        if (containsMesType(messageType)) {
            finalMesType = messageType.toCharArray();
        } else {
            System.out.println("Invalid Message Type");
            return;
        }
        // Version
        if (Character.isDigit(version.charAt(0)) & Character.isDigit(version.charAt(2)) & version.charAt(1) == '.') {
            finalVersion = version.toCharArray();
        } else {
            System.out.println("Invalid Version Format");
            return;
        }
        // SenderId
        if (senderId >= 0) { // Assumindo que os ids são sempre valores positivos
            finalSenderId = String.valueOf(senderId).toCharArray();
        } else {
            System.out.println("Invalid Sender ID");
            return;
        }
        // FileId
        // String finalHashedFileId = bytesToHex(encodeSHA256(String.valueOf(fileId)));

        // ChunkNo
        if (chunkNo < pow(10, 6)) {
            finalChunkNo = ("" + chunkNo).toCharArray();
        } else {
            System.out.println("Chunk Number was too large");
            return;
        }

        // Replication Degree
        if (replicationDeg < 10) {
            finalRepDeg = ("" + replicationDeg).toCharArray();
        } else {
            System.out.println("Replication Degree was to large");
            return;
        }

        String size = "   Size: ";

        System.out.print(finalMesType);
        System.out.println(size + finalMesType.length);
        System.out.print(finalVersion);
        System.out.println(size + finalVersion.length);
        System.out.print(finalSenderId);
        System.out.println(size + finalSenderId.length);
        //System.out.print(finalHashedFileId);
        //System.out.println(size + finalHashedFileId.length());
        System.out.print(finalChunkNo);
        System.out.println(size + finalChunkNo.length);
        System.out.print(finalRepDeg);
        System.out.println(size + finalRepDeg.length);

    }

    public Message() {
    }

    // <MessageType> <Version> <SenderId> <FileId> <ChunkNo> <ReplicationDeg> <CRLF>
    public static String mes_putchunk(String version, String id, String fileId, int chunkNo, int repDeg) {
        // TODO: check all of the input values

        String finalVersion;
        String finalId;
        if (Character.isDigit(version.charAt(0)) & Character.isDigit(version.charAt(2)) & version.charAt(1) == '.') {
            finalVersion = version;
        } else {
            return "ERROR";
        }

        return "PUTCHUNK " + finalVersion + " " + Integer.parseInt(id) + " " + fileId + " " + chunkNo + " " + repDeg
                + "\r\n\r\n";
    }

    public static String mes_addBody(String message, byte[] body) {
        System.out.println(" - MES Header Size: " + message.length());
        System.out.println(" - MES Body Size: " + body.length);
        String ret = message + new String(body, 0, body.length);
        System.out.println(" - MES Ret Size: " + ret.length());
        return ret;
    }

    // STORED <Version> <SenderId> <FileId> <ChunkNo> <CRLF><CRLF>
    public static String mes_stored(String version, String id, String fileId, int chunkNo) {
        // TODO: check all of the input values

        String finalVersion;
        String finalId;
        if (Character.isDigit(version.charAt(0)) & Character.isDigit(version.charAt(2)) & version.charAt(1) == '.') {
            finalVersion = version;
        } else {
            return "ERROR";
        }

        return "STORED " + finalVersion + " " + Integer.parseInt(id) + " " + fileId + " " + chunkNo + "\r\n\r\n";
    }

    // GETCHUNK <Version> <SenderId> <FileId> <ChunkNo> <CRLF><CRLF>
    public static String mes_getchunk(String version, String senderId, String fileId, String chunkNo) {

        return "GETCHUNK";
    }

    // CHUNK <Version> <SenderId> <FileId> <ChunkNo> <CRLF><CRLF><Body>
    public static String mes_chunk(String version, String senderId, String fileId, String chunkNo, String body) {

        return "CHUNK";
    }

    // DELETE <Version> <SenderId> <FileId> <CRLF><CRLF>
    public static String mes_delete(String version, String senderId, String fileId) {

        String finalVersion;
        String finalId;
        if (Character.isDigit(version.charAt(0)) & Character.isDigit(version.charAt(2)) & version.charAt(1) == '.') {
            finalVersion = version;
        } else {
            return "ERROR";
        }

        return "DELETE " + finalVersion + " " + Integer.parseInt(senderId) + " " + fileId + "\r\n\r\n";
    }

    // REMOVED <Version> <SenderId> <FileId> <ChunkNo> <CRLF><CRLF>
    public static String mes_removed(String version, String senderId, String fileId, String chunkNo) {

        return "REMOVED";
    }
}
