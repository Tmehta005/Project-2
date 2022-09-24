package gitlet;

import java.io.File;

import java.io.IOException;
import java.util.*;

import static gitlet.Utils.*;


/**
 * Represents a gitlet repository.
 *  does at a high level.
 *
 * @author Tanya Mehta
 */
public class Repository {
    /**
     *
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    /**
     * The current working directory.
     */
    public static final File CWD = new File(System.getProperty("user.dir"));

    /**
     * The .gitlet directory.
     */
    public static final File GITLET_DIR = join(CWD, ".gitlet");

    public static final File COMMITS = join(GITLET_DIR, "Commits");

    public static final File STAGINGAREA = join(GITLET_DIR, "StagingArea");

    public static final File HEAD = join(GITLET_DIR, "Head");

    public static final File BRANCHES = join(GITLET_DIR, "Branches");

    public static final File BLOBS = join(GITLET_DIR, "Blobs");


    private StagingArea stage;




    public Repository() throws IOException {
        this.stage = new StagingArea();
    }

    public void init() {
        if (GITLET_DIR.exists()) {
            Main.exitWithError("A Gitlet version-control system "
                    + "already exists in the current directory.");
        } else {
            GITLET_DIR.mkdir();
            COMMITS.mkdir();
            STAGINGAREA.mkdir();
            BRANCHES.mkdir();
            BLOBS.mkdir();
            writeContents(HEAD, "");

            Commit initCommit = new Commit();
            initCommit.save();
            Head.setHead("main", initCommit);
            Branch initBranch = new Branch("main", Head.getHead());
            initBranch.save();

        }
    }


    public void add(String filename) {
        File newFile = new File(filename);
        if (!newFile.exists()) {
            Main.exitWithError("File does not exist.");
        } else {
            byte[] blob = Utils.readContents(newFile);
            String blobID = Utils.sha1((Object) blob);
            if (Head.getHead().getSnapshot().get(filename) != null
                    && Head.getHead().getSnapshot().get(filename).equals(blobID)) {
                if (stage.getRemoval().contains(filename)) {
                    stage.getRemoval().remove(filename);
                }
                stage.save();
                return;
            }
            if (stage.getRemoval().contains(filename)) {
                stage.getRemoval().remove(filename);
            }
            Blob newBlob = new Blob(newFile);
            newBlob.save();
            this.stage = stage.fromFile();
            this.stage.add(filename, newBlob.getID());
            this.stage.save();
        }


    }


    public void commit(String message) {
        this.stage = stage.fromFile();
        if (this.stage.isEmpty()) {
            Main.exitWithError("No changes added to the commit.");
        }
        Commit headCommit = Head.getHead();
        String headId = headCommit.getID();
        Map<String, String> parentCopy = headCommit.getSnapshot();
        HashMap<String, String> addition = this.stage.getAddition();
        HashSet<String> removal = this.stage.getRemoval();

        parentCopy.putAll(addition);
        for (String file : removal) {
            parentCopy.remove(file);
        }

        Commit commit = new Commit(message, headId, parentCopy);
        commit.save();
        Branch currBranch = Utils.readObject(HEAD, Branch.class);
        Head.setHead(currBranch.getBranchName(), commit);
        currBranch.setHeadPointer(commit);
        Head.otherBranchPointer(currBranch.getBranchName(), commit);


        this.stage = new StagingArea();
        this.stage.save();
    }


    public void log() {
        Commit currHead = Head.getHead();
        while (currHead.getParent() != null) {
            System.out.print("===" + "\n");
            System.out.print("commit " + currHead.getID() + "\n");
            System.out.print("Date: " + currHead.getTimestamp() + "\n");
            System.out.print(currHead.getMessage() + "\n");
            System.out.println("");

            currHead = currHead.getParentObject();
        }
        System.out.print("===" + "\n");
        System.out.print("commit " + currHead.getID() + "\n");
        System.out.print("Date: " + currHead.getTimestamp() + "\n");
        System.out.print(currHead.getMessage() + "\n");
        System.out.println("");
    }

