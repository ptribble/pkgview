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
 * Describe a software metacluster.
 */
public class MetaCluster implements Comparable<MetaCluster> {

    private String cname;
    private String name;
    private String description;
    private Set <PackageCluster> clusters;
    private Set <SolarisPackage> packages;

    private boolean selected;

    /**
     * Create a MetaCluster object.
     *
     * @param cname  The name of this MetaCluster.
     */
    public MetaCluster(String cname) {
	this.cname = cname;
	packages = new TreeSet <SolarisPackage> ();
	clusters = new TreeSet <PackageCluster> ();
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

    public void addCluster(PackageCluster pc) {
	clusters.add(pc);
    }

    public void addPackage(SolarisPackage pkg) {
	packages.add(pkg);
    }

    /**
     * Returns all the clusters of packages that are contained
     * in this MetaCluster.
     *
     * @return A Set of the clusters contained
     * in this MetaCluster.
     */
    public Set <PackageCluster> getClusters() {
	return clusters;
    }

    /**
     * Returns all the individual packages that are explicitly contained
     * in this MetaCluster. Does not recurse into clusters.
     *
     * @return A Set of the packages explicitly contained
     * in this MetaCluster.
     */
    public Set <SolarisPackage> getPackages() {
	return packages;
    }

    /**
     * Gets whether this metacluster explicitly contains the specified
     * package cluster.
     */
    public boolean containsCluster(PackageCluster pc) {
	return (clusters.contains(pc)) ? true :
	    containsCluster(pc.getClusterName());
    }

    /**
     * Gets whether this metacluster explicitly contains the specified
     * package cluster.
     */
    public boolean containsCluster(String pcname) {
	// check if any names match
	for (PackageCluster pkgc : clusters) {
	    if (pkgc.getClusterName().equals(pcname)) {
		return true;
	    }
	}
	return false;
    }

    /**
     * Gets whether this metacluster explicitly contains the specified
     * package.
     */
    public boolean containsPackage(SolarisPackage p) {
	return (packages.contains(p)) ? true : containsPackage(p.getName());
    }

    /**
     * Gets whether this metacluster explicitly contains the specified
     * package.
     */
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
     * Gets whether this metacluster contains the specified package,
     * either explicitly or implicitly via membership of a cluster.
     */
    public boolean includesPackage(SolarisPackage p) {
	if (containsPackage(p)) {
	    return true;
	}
	// check clusters
	for (PackageCluster pkgc : clusters) {
	    if (pkgc.containsPackage(p)) {
		return true;
	    }
	}
	return false;
    }

    /**
     * Gets whether this metacluster contains the specified package,
     * either explicitly or implicitly via membership of a cluster.
     */
    public boolean includesPackage(String pname) {
	if (containsPackage(pname)) {
	    return true;
	}
	// check clusters
	for (PackageCluster pkgc : clusters) {
	    if (pkgc.containsPackage(pname)) {
		return true;
	    }
	}
	return false;
    }

    /**
     * Sets whether this metacluster is selected. Propagates this selection to
     * contained clusters and packages.
     */
    public void setSelected(boolean selected) {
	this.selected = selected;
	for (PackageCluster pkgc : clusters) {
	    pkgc.setSelected(selected);
	}
	for (SolarisPackage pkg : packages) {
	    pkg.setSelected(selected);
	}
    }

    /**
     * Gets whether this metacluster is selected.
     */
    public boolean isSelected() {
	return selected;
    }

    /**
     * Sets whether the packages and clusters in this metacluster are
     * unselectable. Normally used only for the SUNWCmreq metacluster.
     */
    public void setRequired() {
	for (PackageCluster pkgc : clusters) {
	    pkgc.setRequired();
	}
	for (SolarisPackage pkg : packages) {
	    pkg.setRequired();
	}
    }

    /**
     * For Comparable.
     */
    public int compareTo(MetaCluster mc) {
	return cname.compareTo(mc.getClusterName());
    }
}
