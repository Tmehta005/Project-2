package gitlet;

import java.io.IOException;

/**
 * Driver class for Gitlet, a subset of the Git version-control system.
 *
 * @author Tanya Mehta
 */
public class Main {

    /**
     * Usage: java gitlet.Main ARGS, where ARGS contains
     * <COMMAND> <OPERAND1> <OPERAND2> ...
     */

    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            exitWithError("Please enter a command.");
        }
        Repository rep = new Repository();
        String firstArg = args[0];
        switch (firstArg) {
            case "init":
                rep.init();
                break;
            case "add":
                String filename = args[1];
                rep.add(filename);
                break;
            case "commit":
                if (args[1].isEmpty() && args[1].isBlank()) {
                    exitWithError("Please enter a commit message.");
                }
                String message = args[1];
                rep.commit(message);
                break;
            case "log":
                rep.log();
                break;
            case "checkout":
                if (args.length == 2) {
                    rep.checkoutBranch(args[1]);
                    return;
                }
                if (args.length == 3) {
                    rep.checkout(args[2]);
                    return;
                }
                if (!args[2].equals("--")) {
                    Main.exitWithError("Incorrect operands.");
                }
                if (args.length == 4) {
                    rep.checkout(args[1], args[3]);
                    return;
                }
                break;
            case "rm":
                rep.remove(args[1]);
                break;
            case "global-log":
                rep.globalLog();
                break;
            case "find":
                rep.find(args[1]);
                break;
            case "status":
                rep.status();
                break;
            case "branch":
                rep.branch(args[1]);
                break;
            case "rm-branch":
                rep.rmBranch(args[1]);
                break;
            case "reset":
                rep.reset(args[1]);
                break;
            case "merge":
                rep.merge(args[1]);
                break;
            default:
                Main.exitWithError("No command with that name exists.");
        }


    }

    public static void exitWithError(String s) {
        System.out.println(s);
        System.exit(0);
    }


}


