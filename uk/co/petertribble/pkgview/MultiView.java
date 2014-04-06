/*
 * CDDL HEADER START
 *
 * The contents of this file are subject to the terms of the
 * Common Development and Distribution License, Version 1.0 only
 * (the "License").  You may not use this file except in compliance
 * with the License.
 *
 * You can obtain a copy of the license at usr/src/OPENSOLARIS.LICENSE
 * or http://www.opensolaris.org/os/licensing.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL HEADER in each
 * file and include the License file at usr/src/OPENSOLARIS.LICENSE.
 * If applicable, add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your own identifying
 * information: Portions Copyright [yyyy] [name of copyright owner]
 *
 * CDDL HEADER END
 */

package uk.co.petertribble.pkgview;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.event.*;
import uk.co.petertribble.jingle.JingleInfoFrame;

/**
 * MultiView - GUI wrapper
 *
 * @author Peter Tribble
 * @version 1.0
 */
public class MultiView extends JFrame implements ActionListener {

    private JMenuItem exitItem;
    private JMenuItem helpItem;
    private JMenuItem licenseItem;

    /**
     * Create a new View.
     */
    public MultiView() {
	super(PkgResources.getString("SOFT.TEXT"));

	addWindowListener(new winExit());
	getContentPane().add(new InstalledSoftwarePanel(), BorderLayout.CENTER);

	JMenuBar jm = new JMenuBar();

	JMenu jme = new JMenu(PkgResources.getString("FILE.TEXT"));
	jme.setMnemonic(KeyEvent.VK_F);
	exitItem = new JMenuItem(PkgResources.getString("FILE.EXIT.TEXT"),
				KeyEvent.VK_X);
	exitItem.addActionListener(this);
	jme.add(exitItem);

	jm.add(jme);

	JMenu jmh = new JMenu(PkgResources.getString("HELP.TEXT"));
	jmh.setMnemonic(KeyEvent.VK_H);
	helpItem = new JMenuItem(PkgResources.getString("HELP.ABOUT.SOFT"),
				KeyEvent.VK_A);
	helpItem.addActionListener(this);
	jmh.add(helpItem);
	licenseItem = new JMenuItem(
				PkgResources.getString("HELP.LICENSE.TEXT"),
				KeyEvent.VK_L);
	licenseItem.addActionListener(this);
	jmh.add(licenseItem);

	jm.add(jmh);
	setJMenuBar(jm);

	setSize(720, 600);
	setVisible(true);
    }

    class winExit extends WindowAdapter {
	public void windowClosing(WindowEvent we) {
	    System.exit(0);
	}
    }

    public void actionPerformed(ActionEvent e) {
	if (e.getSource() == exitItem) {
	    System.exit(0);
	}
	if (e.getSource() == helpItem) {
	    new JingleInfoFrame(this.getClass().getClassLoader(),
				"help/software.html", "text/html");
	}
	if (e.getSource() == licenseItem) {
	    new JingleInfoFrame(this.getClass().getClassLoader(),
				"help/CDDL.txt", "text/plain");
	}
    }

    /**
     * Create a new view from the command line.
     *
     * @param args command line arguments, ignored
     */
    public static void main(String args[]) {
	new MultiView();
    }
}
