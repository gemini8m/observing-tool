// Copyright 1997 Association for Universities for Research in Astronomy, Inc.,
// Observatory Control System, Gemini Telescopes Project.
// See the file COPYRIGHT for complete details.
//
// $Id$
//
package jsky.app.ot.util;

/**
 * Utility class support for the coordinate systems supported by Gemini.
 *
 * <p>
 * <b>This class and other changes to support multiple coordinate systems
 * have not been completed.</b>
 */
public class CoordSys {
    // The possible coordinate systems.
    public static final int FK5      = 0;
    public static final int FK4      = 1;
    //public static final int APPARENT = 2;
    public static final int AZ_EL    = 2;

    public static final String[] COORD_SYS = {
	"FK5 (J2000)",
	"FK4 (B1950)",
	//"Apparent",
	"Az/El"
    };

    /**
     * Get an integer representing a coordinate system from its associated
     * string.  If the string is not recognizable, return -1.
     */
    public static int getSystem(String coordSysString) {
	coordSysString = coordSysString.toUpperCase();

	if (coordSysString.indexOf("FK5") != -1) {
	    return FK5;
	} 
	else if (coordSysString.indexOf("FK4") != -1) {
	    return FK4;
	} 
	else if (coordSysString.indexOf("AZ") != -1) {
	    return AZ_EL;
	} 
	/*
	else if (coordSysString.indexOf("APP") != -1) {
	    return APPARENT;
	}
	*/
	return -1;
    }
 
    /**
     * Get the string representing a coordinate system from its associated
     * int.  If the int is out of range, return null.
     */
    public static String getSystem(int coordSysInt) {
	if ((coordSysInt < 0) || (coordSysInt >= COORD_SYS.length)) {
	    return null;
	}
	return COORD_SYS[coordSysInt];
    }
}