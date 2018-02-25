class Process implements Comparable<Process> {

	public int a;
	public int b;
	public int c;
	public int io;


	public int remainingCPU;
	public int remainingCPUBurst;
	public int remainingIOBurst;

	public boolean isRunning;
	public boolean isBlocked;
	public int curWaitingTime;

	public int finishingTime;
	public int turnaroundTime;
	public int ioTime;
	public int waitingTime;
	public int processID;

	public String status;

    private int Finish=0;

    public void decC(){
        this.c--;
    }

    public void setIOT(int IOT) {
        this.IOT = IOT;
    }
    public void incIOT(int i){
        this.IOT+=i;
    }
    public void incWait(int i){
        this.Wait+=i;
    }

    private int IOT=0;
    private int Wait=0;



	public Process(int arrivalTime, int maxCPUBurst, int cpuTime, int maxIOBurst) {
		a = arrivalTime;
		b = maxCPUBurst;
		c = cpuTime;
		io = maxIOBurst;
		processID = 0;

		remainingCPU = c;
		remainingCPUBurst = 0;
		remainingIOBurst = 0;
		isRunning = false;
		isBlocked = false;
		curWaitingTime = 0;

		finishingTime = 0;
		turnaroundTime = 0;
		ioTime = 0;
		waitingTime = 0;

		status = "unstarted";
	}

	public int getA() { return(a); }
	public int getB() { return(b); }
	public int getC() { return(c); }
	public int getIO() { return(io); }

	public int getFTime() { return(finishingTime); }
	public int getTTime() { return(turnaroundTime); }
	public int getIOTime() { return(ioTime); }
	public int getWTime() { return(waitingTime); }

	public void setA(int newA) { a = newA; }
	public void setB(int newB) { b = newB; }
	public void setC(int newC) { c = newC; }
	public void setIO(int newIO) { io = newIO; }
	public void setStatus(int status){
	    switch (status){
            case -2:
                this.status = "terminated";
                break;
            case -1:
                this.status = "unstarted";
                break;
            case 0:
                this.status = "ready";
                break;
            case 1:
                this.status = "running";
                break;
            case 2:
                this.status = "blocked";
                break;
        }
    }

    public String getStatus() {
//       switch (this.status){
//           case "terminated":
//               return -2;
//               break;
        return  this.status;

    }

	public void setFTime(int newF) { finishingTime = newF; }
	public void setTTime(int newT) { turnaroundTime = newT; }
	public void setIOTime(int newIO) { ioTime = newIO; }
	public void setWTime(int newW) { waitingTime = newW; }

    public int getFinish() {
        return Finish;
    }
    public void setFinish(int finish) {
        Finish = finish;
    }

    public int getWait() {
        return Wait;
    }
    public void setWait(int wait) {
        Wait = wait;
    }
    public int getIOT() {
        return IOT;
    }

    public int compareTo(Process a){
        if(a.a>this.a)return -1;
        else if(a.a<this.a)return 1;
        else if(a.b>this.b)return -1;
        else if(a.b<this.b)return 1;
        else if(a.c>this.c)return -1;
        else if(a.c<this.c)return 1;
        else if(a.io>this.io)return -1;
        else if(a.io<this.io)return 1;
        else return 0;
    }


}