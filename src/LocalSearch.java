
import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;

public class LocalSearch {
	
	public static int instanceNum;
	public static String instanceName;
	
	public static int pmNum;
	public static int cpuCapability;
	public static int ramCapability;
	public static int vmNum;
	
	public static int pmANum;
	public static int pmBNum;
	public static int pmACpuCapability;
	public static int pmARamCapability;
	public static int pmBCpuCapability;
	public static int pmBRamCapability;
	
	public static int thisBest;
	public static int totalBest;
	public static int lowerBound;
	
	public static VM vm[];
	public static PM pm[];
	
	public static int[][] tabu;
	public static final int TABULENGTH = 20;
	public static int iter = 0;
	public static int totalIter = 1000000;
	public static int iterBest = 0;

	public static int restartTimes = 0;
	public static int restartLimit = 20;
	public static boolean toRestart = false;
	public static boolean cpuol = false;	
	public static boolean ramol = false;
	public static boolean bnisram = false;
	public static boolean bniscpu = false;
	
	public static int maxRunningTime = 5000;	//ms
	public static boolean timeout = false;
	
	public static long runTime = 0;
	public static double averTime = 0;
	public static double sqTotal = 0;
	public static double averSQ = 0;
	
	public static int errorNum = 0;	
	
	public static void main(String[] args) throws IOException {
		
//		PrintStream ps = new PrintStream("E:\\workspace\\vmp\\10times20tl.txt");
//		System.setOut(ps);

		String baseFolder = "E:\\workspace\\vmp\\VMP_instance";
		File file = new File(baseFolder);
		if(file.isDirectory()) {

			System.out.println("SET" +  "\t"+ "\t" + "instanceNumber" + "\t" + "averageTime(ms)" + "\t" + "averSQ");
			System.out.println("------------------------------------------------------------------------");
			String[] list = file.list();
			for(int i=0;i<list.length;i++) {
				File file2 = new File(baseFolder+"\\"+list[i]);
				if(file2.isDirectory()) {
					String str = baseFolder+"\\"+list[i];
					run(str);
					System.out.println(list[i] + "\t" + instanceNum + "\t"+ "\t" + averTime + "\t" + "\t" + averSQ);
				}
			}
		}else {
			System.out.println(baseFolder+" is not a folder.");
		}
		
//		ps.close();
		
	}
	
