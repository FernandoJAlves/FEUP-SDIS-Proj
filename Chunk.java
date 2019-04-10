import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class Chunk {
    
    private String fileId;
    private int chunkNum;
    private int desiredRepDgr;
    private byte[] data;
    private long lastModified;


    public Chunk(String fileId, int chunkNum, byte[] data, int desiredRepDgr) {
        this(fileId,chunkNum,data,desiredRepDgr,0);
    } 

    public Chunk(String fileId, int chunkNum, byte[] data, int desiredRepDgr, long lastModified) {
        this.fileId = fileId;
        this.chunkNum = chunkNum;
        this.data = data;
        this.desiredRepDgr = desiredRepDgr; 
        this.lastModified = lastModified;
    }

    public String getFileId() {
        return fileId;
    }

    public String getHashedFileId() {
        String finalFileId = fileId + lastModified;
        return Utils.bytesToHex(Utils.encodeSHA256(String.valueOf(finalFileId)));
    }

    public int getNum() {
        return chunkNum;
    }

    public byte[] getData() {
        return data;
    }

    public int getDesiredRepDgr() {
        return desiredRepDgr;
    }

    public String getName() {
        return fileId + "_" + chunkNum;
    }

    public void write() {
        // create chunk file on peer directory
        String fileDir = "Backup" + "/" + Peer.getId() + "/" + getHashedFileId();
        
        File dir = new File(fileDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String filepath = fileDir + "/" + chunkNum;
        File file = new File(filepath);
        if (!file.exists()) {
            try {
                file.createNewFile();
                FileOutputStream stream = new FileOutputStream(filepath);
                if (data != null) {
                    stream.write(data);
                }
                stream.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}