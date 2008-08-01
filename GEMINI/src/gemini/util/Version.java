package gemini.util ;

import java.io.BufferedReader ;

import java.net.URL ;
import java.io.InputStream ;
import java.io.InputStreamReader ;
import java.io.File ;

public class Version
{

	private static Version versionObject = new Version() ;

	private Version()
	{
		setVersion() ;
	}

	public static Version getInstance()
	{
		return versionObject ;
	}

	public String getVersion()
	{
		return System.getProperty( "ot.version" ) ;
	}

	public String getFullVersion()
	{
		return System.getProperty( "ot.fullversion" ) ;
	}

	private void setVersion()
	{
		try
		{
			String cfgFilename = System.getProperty( "ot.cfgdir" , "ot/cfg/" ) + "versionFile" ;
			URL url = null ;
			if( !cfgFilename.matches( "^\\w+://.*" ) )
				url = new File( cfgFilename ).toURL()  ;
			else
				url = new URL( cfgFilename ) ;
			InputStream is = url.openStream() ;
			BufferedReader br = new BufferedReader( new InputStreamReader( is ) ) ;
			String fullVersion = br.readLine().trim() ;
			String version = "unknown" ;
			if( fullVersion.matches( "\\d{8} \\[\\w*:?\\w*\\]" ) )
				version = fullVersion.substring( 0 , 8 ) ;
			System.setProperty( "ot.version" , version ) ;
			System.setProperty( "ot.fullversion" , fullVersion ) ;
			br.close() ;
		}
		catch( Exception e )
		{
			e.printStackTrace() ;
		}
	}
}
