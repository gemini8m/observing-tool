/*==============================================================*/
/*                                                              */
/*                UK Astronomy Technology Centre                */
/*                 Royal Observatory, Edinburgh                 */
/*                 Joint Astronomy Centre, Hilo                 */
/*                   Copyright (c) PPARC 2001                   */
/*                                                              */
/*==============================================================*/
// $Id$

package ot.jcmt.inst.editor;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.*;
import javax.swing.table.*;
import java.awt.event.*;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.FlowLayout;
import java.awt.Container;
import java.awt.Component;
import java.awt.Point;
import java.util.*;
import java.io.*;

import gemini.sp.SpItem;
import gemini.sp.SpTreeMan;
import gemini.sp.SpTelescopePos;
import gemini.sp.obsComp.SpTelescopeObsComp;
import orac.util.TelescopeUtil;
import orac.jcmt.inst.SpInstHeterodyne;
import orac.jcmt.iter.SpIterStareObs;
import edfreq.*;
import jsky.app.ot.editor.OtItemEditor;
import ot.jcmt.iter.editor.IterJCMTGenericGUI;

import org.apache.xerces.parsers.DOMParser;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


//------- NOTES -------------
//
// Transition Strings is GUI and SpInstHeterodyne
// 
// All the transitions Strings in the LineCatalog end with a white space " ".
// The XML methods of SpItem remove this white space when SpInstHeterodyne is
// saved to XML and read back in again. But when the setSelectedItem() method of
// the _w.transitionChoice JComboBox is called with this trimmed String it wound find
// the right String to select.
// It therefore important to make sure that an white space is added to the
// transition String obtained via _inst.getTransition(0) before it is used
// in the GUI, e.g.
//   _w.transitionChoice.setSelectedItem(getObject(_w.transitionChoice, _inst.getTransition(0) + " "));
//
// And before a transition String fron the GUI is saved to the _inst the white space
// should be removed by trimming the String, e.g.
//  _inst.setTransition(_w.transitionChoice.getSelectedItem().toString().trim(), 0);
//
// --------------------------------------------------------

/**
 * Heterodyne Editor.
 *
 * This class implements the HeterodyneEditor interface. Its implementation is based
 * on the old edfreq.FrontEnd class which is not used anymore.
 *
 * @author Dennis Kelly ( bdk@roe.ac.uk ), modified by Martin Folger (M.Folger@roe.ac.uk)
 */
