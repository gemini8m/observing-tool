/*==============================================================*/
/*                                                              */
/*                UK Astronomy Technology Centre                */
/*                 Royal Observatory, Edinburgh                 */
/*                 Joint Astronomy Centre, Hilo                 */
/*                   Copyright (c) PPARC 2001                   */
/*                                                              */
/*==============================================================*/
// $Id$

package ot;

import java.awt.Font;
import javax.swing.JTextArea;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import java.io.PrintWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.StringTokenizer;

/**
 * Frame for displaying formatted ASCII text.
 *
 * Text is displayed in a monospaced font in order to
 * keep the layout (tables etc.) of the ASCII text.
 *
 * @author Martin Folger (M.Folger@roe.ac.uk)
 */
public class FormattedStringBox extends ReportBox {

  /**
   * JTextArea is used instead of JTextPane of the super class.
   *
   * The reason is that the pack() method does not always use the space required
   * by the displayed text to adjust the the overall size of the the frame.
   * JTextArea works better with pack(). So _textPane of the super class is
   * removed from the frame and _textArea is added instead.
   */
  protected JTextArea _textArea = new JTextArea();

  protected FormattedStringBox() {

    _fileChooser.addChoosableFileFilter(_asciiFileFilter);

    _textArea.setFont(new Font("Monospaced", 0, 10));

    _printButton.setVisible(false);

    getContentPane().remove(_textPane);
    getContentPane().add(_textArea);

    setVisible(true);
  }

  public FormattedStringBox(String message) {
    this();

    _textArea.setText(message);

    setLocation(100, 300);
    pack();
    setVisible(true);
  }

  public FormattedStringBox(String message, String title) {
    this(message);
    
    setTitle(title);
  }

  /**
   * Print not supported for FormattedStringBox.
   */
  public void print() { }

  public void save() {
    if(_fileChooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
      return;
    }
    
    String fileName    = _fileChooser.getSelectedFile().getPath();
    String description = _fileChooser.getFileFilter().getDescription();

    if(fileName == null) {
      return;
    }

    try {
      PrintWriter printWriter = new PrintWriter(new FileWriter(fileName));
      StringTokenizer st = new StringTokenizer(_textArea.getText(), "\n");

      while (st.hasMoreTokens()) {
        printWriter.println(st.nextToken());
      }

      printWriter.close();
    }
    catch(IOException e) {
      JOptionPane.showMessageDialog(this, "Problems writing to file \"" + fileName + "\": " + e,
	                                   "Save Error", JOptionPane.ERROR_MESSAGE);
    }
  }

}

