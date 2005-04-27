package edfreq;

import java.util.Hashtable;
import java.util.Vector;

import java.io.InputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;

import orac.util.InstCfg;
import orac.util.InstCfgReader;

/**
 * XML serialised instances of this class are used as Frequency Editor configuration files/resources.
 *
 * This class provides static methods for (de)serialisation using the package JSX.
 * If no xml configuration file is available then the default constructor can be used
 * to create a default configuration object (ACSIS setup).
 * <p>
 * The {@link edfreq.FrontEnd} class hold a public static reference to an FrequencyEditorCfg object
 * ({@link edfreq.FrontEnd.cfg}) which can be used throughout the frequency editor code.
 *
 * <h3>How to create a new xml configuration file</h3>
 *
 * The xml configuration files currently used are <tt>EDFREQ/cfg/edfreq/acsisCfg.xml</tt> and
 * <tt>EDFREQ/cfg/edfreq/dasCfg.xml</tt>.<p>
 *
 * If this FrequencyEditorCfg class or any classes that are used as fields of
 * FrequencyEditorCfg are significanlty changed, i.e. if fields are added or
 * removed from them, then the xml configuration file which is an xml
 * serialisation of an FrequencyEditorCfg object will probably have to be
 * recreated. This can be done using the method
 * {@link #createDefaultCfgFile(java.io.File)} which is used in the main
 * method of FrequencyEditorCfg. So a new xml configuration can be created on
 * the command line using the commands
 *
 * <pre><tt>
 *
 *   cd cvsWorkingDirectory/ot
 *   mv -i EDFREQ/cfg/edfreq/acsisCfg.xml EDFREQ/cfg/edfreq/old_acsisCfg.xml
 *   java -classpath EDFREQ/tools/JSX1.0.1.1.jar:EDFREQ/install/classes edfreq.FrequencyEditorCfg EDFREQ/cfg/edfreq/acsisCfg.xml
 *
 * </tt></pre>
 *
 * If only the values of the fields of FrequencyEditorCfg (or the values of
 * the objects which are themselves fields of FrequencyEditorCfg) have
 * changed. Then the xml configuration file can be editied manually. <b>Make
 * sure you invent appropriate <tt>alias-ID</tt> attributes for each added xml
 * element.</b> I think alias-IDs must not be duplicated. I am not sure
 * whether all alias-ID taken together must form a contiguous. I do not think
 * they have to appear in the right order in the xml configuration file.<p>
 *
 * If no configuration xml file is specified (see {@link #FREQ_EDITOR_CFG_PROPERTY}) or found
 * then the {@link #FrequencyEditorCfg() default constructor} is used.
 *
 * @author Martin Folger
 */
public class FrequencyEditorCfg {

  /**
   * System property containing the location of the xml configuration file
   * as a resource in the classpath.
   */
  public static final String FREQ_EDITOR_CFG_PROPERTY = "FREQ_EDITOR_CFG";

  public String[] frontEnds;
  public Hashtable frontEndTable  = new Hashtable();
  public Hashtable frontEndMixers = new Hashtable();
  public String [] velocityFrames = { "LSR", "Geocentric", "Heliocentric" };
  public boolean centreFrequenciesAdjustable;
  public Hashtable receivers;

  private static FrequencyEditorCfg _frequencyEditorCfg = null;

  /**
   * Constructor used if no xml configuration file is specified or found.
   */
  public FrequencyEditorCfg() {
    frontEnds     = new String[] { "A3", "B3", "WC", "WD", "HARP-B" };

    // Put the default mode (dsb or ssb) first in the array.
    frontEndTable.put("A3",     new String[]{ "dsb" });
    frontEndTable.put("B3",     new String[]{ "ssb", "dsb" });
    frontEndTable.put("WC",     new String[]{ "ssb", "dsb" });
    frontEndTable.put("WD",     new String[]{ "ssb", "dsb" });
    frontEndTable.put("HARP-B", new String[]{ "ssb" });

    frontEndMixers.put("A3",     new String[]{ "Single Mixer" });
    frontEndMixers.put("B3",     new String[]{ "Single Mixer", "Dual Mixer" });
    frontEndMixers.put("WC",     new String[]{ "Single Mixer", "Dual Mixer" });
    frontEndMixers.put("WD",     new String[]{ "Single Mixer", "Dual Mixer" });
    frontEndMixers.put("HARP-B", new String[]{ "Single Mixer" });

    centreFrequenciesAdjustable = true;
    receivers = ReceiverList.getReceiverTable();
  }

