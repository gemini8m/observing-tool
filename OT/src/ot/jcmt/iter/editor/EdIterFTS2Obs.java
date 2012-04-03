package ot.jcmt.iter.editor ;

import java.awt.event.ActionListener ;
import java.awt.event.ActionEvent ;
import java.awt.event.KeyEvent ;
import java.awt.event.KeyListener ;

import javax.swing.JTextField ;
import javax.swing.event.ChangeEvent ;
import javax.swing.event.ChangeListener ;

import gemini.sp.SpAvTable ;
import gemini.sp.SpItem ;
import gemini.util.MathUtil ;
import orac.jcmt.iter.SpIterFTS2 ;
import jsky.app.ot.editor.OtItemEditor ;
import jsky.app.ot.gui.DropDownListBoxWidgetExt ;

public final class EdIterFTS2Obs extends OtItemEditor implements ActionListener , ChangeListener , KeyListener
{
	private SpIterFTS2 _inst ;

	private IterFTS2ObsGUI _w ;

	public EdIterFTS2Obs()
	{
		_title = "JCMT FTS-2" ;
		_presSource = _w = new IterFTS2ObsGUI() ;
		_description = "FTS-2" ;
		_w.specialModes.setChoices( SpIterFTS2.SPECIAL_MODES ) ;
		
		_w.specialModes.addActionListener( this ) ;
		_w.dual.addActionListener( this ) ;
		_w.single.addActionListener( this ) ;
		_w.port1.addActionListener( this ) ;
		_w.port2.addActionListener( this ) ;

		_w.resolutionFOV.setMinimum( SpIterFTS2.minimumResolutionScaled ) ;
		_w.resolutionFOV.setMaximum( SpIterFTS2.maximumResolutionScaled ) ;
		_w.resolutionFOV.addChangeListener( this ) ;

		_w.scanSpeedNyquist.setMinimum( SpIterFTS2.minimumSpeedScaled ) ;
		_w.scanSpeedNyquist.setMaximum( SpIterFTS2.maximumSpeedScaled ) ;
		_w.scanSpeedNyquist.setMajorTickSpacing( SpIterFTS2.speedIncrementScaled ) ;
		_w.scanSpeedNyquist.addChangeListener( this ) ;

		_w.sensitivity450.addKeyListener( this ) ;
		_w.sensitivity850.addKeyListener( this ) ;
		_w.integrationTime.addKeyListener( this ) ;
	}

	/**
	 * Override setup to store away a reference to the SpInstSCUBA2 item.
	 */
	public void setup( SpItem spItem )
	{
		_inst = ( SpIterFTS2 )spItem ;
		super.setup( spItem ) ;
	}

	/**
	 * Implements the _updateWidgets method from OtItemEditor in order to
	 * setup the widgets to show the current values of the item.
	 */
	protected void _updateWidgets()
	{
		_w.specialModes.setSelectedItem( _inst.getSpecialMode() ) ;
		boolean isPort1 = _inst.getTrackingPort() == 1 ;
		_w.port1.setSelected( isPort1 ) ;
		_w.port2.setSelected( !isPort1 ) ;
		boolean isDualPort = _inst.isDualPort() ;
		_w.dual.setSelected( isDualPort ) ;
		_w.single.setSelected( !isDualPort ) ;

		double resolution = _inst.getResolution() ;
		double speed = _inst.getScanSpeed() ;
		_w.resolutionFOV.setValue( ( int )( resolution * SpIterFTS2.resolutionScale ) ) ;
		_w.scanSpeedNyquist.setValue( ( int )( speed * SpIterFTS2.speedScale ) ) ;
		_w.FOV.setText( "" + MathUtil.round( _inst.getFOV() , 4 ) ) ;
		_w.resolution.setText( "" + MathUtil.round( resolution , 4 ) ) ;
		_w.resolutionMHz.setText( "" + MathUtil.round( _inst.getResolutionInMHz() , 4 ) ) ;
		_w.scanSpeed.setText( "" + speed ) ;
		_w.nyquist.setText( "" + MathUtil.round( _inst.getNyquist() , 4 ) ) ;
		_w.southernPanelEnabled( SpIterFTS2.VARIABLE_MODE.equals( _inst.getSpecialMode() ) ) ;
			_w.sensitivity450.setText( "" + MathUtil.round( _inst.getSensitivity450() , 5 ) ) ;
			_w.sensitivity850.setText( "" + MathUtil.round( _inst.getSensitivity850() , 5 ) ) ;
		if( !keypress )
			_w.integrationTime.setText( "" + _inst.getElapsedTime() ) ;
	}

    public void actionPerformed( ActionEvent e )
    {
	    Object source = e.getSource() ;
	    if( _w.specialModes.equals( source ) )
	    {
	    	Object item = (( DropDownListBoxWidgetExt )source).getSelectedItem() ;
	    	if( item instanceof String )
			{
				_inst.setSpecialMode( ( String )item ) ;
				boolean variableMode = SpIterFTS2.VARIABLE_MODE.equals( item ) ;
				_w.southernPanelEnabled( variableMode ) ;
				SpAvTable table = _inst.getTable() ;
				if( variableMode )
				{
					_inst.setResolution( _inst.getResolution() ) ;
					_inst.setScanSpeed( _inst.getScanSpeed() ) ;
				}
				else
				{
					table.rm( SpIterFTS2.FOV ) ;
					table.rm( SpIterFTS2.SCAN_SPEED ) ;
				}
			}
	    }
	    else if( _w.dual.equals( source ) )
	    {
	    	_inst.setIsDualPort( true ) ;
		resetTPE();
	    }
	    else if( _w.single.equals( source ) )
	    {
	    	_inst.setIsDualPort( false ) ;
		resetTPE();
	    }
	    else if( _w.port1.equals( source ) )
	    {
	    	_inst.setTrackingPort( 1 ) ;
		resetTPE();
	    }
	    else if( _w.port2.equals( source ) )
	    {
	    	_inst.setTrackingPort( 2 ) ;
		resetTPE();
	    }
	}

	public void stateChanged( ChangeEvent e )
	{
		Object source = e.getSource() ;
		if( source == _w.resolutionFOV )
		{
			int fovResolution = _w.resolutionFOV.getValue() ;
			double resolution = fovResolution / SpIterFTS2.resolutionScale ;
			_inst.setResolution( resolution ) ;
			_updateWidgets() ;
		}
		else if( source == _w.scanSpeedNyquist )
		{
			int scanSpeed = _w.scanSpeedNyquist.getValue() ;
			double speed = scanSpeed / SpIterFTS2.speedScale ;
			_inst.setScanSpeed( speed ) ;
			_updateWidgets() ;
		}
	}

    public void keyPressed( KeyEvent e ){}

    boolean keypress = false ;
    public void keyReleased( KeyEvent e )
    {
    	JTextField textBox = ( JTextField )e.getSource() ;
    	if( textBox == _w.integrationTime )
    	{
    		String value = textBox.getText() ;	
    		_inst.setSampleTime( value ) ;
    		keypress = true ;
    		_updateWidgets() ;
    		keypress = false ;
    	}
    }

    public void keyTyped( KeyEvent e ){}
}
