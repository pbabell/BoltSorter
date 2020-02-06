float wheelRadius = 75 //outer radius of the drum
float wheelHeight = 50 //height of the drum
int releaseAngle = 10 //the z angle the scoops are turned, changes when bolts are dropped
int wallThickness = 2 //thickness of wall of drum and scoops
int gearPitch = 6 //pitch of gear teeth, defined by arc length per gear tooth
int tollerance = 0 //to be assigned later
int numberOfScoops = 8 //number of scoops in the wheel
double toothDepth = 1/(Math.PI/gearPitch) + 2/(Math.PI/gearPitch) //sum of addendum and dedendum


CSG m5Bearing = Vitamins.get("ballBearing", "695zz") //bearing vitamin
CSG bearingCylinder = new Cylinder(m5Bearing.getMaxY() + tollerance, m5Bearing.getMaxY()+tollerance,
							m5Bearing.getMaxZ(),(int)30).toCSG() //cyinder used for bearing slots, NO TOLLERANCE

CSG m5Cylinder = new Cylinder(2.5+tollerance,2.5+tollerance,20,(int)20).toCSG()//cylinder used for bolt holes

CSG base = new Cylinder(wheelRadius-wallThickness,wheelRadius-wallThickness,5,(int)30).toCSG() //back plate of the  wheel

//create the gears from a different library
def bevelGears = ScriptingEngine.gitScriptRun(
            "https://github.com/madhephaestus/GearGenerator.git", // git location of the library
            "bevelGear.groovy" , // file to load
            // Parameters passed to the funcetion
            [(150+toothDepth)*Math.PI/gearPitch,// Number of teeth gear a
	        24,// Number of teeth gear b
	        6,// thickness of gear A
	        6,// gear pitch in arc length mm
	        0,// shaft angle, can be from 0 to 100 degrees
	        0// helical angle, only used for 0 degree bevels
            ]
            )

//create the outside wall
CSG innerVoid = base.scalez(10)
CSG outerWall = new Cylinder(wheelRadius,wheelRadius,50,(int)70).toCSG().difference(innerVoid)

//create and rotate the first scoop
CSG scoopInner = new Cylinder(10,10,wheelHeight+5,(int)30).toCSG()
CSG scoopOuter = new Cylinder(12,12,wheelHeight+5,(int)30).toCSG()
CSG cutPlane = new Cube(40,20,60).toCSG().movey(-10).toZMin()
CSG scoopCombined = scoopOuter.difference(scoopInner).difference(cutPlane).rotz(releaseAngle).rotx(-10).roty(-3).movex(wheelRadius-12)


CSG wheel = base.union(outerWall) //combine base with outter wall

//iteratively add the scoops to the wheel
for(int i=0; i<numberOfScoops; i++){
	wheel=wheel.union(scoopCombined.rotz(i*(360/numberOfScoops)))
}

wheel=wheel.intersect(new Cylinder(wheelRadius,wheelRadius,wheelHeight,(int)70).toCSG()) //remove anything protruding from the drum
wheel = wheel.union(bevelGears.get(0)) //add the gear teeth to the outside of the drum

//add bearing support on the inside of the drum
CSG bearingSupport = new Cylinder(15,10,15,(int)30).toCSG() 
wheel = wheel.union(bearingSupport)

//hole for bear
wheel = wheel.difference(bearingCylinder.movez(15-m5Bearing.getMaxZ()))
wheel = wheel.difference(bearingCylinder)

//TODO: us m5 vitamin
wheel = wheel.difference(m5Cylinder) //hole for m5 bolt

return wheel