  public static FrequencyEditorCfg getConfiguration() {
    if(_frequencyEditorCfg == null) {
        String acsisCfgFile = System.getProperty("ot.cfgdir") + File.separator + "acsis.cfg";
        _frequencyEditorCfg = getConfiguration(acsisCfgFile);
//       String freqEditorCfgFile = System.getProperty(FREQ_EDITOR_CFG_PROPERTY);
//       URL freqEditorCfgUrl = null;
// 
//       if(freqEditorCfgFile != null) {
//         freqEditorCfgUrl = ClassLoader.getSystemClassLoader().getResource(freqEditorCfgFile);
//       }
// 
//       if(freqEditorCfgUrl != null) {
//         try {
//           _frequencyEditorCfg = getConfiguration(freqEditorCfgUrl.openStream());
//         }
//         catch(IOException e) {
//           System.out.println("Using default FrequencyEditorCfg object.");
//           _frequencyEditorCfg = new FrequencyEditorCfg();
//         }
//       }
//       else {
//         System.out.println("Using default FrequencyEditorCfg object.");
//         _frequencyEditorCfg = new FrequencyEditorCfg();
//       }
    }

    return _frequencyEditorCfg;
  }

//   public static FrequencyEditorCfg getConfiguration(InputStream inputStream) {
//     try {
//       ObjIn objIn = new ObjIn(inputStream);
//       return (FrequencyEditorCfg)objIn.readObject();
//     }
//     catch(Exception e) {
//       System.out.println("Could not create a FrequencyEditorCfg object from the input stream provided.");
//       e.printStackTrace();
//       
//       return new FrequencyEditorCfg();
//     }
//   }

