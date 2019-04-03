public class Chunk {
    
    private String fileId;
    private int chunkNum;
    private int replicationDgr;
    private int desiredReplicationDgr;
    private byte[] data;

    public Chunk(String fileId, int chunkNum, byte[] data) {
        this.fileId = fileId;
        this.chunkNum = chunkNum;
        this.data = data;
    }

    public String getFileId() {
        return fileId;
    }

    public int getChunkNum() {
        return chunkNum;
    }

    public byte[] getData() {
        return data;
    }

    public int getReplicationDgr() {
        return replicationDgr;
    }

    public int getDesiredReplicationDgr() {
        return desiredReplicationDgr;
    }

    public String getChunkName() {
        return fileId + "_" + chunkNum;
    }
}