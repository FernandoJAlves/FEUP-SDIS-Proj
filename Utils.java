import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class Utils {

    public Utils() {
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

    public static void saveStorage() {
        try {
            String dirPath = "peer" + Peer.getId();
            File dir = new File(dirPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            FileOutputStream fileOut = new FileOutputStream(dirPath + "/storage.ser");
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(Peer.getStorage());
            out.close();
            fileOut.close();
        } catch (IOException i) {
            i.printStackTrace();
        }
    }

    public static void loadStorage() {
        try {
            String path = "peer" + Peer.getId() + "/storage.ser";

            File file = new File(path);
            if (file.exists()) {
                System.out.println("Storage loaded");
                FileInputStream fileIn = new FileInputStream(path);
                ObjectInputStream in = new ObjectInputStream(fileIn);
                Peer.setStorage((Storage) in.readObject());
                in.close();
                fileIn.close();
            } else {
                System.out.println("Storage created!");
                Peer.setStorage(new Storage());
            }
        } catch (IOException i) {
            i.printStackTrace();
            return;
        } catch (ClassNotFoundException c) {
            System.out.println("Storage class not found!");
            c.printStackTrace();
            return;
        }
    }
}