import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileManager {

    private String pathname;
    private List<Chunk> chunks;

    public FileManager(String pathname) {
        this.pathname = pathname;
        getChunks();
        writeChunks();
    }

    public List<Chunk> getChunkList(){
        return chunks;
    }

    private void getChunks() {
        chunks = new ArrayList<Chunk>();

        int maxChunkSize = 64 * 1024; // 64KB per Chunk

        File file = new File(this.pathname);
        try (FileInputStream fileStream = new FileInputStream(file)) {
            BufferedInputStream bufStream = new BufferedInputStream(fileStream);

            byte[] buf = new byte[maxChunkSize];
            int readBytes, chunkNum = 1;
            while ((readBytes = bufStream.read(buf)) != -1) {
                if (readBytes == maxChunkSize) {
                    chunks.add(new Chunk(this.pathname,chunkNum,buf));
                } else {
                    byte[] auxBuf = Arrays.copyOf(buf, readBytes);
                    chunks.add(new Chunk(this.pathname,chunkNum,auxBuf));
                }
                chunkNum++;
                buf = new byte[maxChunkSize];
            }
            if (file.length() % maxChunkSize == 0) {
                chunks.add(new Chunk(this.pathname,chunkNum,null)); 
            }
            bufStream.close();

            fileStream.close();
		} catch (IOException e) {
			e.printStackTrace();
        }
    }
    
    private void writeChunks() {
        try {
            for (Chunk chunk : chunks) {
                FileOutputStream fileStream = new FileOutputStream(chunk.getChunkName());
                BufferedOutputStream stream = new BufferedOutputStream(fileStream);
                stream.write(chunk.getData());
            }
        } catch(FileNotFoundException e) {
            e.printStackTrace();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }
}