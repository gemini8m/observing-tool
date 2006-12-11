/*==============================================================*/
/*                                                              */
/*                UK Astronomy Technology Centre                */
/*                 Royal Observatory, Edinburgh                 */
/*                 Joint Astronomy Centre, Hilo                 */
/*                   Copyright (c) PPARC 2001                   */
/*                                                              */
/*==============================================================*/
// $Id$

/*
 * Submillimeter Line Catalog in java format.
 * The lines are taken from the JPL catalog.
 *
 *  Authors :
 *   Merce Crosas (mcrosas@cfa.harvard.edu)
 *  History :
 *   01-Aug-1997: original (mcrosas@cfa.harvard.edu)
 *   15-Feb-2000: added returnLines() (bdk@roe.ac.uk)
 *   23-Feb-2000: added returnSpecies() (bdk@roe.ac.uk)
 */ 

package edfreq;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import org.apache.xerces.parsers.SAXParser;
import org.xml.sax.InputSource;
import org.xml.sax.ContentHandler;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;


/**
 * @author Dennis Kelly ( bdk@roe.ac.uk )
 */
public class LineCatalog
{ 

    public static final String lineCatalogFile = "lineCatalog.xml";
    TreeMap catalog;
    private static LineCatalog lineCatalog = null;
    
    protected LineCatalog() throws Exception { 
	// make sure we can get to the line catalog file
	String catalogFile = System.getProperty("ot.cfgdir") + lineCatalogFile;
	File file = new File ( catalogFile );
	if ( !file.exists() ) {
	    Exception e = new Exception ( "Can not open line catalog file "+catalogFile);
	    throw e;
	}

	// If we get here, construct a SAXParser to read the file
	LocalContentHandler handler = new LocalContentHandler();
	try {
	    SAXParser parser = new SAXParser();
	    parser.setContentHandler ( handler );
	    parser.parse ( new InputSource ( new FileReader ( file ) ) );
	}
	catch ( IOException e) {
	    System.out.println("Unable to read string");
	}
	catch (SAXException e) {
	    System.out.println("Unable to create document: "+e.getMessage());
	}
	catalog = handler.getCatalog();
       
    } 

    public void returnLines ( double minFreq, double maxFreq, 
			      int listSize, LineDetails lineRef[] )
    {
	int i;
	int j;
	int pix;
	double invRange;

	/* Search for all lines between minFreq and maxFreq and record their
	   details in lineRef given a linear scaling between index numbers and
	   frequency. */

	minFreq = minFreq * 1.0E-6;
	maxFreq = maxFreq * 1.0E-6;
	invRange = 1.0 / ( maxFreq - minFreq );


	Iterator molecules = catalog.keySet().iterator();
	while ( molecules.hasNext() ) {
	    String molName = (String) molecules.next();
	    TreeMap currentMap = (TreeMap) catalog.get ( molName );

	    // Get a submap of the keys between min and max frequecny
	    TreeMap submap = new TreeMap (currentMap.subMap ( new Double ( minFreq ), new Double ( maxFreq ) ) );
	    // Now fill the values based on this submap
	    Iterator iter = submap.keySet().iterator();
	    while ( iter.hasNext() ) {
		Double currentFreq = (Double)iter.next();
		String currentName = (String) submap.get(currentFreq);
		pix = (int) ( ((double)listSize) * 
			      ( currentFreq.doubleValue() -  minFreq ) *  invRange );
		lineRef[pix] = new LineDetails ( molName, currentName, currentFreq.doubleValue() );
	    }
	}
    }


    public Vector returnSpecies ( double minFreq, double maxFreq  )
    {
	int i;
	int j;
	Vector v = new Vector();
	SelectionList species;
	Transition transition;

	/* Search for all lines between minFreq and maxFreq and record their
	   details. */

	minFreq = minFreq * 1.0E-6;
	maxFreq = maxFreq * 1.0E-6;
	/*
	  for ( j=0; j<nspec; j++ )
	  {
	  species = null;

	  for ( i=0; i<nlines[j]; i++ )
	  {
	  if ( ( dfreqCat[j][i] > minFreq ) && 
	  ( dfreqCat[j][i] < maxFreq ) )
	  {
	  if ( species == null )
	  {
	  species = new SelectionList ( specCat[j] );
	  v.add ( species );
	  }
	  species.objectList.add ( new Transition ( transCat[j][i], 
	  1.0E6 * dfreqCat[j][i] ) );
	  }
	  }
	  }
	*/
	Iterator mol =  catalog.keySet().iterator();
	while ( mol.hasNext() ) {
	    species = null;
	    String currentSpecies = (String) mol.next();
	    TreeMap specMap = (TreeMap) catalog.get ( currentSpecies );
	    TreeMap submap = new TreeMap (specMap.subMap(new Double (minFreq), new Double ( maxFreq )));
	    // Iterate over the frequencies
	    if ( submap.size() > 0 ) {
		Iterator iter = submap.keySet().iterator();
		while ( iter.hasNext() ) {
		    if ( species == null ) {
			species = new SelectionList ( currentSpecies );
			v.add ( species );
		    }
		    Double key = (Double)iter.next();
		    species.objectList.add ( new Transition ( (String)submap.get(key),
							      1.0e6*key.doubleValue() ) );
		}
	    }
	}

	return v;
    }

    public synchronized static LineCatalog getInstance() throws Exception
	{
		if( lineCatalog == null )
		{
			try
			{
				lineCatalog = new LineCatalog();
			}
			catch( Exception e )
			{
				throw e;
			}
		}
		return lineCatalog;
	}

    static class LocalContentHandler implements ContentHandler {
	String    currentSpecies = null;
	TreeMap   speciesTable = new TreeMap();
	TreeMap   freqTransMap;

	public void startElement(String namespace,
				 String localName,
				 String qName,
				 Attributes attr) {
	    if ( qName.equals("species") ) {
		currentSpecies = attr.getValue(attr.getIndex("name"));
		freqTransMap = new TreeMap();
	    }
	    if ( qName.equals("transition") ) {
		String name = attr.getValue(attr.getIndex("name"));
		Double freq = new Double ( attr.getValue(attr.getIndex("frequency")) );
		freqTransMap.put ( freq, name );
	    }

	}

	public void characters (char [] ch, int start, int length) {}
	public void endElement (String namespace, String localName, String qName) {
	    if ( qName.equals("species") ) {
		speciesTable.put ( currentSpecies, freqTransMap );
	    }
	}
	public void endPrefixMapping(String prefix) {};
	public void ignorableWhitespace(char [] ch, int start, int length){};
	public void processingInstruction(String target, String data) {};
        public void setDocumentLocator(Locator l) {};
	public void skippedEntity(String name) {};
        public void startDocument() {};
        public void startPrefixMapping(String prefix, String uri) {};
	public void endDocument() {};
	public TreeMap getCatalog () {
	    return speciesTable;
	}
    }

} 













