import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;

public class FileManager implements Serializable {

    private static final long serialVersionUID = 1L;
    
    private File file;
    private String pathname, hashedFileId;
    private List<Chunk> chunks;

    public FileManager(String pathname, int repDgr) {
        this.pathname = pathname;
        this.file = new File(pathname);
        this.hashedFileId = getHashedFileId();
        this.chunks = getChunks(repDgr);
    }

    public String getPathname() {
        return pathname;
    }

    public List<Chunk> getChunkList() {
        return chunks;
    }

    public String getHashedFileId() {
        String finalFileId = pathname + file.lastModified(); 
        return Utils.bytesToHex(Utils.encodeSHA256(String.valueOf(finalFileId)));
    }

    private List<Chunk> getChunks(int repDgr) {
        List<Chunk> chunks = new ArrayList<Chunk>();

        int maxChunkSize = 64000; // 64KB per Chunk

        File file = new File(pathname);
        try (FileInputStream fileStream = new FileInputStream(file)) {
            BufferedInputStream bufStream = new BufferedInputStream(fileStream);

            byte[] buf = new byte[maxChunkSize];
            int readBytes, chunkNum = 1;
            while ((readBytes = bufStream.read(buf)) != -1) {
                if (readBytes == maxChunkSize) {
                    chunks.add(new Chunk(hashedFileId, chunkNum, buf, repDgr));
                } else {
                    byte[] auxBuf = Arrays.copyOf(buf, readBytes);
                    chunks.add(new Chunk(hashedFileId, chunkNum, auxBuf, repDgr));
                }
                chunkNum++;
                buf = new byte[maxChunkSize];
            }
            if (file.length() % maxChunkSize == 0) {
                chunks.add(new Chunk(hashedFileId, chunkNum, null, repDgr));
            }
            bufStream.close();

            fileStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return chunks;
    }
}