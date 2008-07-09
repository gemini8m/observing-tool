/*==============================================================*/
/*                                                              */
/*                UK Astronomy Technology Centre                */
/*                 Royal Observatory, Edinburgh                 */
/*                 Joint Astronomy Centre, Hilo                 */
/*                   Copyright (c) PPARC 2001                   */
/*                                                              */
/*==============================================================*/
// $Id$
package ot.jcmt.iter.editor ;

import jsky.app.ot.gui.DropDownListBoxWidgetExt;
import orac.jcmt.iter.SpIterFlatObs ;
import orac.jcmt.inst.SpInstSCUBA2 ;
import gemini.sp.SpItem ;
import gemini.sp.obsComp.SpInstObsComp ;

/**
 * This is the editor for Focus Observe Mode iterator component.
 *
 * @author modified for JCMT by Martin Folger ( M.Folger@roe.ac.uk )
 */
public final class EdIterFlatObs extends EdIterJCMTGeneric
{
	private IterFlatObsGUI _w ; // the GUI layout panel
	private SpIterFlatObs _iterObs ;

	/**
	 * The constructor initializes the title, description, and presentation source.
	 */
	public EdIterFlatObs()
	{
		super( new IterFlatObsGUI() ) ;

		_title = "Flat" ;
		_presSource = _w = ( IterFlatObsGUI )super._w ;
		_description = "Flat Observation Mode" ;

		_w.jPanel1.setVisible( false ) ;
		_w.flatSourceComboBox.addWatcher( this ) ;
	}

	/**
	 * Override setup to store away a reference to the Focus Iterator.
	 */
	public void setup( SpItem spItem )
	{
		_iterObs = ( SpIterFlatObs )spItem ;
		String[] choices = _iterObs.getFlatSources() ;
		if( choices != null )
			_w.flatSourceComboBox.setChoices( choices ) ;
		super.setup( spItem ) ;
	}
	
	public void dropDownListBoxAction( DropDownListBoxWidgetExt ddlbwe , int index , String val )
	{
		if( ddlbwe == _w.flatSourceComboBox )
			_iterObs.setFlatSource( ( String )_w.flatSourceComboBox.getSelectedItem() ) ;
		else
			super.dropDownListBoxAction( ddlbwe , index , val ) ;
	}

	protected void _updateWidgets()
	{
		String flatSource = _iterObs.getFlatSource() ;
		if( flatSource != null )
			_w.flatSourceComboBox.setSelectedItem( flatSource ) ;
		super._updateWidgets() ;
	}

	public void setInstrument( SpInstObsComp spInstObsComp )
	{
		_w.jPanel1.setVisible( false ) ;
		super.setInstrument( spInstObsComp ) ;
		_w.setVisible( spInstObsComp != null && spInstObsComp instanceof SpInstSCUBA2 ) ;
	}
}