	public static void run(String str) throws IOException{
		runTime = 0;
		sqTotal = 0;
		averTime = 0;
		averSQ = 0;
		instanceNum = 0;
		bniscpu = false;
		bnisram = false;
		
		String folderName = str;
		
		File folder = new File(folderName);
		if(folder.exists()){
//			System.out.println("The folder exists.");
		}else{
			System.out.println("The folder doesn't exist.");
		}

		File[] files = folder.listFiles();
		for (File file : files){
			if(file.isFile()){
				
				long stime = System.currentTimeMillis();

				iter = 0;
				instanceNum += 1;
				File f = file;
				
				boolean typeC = folderName.contains("C");
				boolean typeB = folderName.contains("B");
				if(typeC == true){
					initializeC(f);
					bnisram = true;
				}else if(typeB == true){
					initialize(f);
					bniscpu = true;
				}else{
					initialize(f);
				}
				
				calcuBound();
				labelouter:
				while(totalBest != lowerBound){					
					
					if(toRestart == true){	
						restart(totalBest);
						checkPM();
						restartTimes = 0;
						toRestart = false;
					}else{
						nonSolution(totalBest);
						checkPM();
					}

					while(isFeasible() == false){
					
						exchange();
						migrate();
						checkPM();
						
						if(System.currentTimeMillis() - stime >= maxRunningTime){
							timeout = true;
							break labelouter;
						}
						
						if(iter == totalIter){
							break labelouter;
						}	
						if((iter -iterBest) == 3000){
							break labelouter;
						}
						if(toRestart == true){
							continue labelouter;
						}
					}
					
					if(System.currentTimeMillis() - stime >= maxRunningTime){
						timeout = true;
						break labelouter;
					}
					
					if(isFeasible() == true){
						calcuNum();
						if(thisBest < totalBest){
							totalBest = thisBest;
							iterBest = iter;

							continue labelouter;
						}
					}
				}
				
				if(totalBest == lowerBound){
//					System.out.println("------------------");	
//					System.out.println("The  " + iter + "-th iteration get the lower bound :" + totalBest);
				}else if(timeout == true){
//					System.out.println("Timeout:" + maxRunningTime);
//					System.out.println("totalBest£º" + totalBest);
//					System.out.println("lowerBound£º" + lowerBound);								
					timeout = false;
				}else{
//					notReachedNum += 1;
//					totalDistance = totalDistance + (totalBest - lowerBound);
//					System.out.println("----------here--------");
//					System.out.println("Already" + 3000 + "iterations.");
				}
				double s = (float)(totalBest - lowerBound)/lowerBound;
		        double per = 100;
		        double sq = s*per;
				sqTotal += sq;
				
				long etime = System.currentTimeMillis();
				runTime  = runTime + (etime - stime);	
			}		
		}

		averTime = (runTime*1.0)/(instanceNum*1.0);
        BigDecimal bd1 = BigDecimal.valueOf(averTime);
        bd1 = bd1.setScale(2, RoundingMode.HALF_UP);
		averTime = bd1.doubleValue();
        
		averSQ = sqTotal/instanceNum;
        BigDecimal bd = BigDecimal.valueOf(averSQ);
        bd = bd.setScale(4, RoundingMode.HALF_UP);
        averSQ = bd.doubleValue();   

//		System.out.println("average execution time £º" + averTime + "ms");
//		System.out.println("average solution quality £º" + averSQ + "%");
//		System.out.println();
				
	}
    
	public static void initialize(File file)throws IOException{
		File f = file;
		readFile(f);

		tabu = new int[vmNum][vmNum];
		totalBest = pmNum;
		
		initialSolution();
				
		calcuNum();
		if(thisBest < totalBest){
			totalBest = thisBest;
		}
	}

	public static void initializeC(File file)throws IOException{
		File f = file;
		readFileC(f);
		
		tabu = new int[vmNum][vmNum];
		totalBest = pmNum;
		
		initialSolution();
		
		calcuNum();
		if(thisBest < totalBest){
			totalBest = thisBest;
		}
	}
	
	public static void readFile(File file) throws IOException{

		File f = file;
		BufferedReader br = new BufferedReader(new FileReader(f));
		
		String line;
		String[] arrs1 = null;
		String[] arrs2 = null;
		String[] arrs3 = null;
		String[] arrs4 = null;
		String[] arrs5 = null;
		String[] arrs6 = null;
		
		line = br.readLine();
		arrs1 = line.split(" ");
		instanceName = arrs1[0];
		
		line = br.readLine();
		arrs2 = line.split(" ");
		pmNum = Integer.parseInt(arrs2[0]);
		
		line = br.readLine();
		arrs3 = line.split(" ");
		cpuCapability = Integer.parseInt(arrs3[0]);
		
		line = br.readLine();
		arrs4 = line.split(" ");
		ramCapability = Integer.parseInt(arrs4[0]);
		
		line = br.readLine();
		arrs5 = line.split(" ");
		vmNum = Integer.parseInt(arrs5[0]);
		
		
		pm = createPMArrs(pmNum);
		for(int j = 0;j < pmNum ; j++){
			pm[j].id = j;
			pm[j].cpuCap = cpuCapability;
			pm[j].ramCap = ramCapability;
		}

		vm = createVMArrs(vmNum);
		
		int i = 0;
		while((line = br.readLine()) != null){
			arrs6 = line.split(" ");
			int x = Integer.parseInt(arrs6[0]);
			int y = Integer.parseInt(arrs6[1]);

			vm[i].id = i;
			vm[i].cpuReq = x;
			vm[i].ramReq = y;
			i++;
		}

		br.close();
	}
	

