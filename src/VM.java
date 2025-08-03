package vmpproblem;

public class VM {
	public int id;
	public int cpuReq;
	public int ramReq;
	public int pmAssignedTo;
	
	public static void main(String[] args){

	}
	
	public VM(int id ,int cpuReq,int ramReq,int pmAssignedTo){
		this.id = id;
		this.cpuReq = cpuReq;
		this.ramReq = ramReq;
		this.pmAssignedTo = pmAssignedTo;
	}
	
}
