// Copyright 1997 Association for Universities for Research in Astronomy, Inc.,
// Observatory Control System, Gemini Telescopes Project.
// See the file COPYRIGHT for complete details.
//
// $Id$
//
package ot.ukirt.iter.editor;

import java.util.*;

import orac.ukirt.inst.SpInstCGS4;
import orac.ukirt.iter.SpIterCGS4CalObs;
import orac.ukirt.iter.SpCGS4CalUnitConstants;
import orac.util.LookUpTable;

import gemini.sp.SpItem;
import gemini.sp.SpTreeMan;
import gemini.sp.obsComp.SpInstObsComp;

import jsky.app.ot.gui.CheckBoxWidgetExt;
import jsky.app.ot.gui.CheckBoxWidgetWatcher;
import jsky.app.ot.gui.DropDownListBoxWidgetExt;
import jsky.app.ot.gui.DropDownListBoxWidgetWatcher;
import jsky.app.ot.gui.CommandButtonWidgetExt;
import jsky.app.ot.gui.CommandButtonWidgetWatcher;
import jsky.app.ot.gui.TextBoxWidgetExt;
import jsky.app.ot.gui.TextBoxWidgetWatcher;

import java.awt.CardLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import jsky.app.ot.editor.OtItemEditor;

/**
 * This is the editor for the CGS4 CalUnit Observation iterator.
 */
