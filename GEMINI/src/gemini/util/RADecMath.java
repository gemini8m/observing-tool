// Copyright 1997 Association for Universities for Research in Astronomy, Inc.,
// Observatory Control System, Gemini Telescopes Project.
// See the file COPYRIGHT for complete details.
//
// $Id$
//
package gemini.util;


/**
 * Simple RA/Dec math routines.  See also HHMMSS and DDMMSS.
 */
public final class RADecMath
{
 

//
// Rotate the given x/y point through the given angle.
//
private static double[]
_rotate(double x, double y, double angle)
{
   double rad = Angle.degreesToRadians(angle);
   double sin = Angle.sinRadians(rad);
   double cos = Angle.cosRadians(rad);

   double x2 = (x * cos) - (y * sin);
   double y2 = (x * sin) + (y * cos);

   double[] d = {x2, y2};
   return d;
}


//
// Compute the offset of the given position from the given base position,
// both specified in degrees.  The return value is the offset in arcsec.
//
private static double
_getXOff(double pos, double base)
{
   pos  = Angle.normalizeDegrees(pos);
   base = Angle.normalizeDegrees(base);

   //System.out.println("pos = " + pos);
   //System.out.println("base= " + base);

   // In most cases, getting the offset is a simple subtraction.  The
   // difficulty comes when the positions are around 0:00:00 RA, with
   // one slightly east (e.g., 23:59:59) and one slightly west (e.g.,
   // 0:00:01) of 0.

   double offDegrees = pos - base;

   if (Math.abs(offDegrees) > 180) {
      // If the angles are "normalized", and the difference between them
      // greater than 180, one angle must be between 0 and 180, and the other
      // between 180 and 360.  Make them both over 180.
      if (pos < 180) {
         pos  += 360.0;
      } else {
         base += 360.0;
      }
      offDegrees = pos - base;
   }

   //System.out.println("off = " + offDegrees);
   //System.out.println("offA= " + offDegrees * 3600.0);

   Assert.notFalse(offDegrees <= 180);

   return offDegrees * 3600.0;   // Return arcsec
}

/**
 * Get the offset in arcsec given a base position and a reference
 * position both specified in degrees.
 */
public static double[]
getOffset(double posRA,  double posDec,
          double baseRA, double baseDec, double posAngle)
{
   //Assert.notFalse((posDec <= 90.0) && (posDec >= -90.0));
   //Assert.notFalse((baseDec <= 90.0) && (baseDec >= -90.0));
   if (posDec > 90.0) {
      posDec = 180.0 - posDec;
      posRA  = Angle.normalizeDegrees( posRA + 180.0 );
   } else if (posDec < -90.0) {
      posDec = -180.0 - posDec;
      posRA  = Angle.normalizeDegrees( posRA + 180.0 );
   }

   if (baseDec > 90.0) {
      baseDec = 180.0 - baseDec;
      baseRA  = Angle.normalizeDegrees( baseRA + 180.0 );
   } else if (posDec < -90.0) {
      baseDec = -180.0 - baseDec;
      baseRA  = Angle.normalizeDegrees( baseRA + 180.0 );
   }

   double xOff = _getXOff(posRA, baseRA);
   double yOff = (posDec - baseDec) * 3600.0;

   double[] t;
   if (posAngle == 0.0) {
      // Check for a 0 posAngle, just because I suspect it will be a common
      // case.  Would be correct to call _rotate with a 0 posAngle anyway.
      double[] t2 = { xOff, yOff };
      t = t2;
   } else {
      double[] t2 = _rotate( xOff, yOff, posAngle );
      t = t2;
   }

   t[0] = ((double) Math.round(t[0] * 1000.0))/1000.0;
   t[1] = ((double) Math.round(t[1] * 1000.0))/1000.0;

   return t;
}

/**
 * Get the offset in arcsec given a base position and a reference
 * position both specified in degrees.
 */
public static double[]
getOffset(double posRA, double posDec, double baseRA, double baseDec)
{
   return getOffset(posRA, posDec, baseRA, baseDec, 0.0);
}


/**
 * Get the absolute position in degrees given a base position in
 * degrees and an offset in arcsec.
 */
public static double[]
getAbsolute(double ra, double dec, double xOff, double yOff, double posAngle)
{
   // Rotate the offset back through the position angle
   double[] off = { xOff, yOff };
   if (posAngle != 0.0) {
      off = _rotate(xOff, yOff, -posAngle);
   }

   double newRA  = Angle.normalizeDegrees(ra + off[0]/3600.0);

   double newDec = dec + off[1]/3600;
   if (newDec > 90.0) {
      newRA  =  Angle.normalizeDegrees(newRA + 180.0);  // Add 12 hours
      newDec =  180.0 - newDec;
   } else if (newDec < -90.0) {
      newRA  =  Angle.normalizeDegrees(newRA + 180.0);  // Add 12 hours
      newDec = -180.0 - newDec;
   }

   double[] t = { newRA, newDec };
   return t;
}


/**
 * Get the absolute position in degrees given a base position in
 * degrees and an offset in arcsec.
 */
public static double[]
getAbsolute(double ra, double dec, double xOff, double yOff)
{
   return getAbsolute(ra, dec, xOff, yOff, 0.0);
}


/**
 * Convert the given X and Y positions from a String in the given coordinate
 * system to degrees.
 */
public static double[]
string2Degrees(String xaxis, String yaxis, int coordSystem)
{
   // For now, assume everything is FK5/J2000
  // Or FK4 - added by AB for ORAC 13-Oct-99
   Assert.notFalse( (coordSystem == CoordSys.FK5) || (coordSystem == CoordSys.FK4));

   double ra  = 0.0;
   double dec = 0.0;
   try {
      ra  = HHMMSS.valueOf(xaxis);
      dec = DDMMSS.valueOf(yaxis);
   } catch (Exception ex) {
      return null;
   }

   double[] pos = { ra, dec };
   return pos;
}

/**
 * Convert the given ra/dec in degrees to a String in the
 * given coordinate system.
 */
public static String[]
degrees2String(double ra, double dec, int coordSystem)
{
   // For now, assume everything is FK5/J200
  // Or FK4 - added by AB for ORAC 13-Oct-99
   Assert.notFalse((coordSystem == CoordSys.FK5) || (coordSystem == CoordSys.FK4));

   String[] pos = { HHMMSS.valStr(ra), DDMMSS.valStr(dec) };
   return pos;
}

}