  public static FrequencyEditorCfg getConfiguration( String fileName ) {

      InstCfgReader rdr = new InstCfgReader (fileName);
      InstCfg instInfo = null;
      String block = null;

      String [] myFrontEnds = null;
      Hashtable myReceivers = null;

      if ( _frequencyEditorCfg == null )  {
          _frequencyEditorCfg = new FrequencyEditorCfg();
      }

      try {
          while ((block = rdr.readBlock()) != null) {
              instInfo = new InstCfg (block);
              if ( InstCfg.matchAttr (instInfo, "receivers") ) {
                  String [][] recList = instInfo.getValueAs2DArray();
                  myFrontEnds = new String [recList.length];
                  myReceivers = new Hashtable(recList.length);
                  try {
                      for ( int i=0; i<recList.length; i++ ) {
                          myFrontEnds[i] = recList[i][0];
                          double loMin = Double.parseDouble(recList[i][1]);
                          double loMax = Double.parseDouble(recList[i][2]);
                          double feIF  = Double.parseDouble(recList[i][3]);
                          double bw    = Double.parseDouble(recList[i][4]);
                          myReceivers.put( recList[i][0], new Receiver(myFrontEnds[i], loMin, loMax, feIF, bw) );
                      }
                      _frequencyEditorCfg.receivers = myReceivers;
                      _frequencyEditorCfg.frontEnds = myFrontEnds;
                  }
                  catch (Exception e) {
                      e.printStackTrace();
                  }
              }
              else if ( InstCfg.matchAttr (instInfo, "modes") ) {
//                   String [] modes = instInfo.getValueAsArray();
//                   Hashtable myFrontEndTable = new Hashtable(modes.length);
//                   for ( int i=0; i<modes.length; i++ ) {
//                       System.out.println("Setting frontEndTable key="+myFrontEnds[i] + ", value=" + modes[i]);
//                       myFrontEndTable.put( myFrontEnds[i], modes[i].split("\\s+"));
//                   }
                  String [][] modes = instInfo.getValueAs2DArray();
                  Hashtable myFrontEndTable = new Hashtable(modes.length);
                  for (int i=0; i<modes.length; i++ ) {
                      myFrontEndTable.put( myFrontEnds[i], modes[i]);
                  }
                  _frequencyEditorCfg.frontEndTable = myFrontEndTable;
              }
              else if ( InstCfg.matchAttr (instInfo, "mixers") ) {
                  String [][] mixers = instInfo.getValueAs2DArray();
                  Hashtable myFrontEndMixers = new Hashtable(mixers.length);
                  for ( int i=0; i<mixers.length; i++ ) {
                      myFrontEndMixers.put( myFrontEnds[i], mixers[i]);
                  }
                  _frequencyEditorCfg.frontEndMixers = myFrontEndMixers;
              }
              else if ( InstCfg.matchAttr (instInfo, "velocity_frames") ) {
                  String [] myVelocityFrames = instInfo.getValueAsArray();
              }
              else if ( InstCfg.likeAttr ( instInfo, "bandspecs" ) ) {
                  String [][] specs    = instInfo.getValueAs2DArray();
                  double [] bandWidths = new double[specs.length];
                  double [] overlaps   = new double[specs.length];
                  int [] channels      = new int[specs.length];
                  int [] hybSubbands   = new int[specs.length];
                  try {
                      for ( int i=0; i<specs.length; i++ ) {
                          bandWidths[i]  = Double.parseDouble(specs[i][0]);
                          overlaps[i]    = Double.parseDouble(specs[i][1]);
                          channels[i]    = Integer.parseInt(specs[i][2]);
                          hybSubbands[i] = Integer.parseInt(specs[i][3]);
                      }
                      BandSpec bs = null;
                      boolean foundMatch = false;
                      for ( int i=0; i<myFrontEnds.length; i++ ) {
                          if ( InstCfg.likeAttr (instInfo, myFrontEnds[i].replaceAll("[^a-zA-Z0-9]", "")) ) {
                              foundMatch = true;
                              bs = new BandSpec("1", specs.length, bandWidths, overlaps, channels, hybSubbands);
                              Vector bsVector = new Vector();
                              bsVector.add(bs);
                              ((Receiver)_frequencyEditorCfg.receivers.get(myFrontEnds[i])).setBandSpecs(bsVector);
                              break;
                          }
                      }
                      if (!foundMatch) {
                          System.out.println("Failed to find match for keyword " + instInfo.getKeyword());
                      }
                  }
                  catch (Exception e) {
                      e.printStackTrace();
                  }
              }
          }
      }
      catch (IOException ioe) {
          ioe.printStackTrace();
      }

      return _frequencyEditorCfg;
  }

//   public static void main(String [] args) {
//     if(args.length > 0) {
//       File cfgFile = new File(args[0]);
// 
//       if(cfgFile.exists()) {
//         System.out.println("Cannot create xml cfg file " + args[0] + ". File exists.");
// 	return;
//       }
//       else {
//         createDefaultCfgFile(cfgFile);
//       }
//     }
//   }

//   public static void createDefaultCfgFile(File file) {
//     try {
//       ObjOut objOut = new ObjOut(false, new FileWriter(file));
//       FrequencyEditorCfg frequencyEditorCfg = new FrequencyEditorCfg();
//       objOut.writeObject(frequencyEditorCfg);
//     }
//     catch(IOException e) {
//       e.printStackTrace();
//     }
//   }

  public String toString() {
      StringBuffer sb = new StringBuffer();
      sb.append("[\n");
      sb.append("\tnames=[");
      for ( int i=0; i<frontEnds.length; i++ ) {
          sb.append(frontEnds[i] + ";");
      }
      sb.append("]\n");
      sb.append("\tfrontEndTable=[" + frontEndTable + "]\n");
      sb.append("\tfrontEndMixers=[" + frontEndMixers + "]\n");
      sb.append("\treceivers=[" + receivers + "]\n");
      sb.append("]");
      return sb.toString();
  }
}

