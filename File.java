import java.util.List;
import java.util.ArrayList;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

public class File {

    private String pathname;
    private List<Chunk> chunks;

    public File(String pathname) {
        this.pathname = pathname;
        getChunks();
    }

    private void getChunks() {
        chunks = new ArrayList<Chunk>();

        try {
            RandomAccessFile srcFile = new RandomAccessFile(this.pathname, "r");
            long sourceSize = srcFile.length();
            long bytesPerSplit = 64 * 1024L;
            long maxReadBufferSize = 8 * 1024L; // 8KB
            long numSplits = sourceSize / bytesPerSplit; // from user input, extract it from args
            long remainingBytes = sourceSize % numSplits;

            String fileId;

            int i = 1;
            for (; i <= numSplits; i++) {
                fileId = this.pathname + i + ".split";
                FileOutputStream fileStream = new FileOutputStream(fileId);
                BufferedOutputStream stream = new BufferedOutputStream(fileStream);
                
                if (bytesPerSplit > maxReadBufferSize) {
                    long numReads = bytesPerSplit / maxReadBufferSize;
                    long numRemainingRead = bytesPerSplit % maxReadBufferSize;
                    for (int j = 0; j < numReads; j++) {
                        writeChunk(srcFile, stream, maxReadBufferSize, fileId, i);
                    }
                    if (numRemainingRead > 0) {
                        writeChunk(srcFile, stream, numRemainingRead, fileId, i);
                    }
                } else {
                    writeChunk(srcFile, stream, bytesPerSplit, fileId, i);
                }
                stream.close();
            }
            if (remainingBytes > 0) {
                fileId = this.pathname + i + ".split";
                FileOutputStream fileStream = new FileOutputStream(fileId);
                BufferedOutputStream stream = new BufferedOutputStream(fileStream);
                
                writeChunk(srcFile, stream, remainingBytes, fileId, i);
                stream.close();
            }
            srcFile.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeChunk(RandomAccessFile srcFile, BufferedOutputStream stream, long numBytes, String fileId, int i) {
        try {
            byte[] buf = new byte[(int) numBytes];
            int hasRead = srcFile.read(buf);
            if (hasRead != -1) {
                stream.write(buf);
            }
            chunks.add(new Chunk(fileId, i));
        } catch(IOException e) {
            e.printStackTrace();
        }
    }
}
           