    public void checkout(String filename) {
        Map<String, String> snapshot = Head.getHead().getSnapshot();
        if (!snapshot.containsKey(filename)) {
            Main.exitWithError("File does not exist in that commit");
        } else {
            String blobID = snapshot.get(filename);
            File blobFile = Utils.join(BLOBS, blobID);
            Blob blob = Blob.fromFile(blobFile);
            File restoreFile = new File(CWD, filename);
            Utils.writeContents(restoreFile, blob.getVersionContents());

        }
    }


//    public void checkout(String commitID,String filename)  {
//        Commit currHead = head.getHead();
//        String blobID = null;
//        while (currHead.getParent() != null){
//            if (commitID.equals(currHead.getID())){
//                if (currHead.getSnapshot().get(filename) == null) {
//                    Main.exitWithError("File does not exist in that commit.");
//                }
//                blobID = currHead.getSnapshot().get(filename);
//                break;
//            }
//            currHead = currHead.getParentObject();
//        }
//
//        if (blobID == null){
//            Main.exitWithError("No commit with that id exists.");
//        }
//        //File blobFile = Utils.join(Blobs,blobID);
//        //Blob newBlob = new Blob(blobFile);
//        //writeObject(blobFile,newBlob);
//        //Blob blob = Blob.fromFile(blobFile);
//        //File restoreFile = new File(CWD,blob.getBlobFileName());
//        //Utils.writeContents(restoreFile,blob.getVersionContents());
//
//        Map<String,String> snapshot = currHead.getSnapshot();
//        //String blobID = snapshot.get(filename);
//        File blobFile = Utils.join(Blobs,blobID);
//        Blob blob = Blob.fromFile(blobFile);
//        File restoreFile = new File(CWD,blob.getBlobFileName());
//        Utils.writeContents(restoreFile,blob.getVersionContents());
//
//    }


    public void checkout(String commitID, String filename) {
        List<String> dirFiles = Utils.plainFilenamesIn(COMMITS);
        String fullId = commitID;
        boolean found = false;
        for (String s : dirFiles) {
            if (s.contains(commitID)) {
                fullId = s;
                found = true;
            }
        }
        if (!found) {
            Main.exitWithError("No commit with that id exists.");
        }
        File newFile = Utils.join(COMMITS, fullId);
        Commit currCommit = Utils.readObject(newFile, Commit.class);
        if (!currCommit.getSnapshot().containsKey(filename)) {
            Main.exitWithError("File does not exist in that commit.");
        }

        String blobID = currCommit.getSnapshot().get(filename);
        if (blobID == null) {
            Main.exitWithError("No commit with that id exists.");
        }
        File blobFile = Utils.join(BLOBS, blobID);
        Blob blob = Blob.fromFile(blobFile);
        File restoreFile = new File(CWD, blob.getBlobFileName());
        Utils.writeContents(restoreFile, blob.getVersionContents());
//
    }


    public void remove(String filename) {
        this.stage = stage.fromFile();
        Commit currHead = Head.getHead();
        if (!stage.getAddition().containsKey(filename)
                && !currHead.getSnapshot().keySet().contains(filename)) {
            Main.exitWithError("No reason to remove the file.");
        }
        if (stage.getAddition().containsKey(filename)) {
            stage.removeFromAdd(filename);
        }
        if (currHead.getSnapshot().containsKey(filename)) {
            stage.addToRemove(filename, currHead.getID());
            File file = new File(CWD, filename);
            Utils.restrictedDelete(file);
        }
        stage.save();
    }


    public void globalLog() {
        List<String> dirFiles = Utils.plainFilenamesIn(COMMITS);
        int i = 0;
        while (i < dirFiles.size()) {
            File currFile = Utils.join(COMMITS, dirFiles.get(i));
            Commit currCommit = Utils.readObject(currFile, Commit.class);
            System.out.print("===" + "\n");
            System.out.print("commit " + currCommit.getID() + "\n");
            System.out.print("Date: " + currCommit.getTimestamp() + "\n");
            System.out.print(currCommit.getMessage() + "\n");
            System.out.println("");

            i += 1;
        }
    }

    public void find(String message) {
        List<String> dirFiles = Utils.plainFilenamesIn(COMMITS);
        int i = 0;
        int counter = 0;
        while (i < dirFiles.size()) {
            File currFile = Utils.join(COMMITS, dirFiles.get(i));
            Commit currCommit = Utils.readObject(currFile, Commit.class);
            if (currCommit.getMessage().equals(message)) {
                System.out.println(currCommit.getID());
                counter += 1;
            }
            i += 1;
        }
        if (counter == 0) {
            Main.exitWithError("Found no commit with that message.");
        }
        return;
    }

