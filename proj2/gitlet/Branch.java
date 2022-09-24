package gitlet;
import java.io.File;
import java.io.Serializable;

public class Branch implements Serializable {


    private String branchName;


    private Commit headPointer;


    public Branch(String branchName, Commit headPointer) {
        this.branchName = branchName;
        this.headPointer = headPointer;
    }


    public Commit getHeadPointer() {
        return this.headPointer;
    }

    public void save() {
        File file = Utils.join(Repository.BRANCHES, this.branchName);
        Utils.writeObject(file, this);
    }

    public static Branch fromFile(File file)  {
        return Utils.readObject(file, Branch.class);
    }

    public String getBranchName() {
        return this.branchName;
    }

    public void setHeadPointer(Commit commit) {
        this.headPointer = commit;
        save();

    }


//        public void saveCurrBranch(){
//            File file = Utils.join(Repository.currBranch,this.BranchName);
//            Utils.writeObject(file,this);
//        }

//        //public static Branch fromCurrBranchFile() {
//        return Utils.readObject(Repository.currBranch,Branch.class);
//    }


}

