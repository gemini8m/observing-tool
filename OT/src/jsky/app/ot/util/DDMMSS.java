// Copyright 1997 Association for Universities for Research in Astronomy, Inc.,
// Observatory Control System, Gemini Telescopes Project.
// See the file COPYRIGHT for complete details.
//
// $Id$
//
/*
 * NCSA Horizon Image Browser
 * Project Horizon
 * National Center for Supercomputing Applications
 * University of Illinois at Urbana-Champaign
 * 605 E. Springfield, Champaign IL 61820
 * horizon@ncsa.uiuc.edu
 *
 * Copyright (C) 1996, Board of Trustees of the University of Illinois
 *
 * NCSA Horizon software, both binary and source (hereafter, Software) is
 * copyrighted by The Board of Trustees of the University of Illinois
 * (UI), and ownership remains with the UI.
 *
 * You should have received a full statement of copyright and
 * conditions for use with this package; if not, a copy may be
 * obtained from the above address.  Please see this statement
 * for more details.
 */
package jsky.app.ot.util;

import java.util.StringTokenizer;

/**
 * Support for converting between angles in string
 * and double representations.
 */
public class DDMMSS
{
   public static final String MYNAME =
        "Degree-Angle (+/- 90) Coordinate Axis Position Formatter";

/**
 * Covert from a Dec in degrees to a DD:MM:SS String representation.
 */
public static String
valStr(double degrees, int prec)
{ 
   int sign = (degrees < 0) ? -1 : 1;
   degrees  = Math.abs(degrees);

   //System.out.println("--------------> " + MathUtil.doubleToString(degrees));
   int    dd = (int)degrees;
   double tmp= (degrees - (double) dd) * 60.0;
   int    mm = (int) tmp;
   double ss = (tmp - (double) mm) * 60.0;
   //System.out.println("--------------> " + dd + ", " + mm + ", " + ss);

   // correct for formating errors caused by rounding
   if (ss > 59.99999) { 
      ss  = 0;
      mm += 1;
      if (mm >= 60) {
         mm  = 0;
         dd += 1;
      }
   }

   StringBuffer out = new StringBuffer();
   if (sign < 0) out.append('-');
   out.append(dd);
   if (prec == -2) return out.toString();

   out.append(':');
   if (mm < 10) out.append('0');
   out.append(mm);
   if (prec == -1) return out.toString();

   out.append(':');

   // Ignoring prec for now
   ss = ((double) Math.round(ss * 100.0))/100.0;
   if (ss < 10) out.append('0');
   out.append(ss);


   //if (prec < -2) {
   //   if (ss < 0.000099) 
   //      out.append("0.0000");
   //   else
   //      out.append(ss);
   //} else {
   //   // specific precision requested; NOT YET SUPPORTED
   //   out.append( ss );
   //}
   return out.toString();
}

/**
 * Covert from a Dec in degrees to a DD:MM:SS String representation.
 */
public static String
valStr(double degrees)
{ 
   return valStr(degrees, -3);
}


/**
 * Convert from a Dec in DD:MM:SS string format to degrees.
 */
public static double
valueOf(String s) throws NumberFormatException
{
   if (s == null) throw new NumberFormatException(s);

   // Determine the sign from the (trimmed) string
   s = s.trim();
   if (s.length() == 0) throw new NumberFormatException(s);
   int sign = 1;
   if (s.charAt(0) == '-') {
      sign = -1;
      s    = s.substring(1);
   }

   // NOTE: Added a space character to the delimiter string (shane)
   double[] vals = {0.0, 0.0, 0.0};
   StringTokenizer tok = new StringTokenizer(s, ": ");
   for(int i=0; i<3 && tok.hasMoreTokens(); i++) {
       vals[i] = Double.valueOf(tok.nextToken()).doubleValue();
   }

   double out = sign * (vals[0] + vals[1]/60.0 + vals[2]/3600.0);
   return out;
}


/**
 * For testing.
 */
public static void
main(String args[])
{
   for (int i=0; i<args.length; ++i) {
      double converted = DDMMSS.valueOf(args[i]);
      String back      = DDMMSS.valStr(converted);
      System.out.println(args[i]   + " = " + converted);
      System.out.println(converted + " = " + back);
   }
}

}
