import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class Storage implements Serializable {

    private static final long serialVersionUID = 1L;

    private ArrayList<FileManager> localFiles;
    private ArrayList<Chunk> storedChunks, restoredChunks;
    private ConcurrentHashMap<String, ArrayList<Integer>> replicationHashmap;
    private int availableSpace;

    public Storage() {
        localFiles = new ArrayList<FileManager>();
        storedChunks = new ArrayList<Chunk>();
        restoredChunks = new ArrayList<Chunk>();
        replicationHashmap = new ConcurrentHashMap<String, ArrayList<Integer>>();
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

    public synchronized ConcurrentHashMap<String, ArrayList<Integer>> getReplicationHashmap() {
        return replicationHashmap;
    }

    public synchronized int getAvailableSpace() {
        return availableSpace;
    }

    public void setAvailableSpace(int maxDiskSpace) {
        availableSpace = maxDiskSpace - getOccupiedSpace();
	}

    public synchronized int getOccupiedSpace() {
        int occupiedSpace = 0;
        for (Chunk chunk : storedChunks) {
            occupiedSpace += chunk.getData().length;
        }
        return occupiedSpace;
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

    public Chunk getChunk(int index) {
        return storedChunks.get(index);
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
            return replicationHashmap.get(chunkName).size();
        }
        return 0;
    }

    public boolean saveChunk(Chunk chunk, int peerId) {
        int chunkSize = chunk.getData().length;

        // check available storage
        if (availableSpace < chunkSize) {
            System.out.println("Error: storage is full!");
            return false;
        }

        // if chunk file owner or chunk already stored, do not store chunk
        if (isFileOwner(chunk.getFileId()) || getChunk(chunk.getName()) != null) {
            return true;
        }

        // store chunk
        writeChunk(chunk, peerId);

        return true;
    }

    private void writeChunk(Chunk chunk, int peerId) {
        String chunkName = chunk.getName();

        int currReplicationDgr = getChunkRepDgr(chunkName);

        // if under replication degree
        if (currReplicationDgr < chunk.getDesiredRepDgr()) {
            storedChunks.add(chunk);
            // update current replication degree
            insertInReplicationMap(chunkName, peerId);
            // store chunk locally
            chunk.write();
            // decrease peer available space
            availableSpace -= chunk.getData().length;
        } else {
            System.out.println("Warning: max replication degree already reached!");
            return;
        }
    }

    public void removeChunk(int index) {
        if (index > storedChunks.size()) {
            System.out.println("Error: invalid chunk position!");
            return;
        }

        // erase file from filesystem
        Chunk chunk = getChunk(index);
        chunk.delete();

        // delete if empty directory
        String dirPath = "peer" + Peer.getId() + "/backup/" + chunk.getFileId();
        File dir = new File(dirPath);
        if (dir.exists() && dir.list().length == 0) {
            dir.delete();
        }

        // remove chunk from stored chunks
        storedChunks.remove(index);
    }

    public void insertInReplicationMap(String chunkName, int peerId) {
        ArrayList<Integer> peerList;
        if (replicationHashmap.containsKey(chunkName)) {
            peerList = replicationHashmap.get(chunkName);
        } else {
            peerList = new ArrayList<Integer>();
        }
        if (!peerList.contains(peerId)) {
            peerList.add(peerId);
        }
        replicationHashmap.put(chunkName, peerList);
    }

	public void removeFromReplicationMap(String chunkName, int peerId) {
        if (replicationHashmap.containsKey(chunkName)) {
            ArrayList<Integer> peerList = replicationHashmap.get(chunkName);
            if (peerList.contains(peerId)) {
                peerList.removeIf(s -> s == peerId);
            }
            replicationHashmap.put(chunkName, peerList);
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
        if (dir.exists() && dir.list().length == 0) {
            dir.delete();
        }
    }
}