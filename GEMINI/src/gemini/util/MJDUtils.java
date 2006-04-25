package gemini.util ;

import java.util.Calendar ;
import java.util.GregorianCalendar ;
import java.util.StringTokenizer ;

public class MJDUtils
{

	public static void main( String[] args )
	{
		double doubleDate = 0. ;
		if( args.length == 0 )
		{
			while( doubleDate < ( mjd * 3 ) )
			{
				if( dateIsDodgy( doubleDate ) )
				{
					System.out.print( doubleDate + " is dodgy " + "\r" ) ;
				}
				doubleDate += 0.25 ;
			}
		}
		else
		{
			String input = args[ 0 ] ;
			doubleDate = Format.toDouble( input ) ;
			if( doubleDate != 0. )
				System.out.println( convertMJD( doubleDate ) ) ;
			else
				System.out.println( convertMJD( input ) ) ;
		}
	}

	public static final double mjd = 2400000.5 ;

	public static boolean dateIsDodgy( double epoch )
	{
		String StringDate = convertMJD( epoch ) ;
		double converted = convertMJD( StringDate ) ;
		if( converted != epoch )
			return true ;
		return false ;
	}
	
	public static double makeMJD( double mjdDays )
	{
		mjdDays %= mjd ;
		return mjdDays ;
	}

	/* coversion ripped from gemini.sp.SpTelescopePos */

   	/** Year of origin of MJD: 1858. */
	public static final int MJD_0_YEAR = 1858;

	/** Year of origin of MJD: java.util.Calendar.NOVEMBER. */
	public static final int MJD_0_MONTH = Calendar.NOVEMBER;

	/** Day of origin of MJD: 17. */
	public static final int MJD_0_DAY = 17;

   	private static final String[] MONTH_NAMES =
	{ "Jan" , "Feb" , "Mar" , "Apr" , "May" , "Jun" , "Jul" , "Aug" , "Sep" , "Oct" , "Nov" , "Dec" };

	/**
	 * Converts TT in MJD to YYYY MM DD.ddd format.
	 * 
	 * @param mjdDays
	 *            TT in MJD (days).
	 * @return TT in YYYY MM DD.ddd format.
	 */
	public static String convertMJD( double mjdDays )
	{
		mjdDays = makeMJD( mjdDays ) ;
		double fullDays = Math.floor( mjdDays );
		double remainder = mjdDays - fullDays;

		remainder = Math.rint( remainder * 1E7 ) / 1E7;

		GregorianCalendar calendar = new GregorianCalendar( MJD_0_YEAR , MJD_0_MONTH , MJD_0_DAY );
		calendar.add( Calendar.DAY_OF_MONTH , ( int ) fullDays );

		return calendar.get( Calendar.YEAR ) + " " + MONTH_NAMES[ calendar.get( Calendar.MONTH ) ] + " " + calendar.get( Calendar.DAY_OF_MONTH ) + ( "" + remainder ).substring( 1 );
	}

	/**
	 * Converts TT in YYYY MM DD.ddd format to MJD (days).
	 * 
	 * @param yyyymmdd_ddd
	 *            TT in YYYY MM DD.ddd format.
	 * @return TT in MJD (days).
	 */
	public static double convertMJD( String yyyymmdd_ddd )
	{
		StringTokenizer stringTokenizer = new StringTokenizer( yyyymmdd_ddd , " ,:;/" );
		GregorianCalendar calendar = new GregorianCalendar();

		int year = 0;

		// monthIndex for January is 0, for December is 11
		int monthIndex = 0;
		double day = 0;

		String monthString = "";

		double resultInDays = 0.0;

		if( stringTokenizer.hasMoreTokens() )
		{
			year = Format.toInt( stringTokenizer.nextToken() );
		}

		if( stringTokenizer.hasMoreTokens() )
		{
			monthString = stringTokenizer.nextToken().toLowerCase();

			while( !monthString.startsWith( MONTH_NAMES[ monthIndex ].toLowerCase() ) )
			{
				monthIndex++ ;
			}
		}

		if( stringTokenizer.hasMoreTokens() )
		{
			day = Format.toDouble( stringTokenizer.nextToken() );
		}

		// Add the remaining 44 days of 1858 (November 18 - December 31 inclusive)
		resultInDays += 44;

		if( year > MJD_0_YEAR )
		{
			for( int i = MJD_0_YEAR + 1 ; i < year ; i++ )
			{
				resultInDays += 365;

				if( calendar.isLeapYear( i ) )
				{
					resultInDays++;
				}
			}
		}
		else if( year <= MJD_0_YEAR )
		{
			for( int i = MJD_0_YEAR ; i >= year ; i-- )
			{
				resultInDays -= 365;

				if( calendar.isLeapYear( i ) )
				{
					resultInDays--;
				}
			}
		}

		switch( monthIndex )
		{
			case 12 :
				resultInDays += 31;
			case 11 :
				resultInDays += 30;
			case 10 :
				resultInDays += 31;
			case 9 :
				resultInDays += 30;
			case 8 :
				resultInDays += 31;
			case 7 :
				resultInDays += 31;
			case 6 :
				resultInDays += 30;
			case 5 :
				resultInDays += 31;
			case 4 :
				resultInDays += 30;
			case 3 :
				resultInDays += 31;
			case 2 :
				resultInDays += 28;
			case 1 :
				resultInDays += 31;
		}
		
		if( ( calendar.isLeapYear( year ) ) && ( monthIndex > 1 ) )
		{
			resultInDays++;
		}

		resultInDays += day;

		resultInDays = Math.rint( resultInDays * 1E7 ) / 1E7;

		// just to be on the safe side

		resultInDays = makeMJD( resultInDays ) ;

		return resultInDays;
	}

}
