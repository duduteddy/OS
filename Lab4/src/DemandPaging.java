import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class DemandPaging {

    // Variables for provided values
    static int M;
    static int P;
    static int S;
    static int J;
    static int N;
    static String algorithm;
    static int outPut;
    static int numProcesses;

    static FrameTable frameTable;
    static Process[] processes;
    static List<Process> pList;

    static Scanner randNums;



    public static void main(String[] args) {

        // Evaluate the variable values to what is provided by the command line arguments
        if (args.length != 7) {
            System.err.println("ERROR! Improper number of arguments!");
            System.exit(0);
        } else {
            M = Integer.parseInt(args[0]);
            P = Integer.parseInt(args[1]);
            S = Integer.parseInt(args[2]);
            J = Integer.parseInt(args[3]);
            N = Integer.parseInt(args[4]);
            algorithm = args[5];
            outPut = Integer.parseInt(args[6]);
        }

        // Load random numbers from file
        try {
            //File inputFile = new File ("input.txt");
            //Scanner sc = new Scanner(inputFile);
//            while(sc.hasNext()){
//                M = sc.nextInt();
//                P = sc.nextInt();
//                S = sc.nextInt();
//                J = sc.nextInt();
//                N = sc.nextInt();
//                algorithm = sc.next();
//                outPut = sc.nextInt();
//            }
            File randomNumsFile = new File("random-numbers.txt");
            randNums = new Scanner(randomNumsFile);
        } catch (FileNotFoundException e) { e.printStackTrace(); }

        // Output provided input.txt values
        System.out.println("The machine size is " + M + ".");
        System.out.println("The page size is " + P + ".");
        System.out.println("The process size is " + S + ".");
        System.out.println("The job mix number is " + J + ".");
        System.out.println("The number of references per process is " + N + ".");
        System.out.println("The replacement algorithm is " + algorithm + ".");
        System.out.println("The level of debugging output is " + outPut + ".");
        System.out.println("\n");

        // Determine the number of processes, initialize array of processes

        if (J == 1) { numProcesses = 1; } else { numProcesses = 4; }

        // Create processes array
        processes = new Process[numProcesses];

        // Set process IDs
        for (int i = 0; i < processes.length; i++) {
            processes[i] = new Process(i+1);
        }

        // Create frame table
        frameTable = new FrameTable();

        // Run the simulator
//        if (outPut != 0) {
//
//            System.out.println();
//        } else {
//            simulator();
//        }
        simulator();
        System.out.println();

        // Display results
        int totalFaults = 0;
        int totalResidenceTime = 0;
        int totalEvictions = 0;
        double avgResidency = 0;

        System.out.println("\n");

        //Individual results
        for (int i = 0; i < processes.length; i++) {
            processes[i].showResults();
            totalFaults += processes[i].numFaults;
            totalResidenceTime += processes[i].totalResTime;
            totalEvictions += processes[i].numEvictions;
        }

        //Total results
        if (totalEvictions > 0) {
            avgResidency = (double)totalResidenceTime / (double)totalEvictions;
            System.out.printf("The total number of faults is %d and the overall average residency is %.3f.\n", totalFaults, avgResidency);
        } else {
            System.out.printf("The total number of faults is %d and the overall average residency is undefined.\n", totalFaults);
        }

    }

    // Run the simulation
    public static void simulator() {

        Process curProcess;

        // Initialize the process queue
        pList = new ArrayList<>();
        for (int i = 0; i < processes.length; i++) {
            pList.add(processes[i]);
        }

        // Run the simulation
        int curTime = 1;
        Frame munipulateFrame = null;

        while (pList.size() > 0) {

            // Give each process a turn (3 cycles)
            //for (int i = 0; i < pList.size(); i++) {
            for(Process p : pList){
                curProcess = p;

                for (int j = 0; j < 3; j++) {
                    if (curProcess.references > 0) {
                        curProcess.referenceDetail(curTime, randNums);
                        if (frameTable.findHit(curProcess) != -1 ) {
                            if (outPut != 0) { System.out.printf("Hit in frame %d.\n", frameTable.findHit(curProcess)); }

                            frameTable.addResProcessTime();


                        } else {
                            curProcess.numFaults += 1;

                            if (frameTable.freeFrame() == null) {
                                if (algorithm.equalsIgnoreCase("FIFO")) {
                                    munipulateFrame = frameTable.oldestFrame();
                                    if (outPut != 0) { System.out.printf("Fault, evicting page of "+ curProcess.curPage+" of "+curProcess.id+ " from frame %d.\n", munipulateFrame.id); }
                                    munipulateFrame.evictInsert(curProcess, curTime);
                                } else if (algorithm.equalsIgnoreCase("LRU")) {
                                    munipulateFrame = frameTable.lruFrame();
                                    if (outPut != 0) { System.out.printf("Fault, evicting page of "+ curProcess.curPage+" of "+curProcess.id+ " from frame %d.\n", munipulateFrame.id); }

                                    munipulateFrame.evictInsert(curProcess, curTime);

                                } else if (algorithm.equalsIgnoreCase("RANDOM")) {
                                    munipulateFrame = frameTable.randomFrame(randNums);
                                    if (outPut != 0) { System.out.printf("Fault, evicting page of "+ curProcess.curPage+" of "+curProcess.id+ " from frame %d.\n", munipulateFrame.id); }
                                    munipulateFrame.evictInsert(curProcess, curTime);
                                }
                            } else {
                                munipulateFrame = frameTable.freeFrame();
                                if (outPut != 0) { System.out.printf("Fault, evicting page of "+ curProcess.curPage+" of "+curProcess.id+ " from frame %d.\n", munipulateFrame.id); }
                                munipulateFrame.freeInsert(curProcess, curTime);
                            }
                           frameTable.addResProcessTime();

                        }
                        //frameTable.showFrameStatus(); //temporary delete
                        curTime += 1;
                        curProcess.nextWord(randNums);
                    }
                }
                //frameTable.addResProcessTime();
            }

            // Remove finished processes from process queue
            for (int i = 0; i < processes.length; i++) {
                if (processes[i].references == 0) {
                    if (pList.contains(processes[i])) {
                        pList.remove(processes[i]);
                    }
                }
            }

        }

    }



}