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
	
	public TPCMirror(){      //I think this is necessary when this.makeCircuit, this.* not in TPC.java
		this.makeSelections();
		this.makeTerminals();
		this.makeCircuit(); 
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

	public void makeSelections(){
		this.makeAnodeSelection();
		this.makeCathodeSelection();
		this.makeGroundStripSelection("groundstripone",beampiperadius);
		this.makeGroundStripSelection("groundstriptwo",beampiperadius+groundstripwidth+wallwidth);
		this.makeGroundStripSelection("groundstripthree",TPCRadius+2*FSEThickness+FSErSpacing+insulationwidth);
		this.makeGroundStripSelection("groundstripfour",TPCRadius+2*FSEThickness+groundstripwidth+FSErSpacing+insulationwidth+wallwidth);
		for(int i = 0; i < FSENumber; i++){
			this.makeFSESelection(i);
		}
		//this.makeCageSelection(); 
	}
	
	public void makeGroundStripSelection(String name, double radius){
		this.makeBoxSelection(name,radius-FSErSpacing/4,-electrodeThickness-FSEzSpacing/4, radius+groundstripwidth+FSErSpacing/4,TPCRadius+2*electrodeThickness+FSEzSpacing/4);
	}
	
	public void makeAnodeSelection(){ 
		this.model.selection().create("anodeSelection","Box");
		this.model.selection("anodeSelection").set("condition", "inside");
		this.model.selection("anodeSelection").set("entitydim",1);
		this.model.selection("anodeSelection").set("xmin",innerTPCradius-FSEzSpacing/4);
		this.model.selection("anodeSelection").set("xmax",TPCRadius+2*FSEThickness+FSErSpacing+FSErSpacing);
		this.model.selection("anodeSelection").set("ymin",-electrodeThickness-FSEzSpacing/4);
		this.model.selection("anodeSelection").set("ymax",FSEzSpacing/4);		
	}
	public void makeCathodeSelection(){
		this.model.selection().create("cathodeSelection","Box");
		this.model.selection("cathodeSelection").set("condition", "inside");
		this.model.selection("cathodeSelection").set("entitydim",1);
		this.model.selection("cathodeSelection").set("xmin",innerTPCradius-FSEzSpacing/4);
		this.model.selection("cathodeSelection").set("xmax",TPCRadius+2*FSEThickness+FSErSpacing+FSErSpacing);
		this.model.selection("cathodeSelection").set("ymin",TPCLength()-FSEzSpacing/4);
		this.model.selection("cathodeSelection").set("ymax",TPCLength()+electrodeThickness+FSEzSpacing/4);		
	}
	
	public void makeCircuit(){
		this.model.physics().create("cir", "Circuit", "geom");
		
		this.connectAnode();
		this.connectCathode();
		for(int i = 1; i < FSENumber; i++){
			this.addResistor("Resistor"+i,i+"",i+1+"",Resistance+"[\u03a9]");
			this.addItoU("ItoU"+i,i+1+"","G",2*i+1);
		    this.addResistor("InnerResistor"+i,"inner"+i, "inner"+(i+1), Resistance+"[\u03a9]");
		    this.addItoU("InnerItoU"+i,"inner"+(i+1),"G",2*i+2);
		}
		this.connectVoltageSource();
	}
	
	public void setMaterials(){
		this.makeCopper(); // Makes all domains copper.
		this.makeAir(new int[] {1,2,4,6,8,2*FSENumber+10,2*FSENumber+12}); //328,330}); // Changes chosen domains from copper to air.
		}
	
	public void makeTerminals(){
		this.model.physics().create("current", "ConductiveMedia", "geom");
		this.model.physics("current").selection().set(new int[] {1,2,4,6,8,2*FSENumber+10,2*FSENumber+12}); //328,330}); //,2,4,6,8,328,330}); // Domain Selection of electric current physics
		this.makeAnodeTerminal();
		for(int i =0; i < FSENumber; i++){
			makeFSETerminal(i);        
			makeInnerFSE(i);
		}
		this.makeCathodeTerminal();
		this.makeGroundStripTerminal();
	}

	public void makeAnodeTerminal(){
		this.model.physics("current").feature().create("anodeTerminal", "Ground",1);
		this.model.physics("current").feature("anodeTerminal").selection().named("anodeSelection");
	}
	
	public void makeCathodeTerminal(){
		this.model.physics("current").feature().create("cathodeTerminal","Terminal");
		this.model.physics("current").feature("cathodeTerminal").selection().named("cathodeSelection");
		this.model.physics("current").feature("cathodeTerminal").set("TerminalType",1,"Circuit");		
	}
	
}