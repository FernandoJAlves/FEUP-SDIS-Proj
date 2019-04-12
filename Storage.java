import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class Storage implements Serializable {

    private static final long serialVersionUID = 1L;

    private ArrayList<FileManager> localFiles;
    private ArrayList<Chunk> storedChunks, restoredChunks;
    private ConcurrentHashMap<String, Integer> replicationHashmap;
    private int availableSpace;

    public Storage() {
        localFiles = new ArrayList<FileManager>();
        storedChunks = new ArrayList<Chunk>();
        restoredChunks = new ArrayList<Chunk>();
        replicationHashmap = new ConcurrentHashMap<String, Integer>();
        availableSpace = 100000000; // 100 MB
    }

    public ArrayList<FileManager> getLocalFiles() {
        return localFiles;
    }

    public ArrayList<Chunk> getStoredChunks() {
        return storedChunks;
    }

    public ArrayList<Chunk> getRestoredChunks() {
        return restoredChunks;
    }

    public synchronized ConcurrentHashMap<String, Integer> getReplicationHashmap() {
        return replicationHashmap;
    }

    public synchronized int getAvailableSpace() {
        return availableSpace;
    }

    public void addFile(FileManager file) {
        if (!localFiles.contains(file)) {
            localFiles.add(file);
        }
    }

    public void addRestoredChunk(Chunk chunk) {
        for (Chunk c : restoredChunks) {
            if (chunk.getName().equals(c.getName())) {
                return;
            }
        }
        restoredChunks.add(chunk);
    }

    public boolean isFileOwner(String fileId) {
        for (FileManager file : localFiles) {
            if (file.getHashedFileId().equals(fileId)) {
                return true;
            }
        }
        return false;
    }

    public FileManager getLocalFile(String name) {
        for (FileManager fm : localFiles) {
            if (fm.getHashedFileId().equals(name)) {
                return fm;
            }
        }
        return null;
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

    public int getChunkRepDgr(String chunkName) {
        if (replicationHashmap.containsKey(chunkName)) {
            return replicationHashmap.get(chunkName);
        }
        return 0;
    }

    public boolean saveChunk(Chunk chunk) {
        int chunkSize = chunk.getData().length;

        // check available storage
        if (availableSpace < chunkSize) {
            System.out.println("ERROR: storage is full!");
            return false;
        }

        // if chunk file owner or chunk already stored, do not store chunk
        if (isFileOwner(chunk.getFileId()) || getChunk(chunk.getName()) != null) {
            return true;
        }

        // store chunk
        writeChunk(chunk);

        return true;
    }

    private void writeChunk(Chunk chunk) {
        String chunkName = chunk.getName();

        // setup chunk in replication map
        updateHashmap(chunkName, 0);

        // if under replication degree
        if (replicationHashmap.get(chunkName) < chunk.getDesiredRepDgr()) {
            storedChunks.add(chunk);
            // update current replication degree
            updateHashmap(chunkName, 1);
            // store chunk locally
            chunk.write();
            // decrease peer available space
            availableSpace -= chunk.getData().length;
        } else {
            System.out.println("WARNING: max replication degree already reached!");
            return;
        }
    }

    public void updateHashmap(String chunkName, int repDgrOffset) {
        if (Math.abs(repDgrOffset) > 1) {
            System.out.println("Error: repDgrOffset is invalid!");
            return;
        }

        if (replicationHashmap.containsKey(chunkName)) {
            int currRepDgr = replicationHashmap.get(chunkName);
            replicationHashmap.replace(chunkName, currRepDgr + repDgrOffset);
        } else {
            replicationHashmap.put(chunkName, 0);
        }
    }

    public void deleteChunks(String fileId) {
        String dirPath = "peer" + Peer.getId() + "/backup/" + fileId;
    
        List<Chunk> foundChunks = new ArrayList<Chunk>();
        for (Chunk chunk : storedChunks) {
            if (chunk.getFileId().equals(fileId)) {
                foundChunks.add(chunk);
                // erase chunk from replication map
                replicationHashmap.remove(chunk.getName());
                // increase peer available space
                availableSpace += chunk.getData().length;
                // erase file from peer directory
                String filepath = dirPath + "/" + chunk.getNum();
                File file = new File(filepath);
                file.delete();
            }
        }
        // erase chunks from stored chunks
        storedChunks.removeAll(foundChunks);

        // delete empty directory
        File dir = new File(dirPath);
        if (dir.exists()) {
            dir.delete();
        }
    }
}