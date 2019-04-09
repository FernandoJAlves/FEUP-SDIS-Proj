import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class FileManager {

    private String pathname;
    private List<Chunk> chunks;

    public FileManager(String pathname, int repDgr) {
        this.pathname = pathname;
        getChunks(repDgr);
    }

    public String getPathname() {
        return pathname;
    }

    public List<Chunk> getChunkList() {
        return chunks;
    }

    private void getChunks(int repDgr) {
        chunks = new ArrayList<Chunk>();

        int maxChunkSize = 64000; // 64KB per Chunk

        File file = new File(pathname);
        try (FileInputStream fileStream = new FileInputStream(file)) {
            BufferedInputStream bufStream = new BufferedInputStream(fileStream);

            byte[] buf = new byte[maxChunkSize];
            int readBytes, chunkNum = 1;
            while ((readBytes = bufStream.read(buf)) != -1) {
                if (readBytes == maxChunkSize) {
                    chunks.add(new Chunk(pathname, chunkNum, buf, repDgr));
                } else {
                    byte[] auxBuf = Arrays.copyOf(buf, readBytes);
                    chunks.add(new Chunk(pathname, chunkNum, auxBuf, repDgr));
                }
                chunkNum++;
                buf = new byte[maxChunkSize];
            }
            if (file.length() % maxChunkSize == 0) {
                chunks.add(new Chunk(pathname, chunkNum, null, repDgr));
            }
            bufStream.close();

            fileStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}