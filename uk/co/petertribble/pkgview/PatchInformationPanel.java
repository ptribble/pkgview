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
import uk.co.petertribble.jumble.JumbleFile;
import java.io.File;
import uk.co.petertribble.jingle.JingleTextPane;

/**
 * Show information about installed patches in a set of tabbed panels.
 * @author Peter Tribble
 * @version 1.0
 */
public class PatchInformationPanel extends JTabbedPane {

    private static final int TAB_INFO = 0;
    private static final int TAB_PKGS = 1;
    private static final int TAB_RDME = 2;

    private JingleTextPane tp_info;
    private JingleTextPane tp_pkgs;
    private JingleTextPane tp_rdme;

    /**
     * Creates a new PatchInformationPanel.
     */
    public PatchInformationPanel() {
	tp_info = new JingleTextPane();
	tp_pkgs = new JingleTextPane();
	tp_rdme = new JingleTextPane("text/plain");

	insertTab(PkgResources.getString("PKG.INFO"), (Icon) null,
		new JScrollPane(tp_info), (String) null, TAB_INFO);
	insertTab(PkgResources.getString("PKG.PKGS"), (Icon) null,
		new JScrollPane(tp_pkgs), (String) null, TAB_PKGS);
	insertTab(PkgResources.getString("PKG.RDME"), (Icon) null,
		new JScrollPane(tp_rdme), (String) null, TAB_RDME);
    }

    /**
     * Display information about the given patch in the display panel.
     *
     * @param patch  The patch to display information about.
     */
    public void display(SolarisPatch patch) {
	String sinfo = patch.getInfo();
	if (sinfo == null) {
	    updateText(tp_info, "Not available");
	} else {
	    sinfo = sinfo.replace("Installed:", "<tr><td>Installed:</td><td>");
	    sinfo = sinfo.replace("From:", "</td></tr><td>From:</td><td>");
	    sinfo = sinfo.replace("Obsoletes:",
				  "</td></tr><td>Obsoletes:</td><td>");
	    sinfo = sinfo.replace("Requires:",
				  "</td></tr><td>Requires:</td><td>");
	    sinfo = sinfo.replace("Incompatibles:",
				  "</td></tr><td>Incompatibles:</td><td>");
	    StringBuilder sb = new StringBuilder();
	    sb.append("<table border=\"1\">");
	    sb.append(sinfo);
	    sb.append("</td></tr></table>");
	    updateText(tp_info, sb.toString());
	}

	StringBuilder sb = new StringBuilder();
	for (SolarisPackage pkg : patch.getPackages()) {
	    PkgUtils.addRow(sb, pkg.toString(), pkg.getDescription());
	}
	updateText(tp_pkgs, PkgUtils.wrapTable(sb));

	String spname = patch.toString();
	File f = new File("/var/sadm/patch", spname);
	setEnabledAt(TAB_RDME, f.exists());
	updateText(tp_rdme, f.exists() ?
		JumbleFile.getStringContents(new File(f, "README."+spname)) :
		"Not available");
    }

    private void updateText(JingleTextPane tp, String s) {
	tp.setText(s);
    }
}
