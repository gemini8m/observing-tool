// Copyright 1997 Association for Universities for Research in Astronomy, Inc.,
// Observatory Control System, Gemini Telescopes Project.
// See the file COPYRIGHT for complete details.
//
// $Id$
//
package jsky.app.ot.editor;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Enumeration;

import javax.swing.ImageIcon;

import jsky.app.ot.gui.TableWidgetExt;
import jsky.app.ot.gui.TableWidgetWatcher;
import jsky.app.ot.gui.TextBoxWidgetExt;
import jsky.app.ot.gui.TextBoxWidgetWatcher;
import jsky.util.gui.DialogUtil;

import gemini.sp.SpItem;
import gemini.sp.SpOffsetPos;
import gemini.sp.SpOffsetPosList;
import gemini.sp.iter.SpIterOffset;

import jsky.app.ot.tpe.TelescopePosEditor;
import jsky.app.ot.tpe.TpeManager;
import jsky.app.ot.OtCfg;

import gemini.util.TelescopePos;
import gemini.util.TelescopePosWatcher;
import orac.jcmt.iter.SpIterRasterObs;

/**
 * This is the editor for Offset Iterator component.  It allows a list of
 * offset positions to be entered and ordered.
 *
 * @see SpOffsetPos
 *
 * @author modified for JCMT OT by Martin Folger (MFO, M.Folger@roe.ac.uk)
 */
