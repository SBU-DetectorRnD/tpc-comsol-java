import com.comsol.model.*;
import com.comsol.model.util.*;

public class TPC {

	public Model model;

	public double TPCRadius(){ 
		return (800); //End of TPC right before outer strips in r. 800 value is catered towards TPCMirror.
	}
	
	public double TPCLength(){           //Length of TPC in z direction.
		return (FSELength + FSEzSpacing) * (FSENumber)+2*(FSEzSpacing+FSELength/2)-FSEzSpacing;
		}   
	
	public double FSEOuterRadius(){
		return TPCRadius();			  //End of TPC and strips, beginning of outer Insulator
	} 
	                             
	//FSE means Field Shaping Elements
	public int FSENumber = 8; // Number of strips in one vertical line (mirror contains two per inner and outer, single contains one per inner and outer)
	public double FSELength = 9.0; // Strip length in z, 9.0 (mm)
	public double FSEzSpacing = 1.0; // Strip spacing in z, 1 (mm)
	public double FSEThickness = .035; // Strip thickness in r, 
	public double FSErSpacing = .05; // Kapton tape length in r
	public double offsetz() { return FSELength + FSEzSpacing;} //not sure what this is for right now
	public double beampiperadius = 200; //radius of beam pipe, r direction
	public double groundstripwidth = 0.05; // Width of grounding strip
	public double wallwidth = 50; //Width of Honeycomb wall
	public double insulationwidth = 1; // insulator (start with this as air) width
	public double electrodeThickness = 1;
	public double innerTPCradius = beampiperadius+wallwidth+insulationwidth+2*groundstripwidth; // radius right where inner strips begin
	public double CathodeAnodeThickness = FSEOuterRadius()-innerTPCradius;
	public double UpperGroundStripThickness = TPCRadius()+2*FSEThickness+FSErSpacing+insulationwidth+groundstripwidth-(beampiperadius+groundstripwidth+wallwidth)-2*insulationwidth;
	
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
		//this.makeSelections();
		//this.makeTerminals();
		//this.makeCircuit();
		this.setMaterials();
		this.makeSolver();
	    this.model.mesh().create("mesh1", "geom");

	}
	
	public void setVariables(){}
	
	public void makeGeometry(){
		this.model.geom().create("geom", 2);
		this.model.geom("geom").axisymmetric(true); //Creates radial symmetry about r=0 
		this.model.geom("geom").lengthUnit("mm");   //Length values in mm
		
		this.addRect("UpperGroundStrip", beampiperadius+groundstripwidth+wallwidth+insulationwidth, -electrodeThickness+TPCLength()+2*electrodeThickness+insulationwidth, UpperGroundStripThickness, groundstripwidth);
		this.addRect("anodeRect",innerTPCradius,-electrodeThickness,CathodeAnodeThickness,electrodeThickness);
		this.addRect("cathodeRect",innerTPCradius,TPCLength(),CathodeAnodeThickness,electrodeThickness);
		
		this.addRect("BeamPipe",0 ,-electrodeThickness,beampiperadius , TPCLength()+2*electrodeThickness);
		this.addRect("GroundStrip1",beampiperadius ,-electrodeThickness,groundstripwidth , TPCLength()+2*electrodeThickness);
		this.addRect("InnerWall",beampiperadius+groundstripwidth ,-electrodeThickness,wallwidth ,TPCLength()+2*electrodeThickness );
		this.addRect("GroundStrip2",beampiperadius+groundstripwidth+wallwidth ,-electrodeThickness,groundstripwidth ,TPCLength()+2*electrodeThickness );
		this.addRect("InnerInsulator",beampiperadius+2*groundstripwidth+wallwidth ,-electrodeThickness,insulationwidth ,TPCLength()+2*electrodeThickness );
		this.addRect("OuterInsulator",FSEOuterRadius() ,-electrodeThickness, insulationwidth, TPCLength()+2*electrodeThickness);
		this.addRect("GroundStrip3",FSEOuterRadius()+insulationwidth ,-electrodeThickness ,groundstripwidth ,TPCLength()+2*electrodeThickness );
		this.addRect("OuterWall",FSEOuterRadius()+groundstripwidth+insulationwidth,-electrodeThickness,wallwidth, TPCLength()+2*electrodeThickness);
		this.addRect("GroundStrip4",FSEOuterRadius()+groundstripwidth+insulationwidth+wallwidth,-electrodeThickness ,groundstripwidth , TPCLength()+2*electrodeThickness);
		this.addFSEs();
		
		this.addCircle("airsphere",2000,TPCLength()/2); // 'observable universe' where everything contained
		this.model.geom("geom").run();
	}
	public void addRect(String name, double r, double z, double t, double h){
		double size[] = {t, h};
		double pos[] = {r, z};

		this.model.geom("geom").feature().create(name,"Rectangle");
		this.model.geom("geom").feature(name).set("pos",pos);
		this.model.geom("geom").feature(name).set("size",size);
	}
	public void addFSEs(){}  //This Method is overwritten in TPCMirror.java
	public void addCircle(String name, double radius, double center){
		this.model.geom("geom").feature().create(name,"Circle");
		this.model.geom("geom").feature(name).set("r",radius);
		this.model.geom("geom").feature(name).set("pos", new double[] {0,center});
	}

	//public void makeTerminals(){
		//this.model.physics().create("current", "ConductiveMedia", "geom");
		//this.model.physics("current").selection().set(new int[] {1,2,4,6,9,11,13}); //328,330}); //,2,4,6,8,328,330}); // Domain Selection of electric current physics
		//}
	
	public void setMaterials(){
		this.makeCopper(); // Makes all domains copper. In TPC.java, domains independent of FSENumber
		this.makeAir(new int[] {1,2,4,6,9,11,13}); // Changes chosen domains from copper to air.
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

	public void makeSolver(){
	    this.model.study().create("study");
	    this.model.study("study").feature().create("solver", "Stationary");
	    this.model.study("study").feature("solver").activate("current", true);
	    this.model.study("study").feature("solver").activate("cir", true);
	}
}