/*==============================================================*/
/*                                                              */
/*                UK Astronomy Technology Centre                */
/*                 Royal Observatory, Edinburgh                 */
/*                 Joint Astronomy Centre, Hilo                 */
/*                   Copyright (c) PPARC 1999                   */
/*                                                              */
/*==============================================================*/

package ot.jcmt.inst.editor;

import gemini.sp.*;
import gemini.sp.obsComp.*;
import orac.jcmt.inst.SpInstHeterodyne;
import jsky.app.ot.gui.KeyPressWatcher;
import jsky.app.ot.gui.TextBoxWidgetExt;
import jsky.app.ot.gui.TextBoxWidgetWatcher;
import jsky.app.ot.gui.TableWidgetExt;
import jsky.app.ot.gui.TableWidgetWatcher;
import jsky.app.ot.gui.CommandButtonWidgetExt;
import jsky.app.ot.gui.CommandButtonWidgetWatcher;
import jsky.app.ot.gui.CheckBoxWidgetExt;
import jsky.app.ot.gui.CheckBoxWidgetWatcher;
//import jsky.app.ot.gui.util.ErrorBox;
import ot.util.DialogUtil;

//import ot_ukirt.util.*;
import jsky.app.ot.editor.OtItemEditor;
import orac.jcmt.inst.SpDRRecipe;
import orac.util.LookUpTable;

import java.awt.event.KeyEvent;
import java.awt.CardLayout;
import java.util.Vector;

// MFO: Preliminary. Get rid of GUI stuff inside this class.
import javax.swing.border.TitledBorder;
import javax.swing.JComponent;
import java.awt.BorderLayout;



/**
 * This is the editor for the DRRecipe item.
 */
