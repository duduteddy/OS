import java.util.Scanner;

public class Process {

    // Process variables
    int id;
    int curWord;
    int curPage;
    int references;

    // Variables to track results
    int totalResTime, curResTime, curResStart, curResEnd;
    int numFaults;
    int numEvictions;
    private double avgResidency;

    // Constructor (end)
    public Process() {
        id = -1;
        curWord = -1;
        curPage = -1;
        references = -1;

        totalResTime = -1;
        curResTime = -1;
        curResStart = -1;
        curResEnd = -1;
        numFaults = 0;
        numEvictions = 0;
        avgResidency = 0;
    }

    // Constructor (ID provided)
    public Process(int id) {
        this.id = id;
        curWord = (111 * id) % DemandPaging.S;
        curPage = curWord / DemandPaging.P;
        references = DemandPaging.N;
    }

    // Get the next word/page
    public void nextWord(Scanner r) {

        // Get values for A,B,C
        double A=0,B=0,C=0;
        if (DemandPaging.J == 1) {
            A=1; B=0; C=0;
        } else if (DemandPaging.J == 2) {
            A=1; B=0; C=0;
        } else if (DemandPaging.J == 3) {
            A=0; B=0; C=0;
        } else if (DemandPaging.J == 4) {
            if (id == 1) {
                A=.75; B=.25; C=0;
            } else if (id == 2) {
                A=.75; B=0; C=.25;
            } else if (id == 3) {
                A=.75; B=.125; C=.125;
            } else if (id == 4) {
                A=.5; B=.125; C=.125;
            }
        }

        // Determine current word and page
        int randNum = r.nextInt();
        double y = randNum / (Integer.MAX_VALUE + 1d);

        if (y < A) {
            curWord = (curWord + 1) % DemandPaging.S;
        } else if (y < (A+B)) {
            curWord = (curWord - 5 + DemandPaging.S) % DemandPaging.S;
        } else if (y < (A+B+C)) {
            curWord = (curWord + 4) % DemandPaging.S;
        } else {
            curWord = r.nextInt() % DemandPaging.S;
        }

        curPage = curWord / DemandPaging.P;

    }

    // Report reference and decrement remaining references
    public void referenceDetail(int cycle, Scanner r) {
        if (DemandPaging.outPut != 0) { System.out.printf("%d references word %d (page %d) at time %d: ", id, curWord, curPage, cycle); }
        if (DemandPaging.outPut == 2) { System.out.printf("%d uses random number: %d\n", id, r.nextInt()); }
        references -=1 ;
    }

    // Display final individual results
    public void showResults() {

        if (numEvictions == 0) {
            System.out.printf("Process %d had %d faults and an undefined average residency.\n", id, numFaults);
        } else {
            avgResidency = (double)totalResTime / (double)numEvictions;
            System.out.printf("Process %d had %d faults and  %.3f average residency.\n", id, numFaults, avgResidency);
        }

    }

}