import com.comsol.model.Model;

public class TPCSingle extends TPC {	
	public double FSEOuterRadius(){
		return TPCRadius + FSEThickness;
	}
	
	
	
	public static void main(String[] args){
		run();
	}
	public static Model run(){
		return new TPCSingle().model;
	}
	
	public void addFSEs(){
		double z1 = FSEzSpacing+FSELength/2;
		double r1 = TPCRadius;
		
		this.addRect("FSE1Rect",r1,z1,FSEThickness,FSELength);
		this.makeFSEArray(offsetz(),new String[]{"FSE1Rect"},FSENumber);
	}

	public void makeFSESelection(int actualNumber){
		String name = "FSE"+actualNumber+"Selection";
		
		double rmin = TPCRadius - FSErSpacing/4;
		double rmax = rmin + FSEThickness + FSErSpacing/2;
		
		double z1 = FSEzSpacing+FSELength/2;

		double zmin = z1 + offsetz()*actualNumber - FSEzSpacing/4;
		double zmax = zmin + FSELength + FSEzSpacing/2;
		
		this.makeBoxSelection(name,rmin,zmin,rmax,zmax);
	}

}