import com.comsol.model.Model;

public class TPCMirror extends TPC {	
	public double TPCLength(){
		return (FSELength + FSEzSpacing) * (FSENumber + 1)/2;
	}
	public double offsetz(){return super.offsetz()/2;}
	public void setVariables(){
		this.FSENumber = this.FSENumber*2-1;
		this.Resistance = this.Resistance/2;
	}
	
	public static void main(String[] args){
		run();
	}
	public static Model run(){
		return new TPCMirror().model;
	}
	
	public void addFSEs(){
		double z1 = FSEzSpacing/2;
		double r1 = TPCRadius;
		double z2 = z1 + (FSELength+FSEzSpacing)/2;
		double r2 = r1 + FSEThickness +FSErSpacing;
		double r3 = innerTPCradius;
		double z3 = z2;
		double r4 = r3+r2-r1;
		double z4 = z1;
		
		this.addRect("FSE1Rect",r1,z1,FSEThickness,FSELength);
		this.addRect("FSE3Rect",r3,z3,FSEThickness,FSELength);
		this.addRect("FSE2Rect",r2,z2,FSEThickness,FSELength);
		this.addRect("FSE4Rect",r4,z4,FSEThickness,FSELength);
		this.makeFSEArray(2*offsetz(),new String[]{"FSE1Rect","FSE2Rect","FSE3Rect","FSE4Rect"},(FSENumber-1)/2);
		this.addRect("FSE"+FSENumber+"Rect", r1,z1+offsetz()*(FSENumber-1),FSEThickness,FSELength);
		this.addRect("InnerFSE"+FSENumber+"Rect", r4,z4+offsetz()*(FSENumber-1),FSEThickness,FSELength); // changed r1 z1 to r4 z4 in attempt to create upper strip
	}
	
	public void makeFSESelection(int actualNumber){
		String name = "FSE"+actualNumber+"Selection";
		String name1 = "InnerFSE"+actualNumber+"Selection";
		
		double rmin = TPCRadius - FSErSpacing/4;
		if (actualNumber%2 == 1){
			rmin = rmin + FSEThickness + FSErSpacing;
		}
		
		double rmin2 = innerTPCradius + FSEThickness+FSErSpacing - FSErSpacing/4;
		if (actualNumber%2 == 1){
			rmin2 = rmin2 - FSEThickness - FSErSpacing;
		}
	
		double rmax = rmin + FSEThickness + FSErSpacing/2;
		double rmax2 = rmin2 + FSEThickness + FSErSpacing/2;
		
		double z1 = FSEzSpacing/2;

		double zmin = z1 + offsetz()*actualNumber - FSEzSpacing/4;
		double zmax = zmin + FSELength + FSEzSpacing/2;
		
		this.makeBoxSelection(name,rmin,zmin,rmax,zmax);
		this.makeBoxSelection(name1,rmin2,zmin,rmax2,zmax);
	}

}