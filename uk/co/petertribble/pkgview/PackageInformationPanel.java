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

import java.util.Set;
import java.awt.Cursor;
import javax.swing.Icon;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import uk.co.petertribble.jingle.JingleTextPane;
import java.io.File;

/**
 * Show information about installed files or packages in a set of
 * tabbed panels.
 * @author Peter Tribble
 * @version 1.0
 */
public class PackageInformationPanel extends JTabbedPane {

    private JingleTextPane tp_info;
    private JingleTextPane tp_dep;
    private JingleTextPane tp_rdep;
    private JingleTextPane tp_cl;
    private JingleTextPane tp_files;
    private DistributionSelectionPanel dsp;
    private boolean showfiles;
    private boolean showdsp;
    private PkgList plist;
    private ClusterToc ctoc;
    private ContentsParser cp;

    /**
     * Create a default PackageInformationPanel showing the default tabs.
     */
    public PackageInformationPanel(PkgList plist, ClusterToc ctoc) {
	this(plist, ctoc, true);
    }

    /**
     * Create a PackageInformationPanel showing the specified tabs.
     */
    public PackageInformationPanel(PkgList plist, ClusterToc ctoc,
				   boolean showdependencies) {
	this.plist = plist;
	this.ctoc = ctoc;

	tp_info = new JingleTextPane();
	tp_dep = new JingleTextPane();
	tp_rdep = new JingleTextPane();
	tp_cl = new JingleTextPane();
	tp_files = new JingleTextPane();
	add(PkgResources.getString("PKG.INFO"), new JScrollPane(tp_info));
	if (showdependencies) {
	    add(PkgResources.getString("PKG.DEPENDENCIES"),
		new JScrollPane(tp_dep));
	}
	if (ctoc.exists()) {
	    add(PkgResources.getString("PKG.CLUSTERS"),
		new JScrollPane(tp_cl));
	}
    }

    // this is really a metacluster or "Solaris"
    public void setClusterInfo(String s) {
	MetaCluster mc = ctoc.getMetaCluster(s);
	if (mc == null) {
	    infoOnly(s);
	} else {
	    StringBuilder sb = new StringBuilder();
	    PkgUtils.headRow(sb, "MetaCluster Name");
	    PkgUtils.addRow(sb, mc.getName());
	    infoOnly(PkgUtils.wrapTable(sb));
	}
	if (showfiles) {
	    setFilesText("");
	}
	if (showdsp) {
	    dsp.clearSelection();
	}
    }

    public void showPkg(SolarisPackage pkg) {
	if (pkg.exists()) {
	    setInfoText(PkgUtils.infoTable(pkg),
		    PkgUtils.dependencyTable(pkg, plist, showdsp),
		    revDeps(pkg));
	} else {
	    setInfoText("Not installed", "", "");
	}
	setClusterText(PkgUtils.clusterMembership(pkg, ctoc));
	if (showdsp) {
	    dsp.showSelection(pkg);
	}
	if (cp != null) {
	    Cursor c = getCursor();
	    setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
	    setFilesText(PkgUtils.detailTable(pkg, cp));
	    setCursor(c);
	}
    }

    public void showCluster(PackageCluster pc) {
	StringBuilder sb = new StringBuilder();
	PkgUtils.headRow(sb, "Cluster Name");
	PkgUtils.addRow(sb, pc.getName());
	if (!showdsp) {
	    PkgUtils.headRow(sb, "Installation Status");
	    PkgUtils.addRow(sb, pc.installedStatusText());
	}
	setInfoText(PkgUtils.wrapTable(sb),
		    PkgUtils.dependencyTable(pc, plist, showdsp),
		    revDeps(pc));
	setClusterText(PkgUtils.metaClusterMembership(pc, ctoc));
	if (showdsp) {
	    dsp.showSelection(pc);
	}
	if (cp != null) {
	    setFilesText(PkgUtils.detailTable(pc, cp));
	}
    }

    private String revDeps(PackageCluster pc) {
	return revDeps("Depending on this cluster:", pc.getDependantSet());
    }

    private String revDeps(SolarisPackage pkg) {
	return revDeps("Depending on this package:", pkg.getDependantSet());
    }

    private String revDeps(String title, Set <SolarisPackage> pkgs) {
	StringBuilder sb = new StringBuilder();
	PkgUtils.headRow(sb, title);
	if (pkgs != null) {
	    for (SolarisPackage pkg : pkgs) {
		sb.append("<tr><td>").append(pkg.getName());
		if (showdsp && pkg.isSelected()) {
		    sb.append(" (selected)");
		}
		sb.append("</td></tr>\n");
	    }
	}
	return PkgUtils.wrapTable(sb);
    }

