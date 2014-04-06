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
import java.io.IOException;
import java.io.File;
import java.io.FileWriter;
import uk.co.petertribble.jingle.JingleTextPane;

/**
 * Display a jumpstart profile.
 *
 * @author Peter Tribble
 * @version 1.0
 */
public class ProfileDisplayPanel extends JPanel implements ActionListener {

    private PackageProfile profile;

    private JingleTextPane tp;
    private JButton displayButton;
    private JButton pkglistButton;
    private JButton saveButton;

    /**
     * Create a Profile Display Panel.
     *
     * @param profile The PackageProfile to be displayed
     */
    public ProfileDisplayPanel(PackageProfile profile) {
	this.profile = profile;

	setLayout(new BorderLayout());

	tp = new JingleTextPane("text/plain");
	add(new JScrollPane(tp));

	JPanel buttonPanel = new JPanel();

	displayButton = new JButton(
				PkgResources.getString("JS.MSG.SHOWPROFILE"));
	displayButton.addActionListener(this);
	displayButton.setEnabled(true);
	buttonPanel.add(displayButton);

	pkglistButton = new JButton(PkgResources.getString("JS.MSG.SHOWPKGS"));
	pkglistButton.addActionListener(this);
	pkglistButton.setEnabled(true);
	buttonPanel.add(pkglistButton);

	saveButton = new JButton(PkgResources.getString("JS.MSG.SAVE"));
	saveButton.addActionListener(this);
	saveButton.setEnabled(true);
	buttonPanel.add(saveButton);

	add(buttonPanel, BorderLayout.SOUTH);

	validate();

    }

    /**
     * Saves the current profile as a text file. The user can select the
     * filename, and is asked to confirm the overwrite of an existing file.
     */
    public void saveProfile() {
	JFileChooser fc = new JFileChooser();
	int dosave = fc.showSaveDialog(this);
	if (dosave == JFileChooser.APPROVE_OPTION) {
	    File f = fc.getSelectedFile();
	    if (f.exists()) {
		int ok = JOptionPane.showConfirmDialog(this,
		    PkgResources.getString("SAVEAS.OVERWRITE.TEXT") + " "
								+ f.toString(),
		    PkgResources.getString("SAVEAS.CONFIRM.TEXT"),
		    JOptionPane.YES_NO_OPTION);
		if (ok != JOptionPane.YES_OPTION) {
		    return;
		}
	    }
	    try {
		FileWriter fw = new FileWriter(f);
		String s = profile.getProfile();
		fw.write(s, 0, s.length());
		fw.close();
	    } catch (IOException ioe) {
		JOptionPane.showMessageDialog(this, ioe.toString(),
			PkgResources.getString("SAVEAS.ERROR.TEXT"),
			JOptionPane.ERROR_MESSAGE);
	    }
	}
    }

    /**
     * Display the current profile. Also called from ProfileBuilder.
     */
    public void doDisplay() {
	tp.setText(profile.getProfile());
    }

    public void actionPerformed(ActionEvent e) {
	if (e.getSource() == displayButton) {
	    doDisplay();
	}
	if (e.getSource() == saveButton) {
	    saveProfile();
	}
	if (e.getSource() == pkglistButton) {
	    tp.setText(profile.getPkglist());
	}
    }
}