	public static void readFileC(File file) throws IOException{

		BufferedReader br = new BufferedReader(new FileReader(file));
        String line;
		String[] arrs1 = null;
		String[] arrs2 = null;
//		String[] arrs3 = null;
//		String[] arrs4 = null;
		String[] arrs5 = null;
		String[] arrs6 = null;
		
		line = br.readLine();
		arrs1 = line.split(" ");
		instanceName = arrs1[0];
		
		line = br.readLine();
		arrs2 = line.split(" ");
		String numString = arrs2[0];//arrs2[0]:"90,10"
		 
		for (int i = 0; i < numString.length(); i++) {
			String numStr= "";
			while (Character.isDigit(numString.charAt(i))) {
				numStr= numStr.concat(Character.toString(numString.charAt(i)));
				i++;
			}
			if(numString.charAt(i) == ','){
				pmANum = Integer.parseInt(numStr);
				pmBNum  = pmANum/9;
				break;
			}
		}
		pmNum = pmANum + pmBNum;
		
		line = br.readLine();
		pmACpuCapability = 16;
		pmARamCapability = 32;
		
		line = br.readLine();
		pmBCpuCapability = 32;
		pmBRamCapability = 128;
		
		line = br.readLine();
		arrs5 = line.split(" ");
		vmNum = Integer.parseInt(arrs5[0]);
		
		pm = createPMArrs(pmNum);
		
		for(int j = 0;j < pmNum ; j++){
			if(j < pmBNum){
				pm[j].id = j;
				pm[j].cpuCap = pmBCpuCapability;
				pm[j].ramCap = pmBRamCapability;
			}else{
				pm[j].id = j;
				pm[j].cpuCap = pmACpuCapability;
				pm[j].ramCap = pmARamCapability;
			}
		}
		
		vm = createVMArrs(vmNum);
		
		int i = 0;
        while((line = br.readLine()) != null){
        	arrs6 = line.split(" "); 
			int x = Integer.parseInt(arrs6[0]);
			int y = Integer.parseInt(arrs6[1]);
			
			if(y > 32){
				errorNum = errorNum + 1;
				y = 32;
			}
			
			vm[i].id = i;
			vm[i].cpuReq = x;
			vm[i].ramReq = y;
			i++;
        }
		br.close();	
	}
	
	public static PM[] createPMArrs(int t){
		pm = new PM[pmNum];
		
		for(int i = 0;i < pmNum ; i++){
			int id = 0;
			int cpuCap = 0;
			int ramCap = 0;
			int cpuUsage = 0;
			int ramUsage = 0;
			int vmNumber = 0;
			boolean isOverloaded = false;
			
			pm[i] = new PM(id,cpuCap,ramCap,cpuUsage,ramUsage,vmNumber,isOverloaded);
		}
		return pm;
	}
	
	public static VM[] createVMArrs(int t){
		vm = new VM[vmNum];
		
		for(int i = 0;i < vmNum ; i++){
			int id = 0;
			int cpuReq = 0;
			int ramReq = 0;
			int pmAssignedTo = 999;
			
			vm[i] = new VM(id,cpuReq,ramReq,pmAssignedTo);
		}
		return vm;
	}
	
	public static void initialSolution(){
		
		boolean isCpuAvalible = false;
		boolean isRamAvalible = false;
		
		for(int i = 0 ; i < vmNum; i++){
			for(int j = 0 ; j < pmNum ; j++){
				isCpuAvalible = false;
				isRamAvalible = false;
				if((pm[j].cpuCap - pm[j].cpuUsage) >= vm[i].cpuReq){
					isCpuAvalible = true; 
				}
				if((pm[j].ramCap - pm[j].ramUsage) >= vm[i].ramReq){
					isRamAvalible = true; 
				}
				if(isCpuAvalible && isRamAvalible){
					vm[i].pmAssignedTo = j;
					pm[j].cpuUsage = pm[j].cpuUsage + vm[i].cpuReq;
					pm[j].ramUsage = pm[j].ramUsage + vm[i].ramReq;
					pm[j].vmNumber += 1;
					break;
				}
			}
		}
	}
	