    public void status() {
        stage = stage.fromFile();
        List<String> branchFiles = Utils.plainFilenamesIn(BRANCHES);
        List<String> dirFiles = Utils.plainFilenamesIn(CWD);
        if (!GITLET_DIR.exists()) {
            Main.exitWithError("Not in an initialized Gitlet directory.");
        }
        Branch currBranch = Branch.fromFile(HEAD);
        System.out.println("===" + " " + "Branches" + " " + "===");
        System.out.println("*" + currBranch.getBranchName());
        for (String x : branchFiles) {
            if (x.equals(currBranch.getBranchName())) {
                continue;
            }
            System.out.println(x);
        }
        System.out.println("");
        System.out.println("===" + " " + "Staged Files" + " " + "===");
        String[] list = stage.getAddition().keySet().toArray(String[]::new);
        Arrays.sort(list);
        for (String x : list) {
            System.out.println(x);
        }
        System.out.println("");
        System.out.println("===" + " " + "Removed Files" + " " + "===");
        for (String x : stage.getRemoval()) {
            System.out.println(x);
        }
        System.out.println("");
        System.out.println("===" + " " + "Modifications Not Staged For Commit" + " " + "===");
        System.out.println("");
        System.out.println("===" + " " + "Untracked Files" + " " + "===");
        System.out.println("");
    }


    public void checkoutBranch(String branchName) {
        stage = stage.fromFile();
        List<String> dirFiles = Utils.plainFilenamesIn(CWD);
        List<String> branchesFiles = Utils.plainFilenamesIn(BRANCHES);
        Branch currBranch = Utils.readObject(HEAD, Branch.class);
        for (String x : dirFiles) {
            if (!currBranch.getHeadPointer().getSnapshot().containsKey(x)
                    && !stage.getAddition().containsKey(x)
                    && Head.getOtherBranchPointer(branchName).
                    getSnapshot().containsKey(x)) {
                Main.exitWithError("There is an untracked file in the way; "
                        + "delete it, or add and commit it first.");
            }
        }
        if (!branchesFiles.contains(branchName)) {
            Main.exitWithError("No such branch exists.");
        }
        if (branchName.equals(currBranch.getBranchName())) {
            Main.exitWithError("No need to checkout the current branch.");
        }

        Commit commit = Head.getOtherBranchPointer(branchName);
        for (String x : Head.getHead().getSnapshot().keySet()) {
            if (!commit.getSnapshot().containsKey(x)) {
                Utils.restrictedDelete(x);
            }
        }
        Head.setHead(branchName, commit);
        for (String x : commit.getSnapshot().keySet()) {
            checkout(x);
        }

        stage = new StagingArea();
        stage.save();
    }


    public void branch(String branchName) {
        List<String> dirFiles = Utils.plainFilenamesIn(BRANCHES);
        if (dirFiles.contains(branchName)) {
            Main.exitWithError("A branch with that name already exists.");
        }
        Branch newBranch = new Branch(branchName, Head.getHead());
        newBranch.save();
    }


    public void rmBranch(String branchName) {
        Branch currBranch = Utils.readObject(HEAD, Branch.class);
        List<String> dirFiles = Utils.plainFilenamesIn(BRANCHES);
        if (branchName.equals(currBranch.getBranchName())) {
            Main.exitWithError("Cannot remove the current branch.");
        }
        if (!dirFiles.contains(branchName)) {
            Main.exitWithError("A branch with that name does not exist.");
        }
        File file = Utils.join(BRANCHES, branchName);
        file.delete();

    }

