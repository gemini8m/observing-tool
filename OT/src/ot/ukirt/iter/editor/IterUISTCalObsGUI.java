
/**
 * Title:        JSky<p>
 * Description:  <p>
 * Copyright:    Copyright (c) Allan Brighton<p>
 * Company:      <p>
 * @author Allan Brighton (modified for Michelle/UKIRT by M.Folger@roe.ac.uk)
 *         modified for UIST by A.Pickup@roe.ac.uk
 * @version 1.0
 */
package ot.ukirt.iter.editor;

import java.awt.*;
import javax.swing.*;
import jsky.app.ot.gui.*;

public class IterUISTCalObsGUI extends JPanel {
    GridBagLayout gridBagLayout1 = new GridBagLayout();
    DropDownListBoxWidgetExt repeatComboBox = new DropDownListBoxWidgetExt();
    TextBoxWidgetExt observationTime = new TextBoxWidgetExt();
    GridBagLayout gridBagLayout2 = new GridBagLayout();
    JLabel jLabel6 = new JLabel();
    TextBoxWidgetExt exposureTime = new TextBoxWidgetExt();
    JLabel jLabel5 = new JLabel();
    JLabel jLabel4 = new JLabel();
    JLabel jLabel3 = new JLabel();
    JLabel jLabel2 = new JLabel();
    JLabel jLabel1 = new JLabel();
    JLabel jLabel9 = new JLabel();
    DropDownListBoxWidgetExt calType = new DropDownListBoxWidgetExt();
  JPanel emptyPanel = new JPanel();
  JPanel calTypesPanel = new JPanel();
  CardLayout cardLayout1 = new CardLayout();
  JPanel flatPanel = new JPanel();
  GridBagLayout gridBagLayout3 = new GridBagLayout();
  DropDownListBoxWidgetExt flat_source = new DropDownListBoxWidgetExt();
  JLabel jLabel7 = new JLabel();
  CommandButtonWidgetExt useDefaults = new CommandButtonWidgetExt();
  DropDownListBoxWidgetExt arc_source = new DropDownListBoxWidgetExt();
  JPanel arcPanel = new JPanel();
  JLabel jLabel8 = new JLabel();
  GridBagLayout gridBagLayout4 = new GridBagLayout();
  JLabel jLabel10 = new JLabel();
  TextBoxWidgetExt coadds = new TextBoxWidgetExt();