	public static void calcuNum(){
		int usedNum = 0;
		for(int j = 0; j < pmNum ; j++){
			if(pm[j].vmNumber != 0){
				usedNum = usedNum + 1;
			}
		}
		thisBest = usedNum;
	}
	
	public static void calcuBound(){
		int totalCpuReq = 0;
		int totalRamReq = 0;

		for(int i = 0; i < vmNum; i++){
			totalCpuReq += vm[i].cpuReq;
			totalRamReq += vm[i].ramReq;
		}
		
		boolean s = instanceName.contains("C");
		if(s == false){
			if(totalCpuReq/cpuCapability > totalRamReq/ramCapability){
				lowerBound = totalCpuReq/cpuCapability + 1;
			}else{
				lowerBound = totalRamReq/ramCapability + 1;
			}
		}else{
			int x = (totalCpuReq - (pmBNum*pmBCpuCapability))/pmACpuCapability;
			int y = (totalRamReq - (pmBNum*pmBRamCapability))/pmARamCapability;
			if(x >= y){
				lowerBound = pmBNum + x + 1;
			}else{
				lowerBound = pmBNum + y + 1;
			}
		}
		
	}
	
	public static void nonSolution(int k){
		int pmk = 0;
		int pmi = 0;
		int n = k - 1;
		Random random = new Random();
		for(int i = 0; i < vmNum; i++){
			pmi = vm[i].pmAssignedTo;
			if(pmi == n){
				pmk = random.nextInt(n);
				vm[i].pmAssignedTo = pmk;
				
				pm[pmi].cpuUsage -= vm[i].cpuReq;
				pm[pmi].ramUsage -= vm[i].ramReq;
				pm[pmi].vmNumber -= 1;
				pm[pmk].cpuUsage += vm[i].cpuReq;
				pm[pmk].ramUsage += vm[i].ramReq;
				pm[pmk].vmNumber += 1;
			}
		}
	}
	
	//perturbation
	public static void restart(int k){
		int pmk = 0;
		int pmi = 0;
		int n = k;
		Random random = new Random();

		for(int i = 0; i < vmNum; i++){
			pmi = vm[i].pmAssignedTo;
			if(pm[pmi].isOverloaded == true){
				pmk = random.nextInt(n - 1);	//[0,n-1)
				vm[i].pmAssignedTo = pmk;
				
				pm[pmi].cpuUsage -= vm[i].cpuReq;
				pm[pmi].ramUsage -= vm[i].ramReq;
				pm[pmi].vmNumber -= 1;
				pm[pmk].cpuUsage += vm[i].cpuReq;
				pm[pmk].ramUsage += vm[i].ramReq;
				pm[pmk].vmNumber += 1;
			}
		}
	}
	
	public static boolean isFeasible(){
		boolean isFeasible = true;
		for(int j = 0 ; j < pmNum ; j++){
			if(pm[j].isOverloaded == true){
				isFeasible = false;
				break;
			}
		}
		return isFeasible;
	}
	
	public static void checkPM(){
		for(int j = 0; j < pmNum; j++){
			if(pm[j].cpuUsage > pm[j].cpuCap | pm[j].ramUsage > pm[j].ramCap){
				pm[j].isOverloaded = true;
			}else{
				pm[j].isOverloaded = false;
			}
		}
	}
	