    public void reset(String id) {
        stage = stage.fromFile();
        List<String> commitFiles = Utils.plainFilenamesIn(COMMITS);
        if (!commitFiles.contains(id)) {
            Main.exitWithError("No commit with that id exists.");
        }
        Commit commit = Commit.fromFile(id);
        Commit headCommit = Head.getHead();
        List<String> dirFiles = Utils.plainFilenamesIn(CWD);
        Map<String, String> snapshot = commit.getSnapshot();
        Map<String, String> headSnapshot = headCommit.getSnapshot();
        Branch currBranch = Utils.readObject(HEAD, Branch.class);
        for (String x : dirFiles) {
            if (!currBranch.getHeadPointer().getSnapshot().containsKey(x)
                    && !stage.getAddition().containsKey(x)
                    && commit.getSnapshot().containsKey(x)) {
                Main.exitWithError("There is an untracked file in the way; "
                        + "delete it, or add and commit it first.");
            }
        }
        for (String x : headSnapshot.keySet()) {
            if (!snapshot.containsKey(x)) {
                Utils.restrictedDelete(x);
            }
        }
        Head.setHead(currBranch.getBranchName(), commit);
        for (String x : snapshot.keySet()) {
            checkout(x);
        }
        currBranch.setHeadPointer(commit);
        stage = new StagingArea();
        stage.save();
    }

    public void merge(String branchName) {
        stage = stage.fromFile();
        List<String> dirFiles = Utils.plainFilenamesIn(BRANCHES);
        Commit currCommit = Head.getHead();
        if (!stage.isEmpty()) {
            Main.exitWithError("You have uncommitted changes.");
        }
        if (!dirFiles.contains(branchName)) {
            Main.exitWithError("A branch with that name does not exist.");
        }
        Commit otherCommit = Head.getOtherBranchPointer(branchName);
        Branch currBranch = Utils.readObject(HEAD, Branch.class);
        if (currBranch.getBranchName().equals(branchName)) {
            Main.exitWithError("Cannot merge a branch with itself.");
        }
        Commit pointer1 = currCommit;
        Commit pointer2 = otherCommit;
        for (String x : dirFiles) {
            if (!otherCommit.getSnapshot().containsKey(x)
                    && !stage.getAddition().containsKey(x)
                    && currCommit.getSnapshot().containsKey(x)) {
                Main.exitWithError("There is an untracked file in the way; "
                        + "delete it, or add and commit it first.");
            }
        }
        List<String> parentList = new ArrayList<>();
        while (pointer1 != null) {
            parentList.add(pointer1.getParent());
            pointer1 = pointer1.getParentObject();
        }


        for (String x: parentList) {
            if (pointer2.getParent() == x) {
                break;
            }
        }

        Commit splitPoint = pointer2.getParentObject();

        if (splitPoint.getID().equals(otherCommit.getID())) {
            Main.exitWithError("Given branch is an ancestor of the current branch.");
        }
        if (splitPoint.equals(currBranch.getHeadPointer())) {
            Main.exitWithError("Current branch fast-forwarded.");
        }

        List<String> fileList = new ArrayList<>();
        for (String x : splitPoint.getSnapshot().keySet()) {
            if (!fileList.contains(x)) {
                fileList.add(x);
            }
        }
        for (String x: otherCommit.getSnapshot().keySet()) {
            if (!fileList.contains(x)) {
                fileList.add(x);
            }
        }
        for (String x: currCommit.getSnapshot().keySet()) {
            if (!fileList.contains(x)) {
                fileList.add(x);
            }
        }

        for (String x: fileList) {
            condition8(x, splitPoint, currCommit, otherCommit);
            condition1(x, splitPoint, currCommit, otherCommit, branchName);
            condition2(x, splitPoint, currCommit, otherCommit);
            condition3(x, splitPoint, currCommit, otherCommit);
            condition4(x, splitPoint, currCommit, otherCommit);
            condition5(x, splitPoint, currCommit, otherCommit);
            condition6(x, splitPoint, currCommit, otherCommit);
            condition7(x, splitPoint, currCommit, otherCommit, branchName);
        }

        commit("Merged" + " " +  branchName + " "
                +  "into" + " " + currBranch.getBranchName() + ".");
        stage.save();
    }

