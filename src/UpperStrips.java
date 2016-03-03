import com.comsol.model.Model;

public class UpperStrips extends TPC {

	public static Model run(){
		return new TPCSingle().model;
	}	
	
	public void addGroundStrips(){                    
		double z1 = -electrodeThickness+TPCLength()+2*electrodeThickness+insulationwidth; //z position of first strip  
		double r1 = beampiperadius+groundstripwidth+wallwidth+insulationwidth;            //r position of first strip    
		
		this.addRect("TopGroundStrip",r1,z1,FSELength,groundstripwidth);     //Create First Ground Strip
	//	this.makeFSEArray(offsetz(),new String[]{"FSE1Rect"},FSENumber); //Array of Ground strip rectangles
	}
	
	//Need to add method here to ground all strips
	
	
}
