import com.comsol.model.Model;               // imports comsol models

//still need upper grounding strip

public class TPCSingle extends TPC {	     //this file builds off of TPC.java
	public double FSEOuterRadius(){          //FSEOuterRadius different because now only single strip.
		return TPCRadius() + FSEThickness;     
	}
	
	public double TPCRadius(){          //FSEOuterRadius different because now only single strip.
		return 800 - FSEThickness-FSErSpacing;     
	}
	
		public static void main(String[] args){
		run();
	}
	public static Model run(){
		return new TPCSingle().model;
	}
	
	public TPCSingle(){             //I think this is necessary when this.makeCircuit not in TPC.java
		//this.makeSelections();
		this.makeTerminals();     //These are necessary once this.make* is actually used in this file
		//this.makeCircuit(); 
	}
	
	public void addFSEs(){
		double z1 = FSEzSpacing+FSELength/2;     // outer strips
		double r1 = TPCRadius();                 // outer strips
		
		double z2 = FSEzSpacing+FSELength/2;     //inner strips
		double r2 = innerTPCradius; 	         //inner strips
		
		this.addRect("FSE1Rect",r1,z1,FSEThickness,FSELength);            //outer strips
		this.addRect("FSE2Rect",r2,z2,FSEThickness,FSELength);			  //inner strips
		this.makeFSEArray(offsetz(),new String[]{"FSE1Rect"},FSENumber);  //outer strips
		this.makeFSEArrayInner(offsetz(),new String[]{"FSE2Rect"},FSENumber);  //inner strips. Not sure if this works yet.
	}

	public void makeFSEArray(double offset,String[] inputs,int size){
		this.model.geom("geom").feature().create("FSEArray","Array");
		this.model.geom("geom").feature("FSEArray").selection("input").set(inputs);
		this.model.geom("geom").feature("FSEArray").setIndex("displ","0",0);
		this.model.geom("geom").feature("FSEArray").setIndex("displ",offset,1);
		this.model.geom("geom").feature("FSEArray").setIndex("fullsize","1",0);
		this.model.geom("geom").feature("FSEArray").setIndex("fullsize",size+"",1);
	}
	
	public void makeFSEArrayInner(double offset,String[] inputs,int size){
		this.model.geom("geom").feature().create("FSEArrayInner","Array");
		this.model.geom("geom").feature("FSEArrayInner").selection("input").set(inputs);
		this.model.geom("geom").feature("FSEArrayInner").setIndex("displ","0",0);
		this.model.geom("geom").feature("FSEArrayInner").setIndex("displ",offset,1);
		this.model.geom("geom").feature("FSEArrayInner").setIndex("fullsize","1",0);
		this.model.geom("geom").feature("FSEArrayInner").setIndex("fullsize",size+"",1);
	}
	
	public void makeFSESelection(int actualNumber){
		String name = "FSE"+actualNumber+"Selection";
		
		double rmin = TPCRadius() - FSErSpacing/4;
		double rmax = rmin + FSEThickness + FSErSpacing/2;
		
		double z1 = FSEzSpacing+FSELength/2;

		double zmin = z1 + offsetz()*actualNumber - FSEzSpacing/4;
		double zmax = zmin + FSELength + FSEzSpacing/2;
		
		this.makeBoxSelection(name,rmin,zmin,rmax,zmax);
	}

	public void makeBoxSelection(String name, double rmin, double zmin, double rmax, double zmax){
		this.model.selection().create(name,"Box");
		this.model.selection(name).set("condition", "inside");
		this.model.selection(name).set("entitydim",1);
		this.model.selection(name).set("xmin",rmin);
		this.model.selection(name).set("ymin",zmin);
		this.model.selection(name).set("xmax",rmax);
		this.model.selection(name).set("ymax",zmax);
	}
	
	public void makeTerminals(){
		this.model.physics().create("current", "ConductiveMedia", "geom");
		this.model.physics("current").selection().set(new int[] {1,2,4,6,9,2*FSENumber+11,2*FSENumber+13}); //328,330}); //,2,4,6,8,328,330}); // Domain Selection of electric current physics
		}
	
	public void setMaterials(){
		this.makeCopper(); // Makes all domains copper. Different air domains from TPC, TPCMirror.
		this.makeAir(new int[] {1,2,4,6,9,2*FSENumber+11,2*FSENumber+13}); // Changes chosen domains from copper to air. This will change when inner single strips are made.
		}	
	
}