    public IterUISTCalObsGUI() {
        try {
            jbInit();
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    void jbInit() throws Exception {
        jLabel1.setText("Observation time");
        jLabel1.setForeground(Color.black);
    jLabel1.setToolTipText("");
        jLabel1.setFont(new java.awt.Font("Dialog", 0, 12));
        jLabel2.setText("(sec)");
        jLabel2.setForeground(Color.black);
    jLabel2.setToolTipText("");
        jLabel2.setFont(new java.awt.Font("Dialog", 0, 10));
        jLabel3.setText("X");
        jLabel3.setForeground(Color.black);
        jLabel3.setFont(new java.awt.Font("Dialog", 2, 12));
        jLabel4.setText("Observe");
        jLabel4.setForeground(Color.black);
        jLabel4.setFont(new java.awt.Font("Dialog", 0, 12));
        jLabel5.setText("(sec)");
        jLabel5.setForeground(Color.black);
        jLabel5.setFont(new java.awt.Font("Dialog", 0, 10));
        exposureTime.setBorder(BorderFactory.createLoweredBevelBorder());
        jLabel6.setText("Exposure time");
        jLabel6.setForeground(Color.black);
    jLabel6.setToolTipText("");
        jLabel6.setFont(new java.awt.Font("Dialog", 0, 12));
// Next line was for observationTime, switched to coadds by RDK
        coadds.setBorder(BorderFactory.createLoweredBevelBorder());
// End of switch by RDK
        repeatComboBox.setPreferredSize(new Dimension(50, 26));
        repeatComboBox.setAutoscrolls(true);
        this.setMinimumSize(new Dimension(280, 206));
        this.setPreferredSize(new Dimension(280, 206));
        this.setLayout(gridBagLayout1);
        jLabel9.setFont(new java.awt.Font("Dialog", 0, 12));
        jLabel9.setForeground(Color.black);
    jLabel9.setToolTipText("");
        jLabel9.setText("Calibration type");
    calTypesPanel.setLayout(cardLayout1);
    calTypesPanel.setBorder(BorderFactory.createEtchedBorder());
    flatPanel.setLayout(gridBagLayout3);
    jLabel7.setFont(new java.awt.Font("Dialog", 0, 12));
    jLabel7.setForeground(Color.black);
    jLabel7.setToolTipText("");
    jLabel7.setText("Flat source");
    useDefaults.setBorder(BorderFactory.createRaisedBevelBorder());
    useDefaults.setText("Use defaults");
    arcPanel.setLayout(gridBagLayout4);
    jLabel8.setFont(new java.awt.Font("Dialog", 0, 12));
    jLabel8.setForeground(Color.black);
    jLabel8.setToolTipText("");
    jLabel8.setText("Arc source");
    jLabel10.setFont(new java.awt.Font("Dialog", 0, 12));
    jLabel10.setForeground(Color.black);
    jLabel10.setText("Coadds");
    // Added manually
    repeatComboBox.setBackground(Color.white);
    calType.setBackground(Color.white);
    arc_source.setBackground(Color.white);
    flat_source.setBackground(Color.white);
// Next 3 lines were for coadds, switched to observationTime by RDK
    observationTime.setBackground(new Color(220, 220, 220));
    observationTime.setBorder(BorderFactory.createLoweredBevelBorder());
    observationTime.setEditable(false);
// End of switch by RDK
    this.add(repeatComboBox,       new GridBagConstraints(2, 2, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
// Next line was for observationTime, switched by RDK to coadds
        this.add(coadds,        new GridBagConstraints(2, 5, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
// End of switch by RDK
        this.add(jLabel6,         new GridBagConstraints(0, 3, 2, 1, 0.0, 0.0
            ,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
        this.add(exposureTime,       new GridBagConstraints(2, 3, 1, 2, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
        this.add(jLabel5,       new GridBagConstraints(3, 3, 1, 2, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
        this.add(jLabel4,       new GridBagConstraints(0, 2, 2, 1, 0.0, 0.0
            ,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
        this.add(jLabel3,       new GridBagConstraints(3, 2, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
// jLabel2 switched to be in row 6 because it goes with observationTime (RDK)
        this.add(jLabel2,       new GridBagConstraints(3, 6, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
// End of switch by RDK
        this.add(jLabel9,       new GridBagConstraints(0, 0, 2, 1, 0.0, 0.0
            ,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
        this.add(calType,       new GridBagConstraints(2, 0, 2, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
    this.add(calTypesPanel,       new GridBagConstraints(0, 1, 4, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
    calTypesPanel.add(flatPanel, "FLAT");
    flatPanel.add(flat_source,    new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 0), 0, 0));
    flatPanel.add(jLabel7,      new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 5), 0, 0));
    calTypesPanel.add(emptyPanel, "EMPTY");
    this.add(useDefaults,        new GridBagConstraints(0, 7, 4, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 0, 5), 0, 0));
// Positions of jLabel1 and jLabel10 switched by RDK
    this.add(jLabel10,         new GridBagConstraints(0, 5, 2, 1, 0.0, 0.0
            ,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
    this.add(jLabel1,           new GridBagConstraints(0, 6, 2, 1, 0.0, 0.0
            ,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
// End of change by RDK
// Next line was for coadds, switched by RDK to observationTime
    this.add(observationTime,       new GridBagConstraints(2, 6, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
// End of switch by RDK
    arcPanel.add(arc_source, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 0), 0, 0));
    arcPanel.add(jLabel8,  new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 4, 0, 5), 0, 0));
    calTypesPanel.add(arcPanel,    "ARC");
    }
}