public final class EdIterCGS4CalObs extends OtItemEditor
                     implements TextBoxWidgetWatcher, DropDownListBoxWidgetWatcher, ActionListener
{

   private SpItem   _baseItem;

   /** Identifier for a FLAT calibration. */
   public static final int FLAT = 0;

   /** Identifier for an ARC calibration. */
   public static final int ARC  = 1;

   private SpIterCGS4CalObs _calCGS4;

   private IterCGS4CalObsGUI _w;		// the GUI layout

   /**
    * This flag is set true while methods like _init is executed to prevent actionPerformed() to do react to
    * action events caused by initializing widgets.
    */
   private boolean _ignoreActionEvents = false;

/**
 * The constructor initializes the title, description, and presentation source.
 */
public EdIterCGS4CalObs()
{
   _title       ="CGS4 Cal Unit Observation";
   _presSource  =  _w = new IterCGS4CalObsGUI();
   _description ="Configure CGS4's Calibration Unit with this component.";

   for(int i = 0; i < 100; i++) {
     _w.repeatComboBox.addItem("" + (i + 1));
   }

   _w.calType.addActionListener(this);
   _w.lamp.addActionListener(this);
   _w.filter.addActionListener(this);
   _w.mode.addActionListener(this);
   _w.flatSampling.addActionListener(this);
   _w.repeatComboBox.addActionListener(this);
   _w.defaultValues.addActionListener(this);

}

/**
 */
protected void
_init()
{
   _ignoreActionEvents = true;

   DropDownListBoxWidgetExt ddlbw;

   SpIterCGS4CalObs ico = (SpIterCGS4CalObs) _spItem;

   // Set the calibration choices
   _w.calType.setChoices( SpCGS4CalUnitConstants.CALTYPES );

   // Set the lamp choices
   _w.lamp.setChoices( SpIterCGS4CalObs.ARC_LAMPS );

   // Show the filter
   _w.filter.setChoices( SpIterCGS4CalObs.FILTERS );

   // Show the mode
   _w.mode.setChoices( SpIterCGS4CalObs.MODES );

   // Exposure time
   _w.exposureTime.addWatcher( this );

   // Coadds
   _w.coadds.addWatcher( this );

   // CVF Wavelength
   _w.cvfWavelength.addWatcher( this );

   // Flat sampling
   Vector samplingChoiceVector = new Vector();
   samplingChoiceVector.add("1x1");
   samplingChoiceVector.add("AS_OBJECT");
   _w.flatSampling.setChoices (samplingChoiceVector);
   _w.flatSampling.setValue("AS_OBJECT");
   //   _w.flatSampling.addWatcher( this );

   super._init();

   _ignoreActionEvents = false;

}

/**
 * Implements the _updateWidgets method from OtItemEditor in order to
 * setup the widgets to show the current values of the item.
 */
protected void
_updateWidgets()
{
   _ignoreActionEvents = true;

   SpIterCGS4CalObs ico = (SpIterCGS4CalObs) _spItem;

   // Get the choices and defaults from the instrument.

   // Show the calib. type
   _w.calType.setValue( ico.getCalTypeString() );

   // Show the lamp
   _updateLampChoices();

   // Make sure _ignoreActionEvents is set to true again after having been set to
   // false at the end of _updateLampChoices()
   _ignoreActionEvents = true;
   
   String lamp = ico.getLamp();
   _w.lamp.setValue( lamp );
   ico.setLamp(lamp);

   // Show the filter
   // For flats, inherit from instrument, for arcs choose.
//   if (ico.getCalType() == FLAT) {
   String filter = ico.getFilter();
   _w.filter.setValue( filter );
   ico.setFilter (filter);

   // Show the mode
   String mode = ico.getMode();
   _w.mode.setValue( mode);
   ico.setMode (mode);

   // Observe repetitions
   _w.repeatComboBox.setSelectedIndex( ico.getCount() - 1);

   // Exposure time
   _w.exposureTime.setValue( ico.getExposureTime() );

   // Coadds
   _w.coadds.setValue( ico.getCoadds() );

   // Cvf wavelength
   _w.cvfWavelength.setValue( ico.getCvfWavelength() );
   
   // Deal with CVF wavelength according to state of inst & caltype
   SpItem _baseItem = ico;
   SpItem _parent = _baseItem.parent();
   SpInstCGS4 _instCgs4 = (SpInstCGS4) SpTreeMan.findInstrument(_baseItem);
   String grat = _instCgs4.getDisperser();
   if (grat.equalsIgnoreCase("echelle") && (ico.getCalType() == ARC)) {
      // MFO: "ARC" is hard-wired in IterCGS4CalObsGUI (as constraint strings a CardLayout).
      ((CardLayout)(_w.calTypesPanel.getLayout())).show(_w.calTypesPanel, "ARC");
   }else{
   
      // Deal with Flat Sampling according to state of inst & caltype
      String samp = _instCgs4.getSampling();
      _w.flatSampling.setValue( ico.getFlatSampling() );
      if (ico.getCalType() == FLAT) {
         // MFO: "FLAT" is hard-wired in IterCGS4CalObsGUI (as constraint strings a CardLayout).
         ((CardLayout)(_w.calTypesPanel.getLayout())).show(_w.calTypesPanel, "FLAT");
   
         _w.flatSampling.setChoices( ico.getFlatSamplingChoices());
         _w.flatSampling.setValue( ico.getFlatSampling() );
      }else{
         // MFO: "EMPTY" is hard-wired in IterCGS4CalObsGUI (as constraint strings a CardLayout).
         ((CardLayout)(_w.calTypesPanel.getLayout())).show(_w.calTypesPanel, "EMPTY");
      }
   }   

   _ignoreActionEvents = false;
}

//
// Update the lamp choices based upon the calibration type choice
//
private void
_updateLampChoices()
{
   _ignoreActionEvents = true;

   // do I need this? Does it work?
   SpIterCGS4CalObs ico = (SpIterCGS4CalObs) _spItem;

   // get value of calType, set lamp choices accordingly
   if (ico.getCalType() == FLAT) {
      _w.lamp.setChoices ( SpIterCGS4CalObs.FLAT_LAMPS );
   }else{
      _w.lamp.setChoices ( SpIterCGS4CalObs.ARC_LAMPS );
   }
//   _updateWidgets();

   _ignoreActionEvents = false;
}


/**
 * Watch changes to text box widgets.
 */
public void
textBoxKeyPress(TextBoxWidgetExt tbwe)
{
   SpIterCGS4CalObs ico = (SpIterCGS4CalObs) _spItem;
   
   if (tbwe == _w.exposureTime) {
      ico.setExposureTime( tbwe.getText() );
   } else if (tbwe == _w.coadds) {
      ico.setCoadds( tbwe.getText() );
   } else if (tbwe == _w.cvfWavelength) {
      ico.setCvfWavelength( tbwe.getText() );
   }
}
 
/**
 * Text box action.
 */
public void
textBoxAction(TextBoxWidgetExt tbwe) {}

/**
 * DD list box select.
 */
public void
dropDownListBoxSelect(DropDownListBoxWidgetExt ddlbwe, int i, String val) {}


/**
 * DD list box action.
 */
public void
dropDownListBoxAction(DropDownListBoxWidgetExt ddlbwe, int i, String val) {}


/**
 *
 */
public void 
actionPerformed(ActionEvent evt)
{
   if(_ignoreActionEvents) {
      return;
   }

   Object w    = evt.getSource();

   SpIterCGS4CalObs ico = (SpIterCGS4CalObs) _spItem;

   if (w == _w.calType) {
      DropDownListBoxWidgetExt ddlbw = (DropDownListBoxWidgetExt) w;
//      try {
         ico.setCalType(ddlbw.getStringValue());
         ico.useDefaults();
//      }
//      catch(NullPointerException e) { }
      _updateWidgets();
      return;
   }

   if (w == _w.lamp) {
      DropDownListBoxWidgetExt ddlbw = (DropDownListBoxWidgetExt) w;
//      try {
         ico.setLamp(ddlbw.getStringValue());
//      }	 
//      catch(NullPointerException e) { }
      return;
   }

   if (w == _w.filter) {
      DropDownListBoxWidgetExt ddlbw = (DropDownListBoxWidgetExt) w;
//      try {
         ico.setFilter(ddlbw.getStringValue());
//      }
//      catch(NullPointerException e) { }
      return;
   }

   if (w == _w.mode) {
      DropDownListBoxWidgetExt ddlbw = (DropDownListBoxWidgetExt) w;
//      try {
         ico.setMode(ddlbw.getStringValue());
//      }
//      catch(NullPointerException e) { }
      return;
   }

   if (w == _w.flatSampling) {
      DropDownListBoxWidgetExt ddlbw = (DropDownListBoxWidgetExt) w;
//      try {
      if(ddlbw.getItemCount() > 0) {
         ico.setFlatSampling(ddlbw.getStringValue());
      }	 
//      }	 
//      catch(NullPointerException e) { }
      return;
   }

   if (w == _w.repeatComboBox) {
      int i = _w.repeatComboBox.getSelectedIndex() + 1;
      ico.setCount(i);
      return;
   }

   if (w == _w.defaultValues) {
      ico.useDefaults();
      _updateWidgets();
      return;
   }

}

}
