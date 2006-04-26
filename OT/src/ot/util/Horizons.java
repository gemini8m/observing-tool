/*==============================================================*/
/*                                                              */
/*                 Joint Astronomy Centre, Hilo                 */
/*                   Copyright (c) PPARC 2006                   */
/*                                                              */
/*==============================================================*/

package ot.util ;

import java.net.URL ;
import java.io.File ;
import java.io.BufferedReader ;
import java.util.TreeMap ;
import java.net.URLEncoder ;
import java.io.FileReader ;
import java.net.MalformedURLException ;
import java.io.IOException ;
import java.io.FileNotFoundException ;
import java.io.InputStreamReader ;
import java.io.InputStream ;
import java.util.Vector ;
import java.io.UnsupportedEncodingException ;

public class Horizons
{

	static final String server =  "http://ssd.jpl.nasa.gov/" ;
	static final String script =  "horizons_batch.cgi?batch=1" ;

	public static void main( String args[] )
	{
		if( args.length == 0 )
			System.exit( -1 ) ;
		String inputFileName = args[ 0 ] ;

		Horizons horizon = new Horizons() ;
		TreeMap treeMap = null ;
		Vector vector = null ;
		URL lut = null ;
		treeMap = horizon.doLookup( inputFileName ) ;
		lut = horizon.URLBuilder( treeMap ) ;
		if( lut != null )
			vector = horizon.connect( lut ) ;
		if( vector != null )
			treeMap = horizon.parse( vector ) ;
		if( treeMap != null )
			printMap( treeMap ) ;		
	}

	public TreeMap doLookup( String name )
	{
		TreeMap treeMap = new TreeMap() ;
		if( name == null || name.trim().equals( "" ) )
		{
			System.out.println( "No name given" ) ;
		}
		treeMap.put( "COMMAND" , name.trim() ) ;
		treeMap.put( "MAKE_EPHEM" , "YES" ) ;
		treeMap.put( "TABLE_TYPE" , "OBSERVER" ) ;
		treeMap.put( "R_T_S_ONLY" , "YES" ) ;
		return treeMap ;
	}

	public static void printMap( TreeMap map )
	{
		if( map == null )
			return ;
		String key , value ;
		Object tmp ;
		while( map.size() != 0 )
		{
			tmp = map.lastKey() ;
			if( !( tmp instanceof String ) )
			{
				System.out.println( tmp + " not a String - something *really* wrong" ) ;
				return ;
			}
			key = ( String )tmp ;
			tmp = map.remove( key ) ;
			value = tmp.toString() ;
			System.out.println( key + " == " + value ) ;
		}
		
	}

	public TreeMap parse( Vector vector )
	{
		String line ;
		TreeMap treeMap = new TreeMap() ;
		QuickMatch quickMatch = QuickMatch.getInstance() ;
		TreeMap tmpMap ;
		while( vector.size() != 0 )
		{
			Object tmp = vector.remove( 0 ) ;
			if( !( tmp instanceof String ) )
				continue ;
			line = ( String )tmp ;
			tmpMap = quickMatch.parseLine( line ) ;
			if( tmpMap != null )
				treeMap.putAll( tmpMap ) ;
		}
		return treeMap ;
	}

	public Vector connect( URL url )
	{
		Vector vector = new Vector() ;
		if( url == null )
		{
			System.out.println( "Null URL" ) ;
			return vector ;
		}
		InputStream stream = null ;
		try
		{
			stream = url.openStream() ;
		}
		catch( IOException ioe )
		{
			System.out.println( "Could not open stream" ) ;
			System.out.println( ioe ) ;
			return vector ;
		}
		InputStreamReader streamReader = new InputStreamReader( stream ) ;
		BufferedReader buffer = new BufferedReader( streamReader ) ;
		try
		{
			while( !buffer.ready() ){}

			String line ;
			while( ( line = buffer.readLine() ) != null )
			{
				if( line.trim().matches( "\\!\\$\\$SOF" ) )
					break ;
				vector.add( line ) ;
			}
		}
		catch( IOException ioe )
		{
			System.out.println( ioe ) ;
			return vector ;
		}
		return vector ;
	}
	
	public URL URLBuilder( TreeMap treeMap )
	{
		URL finalURL = null ;
		StringBuffer buffer = new StringBuffer() ;
		String key , value ;
		Object tmp ;
		while( treeMap.size() != 0 )
		{
			tmp = treeMap.lastKey() ;
			if( !( tmp instanceof String ) )
				continue ;
			key = ( String )tmp ;
			tmp = treeMap.remove( key ) ;
			if( !( tmp instanceof String ) )
				continue ;
			value = ( String )tmp ;
			try
			{
				buffer.append( "&" + key.trim() + "=" + URLEncoder.encode( value.trim() , "UTF-8" ) ) ;
			}
			catch( UnsupportedEncodingException uee )
			{  
				System.out.println( "Character encoding not supported" + uee ) ;
				System.exit( -10 ) ;
			}
		}
		String url = buffer.toString() ;
		buffer = null ;
		buffer = new StringBuffer() ;
		buffer.append( server ) ;
		buffer.append( script ) ;
		buffer.append( url ) ;
		try
		{
			finalURL = new URL( buffer.toString() ) ;
		}
		catch( MalformedURLException mue )
		{
			System.out.println( mue ) ;
			return null ;
		}
		return finalURL ;
	}

	public TreeMap readInputFile( String fileName )
	{
		String line ;
		String[] parts ;
		String key , value ;
		TreeMap treeMap = new TreeMap() ;
		File file = new File( fileName ) ;
		if( !( file.exists() && file.canRead() ) )
		{
			System.out.println( fileName + " not available" ) ;
			return treeMap ;
		}
		FileReader reader = null ;
		try
		{
			reader = new FileReader( file )	;
		}
		catch( FileNotFoundException fnfe )
		{
			System.out.println( fnfe ) ;
			return treeMap ;
		}
		BufferedReader buffer = new BufferedReader( reader ) ;
		try
		{
			while( !buffer.ready() ){}

			while( ( line = buffer.readLine() ) != null )
			{
				parts = line.split( "=" ) ;
				if( parts.length < 2 )
					continue ;
				if( parts.length == 2 ) 
				{
					key = parts[ 0 ].trim() ;
					value = parts[ 1 ].trim() ;
					treeMap.put( key , value ) ;
				}
				else
				{
					System.out.println( "Error ! " + line ) ;
					return treeMap ;
				}
			}
		}
		catch( IOException ioe )
		{
			System.out.println( ioe ) ;
			return treeMap ;
		}
		return treeMap ;
	}
}
