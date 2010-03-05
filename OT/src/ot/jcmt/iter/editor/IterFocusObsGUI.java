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

import javax.swing.JPanel ;
import javax.swing.JLabel ;
import javax.swing.BorderFactory ;
import javax.swing.border.Border ;
import javax.swing.border.BevelBorder ;
import java.awt.GridBagLayout ;
import java.awt.GridBagConstraints ;
import java.awt.Insets ;
import java.awt.Color ;
import java.awt.BorderLayout ;
import jsky.app.ot.gui.DropDownListBoxWidgetExt ;
import jsky.app.ot.gui.TextBoxWidgetExt ;

/**
 * Title:        <p>
 * Description:  <p>
 * Copyright:    Copyright (c) Martin Folger<p>
 * Company:      UK ATC<p>
 * @author Martin Folger
 * @version 1.0
 */

@SuppressWarnings( "serial" )
public class IterFocusObsGUI extends IterJCMTGenericGUI
{
	JPanel scubaAcsisPanel = new JPanel() ;
	GridBagLayout gridBagLayout1 = new GridBagLayout() ;
	JLabel jLabel3 = new JLabel() ;
	DropDownListBoxWidgetExt axis = new DropDownListBoxWidgetExt() ;
	JLabel stepsLabel = new JLabel() ;
	TextBoxWidgetExt steps = new TextBoxWidgetExt() ;
	JLabel mmLabel = new JLabel() ;
	JLabel jLabel6 = new JLabel() ;
	TextBoxWidgetExt focusPoints = new TextBoxWidgetExt() ;
	GridBagLayout gridBagLayout2 = new GridBagLayout() ;
	JPanel acsisPanel = new JPanel() ;
	JLabel jLabel2 = new JLabel() ;
	JLabel jLabel1 = new JLabel() ;

	public IterFocusObsGUI()
	{
		try
		{
			jbInit() ;
		}
		catch( Exception e )
		{
			e.printStackTrace() ;
		}
	}

	private void jbInit() throws Exception
	{
		scubaAcsisPanel.setLayout( gridBagLayout1 ) ;
		Border bevelBorder = BorderFactory.createBevelBorder( BevelBorder.LOWERED ) ;
		Border titleBorder = BorderFactory.createTitledBorder( bevelBorder , "Focus setup" ) ;
		scubaAcsisPanel.setBorder( titleBorder ) ;
		jLabel3.setFont( new java.awt.Font( "Dialog" , 0 , 12 ) ) ;
		jLabel3.setForeground( Color.black ) ;
		jLabel3.setText( "Axis" ) ;
		stepsLabel.setFont( new java.awt.Font( "Dialog" , 0 , 12 ) ) ;
		stepsLabel.setForeground( Color.black ) ;
		stepsLabel.setText( "Steps" ) ;
		steps.setColumns( 8 ) ;
		mmLabel.setFont( new java.awt.Font( "Dialog" , 0 , 12 ) ) ;
		mmLabel.setForeground( Color.black ) ;
		mmLabel.setText( "(mm)" ) ;
		jLabel6.setFont( new java.awt.Font( "Dialog" , 0 , 12 ) ) ;
		jLabel6.setForeground( Color.black ) ;
		jLabel6.setText( "No of Focus Points" ) ;
		secsPerCycle.setColumns( 8 ) ;
		acsisPanel.setLayout( gridBagLayout2 ) ;
		cycleReversal.setText( "Cycle Reversal" ) ;
		cycleReversal.setFont( new java.awt.Font( "Dialog" , 0 , 12 ) ) ;
		jLabel1.setFont( new java.awt.Font( "Dialog" , 0 , 12 ) ) ;
		jLabel1.setForeground( Color.black ) ;
		jLabel1.setText( "Secs per Cycle" ) ;
		axis.setFont( new java.awt.Font( "Dialog" , 0 , 12 ) ) ;
		automaticTarget.setText( "Automatic focus/align target" ) ;
		automaticTarget.setFont( new java.awt.Font( "Dialog" , 0 , 12 ) ) ;
		automaticTarget.setSelected( true ) ;
		this.add( scubaAcsisPanel , BorderLayout.CENTER ) ;
		scubaAcsisPanel.add( jLabel3 , new GridBagConstraints( 0 , 0 , 1 , 1 , 0. , 0. , GridBagConstraints.EAST , GridBagConstraints.NONE , new Insets( 0 , 0 , 0 , 0 ) , 0 , 0 ) ) ;
		scubaAcsisPanel.add( axis , new GridBagConstraints( 1 , 0 , 2 , 1 , 0. , 0. , GridBagConstraints.WEST , GridBagConstraints.NONE , new Insets( 5 , 5 , 5 , 5 ) , 0 , 0 ) ) ;
		scubaAcsisPanel.add( stepsLabel , new GridBagConstraints( 0 , 1 , 1 , 1 , 0. , 0. , GridBagConstraints.EAST , GridBagConstraints.NONE , new Insets( 0 , 20 , 0 , 0 ) , 0 , 0 ) ) ;
		scubaAcsisPanel.add( steps , new GridBagConstraints( 1 , 1 , 1 , 1 , 0. , 0. , GridBagConstraints.CENTER , GridBagConstraints.NONE , new Insets( 5 , 5 , 5 , 5 ) , 0 , 0 ) ) ;
		scubaAcsisPanel.add( mmLabel , new GridBagConstraints( 2 , 1 , 1 , 1 , 0. , 0. , GridBagConstraints.WEST , GridBagConstraints.NONE , new Insets( 0 , 0 , 0 , 0 ) , 0 , 0 ) ) ;
		scubaAcsisPanel.add( jLabel6 , new GridBagConstraints( 0 , 3 , 1 , 1 , 0. , 0. , GridBagConstraints.EAST , GridBagConstraints.NONE , new Insets( 0 , 0 , 0 , 0 ) , 0 , 0 ) ) ;
		scubaAcsisPanel.add( focusPoints , new GridBagConstraints( 1 , 3 , 1 , 1 , 0. , 0. , GridBagConstraints.CENTER , GridBagConstraints.HORIZONTAL , new Insets( 5 , 5 , 5 , 5 ) , 0 , 0 ) ) ;
		scubaAcsisPanel.add( automaticTarget , new GridBagConstraints( 0 , 4 , 3 , 1 , 0. , 0. , GridBagConstraints.CENTER , GridBagConstraints.NONE , new Insets( 20 , 0 , 0 , 0 ) , 0 , 0 ) ) ;
		this.add( acsisPanel , BorderLayout.SOUTH ) ;
	}
}
