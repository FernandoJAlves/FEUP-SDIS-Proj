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
        availableSpace = 100000000; // 100 MB
    }

    public ArrayList<Chunk> getStoredChunks() {
        return storedChunks;
    }

    public int getAvailableSpace() {
        return availableSpace;
    }

    public Chunk getChunk(String name) {
        for (Chunk chunk : storedChunks) {
            if (chunk.getName().equals(name)) {
                return chunk;
            }
        }
        return null;
    }

    public Chunk getChunk(String fileId, int chunkNum) {
        return getChunk(fileId + "_" + chunkNum);
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
    public synchronized boolean saveChunk(Chunk chunk) {
        String chunkName = chunk.getName();
        int chunkSize = chunk.getData().length;

        // check available storage
        if (availableSpace < chunkSize) {
            System.out.println("ERROR: localStorage is full!");
            return false;
        }
        
        // if chunk file owner or chunk already stored, do not store chunk
        if (isFileOwner(chunk.getFileId()) || getChunk(chunk.getName()) != null) {
            return true;
        }

        // check current replication degree and store chunk
        if (replicationHashmap.containsKey(chunkName)) {
            /*if (replicationHashmap.get(chunkName) < chunk.getDesiredReplicationDgr()) {
                storedChunks.add(chunk);
                replicationHashmap.put(chunkName, replicationHashmap.get(chunkName) + 1);
            } else {
                System.out.println("WARNING: max replication degree already reached!");
                return;
            }*/
            storedChunks.add(chunk);
            replicationHashmap.put(chunkName, replicationHashmap.get(chunkName) + 1);
        } else {
            storedChunks.add(chunk);
            replicationHashmap.put(chunkName, 1);
        }

        // store locally
        chunk.write();

        // decrease peer available space
        availableSpace -= chunk.getData().length;

        return true;
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