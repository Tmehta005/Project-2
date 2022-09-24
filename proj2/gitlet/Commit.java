package gitlet;


import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;


/**
 * Represents a gitlet commit object.
 *  does at a high level.
 *
 * @author Tanya Mehta
 */
public class Commit implements Serializable {
    /**
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    /**
     * The message of this Commit.
     */
    private String message;
    private String timestamp;


    // SHA-ID of parent
    private String parent;

    //SHA ID instance variable
    private String ID;

    private File file;


    private Map<String, String> snapshot;


    public Commit() {
        this.message = "initial commit";
        this.parent = null;
        this.ID = createID();
        this.snapshot = new HashMap<>();
        this.file = Utils.join(Repository.COMMITS, this.ID);
        Utils.writeContents(this.file, "");
        DateTimeFormatter format = DateTimeFormatter.ofPattern("EEE LLL d kk:mm:ss uuuu Z");
        this.timestamp = ZonedDateTime.now().format(format);

    }

    public Commit(String message, String parent, Map<String, String> snapshot) {
        this.message = message;
        this.parent = parent;
        this.snapshot = snapshot;
        DateTimeFormatter format = DateTimeFormatter.ofPattern("EEE LLL d kk:mm:ss uuuu Z");
        this.timestamp = ZonedDateTime.now().format(format);
        this.ID = createID();
        this.file = Utils.join(Repository.COMMITS, this.ID);
        Utils.writeContents(this.file, "");

    }

    public String createID() {
        return Utils.sha1(this.message + this.parent + this.timestamp + this.snapshot);
    }


    public void save() {
        Utils.writeObject(file, this);
    }

    public String getID() {
        return this.ID;
    }


    //Reads in and deserializes a Commit from a file with ID in Commits Folder.
    public static Commit fromFile(String id) {
        File commitFile = Utils.join(Repository.COMMITS, id);
        return Utils.readObject(commitFile, Commit.class);

    }

    public Map<String, String> getSnapshot() {
        return this.snapshot;
    }

    public String getMessage() {
        return this.message;
    }

    public String getParent() {
        return this.parent;
    }

    public Commit getParentObject() {
        if (this.parent == null) {
            return null;
        }
        return fromFile(this.parent);
    }

    public String getTimestamp() {
        return this.timestamp;
    }

    public File getFile() {
        return this.file;
    }


}
