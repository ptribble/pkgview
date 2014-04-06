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
import java.util.TreeSet;

/**
 * Describes a Solaris install cluster.
 * @author Peter Tribble
 * @version 1.0
 */
public class PackageCluster implements Comparable<PackageCluster> {

    public static final int FULLY_INSTALLED = 0;
    public static final int PARTIALLY_INSTALLED = 1;
    public static final int NOT_INSTALLED = 2;

    private String cname;
    private String name;
    private String description;
    private Set <SolarisPackage> packages;

    private boolean selected;
    private boolean required;

    /**
     * Create a PackageCluster object.
     *
     * @param cname  The name of this PackageCluster.
     */
    public PackageCluster(String cname) {
	this.cname = cname;
	packages = new TreeSet <SolarisPackage> ();
    }

    public void setName(String name) {
	this.name = name;
    }

    public void setDescription(String description) {
	this.description = description;
    }

    public String getName() {
	return name;
    }

    public String getDescription() {
	return description;
    }

    public String toString() {
	return cname;
    }

    public String getClusterName() {
	return cname;
    }

    public void addPackage(SolarisPackage p) {
	packages.add(p);
    }

    public Set <SolarisPackage> getPackages() {
	return packages;
    }

    public boolean containsPackage(SolarisPackage p) {
	if (packages.contains(p)) {
	    return true;
	}
	// check if any names match
	return containsPackage(p.getName());
    }

    public boolean containsPackage(String pname) {
	// check if any names match
	for (SolarisPackage pkg : packages) {
	    if (pkg.getName().equals(pname)) {
		return true;
	    }
	}
	return false;
    }

    /**
     * Return the Set of package names that this cluster declares itself
     * dependent on.
     */
    public Set <String> getDependencySet() {
	Set <String> deps = new TreeSet <String> ();
	for (SolarisPackage pkg : packages) {
	    deps.addAll(pkg.getDependencySet());
	}
	// remove any of our own packages
	for (SolarisPackage pkg : packages) {
	    deps.remove(pkg.getName());
	}
	return deps;
    }

    /**
     * Return the Set of package names that this cluster declares to be
     * dependent on it.
     */
    public Set <String> getRDependencySet() {
	Set <String> deps = new TreeSet <String> ();
	for (SolarisPackage pkg : packages) {
	    deps.addAll(pkg.getRDependencySet());
	}
	// remove any of our own packages
	for (SolarisPackage pkg : packages) {
	    deps.remove(pkg.getName());
	}
	return deps;
    }

    /**
     * Return the Set of package names that are incompatible with packages in
     * this cluster.
     */
    public Set <String> getIncompatibleSet() {
	Set <String> deps = new TreeSet <String> ();
	for (SolarisPackage pkg : packages) {
	    deps.addAll(pkg.getIncompatibleSet());
	}
	return deps;
    }

    /**
     * Return the Set of packages that are dependant on this cluster.
     */
    public Set <SolarisPackage> getDependantSet() {
	Set <SolarisPackage> depSet = new TreeSet <SolarisPackage> ();
	for (SolarisPackage pkg : packages) {
	    Set <SolarisPackage> tset = pkg.getDependantSet();
	    if (tset != null) {
		depSet.addAll(tset);
	    }
	}
	// remove any of our own packages
	depSet.removeAll(packages);
	return depSet;
    }

    /**
     * Return the Set of packages that are dependant on this cluster
     * and are selected.
     */
    public Set <SolarisPackage> getSelectedDependantSet() {
	Set <SolarisPackage> depSet = new TreeSet <SolarisPackage> ();
	for (SolarisPackage pkg : getDependantSet()) {
	    if (pkg.isSelected()) {
		depSet.add(pkg);
	    }
	}
	return depSet;
    }

    /**
     * Shows whether this cluster is fully installed, partially installed, or
     * not installed.
     */
    public int installedStatus() {
	int iinst = 0;
	for (SolarisPackage pkg : packages) {
	    if (pkg.exists()) {
		iinst++;
	    }
	}
	if (iinst == 0) {
	    return NOT_INSTALLED;
	}
	return (iinst == packages.size()) ? FULLY_INSTALLED
	    : PARTIALLY_INSTALLED;
    }

    /**
     * Shows whether this cluster is fully installed, partially installed, or
     * not installed.
     */
    public String installedStatusText() {
	String t;
	switch (installedStatus()) {
	    case FULLY_INSTALLED:
		t = "Fully Installed";
		break;
	    case PARTIALLY_INSTALLED:
		t = "Partially Installed";
		break;
	    case NOT_INSTALLED:
		t = "Not Installed";
		break;
	    default:
		t = "Unknown";
	}
	return t;
    }

    /**
     * Shows whether this cluster is fully selected, partially selected, or not
     * selected, based on whether its constituent packages are selected.
     */
    public int selectedStatus() {
	int ipkg = 0;
	int iinst = 0;
	for (SolarisPackage pkg : packages) {
	    ipkg++;
	    if (pkg.isSelected()) {
		iinst++;
	    }
	}
	if (iinst == 0) {
	    return NOT_INSTALLED;
	}
	return (iinst == ipkg) ? FULLY_INSTALLED : PARTIALLY_INSTALLED;
    }

    /**
     * Sets whether this package cluster is selected. Propagates that selection
     * to packages contained in this cluster.
     */
    public void setSelected(boolean selected) {
	this.selected = selected;
	for (SolarisPackage pkg : packages) {
	    pkg.setSelected(selected);
	}
    }

    /**
     * Gets whether this package cluster is selected.
     */
    public boolean isSelected() {
	if (selectedStatus() == NOT_INSTALLED) {
	    selected = false;
	}
	return selected ? selected : (selectedStatus() == FULLY_INSTALLED);
    }

    /**
     * Sets whether this package cluster must be selected. Propagates that
     * selection to packages contained in this cluster.
     */
    public void setRequired() {
	required = true;
	for (SolarisPackage pkg : packages) {
	    pkg.setRequired();
	}
    }

    /**
     * Gets whether this package cluster must be selected. Packages and
     * clusters in the SUNWCmreq metacluster must be selected.
     */
    public boolean isRequired() {
	return required;
    }

    /**
     * Gets whether this package cluster contains one or more, but not all,
     * packages that are required. Packages and clusters in the SUNWCmreq
     * metacluster are required.
     */
    public boolean isPartiallyRequired() {
	/*
	 * The aim here is to identify clusters that have some but not all
	 * packages that must be selected. Therefore, if the whole cluster
	 * is required, then it can't be partially required, so we return
	 * false here.
	 */
	if (required) {
	    return false;
	}
	for (SolarisPackage pkg : packages) {
	    if (pkg.isRequired()) {
		return true;
	    }
	}
	return false;
    }

    /**
     * For Comparable.
     */
    public int compareTo(PackageCluster pc) {
	return cname.compareTo(pc.getClusterName());
    }
}
