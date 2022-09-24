package gitlet;

import java.io.File;
import java.io.Serializable;

public class Blob implements Serializable {


    private String BlobID;

    private byte[] _versionContents;

    private String blobFileName;

    private File blobFile;

    public Blob(File blobFile) {
        this.blobFileName = blobFile.getName();
        this.BlobID = Utils.sha1(Utils.readContents(blobFile));
        this.blobFile = Utils.join(Repository.BLOBS, this.BlobID);
        Utils.writeContents(this.blobFile, Utils.readContents(blobFile));
        this._versionContents = Utils.readContents(this.blobFile);
    }


    public String createBlobID() {
        return Utils.sha1(Utils.serialize(this));
    }

    public void save() {
        Utils.writeObject(this.blobFile,  this);
    }

    public static Blob fromFile(File blob) {
        return Utils.readObject(blob, Blob.class);

    }
    public String getID() {
        return this.BlobID;
    }

    public String getBlobFileName() {
        return this.blobFileName;
    }

    public byte[] getVersionContents() {
        return this._versionContents;
    }
}
