import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Lab3 {


    static int T;
    public static int R;
    static int[] resources;

    static Task[] tasks;
    static List<Task> readyList = new ArrayList<>();
    static List<Task> blockedTasks = new ArrayList<>();
    private static List<Task> nonblockedTasks = new ArrayList<>();

    static int[] resourcesArray = new int[R];

    static boolean helper; //use args[1] to help compute


    // get input file
    private static void input(String fileName) {

        File file = new File(fileName);

        String activityType;
        int taskNum;
        int delay;
        int resourceType;
        int value;

        try {

            Scanner fileInput = new Scanner(file);

            /*
            the number of tasks
            resource types
            */
            T = fileInput.nextInt();
            R = fileInput.nextInt();

            //Create an integer array of resource types
            resources = new int[R];
            resourcesArray = new int[R];

            //Evaluate the number of units each resource type has based on the input.
            //Initialize pending resource gain array as array of zeroes
            for (int i = 0; i < resources.length; i++) {
                resources[i] = fileInput.nextInt();
                resourcesArray[i] = 0;
            }

            //Initialize the array of tasks.
            tasks = new Task[T];

            for (int i = 0; i < tasks.length; i++) {
                tasks[i] = new Task((i+1), R);
            }


            // Fill the tasks with its activities from the input
            while (fileInput.hasNext()) {

                // Retrieve activity information from input
                activityType = fileInput.next();
                taskNum = fileInput.nextInt();
                delay = fileInput.nextInt();
                resourceType = fileInput.nextInt();
                value = fileInput.nextInt();

                // Add activity to the appropriate task
                tasks[taskNum-1].addActivity(activityType,delay,(resourceType-1),value);
            }

            //Set current activity (start activity) for the tasks
            for (int i = 0; i < tasks.length; i++) {
                tasks[i].activity = tasks[i].activities.get(0);
                tasks[i].curDelay = tasks[i].activity.delay;
            }

            fileInput.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }



    // Main method
    public static void main(String[] args) {

        String fileName = "";

        if(args.length != 1){
            System.out.println(" ERROR: Incorrect length of args was given");
            return;
        }

        // Run the optimistic manager simulation
        fileName = args[0];
        input(fileName);
        runOptimistic();

        // Compute optimistic manager stats
        int totalTime = 0;
        int totalWaitTime = 0;
        double waitPercent = 0.0;

        System.out.println("FIFO :");

        for (int i = 0; i < T; i++) {

            tasks[i].output();

            if (tasks[i].isAborted == false) {
                totalTime += tasks[i].takenTime;
                totalWaitTime += tasks[i].waitTime;
            }


        }
        waitPercent = ((double)totalWaitTime / (double)totalTime) * 100;

        // print the total result of optimistic FIFO
        System.out.printf("\t%-8s: ", "Total");
        System.out.printf(" %d    %d    %.0f%%\n", totalTime, totalWaitTime, waitPercent);
        System.out.println("\n\n");

        // Run the banker manager simulation
        //separate input
        input(fileName);
        bankerManager();

        // Compute banker manager stats
        totalTime = 0;
        totalWaitTime = 0;
        //waitPercent = 0.0;

        System.out.println("BANKER'S : ");

        for (int i = 0; i < T; i++) {

            tasks[i].output();

            if (tasks[i].isAborted == false) {
                totalTime += tasks[i].takenTime;
                totalWaitTime += tasks[i].waitTime;
            }


        }
        waitPercent = ((double)totalWaitTime / (double)totalTime) * 100;

        // print total usage
        System.out.printf("\t%-8s: ", "Total");
        System.out.printf(" %d    %d    %.0f%%\n", totalTime, totalWaitTime, waitPercent);

    }


    // initiate acticity
    public static void initiate(Task task, String managerType) {

        int resource = task.activity.resource;
        int claim = task.activity.num;

        // Check if delay is present
        if (task.curDelay > 0) {

            task.isDelayed = true;
            task.curDelay -= 1;

            if (task.curDelay == 0) { task.isDelayed = false; }
            nonblockedTasks.add(task);

            // Run initiate procedure if no delay is present
        } else {

            // Update the task's resource claims
            task.updateClaim(resource, claim);

            // Check if claim is higher than resources available; abort if so
            if (managerType.equals("banker")) {
                for (int i = 0; i < R; i++) {
                    if (task.wanting[i] > resources[i]) {
                        task.isAborted = true;
                    }
                }
            }

            // Finish initiate procedure if abortion is unnecessary
            if (task.isAborted == false) {
                task.nextActivity();
                task.curDelay = task.activity.delay;
                nonblockedTasks.add(task);

            }

        }

    }

    // Returns true if safe state preserved, false if unsafe state found
    public static boolean isSafeState(int idRequesting, int resourceRequested, int resourceAmountRequested) {

        Task taskRequesting = new Task();

        // Create copy of active tasks (ensure no accidental modificiation of original data structure)
        ArrayList<Task> tasks = new ArrayList<Task>();
        for (int i = 0; i < readyList.size(); i++) {
            tasks.add(new Task());
            tasks.get(i).id = readyList.get(i).id;
            tasks.get(i).wanting = readyList.get(i).wanting;
            tasks.get(i).mataining = readyList.get(i).mataining;
        }

        /* Tag the active task that is requesting the resource */
        for (int i = 0; i < readyList.size(); i++) {
            if (idRequesting == readyList.get(i).id) {
                taskRequesting.id = readyList.get(i).id;
                taskRequesting.wanting = readyList.get(i).wanting;
                taskRequesting.mataining = readyList.get(i).mataining;
            }
        }

        // Create copy of resources
        // ensure no modification of original data structure
        int[] availableResources = new int[R];
        for (int i = 0; i < R; i++) availableResources[i] = resources[i];

        // grant resource request
        // does not actually grant, but will be used to check if granting the request can maintain a safe state
        availableResources[resourceRequested] -= resourceAmountRequested;
        taskRequesting.wanting[resourceRequested] -= resourceAmountRequested;
        taskRequesting.mataining[resourceRequested] += resourceAmountRequested;

        // Check if the resource request leads to a safe (return true) or unsafe state (return false)
        boolean safeStatePossible = true; //evaluates if a safe state is possible
        boolean completable = false; //evaluates if a certain task is completable

        // If the while loop is completed and tasks.size() > 0, then we have an unsafe state
        while (safeStatePossible == true) {
            safeStatePossible = false; //Unsafe state until proven otherwise

            for (int i = 0; i < tasks.size(); i++) {
                completable = true; // Task is completable until proven otherwise

                //Check if the current task is completable
                for (int j = 0; j < R; j++) {
                    if (tasks.get(i).wanting[j] > availableResources[j]) {
                        completable = false;
                    }
                }

                //If at least one task is completable, the simulation could be in a safe state
                if (completable == true) {

                    //Report potential safe state
                    safeStatePossible = true;

                    //Give resources back (simulate resource return)
                    for (int j = 0; j < R; j++) {
                        availableResources[j] += tasks.get(i).mataining[j];
                    }

                    //Remove "finished" task from the arraylist
                    tasks.remove(tasks.get(i));

                }

            }

            //Check is the tasks arraylist is empty; if so, the simulation state is safe
            if (tasks.size() == 0) { return(true); }
        }

        // If this line is reached, then safeStatePossible=false and tasks.size() > 0. UNSAFE!
        return(false);
    }

    // request activity (optimistic)
    public static void request1(Task task) {

        int resource = task.activity.resource;
        int unitsRequested = task.activity.num;
        int unitsAvailable = resources[resource];

        // Check if delay is present
        if (task.curDelay > 0) {

            task.isDelayed = true;
            task.curDelay -= 1;

            if (task.curDelay == 0) { task.isDelayed = false; }
            nonblockedTasks.add(task);

            // Run request procedure if no delay is present
        } else {

            // Request units
            // Grant request
            if (unitsAvailable >= unitsRequested) {
                resources[resource] -= unitsRequested;
                task.getUnits(resource, unitsRequested);
                task.nextActivity();
                task.curDelay = task.activity.delay;

                nonblockedTasks.add(task);
                // Reject request
            } else {
                task.waitTime += 1;
                blockedTasks.add(task);
            }

        }

    }

    // request activity(banker)
    public static void request2(Task task) {

        int resource = task.activity.resource;
        int unitsRequested = task.activity.num;
        int unitsAvailable = resources[resource];
        int maxSafeUnits = -1;

        // Check if delay is present
        if (task.curDelay > 0) {

            task.isDelayed = true;
            task.curDelay -= 1;

            if (task.curDelay == 0) { task.isDelayed = false; }
            nonblockedTasks.add(task);

            // Run request procedure if no delay is present
        } else {

            // Check if the request is illegal (would make units owned higher than units claimed)
            // If request is illegal, task should be isAborted
            if (unitsRequested > task.wanting[resource]) {
                task.isAborted = true;
            }

            // Check if the request is unsafe
            int unitsNeeded = task.wanting[resource];
            int unitsOwned = task.mataining[resource];
            boolean isSafeRequest = isSafeState(task.id, resource, unitsRequested);
            task.wanting[resource] = unitsNeeded;
            task.mataining[resource] = unitsOwned;

            // Request units
            if (task.isAborted == false) {
                // Grant request
                if ((unitsAvailable >= unitsRequested) && (isSafeRequest == true)) {
                    resources[resource] -= unitsRequested;
                    task.getUnits(resource, unitsRequested);
                    task.nextActivity();
                    task.curDelay = task.activity.delay;
                    nonblockedTasks.add(task);
                    // Reject request (unsafe)
                } else if ((unitsAvailable >= unitsRequested) && (isSafeRequest == false)) {
                    task.waitTime += 1;
                    blockedTasks.add(task);
                    // Reject request (unavailable)
                } else {
                    task.waitTime += 1;
                    blockedTasks.add(task);
                }
            } else {
                task.isAborted = true;
                task.terminate = true;
                blockedTasks.add(task);
            }

        }

    }

    // release activity
    public static void release(Task task) {

        int delay = task.activity.delay;
        int resourceId = task.activity.resource;
        int unitsToSend = task.activity.num;
        int unitsOwned = task.mataining[resourceId];

        // Check if delay is present
        if (task.curDelay > 0) {

            task.isDelayed = true;
            task.curDelay -= 1;

            if (task.curDelay == 0) { task.isDelayed = false; }
            nonblockedTasks.add(task);

            // Run release procedure if no delay is present
        } else {

            /*
            Request units
            Accept release
            */
            if (unitsOwned >= unitsToSend) {
                resourcesArray[resourceId] += unitsToSend;
                task.releaseUnits(resourceId, unitsToSend);
                task.nextActivity();
                task.curDelay = task.activity.delay;
                nonblockedTasks.add(task);
                // Reject release
            } else {
                task.waitTime += 1;
                blockedTasks.add(task);
            }

        }

    }

    // terminate activity
    public static void terminate(Task task, int endCycle) {


        // Check if delay is present
        if (task.curDelay > 0) {

            task.isDelayed = true;
            task.curDelay -= 1;

            if (task.curDelay == 0) { task.isDelayed = false; }
            nonblockedTasks.add(task);
            // Run terminate procedure if no delay is present
        } else {

            task.terminate = true;
            task.takenTime = endCycle;

        }
    }

    // detect and resolve deadlock
    public static void detectDeadlock() {

        int numAbortions = blockedTasks.size() - 1;
        int lowestID = Integer.MAX_VALUE; //Arbitrary number to guarantee all found ID's are lower

        // Abort all tasks but the highest (highest ID)
        for (int i = 0; i < numAbortions; i++) {

            lowestID = 100000;

            // Determine the lowest ID within the blocked tasks
            for (int j = 0; j < blockedTasks.size(); j++) {
                if (blockedTasks.get(j).id < lowestID) {
                    lowestID = blockedTasks.get(j).id;
                }
            }

            // Abort the task that possesses the lowest ID
            for (int j = 0; j < blockedTasks.size(); j++) {
                if (blockedTasks.get(j).id == lowestID) {

                    blockedTasks.get(j).isAborted = true;
                    blockedTasks.get(j).complete = true;
                    blockedTasks.get(j).terminate = true;

                    //Release all resources
                    for (int k = 0; k < R; k++) {
                        resourcesArray[k] += blockedTasks.get(j).mataining[k];
                        blockedTasks.get(j).mataining[k] = 0;
                    }

                    blockedTasks.remove(j);
                    break;
                }
            }
        }


    }

    // Run the banker simulation
    public static void bankerManager() {

        Task curTask = new Task();

        // Initialize all tasks as active
        readyList.addAll(Arrays.asList(tasks));

        // Perform simulation
        int curCycle = 0;


        while (readyList.size() > 0) {


            // Perform a cycle
            for (int i = 0; i < readyList.size(); i++) {

                curTask = readyList.get(i);


                // Perform appropriate activity procedure
                if (curTask.activity.type.equals("initiate")) {
                    initiate(curTask, "banker");
                } else if (curTask.activity.type.equals("request")) {
                    request2(curTask);
                } else if (curTask.activity.type.equals("release")) {
                    release(curTask);
                } else if (curTask.activity.type.equals("terminate")) {
                    terminate(curTask, curCycle);
                } else {
                }


            }

            // Resolve deadlock
            if ((blockedTasks.size() > 0) && (nonblockedTasks.size() == 0)) {

                // Check if a task has already been isAborted first
                boolean taskAlreadyAborted = false;
                for (int j = 0; j < blockedTasks.size(); j++) {
                    if (blockedTasks.get(j).isAborted == true) {
                        taskAlreadyAborted = true;

                        //Return resources
                        for (int k = 0; k < R; k++) {
                            resources[k] += blockedTasks.get(j).mataining[k];
                        }

                        blockedTasks.remove(blockedTasks.get(j));
                        break;
                    }
                }

                // Resolve deadlock if no task has been isAborted
                if (taskAlreadyAborted == false) {
                    detectDeadlock();
                }
            }

            //pending resource gains
            for (int i = 0; i < R; i++) {
                resources[i] += resourcesArray[i];
                resourcesArray[i] = 0;
            }

            // readyList for next cycle
            readyList.clear();
            readyList.addAll(blockedTasks);
            readyList.addAll(nonblockedTasks);
            blockedTasks.clear();
            nonblockedTasks.clear();

            // Increase the time
            curCycle += 1;

        }


    }

    // Run the optimistic manager
    public static void runOptimistic() {

        Task curTask = new Task();

        // Initialize all tasks as active
        for (int i = 0; i < tasks.length; i++) {
            readyList.add(tasks[i]);
        }

        // Perform simulation
        int curCycle = 0;


        while (readyList.size() > 0) {



            // Perform a cycle
            for (int i = 0; i < readyList.size(); i++) {

                curTask = readyList.get(i);



                // Perform appropriate activity procedure
                switch (curTask.activity.type) {
                    case "initiate": initiate(curTask, "optimistic"); break;
                    case "request": request1(curTask); break;
                    case "release": release(curTask); break;
                    case "terminate": terminate(curTask, curCycle); break;
                    default: break;
                }

            }

            // figure out deadlock
            if ((blockedTasks.size() > 0) && (nonblockedTasks.size() == 0)) {
                detectDeadlock();
            }

            // Process pending resource gains
            for (int i = 0; i < R; i++) {
                resources[i] += resourcesArray[i];
                resourcesArray[i] = 0;
            }

            // Arrange readyList order for next cycle
            readyList.clear();
            readyList.addAll(blockedTasks);
            readyList.addAll(nonblockedTasks);
            blockedTasks.clear();
            nonblockedTasks.clear();

            // Increment the cycle
            curCycle += 1;

        }


    }





}


class Task {

    List<Activity> activities;
    Activity activity;
    int activityIndex;



    int id;
    int curDelay;
    int takenTime;
    int waitTime;
    double waitPercent;

    /**
     *  Task has owned some resources
     *  and need some other resources
     */
    int[] mataining;
    int[] wanting;

    boolean terminate;
    boolean isAborted;
    boolean isDelayed;
    boolean complete;

    // Empty constructor
    public Task() {

        activities = new ArrayList<Activity>();
        activity = new Activity();
        activityIndex = 0;

        mataining = new int[0];
        wanting = new int[0];

        id = -1;
        curDelay = 0;
        takenTime = 0;
        waitTime = 0;
        waitPercent = 0;

        terminate = false;
        isAborted = false;
        isDelayed = false;
        complete = false;

    }

    // Constructor to initialize the activities array list and resources owned/needed arrays
    public Task(int id, int numResourceTypes) {
        activities = new ArrayList<>();
        activity = new Activity();
        activityIndex = 0;
        curDelay = 0;

        mataining = new int[numResourceTypes];
        wanting = new int[numResourceTypes];

        for (int i = 0; i < numResourceTypes; i++) {
            mataining[i] = 0;
            wanting[i] = 0;
        }

        this.id = id;

    }

    // Add an activity to the task
    public void addActivity(String type, int delay, int resource, int value) {
        activities.add(new Activity(type,delay,resource,value));
    }

    // Move on to the next activity
    public void nextActivity() {
        activityIndex += 1;
        activity = activities.get(activityIndex);
    }

    // Update the task's current possession of a resource type
    public void getUnits(int resourceIndex, int units) {
        mataining[resourceIndex] += units;
        wanting[resourceIndex] -= units;
    }

    // Update the task's current possession of a resource type
    public void releaseUnits(int resourceType, int units) {
        mataining[resourceType] -= units;
        wanting[resourceType] += units;
    }

    // Update the task's claim for a resource type
    public void updateClaim(int resource, int value) {
        wanting[resource] = value;
    }

    // Abort the task
    public void abort() {
        isAborted = true;
    }

    // Compute stats for the task
    public void terminate(int endCycle) {

        takenTime = endCycle;
        terminate = true;
    }

    /**
     * print standard output result to screen
     */
    public void output() {

        waitPercent = ((double)waitTime / (double) takenTime) * 100;

        if (isAborted == false) {
            System.out.printf("\t%-8s: ", ("Task " + id));
            System.out.printf(" %d    %d    %.0f%%\n", takenTime, waitTime, waitPercent);
        } else {
            System.out.printf("\t%-8s: ", ("Task " + id));
            System.out.println("Aborted");
        }

    }


}

class Activity {

    String type;
    int delay;
    int resource;
    int num;


    public Activity() {
        type = "";
        delay = 0;
        resource = 0;
        num = 0;
    }

    //Constructor with three parameters
    public Activity(String activity, int delay, int resource, int num) {
        this.type = activity;
        this.delay = delay;
        this.resource = resource;
        this.num = num;
    }


}
