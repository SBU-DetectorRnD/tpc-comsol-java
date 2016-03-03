import com.comsol.model.Model;

public class TPCSingle extends TPC {	
	public double TPCLength(){
		return (FSELength + FSEzSpacing) * (FSENumber + 1)/2;
	}
	
	
	
	public static void main(String[] args){
		run();
	}
	public static Model run(){
		return new TPCSingle().model;
	}
	
	public void addFSEs(){                    //Add ground strips
		//Create first upper grounding strip
		double z1 = FSEzSpacing+FSELength/2;  // z1=-electrodeThickness+TPCLength()+2*electrodeThickness+insulationwidth
		double r1 = TPCRadius;                // r1=beampiperadius+groundstripwidth+wallwidth+insulationwidth
		
		this.addRect("FSE1Rect",r1,z1,FSEThickness,FSELength);           //Ground Strip Rectangle
		this.makeFSEArray(offsetz(),new String[]{"FSE1Rect"},FSENumber); //Array of Ground strip rectangles
	}

	public void makeFSESelection(int actualNumber){  //make ground strips grounded.
		String name = "FSE"+actualNumber+"Selection";
		
		double rmin = TPCRadius - FSErSpacing/4;
		double rmax = rmin + FSEThickness + FSErSpacing/2;
		
		double z1 = FSEzSpacing+FSELength/2;

		double zmin = z1 + offsetz()*actualNumber - FSEzSpacing/4;
		double zmax = zmin + FSELength + FSEzSpacing/2;
		
		this.makeBoxSelection(name,rmin,zmin,rmax,zmax);
	}

}