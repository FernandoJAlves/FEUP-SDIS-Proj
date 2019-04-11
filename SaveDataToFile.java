import java.util.concurrent.TimeUnit;

public class SaveDataToFile implements Runnable {

    public SaveDataToFile() {
    }

    public void run() {
        //System.out.println("Save");
        Utils.saveStorage();
    }
}