    private void condition8(String filename, Commit splitPoint,
                            Commit currCommit, Commit otherCommit) {
        stage = stage.fromFile();
        if (currCommit.getSnapshot().containsKey(filename)
                && otherCommit.getSnapshot().containsKey(filename)) {
            if (!splitPoint.getSnapshot().containsKey(filename)
                    && !currCommit.getSnapshot().
                    get(filename).equals(otherCommit.getSnapshot().get(filename))) {
                File file = Utils.join(CWD, filename);
                File file1 = Utils.join(BLOBS,
                        currCommit.getSnapshot().get(filename));
                Blob blob1 = Utils.readObject(file1, Blob.class);
                byte[] b1 = blob1.getVersionContents();
                String contents1 = new String(b1);
                File file2 = Utils.join(BLOBS,
                        otherCommit.getSnapshot().get(filename));
                Blob blob2 = Utils.readObject(file2, Blob.class);
                byte[] b2 = blob2.getVersionContents();
                String contents2 = new String(b2);
                String text = "<<<<<<< HEAD" + "\n"
                        + contents1  + "=======" + "\n" + contents2 + ">>>>>>>" + "\n";
                Utils.writeContents(file, text);
                String blobID = Utils.sha1(text);
                stage.add(filename, blobID);
                stage.save();
                System.out.println("Encountered a merge conflict.");
            }
        }
        if (currCommit.getSnapshot().containsKey(filename)
                && otherCommit.getSnapshot().containsKey(filename)
                && splitPoint.getSnapshot().containsKey(filename)) {
            if (!splitPoint.getSnapshot().get(filename).
                    equals(otherCommit.getSnapshot().get(filename))
                    && !splitPoint.getSnapshot().get(filename).
                    equals(currCommit.getSnapshot().get(filename))
                    && !otherCommit.getSnapshot().get(filename).
                    equals(currCommit.getSnapshot().get(filename))) {
                File file = Utils.join(CWD, filename);
                File file1 = Utils.join(BLOBS,
                        currCommit.getSnapshot().get(filename));
                Blob blob1 = Utils.readObject(file1, Blob.class);
                byte[] b1 = blob1.getVersionContents();
                String contents1 = new String(b1);
                File file2 = Utils.join(BLOBS,
                        otherCommit.getSnapshot().get(filename));
                Blob blob2 = Utils.readObject(file2, Blob.class);
                byte[] b2 = blob2.getVersionContents();
                String contents2 = new String(b2);
                String text = "<<<<<<< HEAD" + "\n"
                        + contents1 + "=======" + "\n"
                        + contents2 + ">>>>>>>" + "\n";
                Utils.writeContents(file, text);
                String blobID = Utils.sha1(text);
                stage.add(filename, blobID);
                stage.save();
                System.out.println("Encountered a merge conflict.");
            }
        }
        if (!currCommit.getSnapshot().containsKey(filename)) {
            if (splitPoint.getSnapshot().containsKey(filename)
                    && otherCommit.getSnapshot().containsKey(filename)
                    && !splitPoint.getSnapshot().get(filename).
                    equals(otherCommit.getSnapshot().get(filename))) {
                File file = Utils.join(CWD, filename);
                File file2 = Utils.join(BLOBS,
                        otherCommit.getSnapshot().get(filename));
                Blob blob2 = Utils.readObject(file2, Blob.class);
                byte[] b2 = blob2.getVersionContents();
                String contents2 = new String(b2);
                String text = "<<<<<<< HEAD" +  "\n" + "======="
                        + "\n" + contents2 +  ">>>>>>>" + "\n";
                Utils.writeContents(file, text);
                String blobID = Utils.sha1(text);
                stage.add(filename, blobID);
                stage.save();
                System.out.println("Encountered a merge conflict.");
            }
        }
        condition82(filename, splitPoint, currCommit, otherCommit);
    }


    private void condition82(String filename, Commit splitPoint,
                             Commit currCommit, Commit otherCommit) {
        if (!otherCommit.getSnapshot().containsKey(filename)) {
            if (splitPoint.getSnapshot().containsKey(filename)
                    && currCommit.getSnapshot().containsKey(filename)
                    && !splitPoint.getSnapshot().get(filename).
                    equals(currCommit.getSnapshot().get(filename))) {
                File file = Utils.join(CWD, filename);
                File file1 = Utils.join(BLOBS,
                        currCommit.getSnapshot().get(filename));
                Blob blob1 = Utils.readObject(file1, Blob.class);
                byte[] b1 = blob1.getVersionContents();
                String contents1 = new String(b1);
                String text = "<<<<<<< HEAD" + "\n"
                        + contents1 + "======="
                        + "\n"  + ">>>>>>>" + "\n";
                Utils.writeContents(file, text);
                String blobID = Utils.sha1(text);
                stage.add(filename, blobID);
                stage.save();
                System.out.println("Encountered a merge conflict.");
            }
        }
    }
    private void condition7(String filename,
                            Commit splitPoint,
                            Commit currCommit, Commit otherCommit,
                            String b) {
        stage = stage.fromFile();
        if (!splitPoint.getSnapshot().containsKey(filename)
                && !currCommit.getSnapshot().containsKey(filename)
                && otherCommit.getSnapshot().containsKey(filename)) {
            stage.add(filename, otherCommit.getSnapshot().get(filename));
            stage.save();
            File file1 = Utils.join(CWD, filename);
            File file2 = Utils.join(BLOBS, otherCommit.getSnapshot().get(filename));
            Blob blob = Utils.readObject(file2, Blob.class);
            Utils.writeContents(file1, blob.getVersionContents());
        }
    }

