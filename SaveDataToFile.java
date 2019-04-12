public class SaveDataToFile implements Runnable {

    public SaveDataToFile() {
    }

    public void run() {
        Utils.saveStorage();
    }
}