	public static int[] getIndex(){
		int[] index = new int[4];
		float[][] afterSpace = new float[vmNum][vmNum];
		int pm1;
		int pm2;
		int oc = 0;
		int or = 0;
		int bc = 0;
		int br = 0;
		int afterCpu1;
		int afterRam1;
		int afterCpu2;
		int afterRam2;
		boolean pm1CpuAva = false;
		boolean pm1RamAva = false;
		boolean pm2CpuAva = false;
		boolean pm2RamAva = false;
		

		for(int j = 0 ; j < pmNum ; j++){
			if(pm[j].isOverloaded == false){
				continue;
			}else{
				if(pm[j].ramUsage > pm[j].ramCap){
					ramol = true;
				}
				if(pm[j].cpuUsage > pm[j].cpuCap){
					cpuol  = true;
				}
				break;
			}
		}
		
		if(cpuol){oc = 1;}
		if(ramol){or = 1;}
		if(bniscpu){bc = 1;}
		if(bnisram){br = 1;}
		
		for(int i = 0 ; i < vmNum ; i++){
			pm1 = vm[i].pmAssignedTo;
			if(pm[pm1].isOverloaded == true){continue;}
			
			for(int j = 0 ; j < vmNum ; j++){
				pm2 = vm[j].pmAssignedTo;
				if(pm[pm2].isOverloaded == true){continue;}
				if(pm1 == pm2){
					continue;
				}
				pm1CpuAva = false;
				pm1RamAva = false;
				pm2CpuAva = false;
				pm2RamAva = false;
				
				afterCpu1 = pm[pm1].cpuUsage - vm[i].cpuReq + vm[j].cpuReq;
				afterRam1 = pm[pm1].ramUsage - vm[i].ramReq + vm[j].ramReq;
				afterCpu2 = pm[pm2].cpuUsage - vm[j].cpuReq + vm[i].cpuReq;
				afterRam2 = pm[pm2].ramUsage - vm[j].ramReq + vm[i].ramReq;
				
				if(afterCpu1 <= pm[pm1].cpuCap){
					pm1CpuAva = true;
				}
				if(afterRam1 <= pm[pm1].ramCap){
					pm1RamAva = true;
				}
				if(afterCpu2 <= pm[pm2].cpuCap){
					pm2CpuAva = true;
				}
				if(afterRam2 <= pm[pm2].ramCap){
					pm2RamAva = true;
				}
				int cpuRemain = pm[pm1].cpuCap - afterCpu1;
				int ramRemain = pm[pm1].ramCap - afterRam1;
				
				if(pm1CpuAva && pm1RamAva && pm2CpuAva && pm2RamAva && cpuRemain != 0 && ramRemain != 0){
						afterSpace[i][j] = (2+oc+bc)*cpuRemain + (1+or+br)*ramRemain;

				}
			}
		}
		float spaceBest = 0;
		for(int i = 0; i < vmNum; i++){
			for(int j = 0; j < vmNum; j++){
				if(tabu[i][j] >= iter){
					continue;
				}
				if(afterSpace[i][j] > spaceBest){
					spaceBest = afterSpace[i][j];
					index[0] = i;
					index[1] = j;
					index[2] = vm[i].pmAssignedTo;
					index[3] = vm[j].pmAssignedTo;
				}
			}
		}
		return index;
	}
	
	public static void exchange(){
		int[] index = new int[4];
		
		index = getIndex();
		
		int indexLeft = index[0];
		int indexRight = index[1];
		int temp = 0;
		int pm1 = index[2];
		int pm2 = index[3];

		//½»»»¹ý³Ì
		temp = pm1;
		vm[indexLeft].pmAssignedTo = pm2;
		vm[indexRight].pmAssignedTo = temp;
		
		pm[pm1].cpuUsage = pm[pm1].cpuUsage - vm[indexLeft].cpuReq + vm[indexRight].cpuReq;
		pm[pm1].ramUsage = pm[pm1].ramUsage - vm[indexLeft].ramReq + vm[indexRight].ramReq;
		pm[pm2].cpuUsage = pm[pm2].cpuUsage - vm[indexRight].cpuReq + vm[indexLeft].cpuReq;
		pm[pm2].ramUsage = pm[pm2].ramUsage - vm[indexRight].ramReq + vm[indexLeft].ramReq;
		int y = index[0];
		int z = index[1];
		tabu[y][z] = iter + TABULENGTH;
		tabu[z][y] = iter + TABULENGTH;
		iter += 1;	
		
	}
	
