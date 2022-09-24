package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;

public class StagingArea implements Serializable {

    private HashMap<String, String> addition;
    private HashSet<String> removal;


    public StagingArea() {
        this.addition = new HashMap<>();
        this.removal = new HashSet<>();

    }


    public StagingArea(HashMap<String, String> trackedFiles, HashSet<String> untrackedFiles) {
        this.addition = trackedFiles;
        this.removal = untrackedFiles;
    }


    public void save() {
        StagingArea newStage = new StagingArea(this.addition, this.removal);
        File file = Utils.join(Repository.STAGINGAREA, "file");
        Utils.writeObject(file, newStage);
    }

    public StagingArea fromFile() {
        File file = Utils.join(Repository.STAGINGAREA, "file");
        if (!file.exists()) {
            return new StagingArea();
        }
        return Utils.readObject(file, StagingArea.class);
    }

    public void add(String filename, String blobId) {
        this.addition.put(filename, blobId);
    }

    public void removeFromAdd(String filename) {
        this.addition.remove(filename);
    }

    public void addToRemove(String filename, String blobId) {
        this.removal.add(filename);
    }


    public HashMap<String, String> getAddition() {
        return this.addition;
    }

    public HashSet<String> getRemoval() {
        return this.removal;
    }

    public Boolean isEmpty() {
        return this.addition.isEmpty() && this.removal.isEmpty();
    }


}














