import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class Scheduler {
    public static Scanner randNumScanner;
    public static  boolean hasVerbose = false ,isDone= false;
    public static Process[] processes;
    public static int finishingTime;
    private static int time, running, cpuBurst;
    private static int[] blocked;
    private static int[] bursts;
    private static ArrayList<Process> readyQue;
    private static ArrayList<Process> readyQue1;


    static double cpuUtilization = 0, ioUtilization = 0;




    //"RANDOM" NUMBER GENERATOR
    public static int randomOS(int u) {

        return(1+(randNumScanner.nextInt() % u));
    }

    public static void  main(String args[]){

        Scanner  inputScanner;
        int processCnt ;



        try {
            File fileRandom = new File("randomNums.txt");
            randNumScanner = new Scanner(fileRandom);

            // may have 2 arguments -verbose -filename
            File fileName = new File(args[0]);
            if(args.length == 1){
                hasVerbose = false;

            }else if(args.length == 2){
                hasVerbose = true;
                fileName = new File(args[1]);
            }else{
                System.err.println(" Invalid input args length( No more than 2 parameters) ");
            }

            //print original input
            inputScanner = new Scanner(fileName);
            System.out.print("The original input was:    ");

            processCnt = inputScanner.nextInt();
            List<Integer> arrivalTime = new ArrayList<>(processCnt);

            System.out.print(processCnt + "    ");
            for(int i=0; i< processCnt; i++) {
                arrivalTime.add(inputScanner.nextInt());
                System.out.print(arrivalTime.get(i) + " " + inputScanner.nextInt() + " " + inputScanner.nextInt()
                        + " " + inputScanner.nextInt() + "    ");
            }
            System.out.println();
                //sort the list
                Collections.sort(arrivalTime);
                inputScanner.close();

                //read the processes
                inputScanner = new Scanner(fileName);
                processCnt = inputScanner.nextInt();
                processes  = new Process[processCnt];

                for(int i = 0; i< processCnt; i++){
                    Process curProcess = new Process(inputScanner.nextInt(),inputScanner.nextInt(),
                            inputScanner.nextInt(),inputScanner.nextInt());
                    processes[arrivalTime.indexOf(curProcess.a)] = curProcess; //insert it in the index of matching arrival time
                    arrivalTime.set(arrivalTime.indexOf(curProcess.a), -1);

                }

                inputScanner.close();

            //print sorted input
            System.out.print("The sorted input is:\t"+ processCnt+"\t");
            for(int i = 0; i< processCnt; i++){
                processes[i].processID = i;
                System.out.print(processes[i].getA()+" "+processes[i].getB()+" "+ processes[i].getC()
                +" "+processes[i].getIO()+ "\t");
            }
            System.out.println("\n");

            //choose the algorithm
            Scanner algorithmSc = new Scanner(System.in);
            System.out.print("Choose an algorithm [FCFS || RR || PSJF(SRTN) || uniprogram ");
            String input = algorithmSc.nextLine();
            System.out.println();

            //
            switch (input){
                case "FCFS":
                case "fcfs":
                    fcfs();break;
                case "RR":
                case "rr":
                    rr();
                    break;
                case "srtn":
                case "SRTN":
                case "psjf":
                case "PSJF":
                    srtn();
                    break;
                case "uniprogram":
                    uniprogram();
                    break;

            }



            System.out.println();
            int count = 0;
            double avgWaitingTime = 0, avgTurnaroundTime = 0, max = -1;
            for (Process p : processes) {
                if (p.getFinish() > max) max = p.getFinish();
                avgWaitingTime += p.getWait();
                avgTurnaroundTime += p.getFinish() - p.getA();
                System.out.println("Process " + count++ + ":");
                System.out.println("	(A,B,C,IO) = (" + p.getA() + "," + p.getB() + "," + (p.getFinish() - p.getA() - p.getIOT() - p.getWait()) + "," + p.getIO() + ")");
                System.out.println("	Finishing time: " + p.getFinish());
                System.out.println("	Turnaround time: " + (p.getFinish() - p.getA()));
                System.out.println("	I/O time: " + p.getIOT());
                System.out.println("	Waiting time: " + p.getWait());
                System.out.println();
            }
            System.out.println("Summary Data:");
            System.out.println("	Finishing time: " + max);
            System.out.println("	CPU Utilization: " + cpuUtilization / max);
            System.out.println("	I/O Utilization: " + ioUtilization / max);
            System.out.println("	Throughput " + (count / max) * 100 + " processes per hundread cycles");
            System.out.println("	Average Turnaround time " + avgTurnaroundTime / count);
            System.out.println("	Average Wait time " + avgWaitingTime / count);
            System.out.println();



        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static void srtn() {

        ArrayList<Process> processList = new ArrayList<>();
        time = 0;
        running = -1;
        isDone = false;
        readyQue = new ArrayList<>();
        int totalTime = 0;

        for(int i = 0; i< processes.length; i++){
            processList.add(processes[i]);
            totalTime+= processes[i].getC();
        }
        System.out.println("The scheduling algorithm used was Shortest Job First");
        System.out.println();
        int cnt = 0;
        blocked = new int[processList.size()];
        for (int i = 0; i < processList.size(); i++)
            blocked[i] = -1;
        while (!isDone) {
            if (hasVerbose) {
                System.out.print("Before cycle " + time + "  :	");
                for (Process p : processList) {
                    if (p.getStatus().equals("terminated")) {
                        System.out.print("Terminated 0	");
                    }
                    if (p.getStatus().equals("unstarted")) {
                        System.out.print("Unstarted 0	");
                    }
                    if (p.getStatus() .equals("ready")) {
                        System.out.print("Ready 0		");
                    }
                    if (p.getStatus().equals("running")) {
                        System.out.print("Running " + cpuBurst + "	");
                    }
                    if (p.getStatus().equals("blocked")) {
                        System.out.print("Blocked " + blocked[processList.indexOf(p)] + "	");
                    }
                }
                System.out.println();
            }
            for (int i = 0; i < processList.size(); i++) {//unblock
                if (blocked[i] == 0) {
                    blocked[i]--;
                    if (!processList.get(i).getStatus().equals("terminated")) {
                        processList.get(i).setStatus(0);
                        readyQue.add(processList.get(i));
                    }
                }
            }
            for (Process p : processList) {//if a new process came in
                if (p.getA() == time) {
                    p.setStatus(0);
                    readyQue.add(p);
                }
            }
            //unblock 1st, check for new, run, block

            if (running == -1) {//if there's nothing running
                if (!readyQue.isEmpty()) {
                    int i = 0, min = readyQue.get(0).getC(), runInd = 0;
                    for (; i < readyQue.size(); i++) {
                        if (readyQue.get(i).getC() < min) {
                            min = readyQue.get(i).getC();
                            runInd = i;
                        }
                    }
                    readyQue.get(runInd).setStatus(1);
                    running = processList.indexOf(readyQue.get(runInd));
                    cpuBurst = randomOS( processList.get(running).getB());
                    readyQue.remove(runInd);
                }
            } else if (running != -1 && cpuBurst == 1 ) {//if you have to block
                if (processList.get(running).getC() != 1) {
                    processList.get(running).setStatus(2);
                    processList.get(running).decC();
                    int io = processList.get(running).getIO();
                    blocked[running] = randomOS(io);
                    totalTime +=io;
                    processList.get(running).incIOT(blocked[running]);
                } else {
                    if (processList.get(running).getFinish() == 0) {
                        processList.get(running).setFinish(time);
                    }
                    processList.get(running).setStatus(-2);
                    processList.get(running).decC();

                }
                if (!readyQue.isEmpty()) {
                    int i = 0, min = readyQue.get(0).getC(), runInd = 0;
                    for (; i < readyQue.size(); i++) {
                        if (readyQue.get(i).getC() < min) {
                            min = readyQue.get(i).getC();
                            runInd = i;
                        }
                    }
                    readyQue.get(runInd).setStatus(1);
                    running = processList.indexOf(readyQue.get(runInd));
                    cpuBurst = randomOS( processList.get(running).getB());
                    readyQue.remove(runInd);
                } else
                    running = -1;
            } else if (running != -1 && cpuBurst > 0 /*&& !ran*/) {//if there's something running
                cpuBurst--;
                processList.get(running).decC();
            }

            for (int i = 0; i < processList.size(); i++) {//decrement blocked time for all
                if (blocked[i] > 0) {
                    blocked[i]--;
                }
            }
            cnt = 0;
            for (Process p : processList) {
                if (p.getC() == 0) {
                    if (processList.indexOf(p) == running) {
                        if (!readyQue.isEmpty()) {
                            int i = 0, min = readyQue.get(0).getC(), runInd = 0;
                            for (; i < readyQue.size(); i++) {
                                if (readyQue.get(i).getC() < min) {
                                    min = readyQue.get(i).getC();
                                    runInd = i;
                                }
                            }
                            readyQue.get(runInd).setStatus(1);
                            running = processList.indexOf(readyQue.get(runInd));
                            cpuBurst = randomOS( processList.get(running).getB());
                            readyQue.remove(runInd);
                        } else running = -1;
                    }
                    cnt++;
                    if (p.getFinish() == 0) {
                        p.setFinish(time);
                    }
                    p.setStatus(-2);
                    readyQue.remove(p);
                }
                if (cnt == processList.size()) {
                    isDone = true;
                }
            }
            for (Process p : processList) {
                if (p.getStatus().equals("ready") ) p.incWait(1);
            }
            time++;
            if (running != -1) cpuUtilization++;
            for (int i = 0; i < processList.size(); i++) {
                if (blocked[i] >= 0) {
                    ioUtilization++;
                    break;
                }
            }

            if (time == totalTime) break;
        }
    }

    private static void rr() {
        ArrayList<Process> processList = new ArrayList<>();
        time = 0;
        running = -1;
        isDone = false;
        readyQue = new ArrayList<>();
        readyQue1 = new ArrayList<>();
        for(int i = 0; i< processes.length; i++){
            processList.add(processes[i]);
        }
        System.out.println("The scheduling algorithm used was Round Robin");
        System.out.println();
        int cnt = 0;
        int quantum = 2;
        blocked = new int[processList.size()];
        bursts = new int[processList.size()];
        for (int i = 0; i < processList.size(); i++) {
            blocked[i] = -1;
            bursts[i] = 0;
        }
        while (!isDone) {
            if (hasVerbose) {
                System.out.print("Before cycle " + time + "  :	");
                for (Process p : processList) {
                    if (p.getStatus() .equals("terminated")) {
                        System.out.print("Terminated 0	");
                    }
                    if (p.getStatus() .equals("unstarted")) {
                        System.out.print("Unstarted 0	");
                    }
                    if (p.getStatus().equals("ready") ) {
                        System.out.print("Ready 0		");
                    }
                    if (p.getStatus().equals("running") ) {
                        System.out.print("Running " + bursts[processList.indexOf(p)] + "	");
                    }
                    if (p.getStatus().equals("blocked") ) {
                        System.out.print("Blocked " + (blocked[processList.indexOf(p)] + 1) + "	");
                    }
                }
                System.out.println();

            }
            for (int i = 0; i < processList.size(); i++) {//unblock
                if (blocked[i] == 0) {
                    blocked[i]--;
                    if (!processList.get(i).getStatus().equals("terminated")) {
                        processList.get(i).setStatus(0);
                        readyQue1.add(processList.get(i));
                    }
                }
            }
            for (Process p : processList) {//if a new process came in
                if (p.getA() == time) {
                    p.setStatus(0);
                    readyQue1.add(p);
                }
            }
            //unblock 1st, check for new, run, block

            if (running == -1) {//if there's nothing running
                if (!readyQue.isEmpty()) {
                    readyQue.get(0).setStatus(1);
                    running = processList.indexOf(readyQue.get(0));
                    if (bursts[running] == 0)
                        bursts[running] =randomOS(processList.get(running).getB());
                    readyQue.remove(0);
                    quantum = 2;

                } else if (!readyQue1.isEmpty()) {
                    readyQue1.get(0).setStatus(1);
                    running = processList.indexOf(readyQue1.get(0));
                    if (bursts[running] == 0)
                        bursts[running] = randomOS(processList.get(running).getB());
                    readyQue1.remove(0);
                    quantum = 2;

                }
            } else if (running != -1 && bursts[running] == 1 /*&& !ran*/) {//if you have to block
                if (processList.get(running).getC() != 1) {
                    processList.get(running).setStatus(2);
                    bursts[running]--;
                    processList.get(running).decC();
                    blocked[running] = randomOS(processList.get(running).getIO());
                    processList.get(running).incIOT(blocked[running]);
                } else {
                    if (processList.get(running).getFinish() == 0) {
                        processList.get(running).setFinish(time);
                    }
                    processList.get(running).setStatus(-2);
                    processList.get(running).decC();

                }
                if (!readyQue.isEmpty()) {
                    running = processList.indexOf(readyQue.get(0));
                    processList.get(running).setStatus(1);
                    readyQue.remove(0);
                    if (bursts[running] == 0)
                        bursts[running] = randomOS( processList.get(running).getB());
                    quantum = 2;
                } else if (!readyQue1.isEmpty()) {
                    readyQue1.get(0).setStatus(1);
                    running = processList.indexOf(readyQue1.get(0));
                    if (bursts[running] == 0)
                        bursts[running] = randomOS( processList.get(running).getB());
                    readyQue1.remove(0);
                    quantum = 2;
                } else
                    running = -1;
            } else if (running != -1 && bursts[running] > 0) {//if there's something running
                if (quantum == 1) {
                    processList.get(running).setStatus(0);
                    processList.get(running).decC();
                    bursts[running]--;
                    readyQue1.add(processList.get(running));
                    if (!readyQue.isEmpty()) {
                        running = processList.indexOf(readyQue.get(0));
                        processList.get(running).setStatus(1);
                        readyQue.remove(0);
                        if (bursts[running] == 0)
                            bursts[running] = randomOS(processList.get(running).getB());
                        quantum = 2;
                    } else if (!readyQue1.isEmpty()) {
                        readyQue1.get(0).setStatus(1);
                        running = processList.indexOf(readyQue1.get(0));
                        if (bursts[running] == 0)
                            bursts[running] = randomOS(processList.get(running).getB());
                        readyQue1.remove(0);
                        quantum = 2;
                    }
                } else {
                    quantum--;
                    bursts[running]--;
                    processList.get(running).decC();
                }
            }
            Collections.sort(readyQue1);
            readyQue.addAll(readyQue1);
            readyQue1.clear();
            for (int i = 0; i < processList.size(); i++) {//decrement blocked time for all
                if (blocked[i] > 0) {
                    blocked[i]--;
                }
            }
            cnt = 0;
            for (Process p : processList) {
                if (p.getC() == 0) {
                    if (processList.indexOf(p) == running) {
                        if (!readyQue.isEmpty()) {
                            running = processList.indexOf(readyQue.get(0));
                            processList.get(running).setStatus(1);
                            readyQue.remove(0);
                            if (bursts[running] == 0)
                                bursts[running] = randomOS(processList.get(running).getB());
                            quantum = 2;
                        } else if (!readyQue1.isEmpty()) {
                            readyQue1.get(0).setStatus(1);
                            running = processList.indexOf(readyQue1.get(0));
                            if (bursts[running] == 0)
                                bursts[running] = randomOS( processList.get(running).getB());
                            readyQue1.remove(0);
                            quantum = 2;
                        }
                    }
                    cnt++;
                    if (p.getFinish() == 0) {
                        p.setFinish(time);
                    }
                    p.setStatus(-2);
                    readyQue.remove(p);
                }
                if (cnt == processList.size()) {
                    isDone = true;
                }
            }
            for (Process p : processList) {
                if (p.getStatus().equals("ready")) p.incWait(1);
            }
            time++;
            if (running != -1) cpuUtilization++;
            for (int i = 0; i < processList.size(); i++) {
                if (blocked[i] >= 0) {
                    ioUtilization++;
                    break;
                }
            }

        }
    }


    //First come first served algorithm
    private static void fcfs() {
        ArrayList<Process> processList = new ArrayList<>();
        time = 0;
        running = -1;
        isDone = false;
        readyQue = new ArrayList<>();
        readyQue1 = new ArrayList<>();
        for(int i = 0; i< processes.length; i++){
            processList.add(processes[i]);
        }
        System.out.println("The scheduling algorithm used was First Come First Served");
        System.out.println();
        int cnt;

        blocked = new int[processList.size()];
        for (int i = 0; i < processList.size(); i++)
            blocked[i] = -1;
        while (!isDone) {
            if (hasVerbose) {
                System.out.print("Before cycle " + time + "  :	");
                for (Process p : processList) {
                    if (p.status.equals("terminated")) {
                        System.out.print("Terminated 0	");
                    }
                    if (p.status.equals("unstarted")) {
                        System.out.print("Unstarted 0	");
                    }
                    if (p.status .equals( "ready")) {
                        System.out.print("Ready 0		");
                    }
                    if (p.status.equals("running")) {
                        System.out.print("Running " + cpuBurst + "	");
                    }
                    if (p.status.equals("blocked")) {
                        System.out.print("Blocked " + blocked[processList.indexOf(p)] + "	");
                    }
                }
                System.out.println();
            }

            //unblock
            for (int i = 0; i < processList.size(); i++) {
                if (blocked[i] == 0) {
                    blocked[i]--;
                    if (!processList.get(i).status.equals("terminated")) {
                        processList.get(i).setStatus(0);
                        readyQue.add(processList.get(i));
                    }
                }
            }
            for (Process p : processList) {//if a new process came in
                if (p.getA() == time) {
                    p.setStatus(0);
                    readyQue.add(p);
                }
            }
            //unblock 1st, check for new, run, block

            if (running == -1) {//if there's nothing running
                if (!readyQue.isEmpty()) {
                    readyQue.get(0).setStatus(1);
                    running = processList.indexOf(readyQue.get(0));
                    //System.out.println("@@@@@@@@"+processList.get(running).getB());
                    cpuBurst = randomOS(processList.get(running).getB());
                    //System.out.println(cpuBurst+"@@@@@@@@");
                    readyQue.remove(0);
                }
            } else if (running != -1 && cpuBurst == 1 ) {//if you have to block
                if (processList.get(running).getC() != 1) {
                    processList.get(running).setStatus(2);
                    processList.get(running).decC();
                    blocked[running] = randomOS(processList.get(running).getIO());
                    processList.get(running).incIOT(blocked[running]);
                } else {
                    if (processList.get(running).getFinish() == 0) {
                        processList.get(running).setFinish(time);
                    }
                    processList.get(running).setStatus(-2);
                    processList.get(running).decC();

                }
                if (!readyQue.isEmpty()) {
                    running = processList.indexOf(readyQue.get(0));
                    processList.get(running).setStatus(1);
                    readyQue.remove(0);
                    cpuBurst = randomOS( processList.get(running).getB());
                } else
                    running = -1;
            } else if (running != -1 && cpuBurst > 0 ) {//if there's something running
                cpuBurst--;
                processList.get(running).c -=1;
            }

            for (int i = 0; i < processList.size(); i++) {//decrement blocked time for all
                if (blocked[i] > 0) {
                    blocked[i]--;
                }
            }
            cnt = 0;
            for (Process p : processList) {
                if (p.getC() == 0) {
                    if (processList.indexOf(p) == running) {
                        if (!readyQue.isEmpty()) {
                            running = processList.indexOf(readyQue.get(0));
                            processList.get(running).setStatus(1);
                            readyQue.remove(0);
                            cpuBurst = randomOS(processList.get(running).getB());
                        }
                    }
                    cnt++;
                    if (p.getFinish() == 0) {
                        p.setFinish(time);
                    }
                    p.setStatus(-2);
                    readyQue.remove(p);
                }
                if (cnt == processList.size()) {
                    isDone = true;
                }
            }
            for (Process p : processList) {
                if (p.status.equals("ready")) p.incWait(1);
            }
            time++;
            if (running != -1) cpuUtilization++;
            for (int i = 0; i < processList.size(); i++) {
                if (blocked[i] >= 0) {
                    ioUtilization++;
                    break;
                }
            }

        }


    }

    private static void uniprogram() {

        ArrayList<Process> processList = new ArrayList<>();
        time = 0;
        running = -1;
        isDone = false;
        readyQue = new ArrayList<>();
        readyQue1 = new ArrayList<>();
        for(int i = 0; i< processes.length; i++){
            processList.add(processes[i]);
        }
        System.out.println("The scheduling algorithm used was Uniprogrammed");
        System.out.println();
        boolean firstGo = false;
        int cnt = 0;
        blocked = new int[processList.size()];
        for (int i = 0; i < processList.size(); i++)
            blocked[i] = -1;
        while (!isDone) {
            firstGo = false;
            if (hasVerbose) {
                System.out.print("Before cycle " + time + "  :	");
                for (Process p : processList) {
                    if (p.getStatus().equals("terminated")) {
                        System.out.print("Terminated 0	");
                    }
                    if (p.getStatus().equals("unstarted")) {
                        System.out.print("Unstarted 0	");
                    }
                    if (p.getStatus().equals("ready")) {
                        System.out.print("Ready 0		");
                    }
                    if (p.getStatus().equals("running")) {
                        System.out.print("Running " + cpuBurst + "	");
                    }
                    if (p.getStatus().equals(blocked)) {
                        System.out.print("Blocked " + (blocked[processList.indexOf(p)] + 1) + "	");
                    }
                }
                System.out.println();
            }
            if (running != -1) {
                if (blocked[running] == 0) {
                    blocked[running]--;
                    if (!processList.get(running).getStatus().equals("terminated")) {
                        processList.get(running).setStatus(1);
                        cpuBurst = randomOS( processList.get(running).getB());
                        firstGo = true;
                    }
                }
            }
            for (Process p : processList) {//if a new process came in
                if (p.getA() == time) {
                    if (running == -1) {
                        running = processList.indexOf(p);
                        cpuBurst = randomOS( processList.get(running).getB());
                        firstGo = true;
                        p.setStatus(1);
                    } else p.setStatus(0);
                }
            }

            if (running != -1 && cpuBurst == 1 && !firstGo) {//if you have to block
                if (processList.get(running).getStatus().equals("running")) {
                    if (processList.get(running).getC() != 1) {
                        processList.get(running).setStatus(2);
                        processList.get(running).decC();
                        blocked[running] = randomOS(processList.get(running).getIO());//TODO generate random number +1!!!!
                        processList.get(running).incIOT(blocked[running]);
                    } else {
                        if (processList.get(running).getFinish() == 0) {
                            processList.get(running).setFinish(time);
                        }
                        processList.get(running).setStatus(-2);
                        processList.get(running).decC();
                        if (running == processList.size() - 1) {
                            isDone = true;
                        } else if (processList.get(running + 1).getStatus().equals("ready")) {
                            running++;
                            processList.get(running).setStatus(1);
                            cpuBurst = randomOS( processList.get(running).getB());
                        } else running = -1;
                    }
                }
            } else if (running != -1 && cpuBurst > 1 && !firstGo) {//if there's something running
                if (processList.get(running).getStatus().equals("running")) {
                    if (processList.get(running).getC() == 1) {
                        if (processList.get(running).getFinish() == 0) {
                            processList.get(running).setFinish(time);
                        }
                        processList.get(running).setStatus(-2);
                        processList.get(running).decC();
                        if (running == processList.size() - 1) {
                            isDone = true;
                        } else if (processList.get(running + 1).getStatus() .equals("ready")) {
                            running++;
                            processList.get(running).setStatus(1);
                            cpuBurst = randomOS( processList.get(running).getB());
                        } else running = -1;
                    } else {
                        cpuBurst--;
                        processList.get(running).decC();
                    }
                }
            }
            if (blocked[running] > 0) {
                blocked[running]--;
            }
            cnt = 0;
            for (Process p : processList) {
                if (p.getC() == 0) {
                    cnt++;
                }
                if (cnt == processList.size()) {
                    isDone = true;
                }
            }
            boolean didRun = false;
            for (Process p : processList) {
                if (p.getStatus().equals("runnning")) didRun = true;
                if (p.getStatus().equals("ready")) p.incWait(1);
            }
            if (didRun) cpuUtilization++;
            time++;
            for (int i = 0; i < processList.size(); i++) {
                if (blocked[i] >= 0) {
                    ioUtilization++;
                    break;
                }
            }
        }
    }

}