	public static void migrate(){
		int pmi = 1000;
		int pmj = 1000;
		int vmToInsert = 1000;
		boolean isCpuAvalible;
		boolean isRamAvalible;
		int[][] vmj = new int[30][2];
		
		while(pmi == 1000){
			Random random = new Random();
			int temp = random.nextInt(totalBest - 1);
			if(pm[temp].isOverloaded == true){
				pmi = temp;
				if(pm[pmi].ramUsage > pm[pmi].ramCap){
					ramol = true;
				}
				if(pm[pmi].cpuUsage > pm[pmi].cpuCap){
					cpuol  = true;
				}
				break;
			}
		}
		

		int oc = 0;
		int or = 0;
		if(cpuol){oc = 1;}
		if(ramol){or = 1;}
		int index = 0;
		for(int i = 0 ; i < vmNum ; i++){
			if(vm[i].pmAssignedTo == pmi ){
				vmj[index][0] = i;
				vmj[index][1] = (2+oc)*vm[i].cpuReq + (1+or)*vm[i].ramReq;	
				index++;
			}
		}

		int temp1 = 0;
		for(int i = 0 ; i < vmj.length ; i++){
			if(vmj[i][1] >= temp1){
				temp1 = vmj[i][1];
				vmToInsert = vmj[i][0];
			}
		}

		for(int j = 0 ; j < totalBest - 1 ; j++){
			if(pm[j].isOverloaded == true){
				continue;
			}
			isCpuAvalible = false;
			isRamAvalible = false;
			if((pm[j].cpuCap - pm[j].cpuUsage) >= vm[vmToInsert].cpuReq){
				isCpuAvalible = true; 
			}
			if((pm[j].ramCap - pm[j].ramUsage) >= vm[vmToInsert].ramReq){
				isRamAvalible = true; 
			}
			if(isCpuAvalible && isRamAvalible){			
				pmj = j;
				break;
			}
		}
		
		
		if(pmj == 1000){
			outer:
			for(int i = 0 ; i < vmNum ; i++){
				int temp = vm[i].pmAssignedTo;
				if(pm[temp].isOverloaded == true){		
					vmToInsert = i;
					pmi = temp;
					for(int k = 0 ; k < totalBest - 1 ; k++){
						if(pm[k].isOverloaded == true){
							continue;
						}
						isCpuAvalible = false;
						isRamAvalible = false;
						if((pm[k].cpuCap - pm[k].cpuUsage) >= vm[vmToInsert].cpuReq){
							isCpuAvalible = true; 
						}else{continue;}
						if((pm[k].ramCap - pm[k].ramUsage) >= vm[vmToInsert].ramReq){
							isRamAvalible = true; 
						}else{continue;}
						if(isCpuAvalible && isRamAvalible){
							pmj = k;
							break outer;
						}
					}
				}
			}
		}
		
		if(pmj == 1000){
			restartTimes++;
			iter = iter + 1;
			if(restartTimes == restartLimit){
				toRestart = true;
			}
			return;
		}
		vm[vmToInsert].pmAssignedTo = pmj;
		pm[pmi].cpuUsage = pm[pmi].cpuUsage - vm[vmToInsert].cpuReq;
		pm[pmi].ramUsage = pm[pmi].ramUsage - vm[vmToInsert].ramReq;
		pm[pmi].vmNumber = pm[pmi].vmNumber - 1;
		
		pm[pmj].vmNumber = pm[pmj].vmNumber + 1;
		pm[pmj].cpuUsage = pm[pmj].cpuUsage + vm[vmToInsert].cpuReq;
		pm[pmj].ramUsage = pm[pmj].ramUsage + vm[vmToInsert].ramReq;

		iter = iter + 1;		
	}

}
