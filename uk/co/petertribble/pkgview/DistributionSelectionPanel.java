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
import javax.swing.table.AbstractTableModel;
import java.util.Set;
import java.util.TreeSet;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.*;
import uk.co.petertribble.jingle.JingleTextPane;

/**
 * A panel allowing the user to select software from a Solaris distribution.
 *
 * @author Peter Tribble
 * @version 1.0
 */
public class DistributionSelectionPanel extends JPanel
					implements ActionListener {

    private static final int ACTION_REMOVE = 0;
    private static final int ACTION_ADD = 1;
    private static final int ACTION_PART_REMOVE = 2;
    private static final int ACTION_RECURSE_REMOVE = 3;

    private JingleTextPane tp_info;
    private JButton selectButton;

    private PackageProfile profile;
    private PkgList plist;
    private ClusterToc ctoc;

    private int chosen_action = ACTION_REMOVE;
    private Object chosen_object;

    private ClusterTree ctree;
    private AbstractTableModel tmodel;

    public DistributionSelectionPanel(PackageProfile profile, PkgList plist,
				ClusterToc ctoc) {
	this.profile = profile;
	this.plist = plist;
	this.ctoc = ctoc;

	setLayout(new BorderLayout());

	tp_info = new JingleTextPane();
	add(new JScrollPane(tp_info));

	selectButton = new JButton("");
	selectButton.setEnabled(false);
	selectButton.addActionListener(this);
	add(selectButton, BorderLayout.SOUTH);

	setMinimumSize(new Dimension(200, 100));
    }

    public void setTree(ClusterTree ctree) {
	this.ctree = ctree;
    }

    public void setModel(AbstractTableModel tmodel) {
	this.tmodel = tmodel;
    }

    /*
     * Packages and Clusters declare their dependencies as Sets of Strings,
     * so we need to convert those into Sets of Packages.
     */
    private Set <SolarisPackage> getDependencies(Set <String> deps) {
	Set <SolarisPackage> pkgdeps = new TreeSet <SolarisPackage> ();
	for (String s : deps) {
	    SolarisPackage pkg = plist.getPackage(s);
	    /*
	     * pkg shouldn't be null, but a common cause is depending on
	     * architecture-specific packages that don't exactly match by name.
	     */
	    if (pkg != null) {
		pkgdeps.add(pkg);
	    }
	}
	return pkgdeps;
    }

    /*
     * For much the same reason as above, we need to work out here
     * which dependencies are unselected.
     */
    private Set <SolarisPackage> getUnselectedDeps(
					Set <SolarisPackage> deps) {
	Set <SolarisPackage> pkgdeps = new TreeSet <SolarisPackage> ();
	for (SolarisPackage pkg : deps) {
	    if (!pkg.isSelected()) {
		pkgdeps.add(pkg);
	    }
	}
	return pkgdeps;
    }

    private Set <SolarisPackage> getUnselectedDeps(SolarisPackage pkg) {
	return getUnselectedDeps(getDependencies(pkg.getDependencySet()));
    }

    private Set <SolarisPackage> getUnselectedDeps(PackageCluster pc) {
	return getUnselectedDeps(getDependencies(pc.getDependencySet()));
    }

    /*
     * Display a table of packages, one package per line. If the
     * package is contained in a cluster, show that too.
     */
    private String pkgTableList(Set <SolarisPackage> pkgset) {
	StringBuilder sb = new StringBuilder();
	sb.append("<hr><table>");
	for (SolarisPackage pkg : pkgset) {
	    sb.append("<tr><td>").append(pkg.getName());
	    boolean first = true;
	    for (PackageCluster pc : ctoc.containingClusters(pkg)) {
		if (first) {
		    sb.append(" contained in cluster ");
		    first = false;
		} else {
		    sb.append(", ");
		}
		sb.append(pc.getClusterName());
	    }
	    sb.append("</td></tr>\n");
	}
	sb.append("</table><hr>");
	return sb.toString();
    }

    public void showSelection(PackageCluster pc) {
	StringBuilder sb = new StringBuilder();
	sb.append("Cluster ").append(pc.getClusterName());
	if (pc.isRequired()) {
	    sb.append(" is required and cannot be unselected");
	    disableButton();
	} else {
	    if (pc.isSelected()) {
		Set <SolarisPackage> pkgset = pc.getSelectedDependantSet();
		showSelected(sb, pkgset);
		if (pkgset.isEmpty()) {
		    /*
		     * If the cluster contains some required packages, only
		     * allow the user to remove the other packages.
		     */
		    enableButton(pc, pc.isPartiallyRequired()
				? ACTION_PART_REMOVE : ACTION_REMOVE);
		} else {
		    enableButton(pc, ACTION_RECURSE_REMOVE);
		}
	    } else {
		showUnselected(sb, getUnselectedDeps(pc));
		enableButton(pc, ACTION_ADD);
	    }
	}
	tp_info.setText(sb.toString());
    }

    public void showSelection(SolarisPackage pkg) {
	StringBuilder sb = new StringBuilder();
	sb.append("Package ").append(pkg.getName());
	if (pkg.isRequired()) {
	    sb.append(" is required and cannot be unselected");
	    disableButton();
	} else {
	    if (pkg.isSelected()) {
		Set <SolarisPackage> pkgset = pkg.getSelectedDependantSet();
		showSelected(sb, pkgset);
		enableButton(pkg, pkgset.isEmpty() ? ACTION_REMOVE
				: ACTION_RECURSE_REMOVE);
	    } else {
		showUnselected(sb, getUnselectedDeps(pkg));
		enableButton(pkg, ACTION_ADD);
	    }
	}
	tp_info.setText(sb.toString());
    }

    /*
     * Shared code above.
     */
    private void showSelected(StringBuilder sb, Set <SolarisPackage> pkgset) {
	sb.append(" is currently selected");
	showDepList(sb, pkgset, " selected ");
    }

    private void showUnselected(StringBuilder sb, Set <SolarisPackage> pkgset) {
	sb.append(" is currently unselected");
	showDepList(sb, pkgset, " unselected ");
    }

    private void showDepList(StringBuilder sb, Set <SolarisPackage> pkgset,
			String sel) {
	int i = pkgset.size();
	if (i > 0) {
	    sb.append(" and has ").append(i);
	    sb.append(sel);
	    sb.append(i == 1 ? "dependant" : "dependants");
	    sb.append(pkgTableList(pkgset));
	}
    }

    public void clearSelection() {
	tp_info.setText("");
	disableButton();
    }

    /*
     * The following methods add and remove packages from the profile. The
     * appropriate events are fired on the cluster tree and package list so
     * that the display is updated immediately. For the tree, we tell it
     * that we've updated a given package or cluster, and it works out
     * which nodes to update. So we do it individually for every package
     * or cluster. For the package list, we only do it for packages, as the
     * package list knows nothing of clusters.
     */

    /*
     * Here we add items recursively to the profile in order to resolve
     * dependencies.
     */
    private void addRecursively(PackageCluster pc) {
	pc.setSelected(true);
	profile.addCluster(pc);
	for (SolarisPackage pkg : getUnselectedDeps(pc)) {
	    addRecursively(pkg);
	}
	if (ctree != null) {
	    ctree.nodeChanged(pc);
	}
    }

    /**
     * Add the package, and any package it depends on that hasn't yet been
     * added.
     */
    public void addRecursively(SolarisPackage pkg) {
	pkg.setSelected(true);
	profile.addPackage(pkg);
	for (SolarisPackage apkg : getUnselectedDeps(pkg)) {
	    addRecursively(apkg);
	}
	if (ctree != null) {
	    ctree.nodeChanged(pkg);
	}
	if (tmodel != null) {
	    tmodel.fireTableDataChanged();
	}
    }

    /**
     * Recursive removal of packages. There's an implicit assumption here
     * that we will never call this on required packages. The display logic
     * should ensure that we never try to directly remove a required package,
     * and the case of a required package depending on a non-required
     * package should be impossible.
     */
    public void removeRecursively(SolarisPackage pkg) {
	pkg.setSelected(false);
	profile.removePackage(pkg);
	for (SolarisPackage pkg2 : pkg.getSelectedDependantSet()) {
	    removeRecursively(pkg2);
	}
	if (ctree != null) {
	    ctree.nodeChanged(pkg);
	}
	if (tmodel != null) {
	    tmodel.fireTableDataChanged();
	}
    }

    private void removeRecursively(PackageCluster pc) {
	pc.setSelected(false);
	profile.removeCluster(pc);
	for (SolarisPackage pkg : pc.getSelectedDependantSet()) {
	    removeRecursively(pkg);
	}
	if (ctree != null) {
	    ctree.nodeChanged(pc);
	}
    }

    /*
     * Remove the packages from a cluster that aren't mandatory.
     */
    private void removePartially(PackageCluster pc) {
	pc.setSelected(false);
	for (SolarisPackage pkg : pc.getPackages()) {
	    if (!pkg.isRequired()) {
		pkg.setSelected(false);
		profile.removePackage(pkg);
	    }
	}
	if (ctree != null) {
	    ctree.nodeChanged(pc);
	}
    }

    /*
     * Here we manipulate the state and effect of the selection button
     */
    private void disableButton() {
	selectButton.setText("");
	selectButton.setEnabled(false);
    }

    private void enableButton(SolarisPackage pkg, int act) {
	chosen_action = act;
	chosen_object = pkg;
	if (act == ACTION_ADD) {
	    enableButton("Add package " + pkg.getName());
	} else if (act == ACTION_RECURSE_REMOVE) {
	    enableButton("Remove package " + pkg.getName() +
			" and its dependants");
	} else {
	    enableButton("Remove package " + pkg.getName());
	}
    }

    private void enableButton(PackageCluster pc, int act) {
	chosen_action = act;
	chosen_object = pc;
	if (act == ACTION_ADD) {
	    enableButton("Add cluster " + pc.getClusterName());
	} else if (act == ACTION_PART_REMOVE) {
	    enableButton("Remove removable packages from " +
			pc.getClusterName());
	} else if (act == ACTION_RECURSE_REMOVE) {
	    enableButton("Remove cluster " + pc.getClusterName() +
			" and its dependants");
	} else {
	    enableButton("Remove cluster " + pc.getClusterName());
	}
    }

    private void enableButton(String s) {
	selectButton.setText(s);
	selectButton.setEnabled(true);
    }

    public void actionPerformed(ActionEvent e) {
	if (chosen_object instanceof SolarisPackage) {
	    SolarisPackage pkg = (SolarisPackage) chosen_object;
	    if (chosen_action == ACTION_ADD) {
		addRecursively(pkg);
	    } else {
		removeRecursively(pkg);
	    }
	    showSelection(pkg);
	} else {
	    PackageCluster pc = (PackageCluster) chosen_object;
	    if (chosen_action == ACTION_ADD) {
		addRecursively(pc);
	    } else if (chosen_action == ACTION_PART_REMOVE) {
		removePartially(pc);
	    } else {
		removeRecursively(pc);
	    }
	    showSelection(pc);
	}
    }
}
