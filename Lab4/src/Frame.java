public class Frame {

    int id;
    boolean free;
    int firstUse;
    int mostRecentUse;
    Process savedProcess;
    int savedPage;

    int resTime;


    public Frame() {
        id = -1;
        free = true;
        firstUse = -1;
        mostRecentUse = -1;
        savedProcess = null;
        savedPage = -1;
        resTime = 0;
    }

    Frame(int id) {
        this.id = id;
        free = true;
        firstUse = -1;
        mostRecentUse = -1;
        savedProcess = null;
        savedPage = -1;
        resTime = 0;
    }

    // insert when there has free frame
    public void freeInsert(Process process, int time) {
        process.curResStart = time;
        free = false;
        firstUse = time;
        mostRecentUse = time;
        savedProcess = process;
        savedPage = process.curPage;
    }

    // evict the process in the frame and replace by a new one
    public void evictInsert(Process newProcess, int time) {

        //savedProcess.curResEnd = time;

        this.savedProcess.totalResTime += this.resTime;

        this.resTime = 0;
        savedProcess.numEvictions += 1;

        free = false;
        firstUse = time;
        mostRecentUse = time;
        savedProcess = newProcess;
        savedProcess.curResTime = 0;
        savedPage = newProcess.curPage;

    }

}