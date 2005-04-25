/*                                                              */
/*                UK Astronomy Technology Centre                */
/*                 Royal Observatory, Edinburgh                 */
/*                 Joint Astronomy Centre, Hilo                 */
/*                   Copyright (c) PPARC 2003                   */
/*                                                              */
/*==============================================================*/
// $Id$

package edfreq.region;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPopupMenu;
import javax.swing.JMenuItem;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.Box;
import javax.swing.border.TitledBorder;
import javax.swing.border.EtchedBorder;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Window;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Vector;
import java.util.HashMap;

import edfreq.EdFreq;

/**
 * Tool for selecting baseline fit regions and line regions in a spectrum.
 *
 * Terminology: "Baseline region", "Baseline fit region" and "Fit region" all mean the same
 * in the context of this package. They are different from the "Line Region".
 *
 * @author Martin Folger (M.Folger@roe.ac.uk)
 */
public class VelocityRegionEditor extends JPanel implements ActionListener {

    private Color FIT_REGION_BAR_COLOR  = Color.blue;
    private Color LINE_REGION_BAR_COLOR = Color.red;

    private int [] ZOOM_OPTIONS = { 1, 2, 3, 4 };

    private JButton _addFitRegionButton     = new JButton("Add");

    /**
     * Contains the Baseline Fit Region SingleSidebandRangeBar bars.
     *
     * Every SingleSidebandRangeBar that is in _fitRegionBars is also in
     * {@link #_allRegionBars}.
     */
    private Vector _fitRegionBars  = new Vector();

    private Window      _parent = null;
    private JPanel      _rangeBarPanel  = new JPanel();
    private JPanel      _rangeBarLineDisplayPanel = new JPanel(new BorderLayout());
    private JScrollPane _scrollPane;
    private JPanel      _allButtonPanels = new JPanel(new GridLayout(3, 1));

    private VelocityDisplay _vDisp = new VelocityDisplay();

    private int    _feBandWidthPixels;
    private double _feBandWidth;
    private double _feIF;
    private Vector _beBandWidth = new Vector(4);
    private Vector _beIF = new Vector(4);

    private double _mainLine;

    /**
     * Indicates the sideband in which the main (first, uppermost) transition line is located.
     *
     * The meaning of _sideBand differs from that of
     * {@link edfreq.region.RangeBar._associatedSideBand}.
     */
    private int    _sideBand;

    /** Double sideband versus single sideband. */
    private boolean _dsb;

    /**
     * Used to create alternating baseline fit regions below and above the
     * line region when the user adds baseline regions repeatedly.
     *
     * "Inside" means the baseline fit regions is between the LO1 and the
     * center of the sideband.
     */
    private boolean _fitRegionInside = false;

    /**
     * Used to indicate that the baseline regions have been moved.
     * This is to help stop rounding errors when swapping from
     * frequency to pixels and back.
     */
    private boolean _baselinesChanged = false;

    private HashMap _inputValues = new HashMap();

