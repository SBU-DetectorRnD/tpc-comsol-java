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
		this.makeSelections();
		this.makeTerminals();     //These are necessary once this.make* is actually used in this file
		this.makeCircuit(); 
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
	
	public void makeSelections(){
		this.makeAnodeSelection();
		this.makeCathodeSelection();
		this.makeGroundStripSelection("groundstripone",beampiperadius);
		this.makeGroundStripSelection("groundstriptwo",beampiperadius+groundstripwidth+wallwidth);
		this.makeGroundStripSelection("groundstripthree",FSEOuterRadius()+insulationwidth);
		this.makeGroundStripSelection("groundstripfour",FSEOuterRadius()+groundstripwidth+insulationwidth+wallwidth);
		for(int i = 0; i < FSENumber; i++){
			this.makeFSESelection(i);
		}
		//this.makeCageSelection(); 
	}
	
	public void makeAnodeSelection(){ 
		this.model.selection().create("anodeSelection","Box");
		this.model.selection("anodeSelection").set("condition", "inside");
		this.model.selection("anodeSelection").set("entitydim",1);
		this.model.selection("anodeSelection").set("xmin",innerTPCradius-FSEzSpacing/4);
		this.model.selection("anodeSelection").set("xmax",TPCRadius()+2*FSEThickness+FSErSpacing+FSErSpacing);
		this.model.selection("anodeSelection").set("ymin",-electrodeThickness-FSEzSpacing/4);
		this.model.selection("anodeSelection").set("ymax",FSEzSpacing/4);		
	}
	public void makeCathodeSelection(){
		this.model.selection().create("cathodeSelection","Box");
		this.model.selection("cathodeSelection").set("condition", "inside");
		this.model.selection("cathodeSelection").set("entitydim",1);
		this.model.selection("cathodeSelection").set("xmin",innerTPCradius-FSEzSpacing/4);
		this.model.selection("cathodeSelection").set("xmax",TPCRadius()+2*FSEThickness+FSErSpacing+FSErSpacing);
		this.model.selection("cathodeSelection").set("ymin",TPCLength()-FSEzSpacing/4);
		this.model.selection("cathodeSelection").set("ymax",TPCLength()+electrodeThickness+FSEzSpacing/4);		
	}
	
	public void makeGroundStripSelection(String name, double radius){
		this.makeBoxSelection(name,radius-FSErSpacing/4,-electrodeThickness-FSEzSpacing/4, radius+groundstripwidth+FSErSpacing/4,TPCRadius()+2*electrodeThickness+FSEzSpacing/4);
	}
	
	
	public void makeFSESelection(int actualNumber){
		String name = "FSE"+actualNumber+"Selection";        //outer strips
		String name2 = "InnerFSE"+actualNumber+"Selection";  //inner strips
		
		double rmin = TPCRadius() - FSErSpacing/4;        //Outer Strips
		double rmax = rmin + FSEThickness + FSErSpacing/2;//Outer Strips
		
		double rmin2 = innerTPCradius-FSErSpacing/4;       //inner strips
		double rmax2 = rmin2 + FSEThickness+FSErSpacing/2; //inner strips
		
		double z1 = FSEzSpacing+FSELength/2;

		double zmin = z1 + offsetz()*actualNumber - FSEzSpacing/4; //Outer Strips
		double zmax = zmin + FSELength + FSEzSpacing/2;            //Outer Strips
		
		double zmin2 = z1 + offsetz()*actualNumber - FSEzSpacing/4; //Outer Strips
		double zmax2 = zmin + FSELength + FSEzSpacing/2;            //Outer Strips
		
		this.makeBoxSelection(name,rmin,zmin,rmax,zmax);          //Outer strip selections
		this.makeBoxSelection(name2, rmin2, zmin2, rmax2, zmax2); //inner strip selections
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
	
	public void makeFSETerminal(int actualNumber){            //These are from mirror.
		String terminal = "FSE"+actualNumber+"Terminal";      //May need to be configured for single.
		String selection = "FSE"+actualNumber+"Selection"; 
		this.model.physics("current").feature().create(terminal,"Terminal");
		this.model.physics("current").feature(terminal).selection().named(selection);
		this.model.physics("current").feature(terminal).set("TerminalType",1,"Circuit");
	}
	
	@SuppressWarnings("deprecation")
	public void makeInnerFSE(int actualNumber){                //From mirror.
		String terminal = "InnerFSE"+actualNumber+"Terminal";  //may need to configure for single.
		String selection = "InnerFSE"+actualNumber+"Selection"; 
		this.model.physics("current").feature().create(terminal,"Terminal");
		this.model.physics("current").feature(terminal).selection().named(selection);
		this.model.physics("current").feature(terminal).set("TerminalType",1,"Circuit");
	}
	
	public void makeCathodeTerminal(){
		this.model.physics("current").feature().create("cathodeTerminal","Terminal");
		this.model.physics("current").feature("cathodeTerminal").selection().named("cathodeSelection");
		this.model.physics("current").feature("cathodeTerminal").set("TerminalType",1,"Circuit");		
	}
	
	public void makeGroundStripTerminal(){
		//this.model.physics("current").feature().create("cageTerminal", "Ground", 1);//Here is the line where the connection between the faraday cage and anode terminal
		//this.model.physics("current").feature("cageTerminal").selection().named("cageEdgeSelecction");
		this.model.physics("current").feature().create("GroundStripTerminalone", "Ground", 1);
		this.model.physics("current").feature("GroundStripTerminalone").selection().named("groundstripone");
		this.model.physics("current").feature().create("GroundStripTerminaltwo", "Ground", 1);
		this.model.physics("current").feature("GroundStripTerminaltwo").selection().named("groundstriptwo");
		this.model.physics("current").feature().create("GroundStripTerminalthree", "Ground", 1);
		this.model.physics("current").feature("GroundStripTerminalthree").selection().named("groundstripthree");
		this.model.physics("current").feature().create("GroundStripTerminalfour", "Ground", 1);
		this.model.physics("current").feature("GroundStripTerminalfour").selection().named("groundstripfour");
	}
	
	public void makeCircuit(){    //NEEDS TO BE CONFIGURED FOR SINGLE.
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
	public void connectAnode(){
		this.model.physics("cir").feature("gnd1").set("Connections",1,1,"G");
		this.addResistor("zeroResistor1outer","0","1","0[\u03a9]");
		this.addItoU("ItoU0","0","G",1);
		this.addResistor("Resistor0","1","G",Resistance+"[\u03a9]");
		
		this.addResistor("zeroResistor1inner","inner0","inner1","0[\u03a9]");              // attempt here to connect inner FSE's to anode
		this.addItoU("ItoU0inner","inner0","G",2);                               // not sure if this works
		this.addResistor("InnerFSEtoAnode","inner"+1,"G",Resistance+"[\u03a9]"); // same may have to be done for cathode
		
	}
	public void connectCathode(){
		this.addResistor("zeroResistor2outer","C1","C2","0[\u03a9]");
		this.addItoU("ItoUC","C1","G",2*FSENumber+1); //2*FSENumber+2*(FSENumber-1)-1 or 2*FSENumber+1
		this.addResistor("Resistor"+FSENumber,FSENumber+"","C2",Resistance+"[\u03a9]");
		
		//this.addResistor("zeroResistor2inner","C1","C2","0[\u03a9]");
		//this.addItoU("ItoUCinner","C1","G",2*FSENumber+1); if doesn't work add innerC1 to G here
		this.addResistor("InnerFSEtoCathode","inner"+FSENumber,"C2",Resistance+"[\u03a9]");
	}
	
	@SuppressWarnings("deprecation")
	public void connectVoltageSource(){
		this.model.physics("cir").feature().create("source","VoltageSource",-1);
		this.model.physics("cir").feature("source").set("Connections",1,1,"C2");
		this.model.physics("cir").feature("source").set("Connections",2,1,"G");
		this.model.physics("cir").feature("source").set("value",1,Voltage+"[V]");
	}
	
	@SuppressWarnings("deprecation")
	public void addResistor(String name, String node1, String node2, String value){
		this.model.physics("cir").feature().create(name,"Resistor",-1);
		this.model.physics("cir").feature(name).set("Connections",1,1,node1);
		this.model.physics("cir").feature(name).set("Connections",2,1,node2);
		this.model.physics("cir").feature(name).set("R",1,value);
	}
	@SuppressWarnings("deprecation")
	public void addItoU(String name, String node1, String node2, int terminal){
		this.model.physics("cir").feature().create(name, "ModelDeviceIV");
		this.model.physics("cir").feature(name).set("V_src", 1, "root.comp1.ec.V0_"+terminal);
		this.model.physics("cir").feature(name).set("Connections",1,1,node1);
		this.model.physics("cir").feature(name).set("Connections",2,1,node2);
	}
	
	
	public void setMaterials(){
		this.makeCopper(); // Makes all domains copper. Different air domains from TPC, TPCMirror.
		this.makeAir(new int[] {1,2,4,6,8,2*FSENumber+10,2*FSENumber+12}); // Changes chosen domains from copper to air. This will change when inner single strips are made.
		}	
	
}