public final class EdIterOffset extends OtItemEditor
    implements TableWidgetWatcher,  TextBoxWidgetWatcher, 
	       TelescopePosWatcher, ActionListener, MouseListener {

    private TextBoxWidgetExt     _xOffTBW;
    private TextBoxWidgetExt     _yOffTBW;

    private OffsetPosTableWidget _offTW;
    private SpOffsetPos          _curPos;	// Position being edited
    private SpOffsetPosList	 _opl;		// Position list being edited

    private IterOffsetGUI        _w;             // the GUI layout panel

    private String BUTTON_TEXT_ROTATION_FALSE = "Display Rotated Offsets";
    private String BUTTON_TEXT_ROTATION_TRUE  = "Offsets Rotated";
    private double TO_RADIANS = Math.PI/180.0;
    private double TO_DEGREES = 1.0/TO_RADIANS;

    /**
     * The constructor initializes the title, description, and presentation source.
     */
    public EdIterOffset() {
	_title       ="Offset Iterator";
	_presSource  = _w = new IterOffsetGUI();
	_description ="Construct offset based patterns with this iterator.";

	// JBuilder has some problems with image buttons...
	ClassLoader cl = ClassLoader.getSystemClassLoader();
        _w.topButton.setIcon(new ImageIcon(cl.getResource("jsky/app/ot/images/top.gif")));
        _w.upButton.setIcon(new ImageIcon(cl.getResource("jsky/app/ot/images/up.gif")));
        _w.bottomButton.setIcon(new ImageIcon(cl.getResource("jsky/app/ot/images/bottom.gif")));
        _w.downButton.setIcon(new ImageIcon(cl.getResource("jsky/app/ot/images/down.gif")));
	_w.pqItem.setIcon(new ImageIcon(cl.getResource("jsky/app/ot/images/pq.gif")));

        if(!OtCfg.telescopeUtil.supports(OtCfg.telescopeUtil.FEATURE_OFFSET_GRID_PA)) {
          _w.paLabel.setVisible(false);
	  _w.paTextBox.setVisible(false);
	  _w.displayRotatedOffsets.setVisible(false);
	  _w.setSpacingButton.setVisible(false);
	}

	_w.newButton.addActionListener(this);
	_w.removeAllButton.addActionListener(this);
	_w.removeButton.addActionListener(this);
	_w.topButton.addActionListener(this);
	_w.upButton.addActionListener(this);
	_w.downButton.addActionListener(this);
	_w.bottomButton.addActionListener(this);
	_w.createGridButton.addActionListener(this);
	_w.centreOnBaseButton.addActionListener(this);
	_w.displayRotatedOffsets.addMouseListener(this);

        _w.paTextBox.addWatcher(this);
	_w.setSpacingButton.addActionListener(this);
    }


    /**
     * This method initializes the widgets in the presentation to reflect the
     * current values of the items attributes.
     */
    protected void _init() {
	TextBoxWidgetExt tbwe = _w.titleTBW;
	tbwe.addWatcher(this);

	_xOffTBW = _w.xOffset;
	_xOffTBW.addWatcher(this);
	_xOffTBW.setValue("");

	_yOffTBW = _w.yOffset;
	_yOffTBW.addWatcher(this);
	_yOffTBW.setValue("");

	_offTW = _w.offsetTable;
	_offTW.addWatcher(this);
	_offTW.setColumnHeaders(new String[]{"#", "p Offset", "q Offset"});
	_offTW.setBackground(_w.getBackground());
    }


    /**
     * Show the given SpOffsetPos
     */
    public void	showPos( SpOffsetPos op ) {
	_xOffTBW.setValue( op.getXaxisAsString() );
	_yOffTBW.setValue( op.getYaxisAsString() );
    }

    /**
     * Implements the _updateWidgets method from OtItemEditor in order to
     * setup the widgets to show the current values of the item.
     */
    protected void _updateWidgets() {

	TextBoxWidgetExt tbwe;
	SpIterOffset sio = (SpIterOffset) _spItem;

	// Get the title
	tbwe = _w.titleTBW;
	tbwe.setText( sio.getTitleAttr() );

	// Get the current offset list and fill in the table widget
	_opl = sio.getPosList();
	_offTW.reinit(_opl);

	// Select the position that was previously selected.
	// This has probably never worked (neither Freebongo nor Swing OT)
	// because the table seems to be reset somehow after _updateWidgets().
	// And ".gui.selectedOffsetPos" seems to be set to _curPos.getTag()
	// rather then to the selectedRow. (MFO, 6 December 2001)
	int selIndex = _avTab.getInt(".gui.selectedOffsetPos", 0);
	_offTW.selectPos(selIndex);

        _w.paTextBox.setValue(sio.getPosAngle());

        // added by MFO (19 February 2002)
//	if(containsScanMap(sio)) {
//            _w.createScanMosaicButton.setEnabled(true);
//	}
//	else {
//            _w.createScanMosaicButton.setEnabled(false);
//	}
    }

    /**
     * A table row was selected, so show the selected position.
     *
     * @see TableWidgetWatcher
     */
    public void	tableRowSelected(TableWidgetExt twe, int rowIndex) {
	if (twe != _offTW) return;		// shouldn't happen

	// Show the selected position
	if (_curPos != null) _curPos.deleteWatcher(this);
	_curPos = _offTW.getSelectedPos();
	_curPos.addWatcher(this);
	showPos(_curPos);

	_avTab.set(".gui.selectedOffsetPos", _curPos.getTag());
    }

    /**
     * As part of the TableWidgetWatcher interface, this method must
     * be supported, though we don't care about TableWidget actions.
     * @see TableWidgetWatcher
     */
    public void tableAction(TableWidgetExt twe, int colIndex, int rowIndex) {}

    /**
     * Watch changes to the x and y offset text boxes.
     * @see TextBoxWidgetWatcher
     */
    public void textBoxKeyPress(TextBoxWidgetExt tbwe) {
	SpIterOffset iterOffset = (SpIterOffset) _spItem;

	if (tbwe == _w.xOffset) {
	    double nVal = _xOffTBW.getDoubleValue(0.0);
    
	    if (_curPos == null)  return;
	    _curPos.deleteWatcher(this);
	    _curPos.setXY(nVal, _curPos.getYaxis());
	    _curPos.addWatcher(this);

	} 
	else if (tbwe == _w.yOffset) {
	    double nVal = _yOffTBW.getDoubleValue(0.0);
    
	    if (_curPos == null)  return;
	    _curPos.deleteWatcher(this);
	    _curPos.setXY(_curPos.getXaxis(), nVal);
	    _curPos.addWatcher(this);

	} 
	else if (tbwe == _w.titleTBW) {
	    _spItem.setTitleAttr(tbwe.getText().trim());
	}
	else if (tbwe == _w.paTextBox) {
	    iterOffset.setPosAngle(tbwe.getText().trim());
	}	
    }
 
    /**
     * Text box action, ignore.
     * @see TextBoxWidgetWatcher
     */
    public void textBoxAction(TextBoxWidgetExt tbwe) {}
 

    /**
     * The current position location has changed.
     * @see TelescopePosWatcher
     */
    public void telescopePosLocationUpdate(TelescopePos tp) {
	telescopePosGenericUpdate(tp);
    }
 

    /**
     * The current position has been changed in some way.
     * @see TelescopePosWatcher
     */
    public void telescopePosGenericUpdate(TelescopePos tp) {
	if (tp != _curPos) {
	    // This shouldn't happen ...
	    System.out.println(getClass().getName() + ": received a position " +
			       " update for a position other than the current one: " + tp);
	    return;
	}
	showPos(_curPos);
    }

    private double _getGridXOffset() {
	TextBoxWidgetExt tbwe;
	tbwe = _w.gridXOffset;
	return tbwe.getDoubleValue(0.0);
    }

    private double _getGridYOffset() {
	TextBoxWidgetExt tbwe;
	tbwe = _w.gridYOffset;
	return tbwe.getDoubleValue(0.0);
    }

    private double _getGridXSpacing() {
	TextBoxWidgetExt tbwe;
	tbwe = _w.gridXSpacing;
	return tbwe.getDoubleValue(0.0);
    }

    private double _getGridYSpacing() {
	TextBoxWidgetExt tbwe;
	tbwe = _w.gridYSpacing;
	return tbwe.getDoubleValue(0.0);
    }

    private int _getGridRows() {
	TextBoxWidgetExt tbwe;
	tbwe = _w.gridRows;
	return tbwe.getIntegerValue(1);
    }

    private int _getGridCols() {
	TextBoxWidgetExt tbwe;
	tbwe = _w.gridCols;
	return tbwe.getIntegerValue(1);
    }


    //
    // Add offset positions in a grid pattern, according to the specifications
    // int the "Create Grid" group box.
    //
    private void _createGrid() {
// 	_opl.deleteWatcher(_offTW);
// 	_opl.removeAllPositions();

	double xOff = _getGridXOffset();
	double yOff = _getGridYOffset();

	double xSpace = _getGridXSpacing();
	double ySpace = _getGridYSpacing();

	int rows = _getGridRows();
	int cols = _getGridCols();

	int sign = -1;
	for (int y=0; y<rows; ++y) {
	    for (int x=0; x<cols; ++x) {
		_opl.createPosition(xOff, yOff);
		xOff = xOff + ((sign == -1) ? -xSpace : xSpace);
	    }
	    sign = -sign;
	    xOff  = xOff + ((sign == -1) ? -xSpace : xSpace);
	    yOff -= ySpace;
	}

	_offTW.reinit(_opl);
	_offTW.selectRowAt(0);

	// Added by MFO, 7 December 2001
	// I think this is implemented in a different way in Gemini ot-2000B.12.
	try {
	  TpeManager.get(_spItem).reset(_spItem);
	}
	catch(NullPointerException e) {
	  // ignore
	}
    }

    /**
     * Get the selected position.
     */
    public SpOffsetPos getSelectedPos() {
	return _offTW.getSelectedPos();
    }
 
    /**
     * Handle button presses.
     */
    public void actionPerformed(ActionEvent evt) {
	Object w  = evt.getSource();

	// Create a new offset position

	if (w == _w.newButton) {
	    SpOffsetPos op;
	    if (_curPos == null) {
		op = _opl.createPosition();
	    } else {
		int i = _opl.getPositionIndex(_curPos);
		op = _opl.createPosition(i+1);
	    }
	    return;
	}

	// Remove an offset position
	if (w == _w.removeAllButton) {
	    _opl.removeAllPositions();
	    return;
	}


	// Remove an offset position

	if (w == _w.removeButton) {
	    if (_curPos == null) return;

	    // Remove the selected position, remembering its index
	    int index = _opl.getPositionIndex(_curPos);
	    _opl.removePosition(_curPos);

	    return;
	}

	// Move an offset position to the top

	if (w == _w.topButton) {
	    if (_curPos == null) return;

	    // Move the current position to the front.
	    _opl.positionToFront(_curPos);
	    return;
	}

	// Move an offset position up

	if (w == _w.upButton) {
	    if (_curPos == null) return;

	    // Move the current position to the front.
	    _opl.decrementPosition(_curPos);
	    return;
	}

	// Move an offset position down

	if (w == _w.downButton) {
	    if (_curPos == null) return;

	    // Move the current position to the front.
	    _opl.incrementPosition(_curPos);
	    return;
	}

	// Move an offset position to the back

	if (w == _w.bottomButton) {
	    if (_curPos == null) return;

	    // Move the current position to the front.
	    _opl.positionToBack(_curPos);
	    return;
	}

	// Grid creation
	if (w == _w.createGridButton) {
	    _createGrid();
	    return;
	}

	// Centre grid on base position (MFO, 24 August 2001)
	if (w == _w.centreOnBaseButton) {
	    setGridOffsets();
	    return;
	}

	// Take Scan Area width and height as spacing paramters.
	if (w == _w.setSpacingButton) {
          SpIterRasterObs iterRaster = getRasterObsIterator(_spItem);

	  if(iterRaster == null) {
            DialogUtil.error(_w, "No Scan/Raster Observe found.");
	  }
	  else {
            _w.gridXSpacing.setValue(iterRaster.getWidth());
            _w.gridYSpacing.setValue(iterRaster.getHeight());
	  }
	}
    }

  // Added by MFO (20 February 2002)
  public void mouseClicked(MouseEvent e) { }
  public void mouseEntered(MouseEvent e) { }
  public void mouseExited(MouseEvent e) { }
  public void mousePressed(MouseEvent e) {
    setOffsetsRotation(true);
    _w.displayRotatedOffsets.setText(BUTTON_TEXT_ROTATION_TRUE);
  }

  public void mouseReleased(MouseEvent e) {
    setOffsetsRotation(false);
    _w.displayRotatedOffsets.setText(BUTTON_TEXT_ROTATION_FALSE);
  }


    /**
     * Centers the pattern around (0, 0).
     *
     * Added by MFO (22 August 2001).
     */
    protected void setGridOffsets() {
      // x
      double gridOffset  =  0;
      double gridSpacing = 60;
      double gridSteps   =  2;
      
      gridSpacing = _w.gridXSpacing.getDoubleValue(gridSpacing);
      gridSteps   = _w.gridCols.getDoubleValue(gridSteps);

      gridOffset = (gridSpacing / 2.0) * (gridSteps - 1);
      _w.gridXOffset.setValue(gridOffset);

      // y
      gridOffset  =  0;
      gridSpacing = 60;
      gridSteps   =  2;
      
      gridSpacing = _w.gridYSpacing.getDoubleValue(gridSpacing);
      gridSteps   = _w.gridRows.getDoubleValue(gridSteps);

      gridOffset = (gridSpacing / 2.0) * (gridSteps - 1);
      _w.gridYOffset.setValue(gridOffset);

      _createGrid();
    }

    public void setOffsetsRotation(boolean rotated) {
      if(rotated) {
        _w.paTextBox.setValue("0.0");

	// Get the current offset list
	double posAngle, rotatedX, rotatedY;
	TelescopePos tp;
	for(int i = 0; i < _opl.size(); i++) {
	  tp = _opl.getPositionAt(i);
	  posAngle = _opl.getPosAngle();

	  rotatedX = (tp.getXaxis() *   Math.cos(posAngle*TO_RADIANS) ) + 
	      (tp.getYaxis() * Math.sin(posAngle*TO_RADIANS));
	  rotatedY = (tp.getXaxis() * (-Math.sin(posAngle*TO_RADIANS))) + 
	      (tp.getYaxis() * Math.cos(posAngle*TO_RADIANS));

          _offTW.setValueAt("" + (Math.rint(rotatedX * 10) / 10), i, 1);
          _offTW.setValueAt("" + (Math.rint(rotatedY * 10) / 10), i, 2);
        }
      }
      // Undo the rotation
      else {
        _updateWidgets();
      }
    }

    /**
     * Checks whether this offset iterator contains a Scan/Raster map.
     *
     * Returns true if there is an instance of {@link orac.jcmt.iter.SpIterRasterObs}
     * inside the offset iterator item.
     */
    private static SpIterRasterObs getRasterObsIterator(SpItem spItem) {
      Enumeration children   = spItem.children();
      SpItem  child          = null;
      SpIterRasterObs result = null;

      while(children.hasMoreElements() && (result == null)) {
        child = (SpItem)children.nextElement();

        if(child instanceof SpIterRasterObs) {
          result = (SpIterRasterObs)child;
	}
	else {
          result = getRasterObsIterator(child);
	}
      }

      return result;
    }
}