public class EdCompInstHeterodyne extends OtItemEditor implements ActionListener,
    HeterodyneEditor {

  private LineCatalog _lineCatalog;

  private static final String FREQ_EDITOR_CFG_PROPERTY = "FREQ_EDITOR_CFG";

  private SideBandDisplay _frequencyEditor;

  /**
   * A static configuration object which can used by classes throughout this
   * package to configure themselves.
   */
  protected static FrequencyEditorCfg _cfg = FrequencyEditorCfg.getConfiguration();

  private SpInstHeterodyne _inst;

  boolean _hidingFrequencyEditor = false;

  private HeterodyneGUI _w;		// the GUI layout

  // Arrays of component names
  HashMap feWidgetNames = new HashMap();
  HashMap modeWidgetNames = new HashMap();
  HashMap regionWidgetNames = new HashMap();
  HashMap mixerWidgetNames = new HashMap();
  HashMap sidebandWidgetNames = new HashMap();
  HashMap freqPanelWidgetNames = new HashMap();
  HashMap vPanelWidgetNames = new HashMap();

  // Current receiver
  Receiver _receiver;

  // Information related to each possible spectral region
  Vector [] _regionInfo = new Vector[Integer.parseInt(_w.SUBSYSTEMS[_w.SUBSYSTEMS.length - 1])];

  public EdCompInstHeterodyne() {
      _title       ="JCMT Heterodyne";
      _presSource  = _w = new HeterodyneGUI();
      _description ="The Heterodyne instrument is configured with this component.";

      try {
  	  _lineCatalog = LineCatalog.getInstance();
      }
      catch (Exception e) {};

      if(_frequencyEditor == null) {
         _frequencyEditor = new SideBandDisplay(this);
      }
      _frequencyEditor.addComponentListener ( new ComponentAdapter () {
		 public void componentHidden( ComponentEvent e ) {
		     // If the user has deliberately closed the window
		     // without using the hide button, this will
		     // do the same except we won't get the latest
		     // configuration
		     if ( !_hidingFrequencyEditor ) {
		         enableNamedWidgets(true);
		         _updateWidgets();
		     }
		 }
      });

      // Add listeners to stuff on the front end
      // configuration panel
      for (int i=0; i<_w.feSelector.getComponentCount(); i++) {
	  if ( _w.feSelector.getComponent(i) instanceof JRadioButton ) {
	      ((JRadioButton)_w.feSelector.getComponent(i)).addActionListener ( new ActionListener () {
										public void actionPerformed (ActionEvent e) {
										feAction(e);
										}
										});
	     feWidgetNames.put ( _w.feSelector.getComponent(i).getName(),
		     new Integer(i) );
	  }
      }

      for (int i=0; i<_w.modeSelector.getComponentCount(); i++ ) {
	  if ( _w.modeSelector.getComponent(i) instanceof JRadioButton ) {
	      ((JRadioButton)_w.modeSelector.getComponent(i)).addActionListener ( new ActionListener() {
										      public void actionPerformed (ActionEvent e) {
										      modeAction(e);
										      }
										      });
	      modeWidgetNames.put ( _w.modeSelector.getComponent(i).getName(),
		      new Integer(i) );
	  }
      }

      for ( int i=0; i<_w.regionSelector.getComponentCount(); i++ ) {
	  if ( _w.regionSelector.getComponent(i) instanceof JRadioButton ) {
	      ((JRadioButton)_w.regionSelector.getComponent(i)).addActionListener ( new ActionListener() {
										       public void actionPerformed(ActionEvent e) {
										       regionAction(e);
										       }
										       });
	      regionWidgetNames.put ( _w.regionSelector.getComponent(i).getName(),
		      new Integer(i) );
	  }
      }

      for ( int i=0; i<_w.mixerSelector.getComponentCount(); i++) {
	  if ( _w.mixerSelector.getComponent(i) instanceof JRadioButton ) {
	      ((JRadioButton)_w.mixerSelector.getComponent(i)).addActionListener ( new ActionListener () {
										   public void actionPerformed (ActionEvent e) {
										   mixerAction(e);
										   }
										   });
	      mixerWidgetNames.put (  _w.mixerSelector.getComponent(i).getName(),
		      new Integer(i) );
	  }
      }

      for ( int i=0; i<_w.sbSelector.getComponentCount(); i++ ) {
	  if ( _w.sbSelector.getComponent(i) instanceof JRadioButton ) {
	      ((JRadioButton)_w.sbSelector.getComponent(i)).addActionListener ( new ActionListener() {
										 public void actionPerformed(ActionEvent e) {
										 sbSelectAction(e);
										 }
										 });
	      sidebandWidgetNames.put (  (Object)_w.sbSelector.getComponent(i).getName(),
		      new Integer(i) );
	  }
      }

      // Add other listeners to the components
      // of the frequency and button
      // panels.  We don't need to add anything to the
      // velocity panel since it is for show only.
      _w.bandwidths.addActionListener(this);
      for ( int i=0; i<_w.fPanel.getComponentCount(); i++) {
	  // The fPanel contains a mix of JCombBoxes
	  // buttons and textfields.  We only register
	  // interest in the first two.
	  if ( _w.fPanel.getComponent(i) instanceof AbstractButton ) {
	      ((AbstractButton)_w.fPanel.getComponent(i)).addActionListener(this);
	      freqPanelWidgetNames.put ( _w.fPanel.getComponent(i).getName(), new Integer(i) );
	  }
	  else if ( _w.fPanel.getComponent(i) instanceof JComboBox ) {
	      ((JComboBox)_w.fPanel.getComponent(i)).addActionListener(this);
	      freqPanelWidgetNames.put ( _w.fPanel.getComponent(i).getName(), new Integer(i) );
	  }
	  else if ( _w.fPanel.getComponent(i) instanceof JTextField ) {
	      ((JTextField)_w.fPanel.getComponent(i)).addKeyListener( new KeyAdapter() {
								      public void keyTyped(KeyEvent ke) {
								      freqAction();
								      }
								      });
	      freqPanelWidgetNames.put ( _w.fPanel.getComponent(i).getName(), new Integer(i) );
	  }
      }

      for ( int i=0; i<_w.vPanel.getComponentCount(); i++ ) {
	  String name = _w.vPanel.getComponent(i).getName();
	  if ( name != null ) {
	      vPanelWidgetNames.put( name, new Integer(i) );
	  }
      }
      // Initially disable the accept button
      toggleEnabled(_w.fPanel, "Accept", false);

      for ( int i=0; i<_w.bPanel.getComponentCount(); i++ ) {
	  // The button panel only contains buttons, so 
	  // just add action listeners
	  ((AbstractButton)_w.bPanel.getComponent(i)).addActionListener(this);
      }

      // Disable the hide button...It should only be enabled when 
      // the frequency editor is visible
      toggleEnabled( _w.bPanel, "hide", false);

      // Add an ancestor listener.  This will be invoked
      // when a user changes to another component, and
      // will force the accept button to be pressed.
      _w.addAncestorListener ( new AncestorListener() {
	      public void ancestorAdded(AncestorEvent e) {};
	      public void ancestorMoved(AncestorEvent e) {};
	      public void ancestorRemoved( AncestorEvent e) {
	      clickButton (_w.fPanel, "Accept");
	      }
	      });

      // Booleans indicating what the user edited

   }

   /** Initialises the default values in SpInstHeterodyne. */
   private void _initialiseInstHeterodyne() {
      String   frontEndName = _cfg.frontEnds[0];
      _receiver     = (Receiver)_cfg.receivers.get(frontEndName);
      BandSpec bandSpec     = (BandSpec)(_receiver.bandspecs.get(0));

      // Get hold of the lines in the upper sideband that. Make sure it is not to close to
      // the edge of the range so that the IF can default to the frontend IF.
      // One of the lines should be a CO transition. Find it it use it as default line.
      Vector moleculeVector = _lineCatalog.returnSpecies(_receiver.loMin + _receiver.feIF + _receiver.bandWidth,
                                                        _receiver.loMax                 - _receiver.bandWidth);
      String molecule       = "CO";
      String transitionName = "";
      String frequency      = "";

      Transition transition = null;

      for(int i = 0; i < moleculeVector.size(); i++) {
         if(moleculeVector.get(i).toString().trim().equals(molecule)) {
            transition = (Transition)((SelectionList)moleculeVector.get(i)).objectList.get(0);
         }
      }

      if(transition != null) {

         // Whenever a trinsition is taken from the LineCatalog and consequently saved to
         // it has to be trimmed in order to remove the trailing white space that each transition
         // in the LineCatalog has.
         transitionName = transition.name.trim();
         frequency      = "" + transition.frequency;
      }

      // Find out which backend we are using
      String beName = System.getProperty("FREQ_EDITOR_CFG");
      if (beName.indexOf("das") != -1) {
	  beName = "das";
	  // Remove the listeners from the region selector
      }
      else {
	  // Add the listeners to the region selector
	  beName = "acsis";
      }
 
      _inst.initialiseValues(
	 beName,                                                // Back end name
         frontEndName,						// front end
         ((String[])_cfg.frontEndTable.get(frontEndName))[0],	// mode
         bandSpec.toString(),					// band mode
         ((String[])_cfg.frontEndMixers.get(frontEndName))[0],	// mixer
         "" + bandSpec.defaultOverlaps[0],			// overlap
         "best",						// band
         "" + _receiver.feIF,					// centre frequency
         "" + _receiver.feIF,					// centre frequency
         "" + bandSpec.getDefaultOverlapBandWidths()[0],	// bandwidth
         molecule,						// molecule
         transitionName,					// transition
         frequency						// rest frequency
      );

   }

  /**
   * Override setup to store away a reference to the SpInst item.
   */
  public void setup(SpItem spItem) {
    _inst = (SpInstHeterodyne)spItem;
    super.setup(spItem);
    setAvailableModes();
    setAvailableMixers();
    setAvailableRegions();
    setAvailableMolecules();
    setAvailableTransitions();
    setAvailableSidebands();
  }


  /**
   * Implements the _updateWidgets method from OtItemEditor in order to
   * setup the widgets to show the current values of the item.
   */
  protected void _updateWidgets() {
      if ( !_inst.valuesInitialised() ) {
	  _initialiseInstHeterodyne();
	  // Make sure we click the front end button to 
	  // set things up correctly
	  clickButton( _w.feSelector, _inst.getFrontEnd() );
	  _initialiseRegionInfo();
      }

      _receiver = (Receiver)_cfg.receivers.get(_inst.getFrontEnd());

      // Update the front end panel
      ((AbstractButton) _w.feSelector.getComponent ( ((Integer)feWidgetNames.get(_inst.getFrontEnd())).intValue() )).setSelected(true);
      ((AbstractButton) _w.modeSelector.getComponent ( ((Integer)modeWidgetNames.get(_inst.getMode())).intValue() )).setSelected(true);
      ((AbstractButton) _w.regionSelector.getComponent ( ((Integer)regionWidgetNames.get(_inst.getBandMode())).intValue() )).setSelected(true);
      ((AbstractButton) _w.mixerSelector.getComponent ( ((Integer)mixerWidgetNames.get(_inst.getMixer())).intValue() )).setSelected(true);
      ((AbstractButton) _w.sbSelector.getComponent ( ((Integer)sidebandWidgetNames.get(_inst.getBand())).intValue() )).setSelected(true);

      // Update the bandwidth
      _updateBandwidths();
      /*
      _w.bandwidths.removeActionListener(this);
      _w.bandwidths.setSelectedItem(
	      getObject ( _w.bandwidths, 
		  ""+(_inst.getBandWidth(0)/1.0E6)
		  )
	      );
      _w.bandwidths.addActionListener(this);
      */

      // Update the summary panel
      for (int i=0; i<_w.summaryPanel.getComponentCount(); i++) {
	  Component c = _w.summaryPanel.getComponent(i);
	  String name = c.getName();
	  if ( name != null ) {
	      if ( name.equals ("LowFreqLimit") ) {
		  ((JLabel)c).setText ("" + (int) (_receiver.loMin*1.0E-9) );
	      }
	      if ( name.equals("HighFreqLimit") ) {
		  ((JLabel)c).setText ("" + (int) (_receiver.loMax*1.0E-9) );
	      }
	      if ( name.equals("resolution") ) {
		  double resolution = (_inst.getBandWidth(0) * 1.0E-3)/_inst.getChannels(0);
		  try {
		      resolution = Integer.parseInt(_inst.getMixer()) * resolution;
		  }
		  catch (NumberFormatException nfe) {
		      // Assume one mixer
		  }
		  ((JLabel)c).setText("" + (int)Math.rint(resolution));
	      }
	      if ( name.equals("overlap") ) {
		  ((JLabel)c).setText("" + (_inst.getOverlap(0)*1.0E-6));
	      }
	      if ( name.equals("channel") ) {
		  ((JLabel)c).setText("" + _inst.getChannels(0));
	      }
	  }
      }

      // Update the velocity field
      // First we need to see if we can find a target component
      // somewhere in scope
      SpTelescopeObsComp target = SpTreeMan.findTargetList(_inst);
      String rv;
      String rvDefn;
      String rvFrame;
      if ( target != null ) {
	  SpTelescopePos tp = (SpTelescopePos)target.getPosList().getBasePosition();
	  rv      = tp.getTrackingRadialVelocity();
	  rvDefn  = tp.getTrackingRadialVelocityDefn();
	  rvFrame = tp.getTrackingRadialVelocityFrame();
      }
      else {
	  rv      = "unset";
	  rvDefn  = "unset";
	  rvFrame = "unset";
      }

      // Now get the components on the velocity panel
      // and set them to the new values
      for ( int i=0; i<_w.vPanel.getComponentCount(); i++) {
	  Component c = _w.vPanel.getComponent(i);
	  String name = c.getName();
	  if ( name != null ) {
	      if ( name.equals("velocity") ) {
		  ((JLabel)c).setText(rv);
	      }
	      if ( name.equals("definition") ) {
		  ((JLabel)c).setText(rvDefn);
	      }
	      if ( name.equals("frame") ) {
		  ((JLabel)c).setText(rvFrame);
	      }
	  }
      }

      // Make sure the molecule is accessible
      try {
          _doVelocityChecks();
      }
      catch (Exception e) {
	  // The veolcity is invalid, so set the text appropriately
	  JLabel vWidget;
	  Iterator iter = vPanelWidgetNames.keySet().iterator();
	  while ( iter.hasNext() ) {
	      vWidget = (JLabel)_w.vPanel.getComponent( ((Integer)vPanelWidgetNames.get(iter.next())).intValue() );
	      vWidget.setText("Invalid");
	  }
      }

      _updateMoleculeChoice();
      _updateTransitionChoice();

      _updateRegionInfo();

      _updateTable();
  }


   public void actionPerformed ( ActionEvent ae )
   {
       if ( ae.getSource() == _w.bandwidths ) {
	   // Set the bandwidth for each subsystem/spectral region
	   for ( int i=0; i<Integer.parseInt(_inst.getBandMode()); i++ ) {
	       _inst.setBandWidth( 
		       Double.parseDouble (
		           (String)_w.bandwidths.getSelectedItem()
		           ) * 1.0E6,
		       i );
               setAvailableRegions();
	   }
       }

       try {
           if ( ((Component)ae.getSource()).getName().equals("molecule") ) {
	       // Set the current molecule and update the transitions
               _inst.setCentreFrequency(_receiver.feIF, 0);
	       _inst.setMolecule ( ((JComboBox)ae.getSource()).getSelectedItem().toString(), 0 );
	       _updateTransitionChoice();
	   }

	   else if ( ((Component)ae.getSource()).getName().equals("transition") ) {
	       // Set the current transition
               _inst.setCentreFrequency(_receiver.feIF, 0);
	       _inst.setTransition ( ((JComboBox)ae.getSource()).getSelectedItem().toString(), 0 );
	       // Update the frequency information
	       Object t = ((JComboBox)ae.getSource()).getSelectedItem();
	       if ( t instanceof Transition ) {
		   _updateFrequencyText ( ((Transition)t).frequency /1.0E9 );
	       }
	   }


	   else if ( ((Component)ae.getSource()).getName().equals("Accept") ) {
	       // Set the current Rest Frequency
	       // Get the text field widget
	       int compNum = ((Integer)freqPanelWidgetNames.get("frequency")).intValue();
	       JTextField tf = (JTextField) _w.fPanel.getComponent(compNum);
	       try {
		   double f = Double.parseDouble(tf.getText());
		   _inst.setRestFrequency ( f*1.0E9, 0 );
		   _inst.setSkyFrequency( _inst.getRestFrequency(0) / (1.0+getRedshift()) );
		   // Set the molecule and trasition to NO_LINE
		   _inst.setMolecule( NO_LINE, 0 );
		   _inst.setTransition( NO_LINE, 0 );
		   _updateMoleculeChoice();
	       }
	       catch (Exception e) {
	       }
	       toggleEnabled (_w.fPanel, "Accept", false);
	   }
	   else if ( ((Component)ae.getSource()).getName().equals("show") ) {
	       configureFrequencyEditor();
	       enableNamedWidgets(false);
	       _frequencyEditor.show();
	   }
	   else if ( ((Component)ae.getSource()).getName().equals("hide") ) {
	       _hidingFrequencyEditor = true;
	       getFrequencyEditorConfiguration();
	       enableNamedWidgets(true);
	       _frequencyEditor.hide();
	       _hidingFrequencyEditor = false;
	   }

	   else {
	       System.out.println("Unknown source for action: " + ((Component)ae.getSource()).getName() );
	   }

       }
       catch (Exception e) {
	   // Ignore for now
       };

       _updateWidgets();
   }




   public void feAction ( ActionEvent ae ) {
       if ( ae != null ) {
	   // This has been called as a response
	   // to a user action
	   String feSelected = ((JRadioButton)ae.getSource()).getText();
	   _receiver = (Receiver)_cfg.receivers.get (feSelected);
	   _inst.setFrontEnd(feSelected);
	   _inst.setCentreFrequency(_receiver.feIF, 0);
	   _inst.setFeBandWidth( _receiver.bandWidth );
	   setAvailableModes();
	   setAvailableMixers();
	   setAvailableRegions();
	   setAvailableMolecules();
	   setAvailableTransitions();
	   setAvailableSidebands();
       }
       else {
	   // This has been called as a result
	   // of a call from another action event
       }
       _updateWidgets();
   }

   public void mixerAction (ActionEvent ae) {
       String mixer = ((JRadioButton)ae.getSource()).getText();
       _inst.setMixer(mixer);
       _updateWidgets();
   }

   public void modeAction ( ActionEvent ae ) {
       if ( ae != null ) {
	   // User action
	   String mode = ((JRadioButton)ae.getSource()).getText();
	   _inst.setMode(mode);

	   // Update the band width choices
       }
       else {
	   // System action
       }
       _updateWidgets();
   }

   public void sbSelectAction ( ActionEvent ae ) {
       if ( ae != null ) {
	   String sb = ((JRadioButton)ae.getSource()).getText();

	   // If we are switching between upper and lower sidebands we
	   // need to alter the centre frequency for higher level subsytems
	   if ( Integer.parseInt(_inst.getBandMode()) > 1 ) {
	       String currentBand = _inst.getBand();
	       if ( currentBand.equals("best") && sb.equals("usb") ||
		    currentBand.equals("usb") && sb.equals("best") ) {
		   // Do nothing since 'best' is equivalent to 'usb' as far as
		   // the OT is concerned
	       }
	       else {
		   double topCentreFreq = _inst.getCentreFrequency(0);
		   for ( int i=1; i<Integer.parseInt(_inst.getBandMode()); i++ ) {
		       double diff = topCentreFreq - _inst.getCentreFrequency(i);
		       _inst.setCentreFrequency( topCentreFreq + diff, i );
		   }
	       }
	   }
	   _inst.setBand(sb);
       }
       _updateWidgets();
   }

   public void regionAction ( ActionEvent ae ) {
       if ( ae != null ) {
	   String regions = ((JRadioButton)ae.getSource()).getText();
	   _inst.setBandMode(regions);
       }
       _updateWidgets();
   }

   private void freqAction() {
       // Called when user changes the frequency manually
       toggleEnabled( _w.fPanel, "Accept", true );
   }

   public void bandWidthChoiceAction ( ActionEvent ae ) {
   }

   public void feMoleculeAction ( ActionEvent ae )
   {

   }


   public void feTransitionAction ( ActionEvent ae )
   {
   }

    private void _resetTransition()
    {
    }

   /**
    * Sets the centre frequencies of the remaining subsystems to that of the top subsystem.
    */
   private void _resetAdditionalSubSystems()
   {
   }


   public void moleculeFrequencyChanged()
   {
   }

   public void updateSideBandDisplay() {
   }


   public void updateSideBandDisplay(int nSubBands, Vector shifts) {
   }


   public void setSideBandDisplayVisible(boolean visible) {
   }

   public void setSideBandDisplayLocation(int x, int y) {
   }

   // Added by MFO (8 January 2002)
   /**
    * Returns "usb" (Upper Side Band) or "lsb" (Lower Side Band).
    *
    * Needed by {@link edfreq.SideBand} to shift LO1 when top SideBands are changed.
    */
   public String getFeBand() {
       return _inst.getBand();
   }

   public String getMode() {
       return _inst.getBandMode();
   }


   private void _updateBandwidths() {
       // We need to get the current bandspec
       // from the region selector
       Vector bandSpecs = _receiver.bandspecs;
       BandSpec currentBandSpec = null;
       for ( int i=0; i<bandSpecs.size(); i++) {
	   if ( ((BandSpec)bandSpecs.get(i)).toString().equals(_inst.getBandMode()) ) {
	       currentBandSpec = (BandSpec)bandSpecs.get(i);
	       break;
	   }
       }

       if ( currentBandSpec == null ) {
	   currentBandSpec = (BandSpec)bandSpecs.get(0);
       }

       double [] values = currentBandSpec.getDefaultOverlapBandWidths();
       _w.bandwidths.removeActionListener(this);
       // Remove all the old values
       _w.bandwidths.removeAllItems();

       // Get the current bandwidth
       double currentBandwidth = _inst.getBandWidth(0);

       // Index into the new list to allow us to make sure
       // that it gets reselected if available
       int index = -1;
       double feOverlap=0.0;

       // Set the new bandwidths
       for (int i=0; i<values.length; i++) {
	   double value = Math.rint ( values[i]*1.0E-6 );
	   if ( values[i] == currentBandwidth ) {
	       index = i;
	       feOverlap = currentBandSpec.defaultOverlaps[i];
	       _inst.setOverlap( currentBandSpec.defaultOverlaps[i], 0 );
	       _inst.setChannels( currentBandSpec.getDefaultOverlapChannels()[i], 0 );
	   }
	   _w.bandwidths.addItem("" + value);
       }
       if ( index == -1 ) {
	   // The old value does not exist in the new list
	   for ( int i=0; i<Integer.parseInt( _w.SUBSYSTEMS[_w.SUBSYSTEMS.length-1]); i++ ) {
	       _inst.setBandWidth ( values[0], i );
	       _inst.setOverlap( currentBandSpec.defaultOverlaps[0], i );
	       _inst.setChannels( currentBandSpec.getDefaultOverlapChannels()[0], i );
	   }
	   index = 0;
       }
       _w.bandwidths.setSelectedIndex(index);
       _w.bandwidths.addActionListener(this);
   }

   private void _updateMoleculeChoice() {
       JComboBox molBox = null;
       // First get the molecule checkbox from fPanel
       for ( int i=0; i<_w.fPanel.getComponentCount(); i++) {
	  if ( "molecule".equals(_w.fPanel.getComponent(i).getName())) {
	      molBox = (JComboBox) _w.fPanel.getComponent(i);
	      break;
	  }
       }
       if ( molBox == null ) return;

       // Remove the current actionlistener
       molBox.removeActionListener(this);
       double obsmin = _receiver.loMin -
	   _receiver.feIF - ( _receiver.bandWidth*0.5);
       double obsmax = _receiver.loMax +
	   _receiver.feIF + ( _receiver.bandWidth*0.5);
       Object   currentSelection = getObject( molBox, _inst.getMolecule(0) );
       String   currentSpecies = null;

       if ( currentSelection != null ) {
	   currentSpecies = currentSelection.toString();
       }
       else {
	   currentSpecies = _inst.getMolecule(0);
       }

       // Get a new model to add to the molBox
       molBox.setModel ( new DefaultComboBoxModel (
		   _lineCatalog.returnSpecies (
		       obsmin * ( 1.0 + getRedshift() ),
		       obsmax * ( 1.0 + getRedshift() )
		       )
		   )
	       );
       // Add a"NO LINE option if one does not already exist
       if ( ((DefaultComboBoxModel)molBox.getModel()).getIndexOf(NO_LINE) == -1) {
	   molBox.addItem ( NO_LINE );
       }

       // Go through the new model and see if:
       // (a) the same combination of species/transition is available or
       // (b) the same species is available.
       // In addition, we will actually set things up on the box
       // instead of leaving it to update widgets, since it means
       // we do not need to get the species list again
       DefaultComboBoxModel specModel = (DefaultComboBoxModel)molBox.getModel();
       if ( currentSelection != null &&
	    specModel.getIndexOf (currentSelection) >= 0 ) {
	   molBox.setSelectedIndex( specModel.getIndexOf (currentSelection) );
       }
       else {
	   boolean match = false;
	   // See if the current species is available;
	   // don't use the last element of the model since
	   // this is just the no-line option
	   if ( currentSpecies != null ) {
	       for ( int i=0; i<specModel.getSize()-1; i++ ) {
		   if ( ((SelectionList)specModel.getElementAt(i)).toString().equals(currentSpecies) ) {
		       match = true;
	               molBox.setSelectedIndex( i );
		       break;
		   }
	       }
	   }
	   if ( !match ) {
	       // Set the molecule to the first available one
	       JOptionPane.showMessageDialog (
		       _w,
		       "Selecteing new species; old selection (" + currentSpecies +") out of range",
		       "Molecule changed",
		       JOptionPane.WARNING_MESSAGE);
               for ( int i=0; i<Integer.parseInt(_inst.getBandMode()); i++ ) {
                   _inst.setMolecule( ((SelectionList)specModel.getElementAt(0)).toString(), i );
               }
	       molBox.setSelectedIndex(0);
	   }
       }

       molBox.addActionListener(this);
       _updateTransitionChoice();
   } // End of method
	   

   private void _updateTransitionChoice() {
       JComboBox transBox = null;
       // First get the molecule checkbox from fPanel
       for ( int i=0; i<_w.fPanel.getComponentCount(); i++) {
	  if ( "transition".equals(_w.fPanel.getComponent(i).getName())) {
	      transBox = (JComboBox) _w.fPanel.getComponent(i);
	      break;
	  }
       }
       if ( transBox == null ) return;

       // Remove the current actionlistener
       transBox.removeActionListener(this);

       String currentTransition = _inst.getTransition(0).trim();
       String currentMolecule = _inst.getMolecule(0);
       if ( currentMolecule == null ) {
	   transBox.addActionListener(this);
	   return;
       }

       // Add a space to the end of the the name of the
       // transition
       if ( currentTransition != null ) {
	   //currentTransition += " ";
       }

       // We need to get the current SelectionList based
       // on the molecule.
       // First get all the available species
       double obsmin = _receiver.loMin -
	   _receiver.feIF - ( _receiver.bandWidth*0.5);
       double obsmax = _receiver.loMax +
	   _receiver.feIF + ( _receiver.bandWidth*0.5);
       
       //  Get the new model
       Vector speciesList = _lineCatalog.returnSpecies (
	       obsmin * ( 1.0 + getRedshift() ),
	       obsmax * ( 1.0 + getRedshift() )
	       );
       // Loop through this list to get the element
       // corresponding to the current molecule
       SelectionList currentSpecies = null;
       for ( int i=0; i<speciesList.size();i++ ) {
	   if ( ((SelectionList)speciesList.get(i)).toString().equals(currentMolecule) ) {
	       currentSpecies = (SelectionList)speciesList.get(i);
	       break;
	   }
       }

       if ( currentSpecies != null ) {
	   transBox.setModel( new DefaultComboBoxModel (
		       currentSpecies.objectList ) );
       }

       // Add the no line option
       if(((DefaultComboBoxModel)transBox.getModel()).getIndexOf(NO_LINE) == -1) {
            transBox.addItem(NO_LINE + " ");
       }

       // Check if the previous transition is still in range
       boolean inRange = false;
       if ( currentTransition != null ) {
	   for ( int i=0; i<transBox.getItemCount(); i++ ) {
	       if ( transBox.getItemAt(i).toString().trim().equals (currentTransition) ) {
		   inRange = true;
		   transBox.setSelectedIndex(i);
		   break;
	       }
	   }
       }

       if ( inRange ) {
	   // Do nothing
       }
       else {
	   // We need to set the transition to the first available
	   // for the current species
	   JOptionPane.showMessageDialog (_w,
		   "Transition Changed: " + currentTransition + " out of range.",
		   "Transition Changed!",
		   JOptionPane.PLAIN_MESSAGE);
           for ( int i=0; i<Integer.parseInt(_inst.getBandMode()); i++ ) {
               _inst.setTransition( transBox.getItemAt(0).toString(), i);
           }
	   transBox.setSelectedIndex(0);
       }

       if ( transBox.getSelectedItem() instanceof Transition ) {
	   _updateFrequencyText( ((Transition)transBox.getSelectedItem()).frequency/1.0E9 );
       }
       else {
	   _updateFrequencyText( _inst.getRestFrequency(0)/1.0E9 );
       }
       transBox.addActionListener(this);
   } // End of method


   private void _updateFrequencyText(double f) {
       // Set the value in the instrument component
       // Assume incoming frequency in GHz
       _inst.setRestFrequency( f*1.0E9, 0 );
       _inst.setSkyFrequency( (f*1.0E9)/(1.0+getRedshift()) );
       // Get the component
       JTextField freq = null;
       Iterator iter = freqPanelWidgetNames.keySet().iterator();
       while ( iter.hasNext() ) {
	   String name = (String)iter.next();
	   if ( "frequency".equals(name) ) {
	       // This is the component we want
	       freq = (JTextField)_w.fPanel.getComponent( 
		       ((Integer)freqPanelWidgetNames.get(name)).intValue() );
	       break;
	   }
       }
       if ( freq == null ) return;

       // See if we need to reset the sideband
       checkSideband();

       freq.setText("" + f);
   }

   // See edfreq.HeterodyneEditor for documentation
   public double getRestFrequency(int subsystem) {
       return 0.0;
   }

   // See edfreq.HeterodyneEditor for documentation
   public double getObsFrequency(int subsystem) {
       return 0.0;
   }


  /** Get receiver's central IF. */
  public double getFeIF() {
       return 0.0;
  }


  public void updateCentreFrequency(double centre, int subsystem) {
  }

  public void updateBandWidth(double width, int subsystem) {
  }

  public void updateChannels(int channels, int subsystem) {
  }

  public void updateLineDetails(LineDetails lineDetails, int subsystem) {
  }

  public void updateLO1(double lo1) {
  }

  public double getRedshift() {
      double velocity = 0.0;

      // Get the veolcity information
      String vText = ((JLabel)_w.vPanel.getComponent( ((Integer)vPanelWidgetNames.get("velocity")).intValue() )).getText();
      try {
	  velocity = Double.parseDouble(vText);
      }
      catch ( NumberFormatException nfe ) {
	  return 0.0;
      }

      double c = SpInstHeterodyne.LIGHTSPEED;


      String vDefn = ((JLabel)_w.vPanel.getComponent( ((Integer)vPanelWidgetNames.get("definition")).intValue() )).getText();

      if ( vDefn.equals(TelescopeUtil.TCS_RV_DEFINITIONS[0]) ) {
	  // Radio
	  velocity = ( c / (c - velocity) ) -1.0;
      }
      else if ( vDefn.equals(TelescopeUtil.TCS_RV_DEFINITIONS[1]) ) {
	  // optical
	  velocity = velocity / c;
      }
      else if ( vDefn.equals(TelescopeUtil.TCS_RV_DEFINITIONS[1]) ) {
	  // relativistic
	  velocity = Math.sqrt ( (c - velocity)/(c + velocity) ) -1.0;
      }
      else if ( vDefn.equals(TelescopeUtil.TCS_RV_DEFINITIONS[1]) ) {
	  // redshift
      }

      return velocity;
  }

    public double getCurrentBandwidth ( int subsystem) {
       return _inst.getBandWidth(subsystem);
    }

    private boolean usingFFSwitching(SpItem item) {
	return false;
    }


    private DefaultComboBoxModel getSpecialConfigsModel () {
	return new DefaultComboBoxModel();
    }

    private ConfigurationInformation getConfigFor (String name) {
	return null;
    }

    private void setAvailableModes() {
	List availableModes = Arrays.asList( ( String [] ) _cfg.frontEndTable.get(_inst.getFrontEnd()) );
	String currentMode = _inst.getMode();
	boolean changeMode = false;
	
	// We need to see compare the available modes with the list of
	// total modes, and disable any that are not currently available
	Iterator iter = modeWidgetNames.keySet().iterator();
	while ( iter.hasNext() ) {
	    String str = (String) iter.next();
	    if ( availableModes.contains( str ) ) {
		// Make sure the widget is enabled
		toggleEnabled(_w.modeSelector, 
			str,
			true); 
	    }
	    else {
		// disable the widget and optionally chnage
		// the current mode
		if ( currentMode != null && currentMode.equalsIgnoreCase( str ) ) {
		    changeMode = true;
		}
		toggleEnabled(_w.modeSelector, 
			str,
			false); 
	    }
	}
	if ( changeMode ) {
	    _inst.setMode( (String)availableModes.get(0) );
	}
    }

    private void setAvailableMixers() {
	List available= Arrays.asList( ( String [] ) _cfg.frontEndMixers.get(_inst.getFrontEnd()) );
	String current= _inst.getMixer();
	boolean change = false;
	Iterator iter = mixerWidgetNames.keySet().iterator();

	while ( iter.hasNext() ) {
	    String str = (String)iter.next();
	    if ( available.contains( str ) ) {
		// Make sure the widget is enabled
		toggleEnabled(_w.mixerSelector, 
			str,
			true); 
	    }
	    else {
		// disable the widget and optionally chnage
		// the current mode
		if ( current != null && current.equalsIgnoreCase( str ) ) {
		    change = true;
		}
		toggleEnabled(_w.mixerSelector, 
			str,
			false); 
	    }
	}
	if ( change ) {
	    _inst.setMixer( (String)available.get(0) );
	}
 
    }
	
    private void setAvailableRegions() {
	Vector bandspecs= _receiver.bandspecs;
	Vector available = new Vector();
	for ( int i=0; i< bandspecs.size(); i++ ) {
	    available.add ( ((BandSpec)bandspecs.get(i)).toString() );
	}
        //
        // Special handling for RxA only.  If we are using one of the hybrid modes
        // only 2 regions are allowed
        //
        if ( "A3".equalsIgnoreCase(_receiver.name) ) {
            // Get the number of hybrid subband
            BandSpec currentBandSpec = null;
            for ( int i=0; i<bandspecs.size(); i++) {
                if ( ((BandSpec)bandspecs.get(i)).toString().equals(_inst.getBandMode()) ) {
                    currentBandSpec = (BandSpec)bandspecs.get(i);
                    if ( _w.bandwidths.getSelectedIndex() > -1 ) {
                        int nHybrids = currentBandSpec.getNumHybridSubBands(_w.bandwidths.getSelectedIndex());
                        if ( nHybrids == 4 && available.contains("4") ) {
                            available.remove("4");
                        }
                    }
                }
            }
        }
	String current= _inst.getBandMode();
	boolean change = false;
	Iterator iter = regionWidgetNames.keySet().iterator();

	while ( iter.hasNext() ) {
	    String str = (String)iter.next();
	    if ( available.contains( str ) ) {
		// Make sure the widget is enabled
		toggleEnabled(_w.regionSelector, 
			str,
			true); 
	    }
	    else {
		// disable the widget and optionally chnage
		// the current mode
		if ( current != null && current.equalsIgnoreCase( str ) ) {
		    change = true;
		}
		toggleEnabled(_w.regionSelector, 
			str,
			false); 
	    }
	}
	if ( change ) {
	    _inst.setBandMode( (String)available.get(0) );
	}
    }

    private void setAvailableMolecules() {
    }
    
    private void setAvailableTransitions() {
    }

    private void setAvailableSidebands() {
    }
	
    // Disable a specific button in the specified container
    private void toggleEnabled (Container c, String name, boolean enabled) {
	for ( int i=0; i<c.getComponentCount(); i++ ) {
	    if ( name.equals(c.getComponent(i).getName()) ) {
		c.getComponent(i).setEnabled ( enabled );
		break;
	    }
	}
    }

    private void clickButton(Container c, String name) {
	for ( int i=0; i<c.getComponentCount(); i++ ) {
	    if ( c.getComponent(i).getName() != null &&
		 c.getComponent(i).getName().equals(name) &&
		 c.getComponent(i) instanceof AbstractButton ) {
		((AbstractButton)c.getComponent(i)).doClick();
		break;
	    }
	};
    }

   private Object getObject(JComboBox comboBox, String name) {
     for(int i = 0; i < comboBox.getItemCount(); i++) {
       if(comboBox.getItemAt(i).toString().equalsIgnoreCase(name)) {
         return comboBox.getItemAt(i);
       }
     }

     return null;
   }

   private void _updateTable() {
       // Just collect together all the information for
       // each spectral regions
       try {
           DefaultTableModel tbm = new DefaultTableModel();
           tbm.setColumnIdentifiers(_w.COLUMN_NAMES);
           for ( int i=0; i<Integer.parseInt(_inst.getBandMode()); i++ ) {
	       Vector row = new Vector(_regionInfo[i]);
               row.add(0, new Integer(i) );
	       tbm.addRow(row);
           }
           _w.table.setModel(tbm);
       }
       catch (Exception e) {};
   }

   /*
    * This routine initialises the region info.
    * It assumes that the zeroth component is set
    * and it just copies the zeroth values for now.
    * The vector contains the following elements related
    * to each spectral region:
    * 1. Molecule
    * 2. Transition
    * 3. Rest Frequency
    * 4. Central Frequency
    * 5. Band width
    * 6. Resolution
    * 7. number of channels
    */
   private void _initialiseRegionInfo() {
       for ( int i=0; i<_regionInfo.length; i++ ) {
	  if ( _regionInfo[i] == null ) _regionInfo[i] = new Vector();
	  _regionInfo[i].add(_inst.getMolecule(0));
	  _inst.setMolecule ( _inst.getMolecule(0), i );
	  _regionInfo[i].add(_inst.getTransition(0));
	  _inst.setTransition ( _inst.getTransition(0), i );
	  _regionInfo[i].add(new Double( _inst.getRestFrequency(0) ));
	  _inst.setRestFrequency ( _inst.getRestFrequency(0), i );
	  _inst.setSkyFrequency ( _inst.getRestFrequency(0) / (1.0 + getRedshift()) );
	  _regionInfo[i].add(new Double( _inst.getCentreFrequency(0) ));
	  _inst.setCentreFrequency ( _inst.getCentreFrequency(0), i );
	  _regionInfo[i].add(new Double( _inst.getBandWidth(0) ));
	  _inst.setBandWidth ( _inst.getBandWidth(0), i );
	  int resolution = (int)Math.rint( (_inst.getBandWidth(0) * 1.0E-3)/_inst.getChannels(0) );
	  _regionInfo[i].add(new Integer( resolution ) ); // Need to add resolution here
	  _regionInfo[i].add(new Integer( _inst.getChannels(0) ));
	  _inst.setChannels ( _inst.getChannels(0), i );
       }
   }

   private void _updateRegionInfo() {
       Vector bandSpecs = _receiver.bandspecs;
       BandSpec currentBandSpec = null;

       for ( int i=0; i<bandSpecs.size(); i++) {
	   if ( ((BandSpec)bandSpecs.get(i)).toString().equals(_inst.getBandMode()) ) {
	       currentBandSpec = (BandSpec)bandSpecs.get(i);
	       break;
	   }
       }
       if ( currentBandSpec == null ) currentBandSpec = (BandSpec)bandSpecs.get(0);
       double [] availableBandWidths = currentBandSpec.getDefaultOverlapBandWidths();

       for ( int i=0; i<_regionInfo.length; i++ ) {
	   if ( _regionInfo[i] == null ) _regionInfo[i] = new Vector();
	  _regionInfo[i].clear();
	  _regionInfo[i].add(_inst.getMolecule(i));
	  _regionInfo[i].add(_inst.getTransition(i));
	  _regionInfo[i].add(new Double( _inst.getRestFrequency(i) ));
	  _regionInfo[i].add(new Double( _inst.getCentreFrequency(i) ));
	  _regionInfo[i].add(new Double( _inst.getBandWidth(i) ));
	  // Get the overlap and overlap based on the current b/w
	  for ( int j=0; j<availableBandWidths.length; j++) {
	      if ( availableBandWidths[j] == _inst.getBandWidth(i) ) {
		  double overlap = currentBandSpec.defaultOverlaps[j];
		  int channels = currentBandSpec.getDefaultOverlapChannels()[j];
	          int resolution = (int) Math.rint( (_inst.getBandWidth(i) * 1.0E-3)/channels );
		  _regionInfo[i].add(new Integer(resolution));
		  _regionInfo[i].add(new Double( currentBandSpec.defaultOverlaps[j] *1.0E-6 ));
		  _regionInfo[i].add(new Integer(channels));
		  _inst.setOverlap( overlap, i );
		  _inst.setChannels( channels, i );
		  break;
	      }
	  }
	  _regionInfo[i].add(new Integer( _inst.getChannels(i) ));
       }
   }

   private void configureFrequencyEditor() {
       // First get the current bandspec from the mode selection
       Vector bandSpecs = _receiver.bandspecs;
       BandSpec currentBandSpec = (BandSpec)bandSpecs.get(0);
       for ( int i=0; i<bandSpecs.size(); i++ ) {
	   if ( ((BandSpec)bandSpecs.get(i)).toString().equals(_inst.getBandMode()) ) {
	       currentBandSpec = (BandSpec)bandSpecs.get(i);
	       break;
	   }
       }

       int subbandCount = currentBandSpec.numBands;
       int mixerCount = 1;
       try {
          mixerCount = Integer.parseInt( _inst.getMixer() );
       }
       catch ( NumberFormatException nfe ) {
       }

       _frequencyEditor.updateDisplay (
	       _inst.getFrontEnd(),
	       _receiver.loMin,
	       _receiver.loMax,
               _receiver.feIF,
	       _receiver.bandWidth,
	       mixerCount,
	       getRedshift(),
	       currentBandSpec.getDefaultOverlapBandWidths(),
	       currentBandSpec.getDefaultOverlapChannels(),
	       subbandCount
	       );


       for ( int i=0; i<Integer.parseInt(_inst.getBandMode()); i++ ) {
	   // Set the centre frequencies
	   _frequencyEditor.setCentreFrequency( _inst.getCentreFrequency(i), i);
	   _frequencyEditor.setBandWidth( _inst.getBandWidth(i), i );
	   _frequencyEditor.setLineText( _inst.getMolecule(i) +
		   " " + _inst.getTransition(i) +
		   " " + (_inst.getRestFrequency(i)/1.0E6), i);
       }
	   
       // Configure the frequency editor
       _frequencyEditor.resetModeAndBand( _inst.getMode(), _inst.getBand() );

       // Need to deal with LO1...
       double obsFreq = _inst.getRestFrequency(0) / ( 1.0 + getRedshift() );
       String band = _inst.getBand();
       if ( "best".equals(band) || "usb".equals(band) ) {
	   _frequencyEditor.setLO1( obsFreq - _frequencyEditor.getTopSubSystemCentreFrequency() );
       }
       else {
	   _frequencyEditor.setLO1( obsFreq + _frequencyEditor.getTopSubSystemCentreFrequency() );
       }
       _frequencyEditor.setMainLine ( _inst.getRestFrequency(0) );
    }

   private void getFrequencyEditorConfiguration() {
       Vector [] configs = _frequencyEditor.getCurrentConfiguration();
       for ( int i=0; i<configs.length; i++ ) {
	   _inst.setMolecule( configs[i].get(0).toString(), i );
	   _inst.setTransition ( configs[i].get(1).toString(), i );
	   _inst.setRestFrequency( configs[i].get(2).toString(), i );
	   _inst.setCentreFrequency( configs[i].get(3).toString(), i );
	   _inst.setBandWidth( configs[i].get(4).toString(), i );
	   _inst.setChannels( Integer.parseInt(configs[i].get(5).toString()), i );
       }

   }


   private void enableNamedWidgets(boolean enabled) {
       // Disable all named widgets except the hide button on the bPanel
       Iterator iter;

       iter = feWidgetNames.keySet().iterator();
       while ( iter.hasNext() ) {
	   toggleEnabled( _w.feSelector, (String)iter.next(), enabled);
       }
       iter = modeWidgetNames.keySet().iterator();
       while ( iter.hasNext() ) {
	   toggleEnabled( _w.modeSelector, (String)iter.next(), enabled);
       }
       iter = regionWidgetNames.keySet().iterator();
       while ( iter.hasNext() ) {
	   toggleEnabled( _w.regionSelector, (String)iter.next(), enabled);
       }
       iter = mixerWidgetNames.keySet().iterator();
       while ( iter.hasNext() ) {
	   toggleEnabled( _w.mixerSelector, (String)iter.next(), enabled);
       }
       iter = sidebandWidgetNames.keySet().iterator();
       while ( iter.hasNext() ) {
	   toggleEnabled( _w.sbSelector, (String)iter.next(), enabled);
       }

       iter = freqPanelWidgetNames.keySet().iterator();
       while ( iter.hasNext() ) {
	   // Keep the Accept button disabled...
	   String widget = (String) iter.next();
	   if ( widget.equalsIgnoreCase("accept") && enabled ) {
	       // Do nothing since we don't want to enable it
	   }
	   else {
	       toggleEnabled( _w.fPanel, widget, enabled );
	   }
       }

       _w.bandwidths.setEnabled(enabled);

       // Finally deal with the show and hide buttons
       for ( int i=0; i<_w.bPanel.getComponentCount(); i++ ) {
	   String widget = _w.bPanel.getComponent(i).getName();
	   if ( widget != null ) {
	       if ( widget.equals("show") ) toggleEnabled( _w.bPanel, widget, enabled );
	       if ( widget.equals("hide") ) toggleEnabled( _w.bPanel, widget, (!enabled) );
	   }
       }

       if ( enabled ) {
	   // Disable anything that should not be available
	   setAvailableModes();
	   setAvailableMixers();
	   setAvailableRegions();
	   setAvailableMolecules();
	   setAvailableTransitions();
	   setAvailableSidebands();
       }

   }

   private void checkSideband( ) {
       double freq = _inst.getRestFrequency(0);
       
       // Convert to sky frequency
       freq = freq / (1.0 + getRedshift() );

       String sideband = _inst.getBand();

       if ( "lsb".equals(sideband) ) {
	   if ( (freq + _frequencyEditor.getTopSubSystemCentreFrequency()) > _receiver.loMax ) {
	       JOptionPane.showMessageDialog( _w,
		       "Using upper sideband in order to reach line.",
		       "Changing Sideband!",
		       JOptionPane.WARNING_MESSAGE);
	       _inst.setBand("usb");
	   }
       }
       else {
	   if ( (freq - _frequencyEditor.getTopSubSystemCentreFrequency()) < _receiver.loMin ) {
	       JOptionPane.showMessageDialog( _w,
		       "Using lower sideband in order to reach line.",
		       "Changing Sideband!",
		       JOptionPane.WARNING_MESSAGE);
	       _inst.setBand("lsb");
	   }
       }
   }

   private void _doVelocityChecks() throws Exception {
       double obsmin = _receiver.loMin - _receiver.feIF -
	   ( _receiver.bandWidth * 0.5 );
       obsmin *= ( 1.0 + getRedshift() );
       double obsmax = _receiver.loMin + _receiver.feIF +
	   ( _receiver.bandWidth * 0.5 );
       obsmax *= ( 1.0 + getRedshift() );

       if ( ((Vector)_lineCatalog.returnSpecies(obsmin, obsmax)).size() < 1 ) {
	   String message = "Specified velocity results in frequency range that exceeds the line catalog, " +
	       "\nEither change the front end or change the velocity of the target";

	   JOptionPane.showMessageDialog (
		   _w,
		   message,
		   "Invalid Velocity!",
		   JOptionPane.ERROR_MESSAGE);
	   throw new Exception ("Invalid velocity");
       }
   }


    class ConfigurationInformation {
	public String  $name;
	public String  $feName;
	public Double  $freq;
	public String  $sideBand;
	public String  $mode;
	public Integer $mixers;
	public Integer $subSystems;
	public String  $bandWidth;
	public Vector  $shifts = new Vector();

	public void print() {
	    System.out.println( "name       = "+$name);
	    System.out.println( "feName     = "+$feName);
	    System.out.println( "frequency  = "+$freq.doubleValue());
	    System.out.println( "sideBand   = "+$sideBand);
	    System.out.println( "mode       = "+$mode);
	    System.out.println( "mixers     = "+$mixers);
	    System.out.println( "subSystems = "+$subSystems);
	    System.out.println( "bandwidth  = "+$subSystems);
	}
    }
}