public final class EdDRRecipe extends OtItemEditor
                    implements KeyPressWatcher, TextBoxWidgetWatcher, TableWidgetWatcher {

  private static final String INST_STR_SCUBA      = "scuba";
  private static final String INST_STR_HETERODYNE = "heterodyne";

  private SpDRRecipe  _spDRRecipe;
  private String      _currentRecipeSelected;
  private String      _instStr;

  private DRRecipeGUI _w;		// the GUI layout

  private DataReductionScreen _dataReductionScreen = new DataReductionScreen();


/**
 * The constructor initializes the title, description, and presentation source.
 */
public EdDRRecipe()
{
   _title       = "DR Recipe";
   _presSource  = _w = new DRRecipeGUI();;
   _description = "Enter the Data Reduction recipe to be used"; // for each observation type";
   _resizable   = true;

   _dataReductionScreen.setBorder(new TitledBorder("ACSIS DR"));
   _w.heterodynePanel.add(_dataReductionScreen, BorderLayout.CENTER);
}

/**
 * This method initializes the widgets in the presentation to reflect the
 * current values of the items attributes.
 * Don't do anything in init at the moment.  The initialisation requires a  reference
 * to the instrument component. This isn't known until after setup is invoked, which
 * is after init.  Therefore do the initialisation as part of setup (see initInstWidgets).
 */
protected void
_init()
{


}

/**
 * This method initializes the instrument speific widgets in the presentation 
 * to reflect the current values of the items attributes and set the watchers.
 */
protected void
_initInstWidgets()
{

   CommandButtonWidgetExt cbw = null;
   CheckBoxWidgetExt ckbw = null;

   SpInstObsComp inst = ((SpInstObsComp) SpTreeMan.findInstrument(_spDRRecipe));

   if(inst == null) {
     // MFO: "empty" is hard-wired in DRRecipeGUI (as constraint strings of the
     // respective panels managed my the CardLayout of DRRecipeGUI).
     // Might not be elegant but is done in a similar way with instrument specific panels
     // (see below).  
     ((CardLayout)(_w.getLayout())).show(_w, "empty");
    
     DialogUtil.error(_w, "Can't identify instrument: probably none in scope?");
     return;
   }


   if(inst instanceof SpInstHeterodyne) {
     _instStr = INST_STR_HETERODYNE;
   }
   else {
     _instStr = INST_STR_SCUBA;
   }  

   System.out.println ("Inst is "+_instStr);

   // MFO: inst.type().getReadable() is hard-wired in DRRecipeGUI (as constraint strings of the
   // respective panels managed my the CardLayout of DRRecipeGUI).
   // Might not be elegant but has always been hard-wired in a similar way.
   ((CardLayout)(_w.getLayout())).show(_w, _instStr.toLowerCase());
     

   if(_instStr.equalsIgnoreCase(INST_STR_HETERODYNE)) {
     return;
   }

  // The recipes
  TextBoxWidgetExt rtbw; 

  rtbw = (TextBoxWidgetExt) getWidget(_instStr, "objectRecipe");
  // Disable it so it the user cannot use it.
  // _disableRecipeEntry(true);
  rtbw.setEditable(false);
  cbw = (CommandButtonWidgetExt) getWidget(_instStr, "objectSet");
  cbw.addWatcher( new CommandButtonWidgetWatcher() {
    public void commandButtonAction(CommandButtonWidgetExt cbw) {
      // Set the selected table value
      _spDRRecipe.setObjectRecipeName(_currentRecipeSelected);
      _spDRRecipe.setTitleAttr(_currentRecipeSelected);
  
      TextBoxWidgetExt tbwe = (TextBoxWidgetExt) getWidget(_instStr, "objectRecipe");
      tbwe.setText(_currentRecipeSelected);
      _disableRecipeEntry(true);
      }
  });

  // The table of possible recipes
  TableWidgetExt twe;
  twe = (TableWidgetExt) getWidget(_instStr, "recipeTable");
  twe.setColumnHeaders(new String[]{"Recipe Name", "Description"});
  twe.addWatcher(this);

  // Button to allow user to enter own names
  TextBoxWidgetExt tbwe = (TextBoxWidgetExt) getWidget(_instStr, "userRecipe");
  tbwe.setText("ENTER_YOUR_OWN_RECIPE_HERE");
  _disableRecipeEntry(true);
  tbwe.addWatcher(this);

  cbw = (CommandButtonWidgetExt) getWidget(_instStr, "userSpec");
  cbw.addWatcher (new CommandButtonWidgetWatcher() {
      public void commandButtonAction (CommandButtonWidgetExt cbw) {
	_disableRecipeEntry(false);
      }
  });

  // button to reset the recipe to default
  cbw = (CommandButtonWidgetExt) getWidget(_instStr, "defaultName");
  cbw.addWatcher( new CommandButtonWidgetWatcher() {
    public void commandButtonAction(CommandButtonWidgetExt cbw) {
      _spDRRecipe.useDefaults(_instStr);
      _currentRecipeSelected = "QUICK_LOOK";
      _updateWidgets();
    }
  });
}

private void
_disableRecipeEntry (boolean tf) {

   TextBoxWidgetExt tbwe = (TextBoxWidgetExt) getWidget(_instStr, "userRecipe");
   tbwe.setEditable(!tf);
}

/**
 * Initialize the Recipe table widget according to the selected
 * recipe category.
 */
private void
_showRecipeType(LookUpTable recipes)
{
   Vector[] rowsV = new Vector[recipes.getNumRows()];
   rowsV = recipes.getAsVectorArray();
   TableWidgetExt tw = (TableWidgetExt) getWidget(_instStr, "recipeTable");
   tw.setRows(rowsV);
}

/**
 * Get the index of the recipe in the given array, or -1 if the recipe
 * isn't in the array.
 */
   private int _getRecipeIndex( String recipe, LookUpTable rarray ) {
      int ri = -1;
      try {
         ri = rarray.indexInColumn( recipe, 0 );
      } catch (Exception ex) {
      }
      return ri;
   }

/**
 * Update the recipe choice related widgets.
 */
private void
_updateRecipeWidgets()
{
  String recipe = null;
  String instStr;

   SpInstObsComp inst = ((SpInstObsComp) SpTreeMan.findInstrument(_spDRRecipe));

   if(inst == null) {
     // MFO: "empty" is hard-wired in DRRecipeGUI (as constraint strings of the
     // respective panels managed my the CardLayout of DRRecipeGUI).
     // Might not be elegant but is done in a similar way with instrument specific panels
     // (see below).  
     ((CardLayout)(_w.getLayout())).show(_w, "empty");
    
     System.out.println ("No instrument found in scope");
     return;
   }


   if(inst instanceof SpInstHeterodyne) {
     instStr = INST_STR_HETERODYNE;
   }
   else {
     instStr = INST_STR_SCUBA;
   }  

//   System.out.println ("Inst is "+instStr);

   // MFO: inst.type().getReadable() is hard-wired in DRRecipeGUI (as constraint strings of the
   // respective panels managed my the CardLayout of DRRecipeGUI).
   // Might not be elegant but has always been hard-wired in a similar way.
   ((CardLayout)(_w.getLayout())).show(_w, instStr.toLowerCase());
     

   if(instStr.equalsIgnoreCase(INST_STR_HETERODYNE)) {
     return;
   }

  // First fill in the text box.
  TextBoxWidgetExt tbwe;
  CheckBoxWidgetExt cbwe;

  tbwe = (TextBoxWidgetExt) getWidget(_instStr, "objectRecipe");
  try {
    recipe = _spDRRecipe.getObjectRecipeName();
    tbwe.setValue(recipe);
  }catch (NullPointerException ex) { }
  
  // See which type of recipe the selected recipe is, if any.
  // Get the instrument and display the relevant options.
  // Then look for the recipe in these options. If its there highlight it.

  // What I really need to do is introduce imaging/spec capabilities into
  // instruments which I can then get.  Imager-spectrometers will be a 
  // special case

   LookUpTable  rarray = null;
   int index = -1;

   if (instStr.equalsIgnoreCase(INST_STR_SCUBA)) {

     rarray = SpDRRecipe.SCUBA;
	   
   }else if (instStr.equalsIgnoreCase(INST_STR_HETERODYNE)) {

     rarray = SpDRRecipe.HETERODYNE;

   }
   

   // Show the correct recipes, and select the option widget for the type
   _showRecipeType(rarray);

}

/**
 * Override setup to store away a reference to the SpDRRecipe item. Also initialise the widgets
 * Here.
 */
public void
setup(SpItem spItem)
{
  _spDRRecipe = (SpDRRecipe) spItem;
  _initInstWidgets();
   super.setup(spItem);
}

/**
 * Implements the _updateWidgets method from OtItemEditor in order to
 * setup the widgets to show the current values of the item.
 */
protected void
_updateWidgets()
{
   _updateRecipeWidgets();

}

/**
 * A key has been pressed in the text box widget.
 * @see KeyPressWatcher
 */
   public void keyPressed( KeyEvent evt ) {
     System.out.println( "In kp" );

   }

/**
 * Watch changes to the text box.
 * @see TextBoxWidgetWatcher
 */
public void
textBoxKeyPress(TextBoxWidgetExt tbwe)
{
  // This is the watcher entered hen the user types a character in a text box
  // widget, Currently the user specified recipe is the only such widget.
  _currentRecipeSelected = tbwe.getText().trim();

}
 
/**
 * Text box action, ignore.
 * @see TextBoxWidgetWatcher
 */
   public void textBoxAction( TextBoxWidgetExt tbwe ) {
     System.out.println ( "In tba" );
   }


public void
tableRowSelected(TableWidgetExt twe, int rowIndex)
{
   _currentRecipeSelected = (String) twe.getCell(0, rowIndex);
   String _defaultRecipe = "QUICK_LOOK";

// Allow for blank lines and headings.  The latter is defined to contain
// at least two lowercase letters or any space (testing for a colon
// might also be useful).  Merely substitute the default recipe.   
   if ( _currentRecipeSelected.length() == 0 ) {
      _currentRecipeSelected = _defaultRecipe;

   } else {
      int count = 0;
      for ( int i = 0; i < _currentRecipeSelected.length(); i++ ) {
          if ( Character.isWhitespace( _currentRecipeSelected.charAt( i ) ) ) {
             _currentRecipeSelected = _defaultRecipe;
             break;

          } else if ( Character.isLowerCase( _currentRecipeSelected.charAt( i ) ) ) {
             count++;
             if ( count > 1 ) {
                _currentRecipeSelected = _defaultRecipe;
                break;
             }
          }
       }
    }

   // Don't set the value if the new selection is the same as the old
   // (otherwise, we'd fool the OT into thinking a change had been made)
//    String curValue = _spDRRecipe.getRecipeName();
//    if ((curValue != null) && (curValue.equals(recipe))) {
//        //     System.out.println ("New same as old");
//       return;
//    }

}

/**
 * Must watch table widget actions as part of the TableWidgetWatcher
 * interface, but don't care about them.
 */
   public void tableAction( TableWidgetExt twe, int colIndex, int rowIndex ) {}

   /**
    * @see #getWidget(java.lang.String)
    */
   protected JComponent getWidget(String instrument, String widgetName) {
     return getWidget(instrument.toLowerCase() + "_" + widgetName);
   }

   /**
    * Helper method.
    *
    * In EdDRRecipe the widgets of the associated GUI class cannot as easily be referred to directly
    * as in the subclasses of EdCompInstBase because the widget names in the freebongo version often comprised an
    * instrument part and a paramater part (e.g. _instStr, "flatInGroup"). In freebongo something like this is possible
    * because one gets hold of a widget by calling the getWidget(String) method on the presentation. If this method is
    * called with a String that refers to a widget that does not exist (e.g. "IRCAM3.flatInGroup") then a
    * NullPointerException occurres which can be called and ignored. This was used in the freebongo version of this class
    * to handle every widget for every instrument. Whenever a widget was addressed in the context of an instrument whose GUI
    * panel does not have this widget that a NullPointerException occurrs and is ignored. To use a similar approach in
    * the swing version of the OT this helper method getWidget has been introduced in the EdDRRecipe class.
    *
    * ('_' are used instead of '.' to separate instrument name from widget name.)
    * 
    * @return widget in DRRecipeGUI or null if specified widget does not exist.
    */
   protected JComponent getWidget(String widgetName) {
     try {
       return (JComponent)(_w.getClass().getDeclaredField(widgetName).get(_w));
     }
     catch(NoSuchFieldException e) {
       if((System.getProperty("DEBUG") != null) && System.getProperty("DEBUG").equalsIgnoreCase("ON")) {
         System.out.println ("Could not find widget / component \"" + widgetName + "\".");
       }

       return null;
     }
     catch(Exception e) {
       e.printStackTrace();
       return null;
     }
   }
}