import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;

public class Chunk implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String fileId;
    private int chunkNum;
    private int desiredRepDgr;
    private byte[] data;

    public Chunk(String fileId, int chunkNum, byte[] data, int desiredRepDgr) {
        this.fileId = fileId;
        this.chunkNum = chunkNum;
        this.data = data;
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

    public int getDesiredRepDgr() {
        return desiredRepDgr;
    }

    public String getName() {
        return fileId + "_" + chunkNum;
    }

    public void write() {
         // create chunk file on peer directory
         String fileDir = "peer" + Peer.getId() + "/backup" + "/" + getFileId();
        
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