    private void condition6(String filename,
                            Commit splitPoint,
                            Commit currCommit,
                            Commit otherCommit) {
        if (splitPoint.getSnapshot().containsKey(filename)
                && otherCommit.getSnapshot().
                containsKey(filename)) {
            if (splitPoint.getSnapshot().
                    get(filename).
                    equals(otherCommit.getSnapshot().get(filename))
                    && !currCommit.getSnapshot().containsKey(filename)) {
                return;
            }
        }
    }



    private void condition5(String filename,
                            Commit splitPoint,
                            Commit currCommit,
                            Commit otherCommit) {
        if (!splitPoint.
                getSnapshot().containsKey(filename)
                && currCommit.
                getSnapshot().containsKey(filename)
                && !otherCommit.getSnapshot().containsKey(filename)) {
            return;
        }
    }

    private void condition4(String filename,
                            Commit splitPoint,
                            Commit currCommit,
                            Commit otherCommit) {
        stage.fromFile();
        if (splitPoint.getSnapshot().
                containsKey(filename)
                && currCommit.getSnapshot().containsKey(filename)) {
            if (splitPoint.getSnapshot().
                    get(filename).equals(currCommit.getSnapshot().
                            get(filename))
                    && !otherCommit.
                    getSnapshot().containsKey(filename)) {
                remove(filename);
                stage.save();
            }

        }
    }

    private void condition3(String filename,
                            Commit splitPoint,
                            Commit currCommit,
                            Commit otherCommit) {
        if (splitPoint.getSnapshot().
                containsKey(filename)
                && !otherCommit.getSnapshot().
                containsKey(filename)
                & !currCommit.getSnapshot().
                containsKey(filename)) {
            return;

        }
        if (splitPoint.getSnapshot().containsKey(filename)
                && currCommit.getSnapshot().containsKey(filename)
                && otherCommit.getSnapshot().containsKey(filename)) {
            if (!splitPoint.getSnapshot().
                    get(filename).equals(currCommit.getSnapshot().get(filename))
                    && !splitPoint.getSnapshot().get(filename).
                    equals(otherCommit.getSnapshot().get(filename))
                    && otherCommit.getSnapshot().get(filename).
                    equals(currCommit.getSnapshot().get(filename))) {
                return;
            }

        }
    }

    private void condition2(String filename,
                            Commit splitPoint,
                            Commit currCommit,
                            Commit otherCommit) {
        stage = stage.fromFile();
        if (splitPoint.getSnapshot().get(filename)
                != currCommit.getSnapshot().get(filename)
                && splitPoint.getSnapshot().get(filename)
                == otherCommit.getSnapshot().get(filename)) {
            return;
        }
    }

    private void condition1(String filename,
                            Commit splitPoint,
                            Commit currCommit,
                            Commit otherCommit,
                            String b) {
        stage = stage.fromFile();
        if (!splitPoint.getSnapshot().containsKey(filename)
                || !currCommit.getSnapshot().containsKey(filename)
                || !otherCommit.getSnapshot().containsKey(filename)) {
            return;
        }
        if (splitPoint.getSnapshot().
                get(filename).equals(currCommit.
                        getSnapshot().get(filename))
                && !splitPoint.getSnapshot().get(filename).
                equals(otherCommit.getSnapshot().get(filename))) {
            stage.add(filename, otherCommit.getSnapshot().get(filename));
            stage.save();
            File file1 = Utils.join(CWD, filename);
            File file2 = Utils.join(BLOBS, otherCommit.getSnapshot().get(filename));
            Blob blob = Utils.readObject(file2, Blob.class);
            Utils.writeContents(file1, blob.getVersionContents());
        }

    }
}










