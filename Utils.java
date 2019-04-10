import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class Utils {

    public Utils() {}

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
}