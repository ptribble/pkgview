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
import javax.swing.event.*;
import java.awt.BorderLayout;
import uk.co.petertribble.jingle.TableSorter;

/**
 * Show a table inside a Scroll pane, displaying the packages installed.
 * The columns can be sorted by clicking the column headers. Selecting
 * a row causes information to appear in a tabbed area below the table.
 * @author Peter Tribble
 * @version 1.0
 */
public class PackagePanel extends JPanel {

    private PackageInformationPanel pip;
    private SolarisPackage currentPackage;
    private final JTable ptable;
    private PkgList plist;
    private ClusterToc ctoc;
    private PackageProfile profile;

    public PackagePanel(PkgList plist, ClusterToc ctoc) {
	this(plist, ctoc, (PackageProfile) null);
    }

    public PackagePanel(PkgList plist, ClusterToc ctoc,
			    PackageProfile profile) {
	this.plist = plist;
	this.ctoc = ctoc;
	this.profile = profile;

	setLayout(new BorderLayout());

	JPanel jpp = new JPanel(new BorderLayout());
	final PackageTableModel ptm = new PackageTableModel(plist, profile);
	final TableSorter sortedModel = new TableSorter(ptm);
	ptable = new JTable(sortedModel);
	jpp.add(new JScrollPane(ptable));
	sortedModel.setTableHeader(ptable.getTableHeader());

	pip = new PackageInformationPanel(plist, ctoc);

	JSplitPane psplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
		jpp, pip);
	psplit.setOneTouchExpandable(true);
	psplit.setDividerLocation(180);
	add(psplit);

	ptable.getSelectionModel().addListSelectionListener(
		new ListSelectionListener() {
	    public void valueChanged(ListSelectionEvent e) {
		if ((e.getSource() instanceof DefaultListSelectionModel)
			&& (!e.getValueIsAdjusting())) {
		    int irow = ptable.getSelectedRow();
		    if (irow >= 0) {
			// sorted table, need to convert the index
			int imod = sortedModel.modelIndex(irow);
			showPkg(ptm.getPackageAtRow(imod));
		    }
		}
	    }
	});

	// if we're in the profile builder need to show the selection panel
	if (profile != null) {
	    DistributionSelectionPanel dsp =
		new DistributionSelectionPanel(profile, plist, ctoc);
	    dsp.setModel(ptm);
	    pip.showDsp(dsp);
	    ptm.setDsp(dsp);
	}
    }

    private void showPkg(SolarisPackage pkg) {
	pip.showPkg(pkg);
	if (pkg.exists()) {
	    currentPackage = pkg;
	}
    }

    public void showRevDependencies() {
	pip.showRevDependencies(currentPackage);
    }

    public void showDetailedView() {
	pip.showDetailedView();
    }

    public void setMetaCluster(String metacluster) {
	MetaCluster mcluster = ctoc.getMetaCluster(metacluster);
	if ((profile != null) && (mcluster != null)) {
	    for (SolarisPackage pkg : plist.getPackages()) {
		pkg.setSelected(false);
	    }
	    mcluster.setSelected(true);
	}
	pip.reset();
	ptable.clearSelection();
    }
}