    public void showFile(File f) {
	if (cp == null) {
	    infoOnly("Package information not available.");
	} else {
	    ContentsFileDetail cfd = cp.getFileDetail(f.toString());
	    if (cfd == null) {
		infoOnly("Not a member of any package.");
	    } else {
		setInfoText(fileDetailTable(cfd), "", "");
		setClusterText(clusterMembership(cfd));
	    }
	}
    }

    private String fileDetailTable(ContentsFileDetail cfd) {
	StringBuilder sb = new StringBuilder();
	PkgUtils.headRow2(sb, "Path name: " + cfd.getName());
	PkgUtils.addRow(sb, "File Type:", cfd.getDescriptiveType());
	if (cfd.isLink()) {
	    PkgUtils.addRow(sb, "Link target:", cfd.getTarget());
	} else {
	    PkgUtils.addRow(sb, "Owner:", cfd.getOwner());
	    PkgUtils.addRow(sb, "Group owner:", cfd.getGroup());
	    PkgUtils.addRow(sb, "Permissions:", cfd.getMode());
	    if (cfd.isRegular()) {
		PkgUtils.addRow(sb, "Size:", cfd.getSize());
	    }
	}
	sb.append("</table>\n");
	sb.append("<table width=\"100%\">");
	PkgUtils.headRow(sb, "This file is a member of the following packages");
	for (String pname : cfd.getPackageNames()) {
	    PkgUtils.addRow(sb, pname);
	}
	return PkgUtils.wrapTable(sb);
    }

    /*
     * Display which clusters and metaclusters a given file is in.
     */
    private String clusterMembership(ContentsFileDetail cfd) {
	StringBuilder sb = new StringBuilder();
	PkgUtils.headRow2(sb, "This file is part of the following clusters:");
	for (PackageCluster pc : ctoc.containingClusters(cfd.getPackages())) {
	    PkgUtils.addRow(sb, pc.toString(), pc.getName());
	}
	PkgUtils.headRow2(sb,
			"This file is part of the following Metaclusters:");
	for (MetaCluster mc : ctoc.containingMetaClusters(cfd.getPackages())) {
	    PkgUtils.addRow(sb, mc.toString(), mc.getName());
	}
	return PkgUtils.wrapTable(sb);
    }

    /*
     * The following methods update the text in the tabs. There are 5 possible
     * tabs - info, dep, rdep, cl, files
     */

    private void infoOnly(String s) {
	setInfoText(s, "", "");
	setClusterText("");
    }

    private void setInfoText(String si, String sd, String srd) {
	tp_info.setText(si);
	tp_dep.setText(sd);
	tp_rdep.setText(srd);
    }

    private void setClusterText(String s) {
	tp_cl.setText(s);
    }

    private void setFilesText(String s) {
	if (!showfiles) {
	    showFiles();
	}
	tp_files.setText(s);
    }

    /**
     * Show the reverse dependency tab. If showing the selection panel,
     * the reverse dependency tab is inserted before it.
     */
    public void showRevDependencies(SolarisPackage pkg) {
	if (showdsp) {
	    insertTab(PkgResources.getString("PKG.DEPENDANTS"), (Icon) null,
		new JScrollPane(tp_rdep), (String) null, getTabCount()-2);
	} else {
	    add(PkgResources.getString("PKG.DEPENDANTS"),
		new JScrollPane(tp_rdep));
	}
	if (pkg != null) {
	    showPkg(pkg);
	}
    }

    /*
     * Show the files tab. If showing the selection panel,
     * the files tab is inserted before it.
     */
    private void showFiles() {
	if (showdsp) {
	    insertTab(PkgResources.getString("PKG.FILES"), (Icon) null,
		new JScrollPane(tp_files), (String) null, getTabCount()-2);
	} else {
	    add(PkgResources.getString("PKG.FILES"), new JScrollPane(tp_files));
	}
	showfiles = true;
    }

    /**
     * Show a selection tab, which must be supplied.
     */
    public void showDsp(DistributionSelectionPanel dsp) {
	this.dsp = dsp;
	add(PkgResources.getString("JS.MSG.CUSTOM"), dsp);
	showdsp = true;
    }

    /**
     * Cause the detailed view of package contents to be shown.
     */
    public void showDetailedView() {
	cp = ContentsParser.getInstance();
    }

    /**
     * Reset the info panel to a blank state.
     */
    public void reset() {
	setSelectedIndex(0);
	infoOnly("");
	if (showfiles) {
	    tp_files.setText("");
	}
	if (showdsp) {
	    dsp.clearSelection();
	}
    }
}