    public VelocityRegionEditor(Window parent) {

        setLayout(new BorderLayout());

        JPanel legendPanel = new JPanel( new GridLayout(4, 0));
        JLabel redLabel = new JLabel("Main Line Frequency");
        redLabel.setForeground(Color.red);
        JLabel blueLabel = new JLabel( "Lines in alternate sideband");
        blueLabel.setForeground(Color.blue);
        JLabel normLabel = new JLabel ("Lines in primary sideband");
        legendPanel.setBorder(new TitledBorder ( new EtchedBorder(EtchedBorder.RAISED), "Legend"));
        legendPanel.add ( redLabel );
        legendPanel.add ( blueLabel );
        legendPanel.add ( normLabel );


        JPanel buttonPanel = new JPanel();
        JLabel label = new JLabel("Baseline Fit Region: ");
        label.setForeground(FIT_REGION_BAR_COLOR);
        label.setPreferredSize(new Dimension(160, label.getPreferredSize().height));
        buttonPanel.add(label);
        buttonPanel.add(_addFitRegionButton);
        _allButtonPanels.add(buttonPanel);
        _rangeBarLineDisplayPanel.add(_rangeBarPanel,  BorderLayout.CENTER);
        _rangeBarLineDisplayPanel.add(_vDisp,    BorderLayout.SOUTH);

        _scrollPane = new JScrollPane(_rangeBarLineDisplayPanel);
        _scrollPane.setPreferredSize(new Dimension(820, 100 + 20));

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(_allButtonPanels, BorderLayout.CENTER);
        bottomPanel.add(legendPanel, BorderLayout.WEST);

        add(_scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        _addFitRegionButton.addActionListener(this);

        _parent = parent;

        if(_parent instanceof Dialog) {
            ((Dialog)_parent).setResizable(false);
        }

        if(_parent instanceof Frame) {
            ((Frame)_parent).setResizable(false);
        }
    }

    public void setSideBand(int sideBand) {
        _sideBand = sideBand;
    }

    /**
     * Switches between single/double mode and lower/upper sideband.
     *
     * @param modeStr Allowed values: "ssb" (single side band), "dsb" (double side band)
     *                {@link orac.jcmt.inst.SpInstHeterodyne.setMode(String)}
     * @param bandStr Allowed values: "usb" (upper side band), "lsb" (lower side band), "best" (whichever sideband is best)
     *                {@link orac.jcmt.inst.SpInstHeterodyne.setBand(String)}
     */
    public void setModeAndBand(String modeStr, String bandStr) {
        if(modeStr.equalsIgnoreCase("ssb")) {
            _dsb = false;
        }
        else {
            _dsb = true;
        }

        if(bandStr.equalsIgnoreCase("lsb")) {
            _sideBand = EdFreq.SIDE_BAND_LSB;
        }
        else {
            _sideBand = EdFreq.SIDE_BAND_USB;
        }
    }

    public void addBaselineFitRegions( double [] xMin, double [] xMax, double lo1, boolean resetLayout) {
//         System.out.println("In VelocityRegionEditor::addBaselineFitRegions():");
//         for ( int i=0; i<xMin.length; i++ ) {
//             System.out.println("\txMin[" + i + "]="+xMin[i]);
//             System.out.println("\txMax[" + i + "]="+xMax[i]);
//         }
//         System.out.println("\tLO1="+lo1);

        _inputValues.clear();
        SingleSidebandRangeBar temp = new SingleSidebandRangeBar(_sideBand);
        int [] beUsbPixels = getBeUpperSideBandPixels();
        temp.resetRangeBar( _feBandWidthPixels, _vDisp.getDisplayWidth(),beUsbPixels, _sideBand, _dsb);

        // Find out how many ranges we are expecting
        int nRanges = temp.getNumRanges();
        // We can now delete temp
        temp = null;

        // Call addBaselineFitRegion based on the input
        double [] minArray = new double [nRanges];
        double [] maxArray = new double [nRanges];
        for ( int i=0; i<(xMin.length/nRanges); i++ ) {
            for ( int j=0; j<nRanges; j++ ) {
                minArray[j] = xMin[i*nRanges + j];
                maxArray[j] = xMax[i*nRanges + j];
            }
            double [] minCopy = new double [minArray.length];
            double [] maxCopy = new double [maxArray.length];
            try {
                System.arraycopy( minArray, 0, minCopy, 0, minCopy.length );
                System.arraycopy( maxArray, 0, maxCopy, 0, maxCopy.length );
            }
            catch (Exception e) {
                System.out.println("Error copying arrays");
                e.printStackTrace();
            }
            Object [] vals = {minCopy, maxCopy};
            _inputValues.put (new Integer(i), vals);
            addBaselineFitRegion( minArray, maxArray, lo1, resetLayout);
        }

        // Set the changed flag to false since we read in some existing values
        _baselinesChanged = false;

    }

    /**
     * Adds a SingleSidebandRangeBar representing a baseline fit region.
     *
     * @param xMin        lower bound of baseline fit region in GHz.
     * @param xMax        upper bound of baseline fit region in GHz.
     * @param lo1         LO1 in GHz.
     * @param resetLayout The changes will only become visible if resetLayout == true.
     *                    However, if several calls to {@link #addLineRegion(int,int,boolean)},
     and addBaselineFitRegion(int,int,boolean) or {@link removeAllRegions(boolean)}
     *                    are made in a row then resetLayout should only be set to true in the last of
     *                    these calls.
     */
    public void addBaselineFitRegion(double [] xMin, double [] xMax, double lo1, boolean resetLayout) {
        int barWidth;
        int barX;

        final SingleSidebandRangeBar fitRegionBar = new SingleSidebandRangeBar(_sideBand);

        int [] beUsbPixels = getBeUpperSideBandPixels();

//         System.out.println("Calling resetRangeBars with args:");
//         System.out.println("\t_feBandWidthPixels=" + _feBandWidthPixels);
//         System.out.println("\twidth=" + _vDisp.getDisplayWidth());
//         for ( int i=0; i<beUsbPixels.length; i++ ) {
//             System.out.println("\tbeUsbPixels[" + i + "]=" + beUsbPixels[i]);
//         }
        fitRegionBar.resetRangeBar(_feBandWidthPixels, _vDisp.getDisplayWidth(),
                beUsbPixels, _sideBand, _dsb);

        //fitRegionBar.addObserver(_vDisp);

        double pixelsPerValue = _vDisp.getPixelsPerValue();

        for (int i=0; i<fitRegionBar.getNumRanges(); i++) {
            if ( xMin != null && xMax != null ) {
                barWidth = (int)Math.round((xMax[i] -  xMin[i]) * pixelsPerValue);
                if(xMin[i] > lo1) {
                    System.out.println( "xMin[i] > lo1");
//                     System.out.println("Calculating barX as follows:");
//                     System.out.println("\txMin=" + xMin[i]);
//                     System.out.println("\tmainLine=" + _mainLine);
//                     System.out.println("\tfeBandwidth=" + _feBandWidth);
//                     System.out.println("\tpixelsPerValue=" + pixelsPerValue);
                    //barX = ((int)Math.round((xMin[i] - lo1) * pixelsPerValue)) - ((_vDisp.getDisplayWidth() / 2) - _feBandWidthPixels);
                    barX = ((int)Math.round((xMin[i] - (_mainLine/1.0E9 - _feBandWidth * 1.0E-9/2)) * pixelsPerValue));
                }
                else {
                    System.out.println("xMin[i] <= lo1" );
                    barX = (_vDisp.getDisplayWidth() / 2) - ((int)Math.round((lo1 - xMin[i]) * pixelsPerValue));
                }
            }
            else {
                barWidth = (fitRegionBar.getMaxX(i) - fitRegionBar.getMinX(i)) / 3;
                if ( _fitRegionInside ) {
                    barX = fitRegionBar.getMinX(i);
                }
                else {
                    barX = fitRegionBar.getMaxX(i) - barWidth;
                }
            }
//            System.out.println("Calling setBars(" + barX + ", " + barWidth + ", " + i + ")");
            fitRegionBar.setBars(barX, barWidth, i);
        }


        // Add a popup menu to allow removal
        final JPopupMenu menu = new JPopupMenu();
        JMenuItem  remove = new JMenuItem("Remove");
        remove.addActionListener( new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                _fitRegionBars.remove(fitRegionBar);
                resetLayout();
                }
                });
        menu.add(remove);

