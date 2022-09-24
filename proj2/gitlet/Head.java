package gitlet;

import java.io.File;
import java.io.Serializable;

public class Head implements Serializable {


    public static void setHead(String branchName, Commit headCommit) {
        Branch branch = new Branch(branchName, headCommit);
        Utils.writeObject(Repository.HEAD, branch);

    }


    public static Commit getHead() {
        return Branch.fromFile(Repository.HEAD).getHeadPointer();
    }

    public static void otherBranchPointer(String branchName, Commit commit) {
        Branch branch = new Branch(branchName, commit);
        File branchFile = Utils.join(Repository.BRANCHES, branchName);
        Utils.writeObject(branchFile, branch);
    }

    public static Commit getOtherBranchPointer(String branchName) {
        File branch = Utils.join(Repository.BRANCHES, branchName);
        return Branch.fromFile(branch).getHeadPointer();
    }


}
