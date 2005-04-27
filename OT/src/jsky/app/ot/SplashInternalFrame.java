/*
 * Copyright 2000 Association for Universities for Research in Astronomy, Inc.,
 * Observatory Control System, Gemini Telescopes Project.
 *
 * $Id$
 */

package jsky.app.ot;

import java.awt.Dimension;
import java.net.URL;
import javax.swing.*;
import jsky.util.gui.BasicWindowMonitor;
import jsky.util.gui.LookAndFeelMenu;


/** 
 * Provides a top level window and menubar for the SplashScreen class.
 */
public class SplashInternalFrame extends JInternalFrame {
    
    /** main panel */ 
    protected SplashScreen splash;


    /**
     * Create a top level window containing a Splash panel.
     */
    public SplashInternalFrame(JDesktopPane desktop, URL welcomeTxtURL) {
	super("Welcome to the Observing Tool");
	splash = new SplashScreen(welcomeTxtURL);
	splash.setParentFrame(this);
        getContentPane().add("Center", splash);
	
	// set default window size
	Dimension dim = splash.getPreferredSize();
        splash.setPreferredSize(dim);
	
	// center the window on the screen
	Dimension screen = desktop.getPreferredSize();
	setLocation(screen.width/2 - dim.width/2, screen.height/2 - dim.height/2);

	setClosable(false);
	setIconifiable(false);
	setMaximizable(false);

        pack();
        setVisible(true);

	// include this top level window in any future look and feel changes
	LookAndFeelMenu.addWindow(this);
    }

    /** Return the main science program splash panel */
    public SplashScreen getSplash() {return splash;}
}
