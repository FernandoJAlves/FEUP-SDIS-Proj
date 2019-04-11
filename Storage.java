import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class Storage {

    private ArrayList<FileManager> localFiles;
    private ArrayList<Chunk> storedChunks;
    private ConcurrentHashMap<String, Integer> replicationHashmap;
    private int availableSpace;

    public Storage() {
        localFiles = new ArrayList<FileManager>();
        storedChunks = new ArrayList<Chunk>();
        replicationHashmap = new ConcurrentHashMap<String, Integer>();
        availableSpace = 100000000; // 100 MB
    }

    public ArrayList<FileManager> getLocalFiles() {
        return localFiles;
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

    public synchronized int getChunkRepDgr(String chunkName) {
        if (replicationHashmap.contains(chunkName)) {
            System.out.println("chunkName = " + chunkName);
            return replicationHashmap.get(chunkName);
        }
        return 0;
    }

    public FileManager getFileManager(String name) {
        for (FileManager fm : localFiles) {
            if (fm.getPathname().equals(name)) {
                return fm;
            }
        }
        return null;
    }

    public boolean isFileOwner(String fileId) {
        for (FileManager file : localFiles) {
            if (file.getPathname().equals(fileId)) {
                return true;
            }
        }
        return false;
    }

    public void addFile(FileManager file) {
        if (!localFiles.contains(file)) {
            localFiles.add(file);
        }
    }

    // thread-safe method
    public boolean saveChunk(Chunk chunk) {
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

        // store chunk
        writeChunk(chunk);

        return true;
    }

    private synchronized void writeChunk(Chunk chunk) {
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

    public synchronized void updateHashmap(String chunkName, int repDgrOffset) {

        System.out.println("---HASHMAP---");
        for (String name : replicationHashmap.keySet()) {
            System.out.println(name + " " + replicationHashmap.get(name));
        }

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
        System.out.println("In delete: " + this.storedChunks.size());
        for (Chunk chunk : storedChunks) {
            if (chunk.getFileId().equals(fileId)) {
                // erase chunk from chunks list
                storedChunks.remove(chunk);
                // erase chunk from replication map
                replicationHashmap.remove(chunk.getName());
                // erase file from peer directory
                String filepath = "Backup" + "/" + Peer.getId() + "/" + fileId + "/" + chunk.getNum();
                File file = new File(filepath);
                System.out.println(filepath);
                file.delete();
                // increase peer available space
                availableSpace += chunk.getData().length;
            }
        }
    }
}