        fitRegionBar.addMouseListener( new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                if ( e.getButton() == MouseEvent.BUTTON3 ) {
                menu.show( e.getComponent(), e.getX(), e.getY() );
                }
                }
                });

        _fitRegionBars.add(fitRegionBar);

        if(resetLayout) {
            resetLayout();
        }
    }

    /**
     * Creates default baseline fit region above or below the center of the
     * frontend sideband.
     *
     * @see addBaselineFitRegion(double,double,double,boolean)
     */
    public void addBaselineFitRegion(boolean reset) {

        // Since this is only called when no baselines already exist, we can set the
        // chnaged flah to true
        _baselinesChanged = true;

        // Assume xMin and xMax to be in the upper sideband. In this context it does not matter
        // whether the mode is single or double sideband and whether we are in the upper or lower
        // sideband. Internally the SingleSidebandRangeBar has RangeBars for both sidebands and
        // the method addLineRegion() accepts xMin and xMax values in both sidebands and
        // sets both RangeBars appropriatedly.
        double [] xMinHz = new double[_beIF.size()];
        double [] xMaxHz = new double[_beIF.size()];

        for ( int i=0; i<_beIF.size(); i++) {
            double beIF = ((Double)_beIF.get(i)).doubleValue();
            double beBandWidth = ((Double)_beBandWidth.get(i)).doubleValue();
            if(_fitRegionInside) {
                xMinHz[i] = (_vDisp.getLO1() + (beIF - (beBandWidth / 2.0)))/1.0E9;
                xMaxHz[i] = (_vDisp.getLO1() + (beIF - (beBandWidth / 6.0)))/1.0E9;
            }
            else {
                xMinHz[i] = (_vDisp.getLO1() + (beIF + (beBandWidth / 6.0)))/1.0E9;
                xMaxHz[i] = (_vDisp.getLO1() + (beIF + (beBandWidth / 2.0)))/1.0E9;
            }

            _fitRegionInside = !_fitRegionInside;

        }
        _fitRegionInside = ( (double)_fitRegionBars.size()%2.0 == 0 );
        //addBaselineFitRegion(xMinHz[0], xMaxHz[0], _vDisp.getLO1() / 1.0E9, resetLayout);
        addBaselineFitRegion(null, null, _vDisp.getLO1() / 1.0E9, reset);
    }

    /**
     * Removes all baseline fit and line regions.
     *
     * @param resetLayout The changes will only become visible if resetLayout == true.
     *                    However, if several calls to {@link addLineRegion(int,int,boolean)},
     *                    {@link #addBaselineFitRegion(int,int,boolean)} or removeAllRegions(boolean)
     *                    are made in a row then resetLayout should only be set to true in the last of
     *                    these calls.
     */
    public void removeAllRegions(boolean resetLayout) {
        _fitRegionBars.clear();

        if(resetLayout) {
            resetLayout();
        }
    }


    public void updateDisplay (
            double mainLineFreq,
            double feIF, double feBandWidth,
            double redshift)
    {

//         System.out.println("In VelocityRegionEditor::updateLineDisplay()");
//         System.out.println("\t(lRangeLimit = " + (mainLineFreq + 0.5 * feBandWidth) + ")");
//         System.out.println("\t(uRangeLimit = " + (mainLineFreq - 0.5 * feBandWidth) + ")");
//         System.out.println("\tmainLineFreq = " + mainLineFreq);
//         System.out.println("\tfeIF = " + feIF);
//         System.out.println("\tfeBandWidth = " + feBandWidth);
//         System.out.println("\tredshift= " + redshift);
        _mainLine = mainLineFreq;
        _feBandWidth = feBandWidth;
        _feIF        = feIF;

        _vDisp.updateDisplay(_mainLine, feIF, feBandWidth, _sideBand, _dsb, redshift);

        // Make sure _vDisp.getPixelsPerValue() is called after _vDisp.updateDisplay();
        _feBandWidthPixels = (int)((feBandWidth / 1.0E9) * _vDisp.getPixelsPerValue());

        _parent.pack();
    }

    public void updateBackendValues(double beIF, double beBandWidth) {
        updateBackendValues( beIF, beBandWidth, 0);
    }

    public void updateBackendValues(double beIF, double beBandWidth, int region) {
        // Reset everything if the region is 0
        if ( region == 0 ) {
            _beIF.clear();
            _beBandWidth.clear();
        }

        try {
            _beIF.setElementAt(new Double(beIF), region);
            _beBandWidth.setElementAt(new Double( beBandWidth ), region );
        }
        catch (ArrayIndexOutOfBoundsException ex) {
            // Just add the elements
            _beIF.add(region, new Double(beIF));
            _beBandWidth.add(region, new Double( beBandWidth ) );
        }
        System.out.println("In VelocityRegionEditor::updateBackendValues - updated to:");
        System.out.println("\tbeIF = " + _beIF);
        System.out.println("\tbeBW = " + _beBandWidth);
    }

    /**
     * Calculates the offsets of the backend sidband range relative to the frontend sidband in pixels.
     *
     * {@link _feIF}, {@link _beIF} and {@link _beBandWidth} must have been set correctly for
     * this method to work.
     *
     * @see #updateLineDisplay(double,double,double,double,double)
     * @see #updateBackendValues(double,double)
     *
     * @param feIF        Frontend IF in Hz.
     * @param beIF        Backend IF (sampler centre frequency) in Hz.
     * @param beBandWidth Backend bandwidth (sampler bandwidth) in Hz.
     */
    public int [] getBeUpperSideBandPixels() {

        int [] result = new int [_beIF.size()*2];
        for ( int i=0; i<_beIF.size(); i++ ) {
            double beIF = ((Double)_beIF.get(i)).doubleValue();
            double beBandWidth = ((Double)_beBandWidth.get(i)).doubleValue();
            // Min, max frequencies in range of upper backend sideband.
            double beMinUSB = _vDisp.getLO1() + (beIF - (0.5 * beBandWidth));
            double beMaxUSB = _vDisp.getLO1() + (beIF + (0.5 * beBandWidth));

            // Min frequency in range of upper frontend sideband.
            double feMinUSB = _vDisp.getLO1() + (_feIF - (0.5 * _feBandWidth));

//             System.out.println("In getBeUpperSideBandPixels()");
//             System.out.println("\tbeIF=" + beIF);
//             System.out.println("\tbeBandWidth=" + beBandWidth);
//             System.out.println("\tfeIF=" + _feIF);
//             System.out.println("\tfeBW=" + _feBandWidth);
//             System.out.println("\tLO1=" + _vDisp.getLO1());
//             System.out.println("\tbeMinUSB=" + beMinUSB);
//             System.out.println("\tbeMaxUSB=" + beMaxUSB);
//             System.out.println("\tfeMinUSB=" + feMinUSB);

            double pixelsPerValue = _vDisp.getPixelsPerValue();
//            System.out.println("\tpixelsPerValue=" + pixelsPerValue);

            result[i*2]     = (int)(((beMinUSB - feMinUSB) / 1.0E9) * pixelsPerValue);
            result[i*2 + 1] = (int)(((beMaxUSB - feMinUSB) / 1.0E9) * pixelsPerValue);
//             for ( int c=0; c< 2; c++ ) {
//                 System.out.println("\tlowerValue="+result[i*2]);
//                 System.out.println("\tupperValue="+result[i*2 + 1]);
//             }

        }

        return result;
    }

    public void setMainLine(double mainLine) {
        _mainLine = mainLine;

        resetMainLine();
    }

    /**
     * Resets pixel value of main line in EmissionLines display.
     */
    public void resetMainLine() {
        _vDisp.setMainLine(_mainLine);
    }

    public double getMainLine() {
        return _mainLine;
    }

    public void resetLayout() {
        _rangeBarPanel.removeAll();

        _rangeBarPanel.removeAll();
        _rangeBarPanel.setLayout(new GridLayout(_fitRegionBars.size(), 1));

        for(int i = 0; i < _fitRegionBars.size(); i++) {
            _rangeBarPanel.add((SingleSidebandRangeBar)_fitRegionBars.get(i));
        }

        _scrollPane.setPreferredSize(new Dimension(820, _rangeBarLineDisplayPanel.getPreferredSize().height + 20));

        _parent.pack();
    }

    public double [][] getBaselineFitRegions() {
        //double [][] result = new double[(2 * _combinedRegionBars.size()) + _fitRegionBars.size()][];
        int numRegions = 0;
        for ( int i=0; i<_fitRegionBars.size(); i++ ) {
            numRegions += ((SingleSidebandRangeBar)_fitRegionBars.get(i)).getNumRanges();
        }
        double [][] result = new double[numRegions][];

        int barX;
        int barWidth;

        double pixelsPerValue = _vDisp.getPixelsPerValue();

        int k = 0;
        for(int i = 0; i < _fitRegionBars.size(); i++) {
            for ( int n=0; n<((SingleSidebandRangeBar)_fitRegionBars.get(i)).getNumBars(); n++ ) {
                barX     = ((SingleSidebandRangeBar)_fitRegionBars.get(i)).getBarX(n);
                barWidth = ((SingleSidebandRangeBar)_fitRegionBars.get(i)).getBarWidth(n);
                if ( ((SingleSidebandRangeBar)_fitRegionBars.get(i)).hasBaselineChanged() || _baselinesChanged ) {
                    result[k++] = getRegion(barX, barWidth, pixelsPerValue, _vDisp.getLO1());
                }
                else {
                    Object [] o = ((Object[])_inputValues.get(new Integer(i)));
                    double [] d1 = (double []) o[0];
                    double [] d2 = (double []) o[1];
                    for ( int l=0; l<d1.length; l++ ) {
                        double [] d = {d1[l]*1.0E9, d2[l]*1.0E9};
                        result[k++] = d;
                    }
                }
            }
        }
        _baselinesChanged = false;

        return result;
    }

    public double [] getRegion(int barX, int barWidth, double pixelsPerValue, double lo1) {
        double [] result = new double[2];
//         System.out.println("Called VelocityRegionEditor::getRegion(" + barX + ", "
//                 + barWidth + ", " + pixelsPerValue + ", " + lo1 + ")");
//         System.out.println("barX/pixVal = " + (barX / pixelsPerValue));

        result[0] = ((barX / pixelsPerValue) * 1.0E9) + (_feIF - (0.5 * _feBandWidth)) + lo1;
        result[1] = result[0] + ((barWidth / pixelsPerValue) * 1.0E9);

        // If the region should appear in the lower sideband than reflect the values on LO1.
        if(_sideBand == EdFreq.SIDE_BAND_LSB) {
            result[0] -= 2.0 * (result[0] - lo1);
            result[1] -= 2.0 * (result[1] - lo1);

            // Now swap the values
            double result0 = result[0];
            result[0] = result[1];
            result[1] = result0;
        }

        return result;
    }

    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();

        if(source == _addFitRegionButton) {
            addBaselineFitRegion(false);
        }

        resetLayout();
    }

    public int getNumRegions() {
        int numRegions = 0;
        if ( _fitRegionBars != null ) {
            numRegions = _fitRegionBars.size();
        }
        return numRegions;
    }

    public boolean changedBaseline() {
        boolean flag = false;
        if ( _baselinesChanged ) {
            flag = true;
        }
        if ( !flag ) {
            for ( int i=0; i<_fitRegionBars.size(); i++ ) {
                SingleSidebandRangeBar bar = (SingleSidebandRangeBar)_fitRegionBars.get(i);
                if ( bar.hasBaselineChanged() ) {
                    flag = true;
                    break;
                }
            }
        }

        return flag;
    }

    public static void main(String [] args) {
        //         if(((args.length != 3) && (args.length != 4)) || ((args.length == 1) && args[0].equals("-h"))) {
        //             System.out.println("Usage: VelocityRegionEditor lRangeLimit uRangeLimit redshift [dsb | lsb | usb]");
        //             System.out.println("  where lRangeLimit and uRangeLimit are in GHz");
        //             System.exit(0);
        //         }
        // 
        //         double lRangeLimit = Double.parseDouble(args[0]) * 1.0E9;
        //         double uRangeLimit = Double.parseDouble(args[1]) * 1.0E9;
        //         double redshift    = Double.parseDouble(args[2]);
        // 
        //         double feIF        = 4.0E9;
        //         double feBandWidth = 1.8E9;
        // 
        //         int sideBand = EdFreq.SIDE_BAND_USB;
        //         boolean dsb = true;
        // 
        //         if(args.length == 4) {
        //             dsb = false;
        // 
        //             if(args[3].equalsIgnoreCase("usb")) {
        //                 sideBand = EdFreq.SIDE_BAND_USB;
        //             }
        // 
        //             if(args[3].equalsIgnoreCase("lsb")) {
        //                 sideBand = EdFreq.SIDE_BAND_LSB;
        //             }
        //         }
        // 
        //         System.out.println("Using");
        //         System.out.println("lRangeLimit = " + lRangeLimit);
        //         System.out.println("uRangeLimit = " + uRangeLimit);
        //         System.out.println("feIF        = " + feIF);
        //         System.out.println("feBandWidth = " + feBandWidth);
        //         System.out.println("redshift    = " + redshift);
        //         System.out.println("sideBand    = " + sideBand);
        // 
        //         System.out.println("\nWorking Example: VelocityRegionEditor 200 220 0");
        // 
        //         JFrame frame = new JFrame("Spectral Region Editor");
        // 
        //         VelocityRegionEditor spectralRegionEditor = new VelocityRegionEditor(frame);
        //         spectralRegionEditor.setSideBand(sideBand);
        // 
        //         spectralRegionEditor.updateLineDisplay(lRangeLimit, uRangeLimit,
        //                 feIF, feBandWidth,
        //                 redshift);
        // 
        //         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // 
        //         frame.getContentPane().add(spectralRegionEditor);
        // 
        //         frame.setLocation(100, 100);
        //         frame.pack();
        //         frame.setVisible(true);
    }

}

