
public class PM {
	public int id;
	public int cpuCap;
	public int ramCap;
	public int cpuUsage;
	public int ramUsage;
	public int vmNumber;
	public boolean isOverloaded;
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
	}
	
	public PM(int id,int cpuCap,int ramCap,int cpuUsage,int ramUsage, int vmNumber, boolean isOverloaded){
		this.id = id;
		this.cpuCap = cpuCap;
		this.ramCap = ramCap;
		this.cpuUsage = cpuUsage;
		this.ramUsage = ramUsage;
		this.vmNumber = vmNumber;
		this.isOverloaded = isOverloaded;
	}
	
}