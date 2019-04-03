import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class Storage {

    private ArrayList<FileManager> ownedFiles;
    private ArrayList<Chunk> storedChunks;
    private ConcurrentHashMap<String, Integer> replicationHashmap;
    private int availableSpace;

    public Storage() {
        ownedFiles = new ArrayList<FileManager>();
        storedChunks = new ArrayList<Chunk>();
        replicationHashmap = new ConcurrentHashMap<String, Integer>();
        availableSpace = 100000; // 100 MB
    }

    public ArrayList<Chunk> getStoredChunks() {
        return storedChunks;
    }

    public int getAvailableSpace() {
        return availableSpace;
    }

    public Chunk getChunk(String fileId, int chunkNum) {
        String chunkName = fileId + "_" + chunkNum;
        for (Chunk chunk : storedChunks) {
            if (chunk.getName() == chunkName) {
                return chunk;
            }
        }
        return null;
    }

    public boolean isFileOwner(String fileId) {
        for (FileManager file : ownedFiles) {
            if (file.getPathname().equals(fileId)) {
                return true;
            }
        }
        return false;
    }

    // thread-safe method
    public synchronized void saveChunk(String peerId, Chunk chunk) {
        String chunkName = chunk.getName();
        int chunkSize = chunk.getData().length;

        // check available storage
        if (availableSpace < chunkSize) {
            System.out.println("ERROR: localStorage is full!");
            return;
        }
        // if file owner do not store chunk
        if (isFileOwner(chunk.getFileId()))
            return;

        // check current replication degree and store chunk
        if (replicationHashmap.containsKey(chunkName)) {
            if (replicationHashmap.get(chunkName) < chunk.getDesiredReplicationDgr()) {
                storedChunks.add(chunk);
                replicationHashmap.put(chunkName, replicationHashmap.get(chunkName) + 1);
            } else {
                System.out.println("WARNING: max replication degree already reached!");
                return;
            }
        } else {
            storedChunks.add(chunk);
            replicationHashmap.put(chunkName, 1);
        }

        // create chunk file on peer directory
        File dir = new File(peerId);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String filepath = peerId + "/" + chunkName;
        File file = new File(filepath);
        if (!file.exists()) {
            try {
                file.createNewFile();
                FileOutputStream stream = new FileOutputStream(filepath);
                stream.write(chunk.getData());
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        // decrease peer available space
        availableSpace -= chunk.getData().length;
    }

    public void deleteChunks(String peerId, String fileId) {
        for (Chunk chunk : storedChunks) {
            if (chunk.getFileId().equals(fileId)) {
                // erase chunk from chunks list
                storedChunks.remove(chunk);
                // decrease chunk replication degree
                replicationHashmap.remove(chunk.getName());
                // erase file from peer directory
                String filepath = peerId + "/" + chunk.getName();
                File file = new File(filepath);
                file.delete();
                // increase peer available space
                availableSpace += chunk.getData().length;
            }
        }
    }
}