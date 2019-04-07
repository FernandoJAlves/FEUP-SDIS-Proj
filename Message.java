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

    // TODO: See if there is a better way of doing this
    public static byte[] encodeSHA256(String toEncode) {
        byte[] encodedString = {};
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            encodedString = digest.digest(toEncode.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            System.out.println("Error while hashing");
        }
        return encodedString;
    }

    // Function from: https://www.baeldung.com/sha-256-hashing-java
    public static String bytesToHex(byte[] bytes) {
        StringBuffer hexString = new StringBuffer();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(0xff & bytes[i]);
            if (hex.length() == 1)
                hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
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
        int fileId = Integer.parseInt(args[3]);
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
        String finalHashedFileId = bytesToHex(encodeSHA256(String.valueOf(fileId)));

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
        System.out.print(finalHashedFileId);
        System.out.println(size + finalHashedFileId.length());
        System.out.print(finalChunkNo);
        System.out.println(size + finalChunkNo.length);
        System.out.print(finalRepDeg);
        System.out.println(size + finalRepDeg.length);

    }

    public Message() {
    }

    // <MessageType> <Version> <SenderId> <FileId>
    // <ChunkNo> <ReplicationDeg> <CRLF>

    public static String mes_putchunk(String version, String id, int fileId, int chunkNo, int repDeg) {
        //TODO: check all of the input values
        
        String finalVersion;
        String finalId;
        if (Character.isDigit(version.charAt(0)) & Character.isDigit(version.charAt(2)) & version.charAt(1) == '.') {
            finalVersion = version;
        } else {
            return "ERROR";
        }

        String ret = "PUTCHUNK " + finalVersion + " " + Integer.parseInt(id) + " " + fileId + " " + chunkNo + " "
                + repDeg + "\r\n\r\n";
        return ret;
    }

}
