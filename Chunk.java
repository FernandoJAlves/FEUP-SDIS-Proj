public class Chunk {
    
    private String fileId;
    private int chunkNum;
    private int repDgr;
    private int desiredRepDgr;
    private byte[] data;

    public Chunk(String fileId, int chunkNum, byte[] data) {
        this(fileId,chunkNum,data,-1);
    }

    public Chunk(String fileId, int chunkNum, byte[] data, int desiredRepDgr) {
        this.fileId = fileId;
        this.chunkNum = chunkNum;
        this.data = data;
        this.repDgr = -1;
        this.desiredRepDgr = desiredRepDgr; 
    }

    public String getFileId() {
        return fileId;
    }

    public int getNum() {
        return chunkNum;
    }

    public byte[] getData() {
        return data;
    }

    public int getRepDgr() {
        return repDgr;
    }

    public int getDesiredRepDgr() {
        return desiredRepDgr;
    }

    public String getName() {
        return fileId + "_" + chunkNum;
    }
}