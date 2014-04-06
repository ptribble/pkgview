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
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.BorderLayout;
import javax.swing.event.*;

/**
 * A panel showing Solaris software clusters.
 *
 * @author Peter Tribble
 * @version 1.0
 */
public class ClusterPanel extends JPanel {

    private final ClusterTree tree;
    private PackageInformationPanel pip;
    private SolarisPackage currentPackage;

    public ClusterPanel(PkgList plist, ClusterToc ctoc) {
	this(plist, ctoc, (PackageProfile) null);
    }

    public ClusterPanel(PkgList plist, ClusterToc ctoc,
			    PackageProfile profile) {
	setLayout(new BorderLayout());

	tree = (profile == null) ?
	    new ClusterTree(ctoc, InstalledCluster.getClusterName()) :
	    new ClusterTree(ctoc);
	tree.expandRow(0);

	/*
	 * Set a renderer to do tooltips and custom icons. If we're being
	 * called from the profile builder we show the current state of
	 * package selection, otherwise the installed status.
	 */
	int istyle = (profile == null) ?
		ClusterTreeCellRenderer.ICON_INSTALLED :
		ClusterTreeCellRenderer.ICON_SELECTED;
	tree.setCellRenderer(new ClusterTreeCellRenderer(istyle));
	ToolTipManager.sharedInstance().registerComponent(tree);

	// Listen for when the selection changes.
	tree.addTreeSelectionListener(new TreeSelectionListener() {
	    public void valueChanged(TreeSelectionEvent e) {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode)
		    tree.getLastSelectedPathComponent();

		if (node != null) {
		    Object o = node.getUserObject();
		    if (node.isLeaf()) {
			showPkg((SolarisPackage) o);
		    } else {
			if (o instanceof String) {
			    setClusterInfo(o.toString());
			} else {
			    showCluster((PackageCluster) o);
			}
		    }
		}
	    }
	});
	// end of tree listener

	// tabbed info pane on right
	pip = new PackageInformationPanel(plist, ctoc);

	// if we're in the profile builder show the selection panel
	if (profile != null) {
	    DistributionSelectionPanel dsp =
		new DistributionSelectionPanel(profile, plist, ctoc);
	    dsp.setTree(tree);
	    pip.showDsp(dsp);
	}

	// split pane to hold the lot
	JSplitPane psplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
		new JScrollPane(tree), pip);
	psplit.setOneTouchExpandable(true);
	psplit.setDividerLocation(180);
	add(psplit);
    }

    private void setClusterInfo(String s) {
	currentPackage = null;
	pip.setClusterInfo(s);
    }

    private void showCluster(PackageCluster pc) {
	currentPackage = null;
	pip.showCluster(pc);
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

    public void reset() {
	currentPackage = null;
	tree.clearSelection();
	pip.reset();
    }
}
