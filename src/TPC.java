import com.comsol.model.*;
import com.comsol.model.util.*;


	public class TPC { 

	public Model model;

	public double TPCRadius = 800; // in mm
	public double electrodeThickness = 1; 
	public double TPCLength(){
		return (FSELength + FSEzSpacing) * (FSENumber)+2*(FSEzSpacing+FSELength/2)-FSEzSpacing;
	}
	public double FSEOuterRadius(){
		return TPCRadius + 2*FSEThickness + FSErSpacing; 
	}
	                             // FSE is strips (Field Shaping Elements).
	public int FSENumber = 80; // Number of strips on both inner and outer, total strip number=2*(FSENumber+FSENumber-1) 
	public double FSELength = 9.0; // Strip length, 9.0 (mm)
	public double FSEzSpacing = 1.0; // Strip spacing in z, 1 (mm)
	public double FSEThickness = .035; // Strip thickness, 
	public double FSErSpacing = .05; // Kapton tape length
	public double offsetz() { return FSELength + FSEzSpacing;}
	public double punchthroughThickness = .035; //Don't know what this is.
	public double mirrorLength = 4.6; // Don't know what this is.
	public double beampiperadius = 200; //radius of beam pipe 
	public double groundstripwidth = 0.05; // Width of grounding strip
	public double wallwidth = 50; //Width of Honeycomb wall
	public double insulationwidth = 1; // insulator (start with this as air) width
	public double cageThickness = 10; // first cage parameter
	public double cageEndSpacing = 300; // second cage parameter
	public double cageSideSpacing = 150; // third cage parameter
	public double innerTPCradius = beampiperadius+wallwidth+insulationwidth+2*groundstripwidth; // radius before inner conductive strips
	public double UpperGroundStripThickness = TPCRadius+2*FSEThickness+FSErSpacing+insulationwidth+groundstripwidth-(beampiperadius+groundstripwidth+wallwidth)-2*insulationwidth;
	
	public double Resistance = 1000000; 
	public double Conductivity = .000004;
	public double Voltage = 23000; //Voltage between one end and the middle membrane 34,000 Volts
	
	public static void main(String[] args){
		run();
	}
	public static Model run(){
		return new TPC().model;
	}
	public TPC(){
		this.model = ModelUtil.create("Model");
		this.model.modelNode().create("comp1");
		
		this.setVariables();
		this.makeGeometry();
		this.makeSelections();
		this.makeTerminals();
		this.makeCircuit();
		this.setMaterials();
		//this.makeDataSet();
		this.makeSolver();
	    this.model.mesh().create("mesh1", "geom");

	}
	
	public void setVariables(){}
	
	public void makeGeometry(){
		this.model.geom().create("geom", 2);
		this.model.geom("geom").axisymmetric(true);
		this.model.geom("geom").lengthUnit("mm");
		
		this.addRect("anodeRect",innerTPCradius,-electrodeThickness,TPCRadius-innerTPCradius+2*FSEThickness+FSErSpacing,electrodeThickness);
		this.addRect("cathodeRect",innerTPCradius,TPCLength(),TPCRadius-innerTPCradius+2*FSEThickness+FSErSpacing,electrodeThickness);
		this.addRect("UpperGroundStrip", beampiperadius+groundstripwidth+wallwidth+insulationwidth, -electrodeThickness+TPCLength()+2*electrodeThickness+insulationwidth, UpperGroundStripThickness, groundstripwidth);
		
		this.addRect("BeamPipe",0 ,-electrodeThickness,beampiperadius , TPCLength()+2*electrodeThickness);
		this.addRect("GroundStrip1",beampiperadius ,-electrodeThickness,groundstripwidth , TPCLength()+2*electrodeThickness);
		this.addRect("InnerWall",beampiperadius+groundstripwidth ,-electrodeThickness,wallwidth ,TPCLength()+2*electrodeThickness );
		this.addRect("GroundStrip2",beampiperadius+groundstripwidth+wallwidth ,-electrodeThickness,groundstripwidth ,TPCLength()+2*electrodeThickness+insulationwidth);
		this.addRect("InnerInsulator",beampiperadius+2*groundstripwidth+wallwidth ,-electrodeThickness,insulationwidth ,TPCLength()+2*electrodeThickness );
		this.addRect("OuterInsulator",TPCRadius+2*FSEThickness+FSErSpacing ,-electrodeThickness, insulationwidth, TPCLength()+2*electrodeThickness);
		this.addRect("GroundStrip3",TPCRadius+2*FSEThickness+FSErSpacing+insulationwidth ,-electrodeThickness ,groundstripwidth ,TPCLength()+2*electrodeThickness+insulationwidth);
		this.addRect("OuterWall",TPCRadius+2*FSEThickness+groundstripwidth+FSErSpacing+insulationwidth,-electrodeThickness,wallwidth, TPCLength()+2*electrodeThickness);
		this.addRect("GroundStrip4",TPCRadius+2*FSEThickness+groundstripwidth+FSErSpacing+insulationwidth+wallwidth ,-electrodeThickness ,groundstripwidth , TPCLength()+2*electrodeThickness);
		this.addFSEs();
		
		//double cagez =-electrodeThickness-cageEndSpacing;
		//double cagew = FSEOuterRadius()+cageSideSpacing;
		//double cageh = TPCLength()+2*electrodeThickness+2*cageEndSpacing;
		//this.addRect("cageInRect",0,cagez,cagew,cageh);
		//this.addRect("cageOutRect",0,cagez-cageThickness,cagew+cageThickness,cageh+cageThickness*2);
		this.addCircle("airsphere",2000,TPCLength()/2); // changed airsphere radius from 1000 to 2000
		this.model.geom("geom").run();
	}
	public void addRect(String name, double r, double z, double t, double h){
		double size[] = {t, h};
		double pos[] = {r, z};

		this.model.geom("geom").feature().create(name,"Rectangle");
		this.model.geom("geom").feature(name).set("pos",pos);
		this.model.geom("geom").feature(name).set("size",size);
	}
	public void addFSEs(){}//						THIS METHOD DOSES NOT FUNCITON AND MUST BE OVERRIDDEN
	public void makeFSEArray(double offset,String[] inputs,int size){
		this.model.geom("geom").feature().create("FSEArray","Array");
		this.model.geom("geom").feature("FSEArray").selection("input").set(inputs);
		this.model.geom("geom").feature("FSEArray").setIndex("displ","0",0);
		this.model.geom("geom").feature("FSEArray").setIndex("displ",offset,1);
		this.model.geom("geom").feature("FSEArray").setIndex("fullsize","1",0);
		this.model.geom("geom").feature("FSEArray").setIndex("fullsize",size+"",1);
	}
	public void addCircle(String name, double radius, double center){
		this.model.geom("geom").feature().create(name,"Circle");
		this.model.geom("geom").feature(name).set("r",radius);
		this.model.geom("geom").feature(name).set("pos", new double[] {0,center});
	}

	public void makeSelections(){
		this.makeAnodeSelection();
		this.makeCathodeSelection();
		this.makeUpperGroundStripSelection("uppergroundstrip");
		this.makeGroundStripSelection("groundstripone",beampiperadius);
		this.makeGroundStripSelection("groundstriptwo",beampiperadius+groundstripwidth+wallwidth);
		this.makeGroundStripSelection("groundstripthree",TPCRadius+2*FSEThickness+FSErSpacing+insulationwidth);
		this.makeGroundStripSelection("groundstripfour",TPCRadius+2*FSEThickness+groundstripwidth+FSErSpacing+insulationwidth+wallwidth);
		//this.makeInsulatorSelection();
		for(int i = 0; i < FSENumber; i++){
			this.makeFSESelection(i);
		}
		//this.makeCageSelection(); 
	}
	//public void makeGroundStripOne() {  //commented out unnecessary insulator selection since we are not connecting terminals to it
	//	this.model.selection().create("insulatorSelection","Box");
	//	this.model.selection("insulatorSelection").set("condition", "inside");
	//	this.model.selection("insulatorSelection").set("entitydim",1);
	//	this.model.selection("insulatorSelection").set("xmin",TPCRadius+2*FSEThickness+FSErSpacing); 
	//	this.model.selection("insulatorSelection").set("xmax",TPCRadius+2*FSEThickness+FSErSpacing+insulationwidth+FSEzSpacing/4);
	//	this.model.selection("insulatorSelection").set("ymin",-electrodeThickness-FSEzSpacing/4);
	//	this.model.selection("insulatorSelection").set("ymax",-electrodeThickness+TPCRadius+2*electrodeThickness+FSEzSpacing/4);
		
	//}
	
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
	public void makeFSESelection(int actualNumber){}//	THIS METHOD DOSES NOT FUNCTION AND MUST BE OVERRIDDEN
	public void makeBoxSelection(String name, double rmin, double zmin, double rmax, double zmax){
		this.model.selection().create(name,"Box");
		this.model.selection(name).set("condition", "inside");
		this.model.selection(name).set("entitydim",1);
		this.model.selection(name).set("xmin",rmin);
		this.model.selection(name).set("ymin",zmin);
		this.model.selection(name).set("xmax",rmax);
		this.model.selection(name).set("ymax",zmax);
	}
	public void makeGroundStripSelection(String name, double radius){
		this.makeBoxSelection(name,radius-FSErSpacing/4,-electrodeThickness-FSEzSpacing/4, radius+groundstripwidth+FSErSpacing/4,TPCRadius+2*electrodeThickness+FSEzSpacing/4+insulationwidth);
	}
	public void makeUpperGroundStripSelection(String uppername){
		this.makeBoxSelection(uppername, beampiperadius+groundstripwidth+wallwidth-FSErSpacing/4+insulationwidth, -electrodeThickness+TPCLength()+2*electrodeThickness-FSErSpacing/4+insulationwidth, beampiperadius+groundstripwidth+wallwidth+UpperGroundStripThickness+FSErSpacing/4-insulationwidth, -electrodeThickness+TPCLength()+2*electrodeThickness+groundstripwidth+FSErSpacing/4+insulationwidth);
	}
	
	
	//public void makeCageSelection(){
		//this.model.selection().create("cageVolumeSelection", "Explicit");
		//this.model.selection("cageVolumeSelection").set(new int[]{2});
		//this.model.selection().create("cageEdgeSelecction", "Adjacent");
		//this.model.selection("cageEdgeSelecction").set("input", new String[]{"cageVolumeSelection"});
	//}
	
	public void makeTerminals(){
		this.model.physics().create("current", "ConductiveMedia", "geom");
		this.model.physics("current").selection().set(new int[] {1,2,4,7,8,10,2*FSENumber+12,2*FSENumber+14}); //used to be {1,2,4,6,8,2*FSENumber+10,2*FSENumber+12}; Domain Selection of electric current physics
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
	@SuppressWarnings("deprecation")
	public void makeCathodeTerminal(){
		this.model.physics("current").feature().create("cathodeTerminal","Terminal");
		this.model.physics("current").feature("cathodeTerminal").selection().named("cathodeSelection");
		this.model.physics("current").feature("cathodeTerminal").set("TerminalType",1,"Circuit");		
	}
	@SuppressWarnings("deprecation")
	public void makeInnerFSE(int actualNumber){
		String terminal = "InnerFSE"+actualNumber+"Terminal";
		String selection = "InnerFSE"+actualNumber+"Selection"; 
		this.model.physics("current").feature().create(terminal,"Terminal");
		this.model.physics("current").feature(terminal).selection().named(selection);
		this.model.physics("current").feature(terminal).set("TerminalType",1,"Circuit");
	}
	@SuppressWarnings("deprecation")
	public void makeFSETerminal(int actualNumber){
		String terminal = "FSE"+actualNumber+"Terminal";
		String selection = "FSE"+actualNumber+"Selection"; 
		this.model.physics("current").feature().create(terminal,"Terminal");
		this.model.physics("current").feature(terminal).selection().named(selection);
		this.model.physics("current").feature(terminal).set("TerminalType",1,"Circuit");
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
		this.model.physics("current").feature().create("GroundStripUpper","Ground",1);
		this.model.physics("current").feature("GroundStripUpper").selection().named("uppergroundstrip");
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
	@SuppressWarnings("deprecation")
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
	@SuppressWarnings("deprecation")
	public void connectVoltageSource(){
		this.model.physics("cir").feature().create("source","VoltageSource",-1);
		this.model.physics("cir").feature("source").set("Connections",1,1,"C2");
		this.model.physics("cir").feature("source").set("Connections",2,1,"G");
		this.model.physics("cir").feature("source").set("value",1,Voltage+"[V]");
	}
	
	public void setMaterials(){
		this.makeCopper(); // Makes all domains copper.
		this.makeAir(new int[] {1,2,4,7,8,10,2*FSENumber+12,2*FSENumber+14}); //old: {1,2,4,6,8,2*FSENumber+10,2*FSENumber+12} Changes chosen domains from copper to air.
		}

	@SuppressWarnings("deprecation")
	public void makeCopper(){
		this.model.material().create("mat1");
	    this.model.material("mat1").name("Copper");
	    this.model.material("mat1").set("family", "copper");
	    this.model.material("mat1").propertyGroup("def").set("relpermeability", "1");
	    this.model.material("mat1").propertyGroup("def")
	         .set("electricconductivity", "5.998e7[S/m]");
	    this.model.material("mat1").propertyGroup("def")
	         .set("thermalexpansioncoefficient", "17e-6[1/K]");
	    this.model.material("mat1").propertyGroup("def")
	         .set("heatcapacity", "385[J/(kg*K)]");
	    this.model.material("mat1").propertyGroup("def").set("relpermittivity", "1");
	    this.model.material("mat1").propertyGroup("def")
	         .set("density", "8700[kg/m^3]");
	    this.model.material("mat1").propertyGroup("def")
	         .set("thermalconductivity", "400[W/(m*K)]");
	    this.model.material("mat1").propertyGroup()
	         .create("Enu", "Young's modulus and Poisson's ratio");
	    this.model.material("mat1").propertyGroup("Enu").set("poissonsratio", "0.35");
	    this.model.material("mat1").propertyGroup("Enu")
	         .set("youngsmodulus", "110e9[Pa]");
	    this.model.material("mat1").propertyGroup()
	         .create("linzRes", "Linearized resistivity");
	    this.model.material("mat1").propertyGroup("linzRes")
	         .set("alpha", "0.0039[1/K]");
	    this.model.material("mat1").propertyGroup("linzRes")
	         .set("rho0", "1.72e-8[ohm*m]");
	    this.model.material("mat1").propertyGroup("linzRes").set("Tref", "298[K]");
	    this.model.material("mat1").set("family", "copper");
	}
	@SuppressWarnings("deprecation")
	public void makeAir(int[] regions){
	    this.model.material().create("mat2");
	    this.model.material("mat2").name("Air");
	    this.model.material("mat2").set("family", "air");
	    this.model.material("mat2").propertyGroup("def").set("relpermeability", "1");
	    this.model.material("mat2").propertyGroup("def").set("relpermittivity", "1");
	    this.model.material("mat2").propertyGroup("def")
	         .set("dynamicviscosity", "eta(T[1/K])[Pa*s]");
	    this.model.material("mat2").propertyGroup("def")
	         .set("ratioofspecificheat", "1.4");
	    this.model.material("mat2").propertyGroup("def")
	         .set("electricconductivity", "10^-15[S/m]");
	    this.model.material("mat2").propertyGroup("def")
	         .set("heatcapacity", "Cp(T[1/K])[J/(kg*K)]");
	    this.model.material("mat2").propertyGroup("def")
	         .set("density", "rho(pA[1/Pa],T[1/K])[kg/m^3]");
	    this.model.material("mat2").propertyGroup("def")
	         .set("thermalconductivity", "k(T[1/K])[W/(m*K)]");
	    this.model.material("mat2").propertyGroup("def")
	         .set("soundspeed", "cs(T[1/K])[m/s]");
	    this.model.material("mat2").propertyGroup("def").func()
	         .create("eta", "Piecewise");
	    this.model.material("mat2").propertyGroup("def").func("eta")
	         .set("funcname", "eta");
	    this.model.material("mat2").propertyGroup("def").func("eta").set("arg", "T");
	    this.model.material("mat2").propertyGroup("def").func("eta")
	         .set("extrap", "constant");
	    this.model.material("mat2").propertyGroup("def").func("eta")
	         .set("pieces", new String[][]{{"200.0", "1600.0", "-8.38278E-7+8.35717342E-8*T^1-7.69429583E-11*T^2+4.6437266E-14*T^3-1.06585607E-17*T^4"}});
	    this.model.material("mat2").propertyGroup("def").func()
	         .create("Cp", "Piecewise");
	    this.model.material("mat2").propertyGroup("def").func("Cp")
	         .set("funcname", "Cp");
	    this.model.material("mat2").propertyGroup("def").func("Cp").set("arg", "T");
	    this.model.material("mat2").propertyGroup("def").func("Cp")
	         .set("extrap", "constant");
	    this.model.material("mat2").propertyGroup("def").func("Cp")
	         .set("pieces", new String[][]{{"200.0", "1600.0", "1047.63657-0.372589265*T^1+9.45304214E-4*T^2-6.02409443E-7*T^3+1.2858961E-10*T^4"}});
	    this.model.material("mat2").propertyGroup("def").func()
	         .create("rho", "Analytic");
	    this.model.material("mat2").propertyGroup("def").func("rho")
	         .set("funcname", "rho");
	    this.model.material("mat2").propertyGroup("def").func("rho")
	         .set("args", new String[]{"pA", "T"});
	    this.model.material("mat2").propertyGroup("def").func("rho")
	         .set("expr", "pA*0.02897/8.314/T");
	    this.model.material("mat2").propertyGroup("def").func("rho")
	         .set("dermethod", "manual");
	    this.model.material("mat2").propertyGroup("def").func("rho")
	         .set("argders", new String[][]{{"pA", "d(pA*0.02897/8.314/T,pA)"}, {"T", "d(pA*0.02897/8.314/T,T)"}});
	    this.model.material("mat2").propertyGroup("def").func()
	         .create("k", "Piecewise");
	    this.model.material("mat2").propertyGroup("def").func("k")
	         .set("funcname", "k");
	    this.model.material("mat2").propertyGroup("def").func("k").set("arg", "T");
	    this.model.material("mat2").propertyGroup("def").func("k")
	         .set("extrap", "constant");
	    this.model.material("mat2").propertyGroup("def").func("k")
	         .set("pieces", new String[][]{{"200.0", "1600.0", "-0.00227583562+1.15480022E-4*T^1-7.90252856E-8*T^2+4.11702505E-11*T^3-7.43864331E-15*T^4"}});
	    	    this.model.material("mat2").propertyGroup("def").func()
	         .create("cs", "Analytic");
	    this.model.material("mat2").propertyGroup("def").func("cs")
	         .set("funcname", "cs");
	    this.model.material("mat2").propertyGroup("def").func("cs")
	         .set("args", new String[]{"T"});
	    this.model.material("mat2").propertyGroup("def").func("cs")
	         .set("expr", "sqrt(1.4*287*T)");
	    this.model.material("mat2").propertyGroup("def").func("cs")
	         .set("dermethod", "manual");
	    this.model.material("mat2").propertyGroup("def").func("cs")
	         .set("argders", new String[][]{{"T", "d(sqrt(1.4*287*T),T)"}});
	    this.model.material("mat2").propertyGroup("def").addInput("temperature");
	    this.model.material("mat2").propertyGroup("def").addInput("pressure");
	    this.model.material("mat2").propertyGroup()
	         .create("RefractiveIndex", "Refractive index");
	    this.model.material("mat2").propertyGroup("RefractiveIndex").set("n", "1");
	    this.model.material("mat2").set("family", "air");
	    this.model.material("mat2").selection().set(regions);
	}
	
	//public void export(String file){
	//	this.makeDataSet();
		
	//}
	//public void makeDataSet(){
	//    this.model.result().dataset().create("cpt1", "CutPoint2D");
	//    this.model.result().dataset("cpt1").set("method", "grid");
	//    this.model.result().dataset("cpt1").set("gridx", "range(0,1,359)");
	//    this.model.result().dataset("cpt1").set("gridy", "range(0,1,"+this.TPCLength()+")");
	//}

	public void makeSolver(){
	    this.model.study().create("study");
	    this.model.study("study").feature().create("solver", "Stationary");
	    this.model.study("study").feature("solver").activate("current", true);
	    this.model.study("study").feature("solver").activate("cir", true);
	}
	}
//}