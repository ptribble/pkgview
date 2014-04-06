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
 * PackageProfile - describes a Solaris jumpstart profile.
 * @author Peter Tribble
 * @version 1.0
 */
public class PackageProfile {

    private ClusterToc ctoc;
    private MetaCluster mcluster;
    private Set <PackageCluster> addClusters;
    private Set <PackageCluster> removeClusters;
    private Set <SolarisPackage> addPackages;
    private Set <SolarisPackage> removePackages;
    private Set <String> geos;

    private static final String pkgstr = "package";
    private static final String clstr = "cluster";
    private static final String geostr = "geo";

    private GeoSelectionPanel gpanel;

    /**
     * The operation succeeded with no extra actions.
     */
    public static final int SUCCESS = 0;

    /**
     * The operation was ignored because it was unnecessary.
     * This is the case if a package or cluster is added or
     * removed and the parent cluster or metacluster already
     * contains or excludes the package or cluster.
     */
    public static final int IGNORED = 99;

    /**
     * The operation succeeded, and changed the package list.
     */
    public static final int SUCCESS_CHANGED_PACKAGES = 98;

    /**
     * The operation succeeded, and changed the cluster list.
     */
    public static final int SUCCESS_CHANGED_CLUSTERS = 97;

    /**
     * The operation succeeded, but did nothing because the
     * requested change was a duplicate.
     */
    public static final int SUCCESS_DUPLICATE = 96;

    /**
     * The operation was declined because it violated package constraints.
     */
    public static final int DECLINED = 95;

    /**
     * An initial install.
     */
    public static final int INSTALL_INITIAL = 0;

    /**
     * An upgrade install.
     */
    public static final int INSTALL_UPGRADE = 1;

    /**
     * A flash install.
     */
    public static final int INSTALL_FLASH = 2;

    /**
     * A differential flash install.
     */
    public static final int INSTALL_FLASHUPDATE = 3;

    /**
     * Create a Solaris Installation profile, consisting of a base
     * MetaCluster, and then additional software clusters and
     * packages that can fine tune the installed software. With this
     * constructor, the caller must explicitly specify the starting
     * MetaCluster with the initialize() method.
     */
    public PackageProfile(ClusterToc ctoc) {
	this.ctoc = ctoc;
    }

    /**
     * Create a Solaris Installation profile, consisting of a base
     * MetaCluster, and then additional software clusters and
     * packages that can fine tune the installed software.
     */
    public PackageProfile(ClusterToc ctoc, MetaCluster mcluster) {
	this.ctoc = ctoc;
	initialize(mcluster);
    }

    public void initialize(MetaCluster mcluster) {
	this.mcluster = mcluster;
	addClusters = new TreeSet <PackageCluster> ();
	addPackages = new TreeSet <SolarisPackage> ();
	removeClusters = new TreeSet <PackageCluster> ();
	removePackages = new TreeSet <SolarisPackage> ();
	geos = new TreeSet <String> ();
    }

    public void setgeo(GeoSelectionPanel gpanel) {
	this.gpanel = gpanel;
    }

    /**
     * Add a software cluster to the list. Implicitly adds all
     * the software packages contained in that cluster.
     */
    public int addCluster(PackageCluster cluster) {
	// if a duplicate request, say so
	if (addClusters.contains(cluster)) {
	    return SUCCESS_DUPLICATE;
	}

	// if it's removed, unremove it
	removeClusters.remove(cluster);

	// remove any packages in this cluster from the package lists
	addPackages.removeAll(cluster.getPackages());
	removePackages.removeAll(cluster.getPackages());

	/*
	 * If the chosen metacluster contains this cluster then there's
	 * no need to add it.
	 */
	if (!mcluster.containsCluster(cluster)) {
	    addClusters.add(cluster);
	}
	return SUCCESS;
    }

    /**
     * Remove a software cluster from the list. Implicitly removes
     * all the software packages contained in that cluster.
     */
    public int removeCluster(PackageCluster cluster) {
	// if a duplicate request, say so
	if (removeClusters.contains(cluster)) {
	    return SUCCESS_DUPLICATE;
	}

	// if it's required, don't let it be removed
	if (cluster.isRequired()) {
	    return DECLINED;
	}

	// if it's added, unadd it
	addClusters.remove(cluster);

	// remove any packages in this cluster from the package lists
	addPackages.removeAll(cluster.getPackages());
	removePackages.removeAll(cluster.getPackages());

	/*
	 * If the chosen metacluster didn't contain this cluster, then there's
	 * no need to remove it. However, in that case we need to remove any
	 * individual packages from this cluster that may be in the chosen
	 * metacluster.
	 */
	if (mcluster.containsCluster(cluster)) {
	    removeClusters.add(cluster);
	} else {
	    for (SolarisPackage pkg : cluster.getPackages()) {
		if (mcluster.containsPackage(pkg) && pkg.isRequired()) {
		    removePackages.add(pkg);
		}
	    }
	}
	return SUCCESS;
    }

    /**
     * Add a software package to the list.
     */
    public int addPackage(SolarisPackage pkg) {
	// if a duplicate request, say so
	if (addPackages.contains(pkg)) {
	    return SUCCESS_DUPLICATE;
	}

	// if it's removed, don't remove it
	removePackages.remove(pkg);

	/*
	 * If it was in the main metacluster then we only need to add it to
	 * the list if it's contained in a removed cluster. If it isn't in
	 * the main metacluster then we need not add it if it's contained in
	 * an already added cluster.
	 */
	if (mcluster.includesPackage(pkg)) {
	    for (PackageCluster pc : removeClusters) {
		if (pc.containsPackage(pkg)) {
		    addPackages.add(pkg);
		}
	    }
	} else {
	    for (PackageCluster pc : addClusters) {
		if (pc.containsPackage(pkg)) {
		    return SUCCESS_DUPLICATE;
		}
	    }
	    addPackages.add(pkg);
	}
	return SUCCESS;
    }

    /**
     * Remove a software package from the list.
     */
    public int removePackage(SolarisPackage pkg) {
	// if a duplicate request, say so
	if (removePackages.contains(pkg)) {
	    return SUCCESS_DUPLICATE;
	}

	// if it's added, remove it
	addPackages.remove(pkg);

	/*
	 * If it isn't in the main metacluster then we only need to remove it
	 * from the list if it's contained in an added cluster. If it is in
	 * the main metacluster then we need not remove it if it's contained in
	 * an already removed cluster.
	 */
	if (mcluster.includesPackage(pkg)) {
	    for (PackageCluster pc : removeClusters) {
		if (pc.containsPackage(pkg)) {
		    return SUCCESS_DUPLICATE;
		}
	    }
	    removePackages.add(pkg);
	} else {
	    for (PackageCluster pc : addClusters) {
		if (pc.containsPackage(pkg)) {
		    removePackages.add(pkg);
		}
	    }
	}
	return SUCCESS;
    }

    /*
     * Normalize the profile by checking the added and removed packages
     * don't comprise entire clusters, replacing the packages by clusters
     * if possible.
     */
    private void normalizeSelection() {
	for (PackageCluster pc : ctoc.includedClusters(addPackages)) {
	    addCluster(pc);
	}
	for (PackageCluster pc : ctoc.includedClusters(removePackages)) {
	    removeCluster(pc);
	}
    }

    /**
     * Get the Set of packages contained in this profile. This is backed by
     * a TreeSet and is therefore sorted.
     */
    public Set <SolarisPackage> getPackages() {
	Set <SolarisPackage> pkgs = new TreeSet <SolarisPackage> ();
	// take all packages from the starting metacluster
	for (PackageCluster pc : mcluster.getClusters()) {
	    pkgs.addAll(pc.getPackages());
	}
	pkgs.addAll(mcluster.getPackages());
	// from the customized clusters
	for (PackageCluster pc : addClusters) {
	    pkgs.addAll(pc.getPackages());
	}
	for (PackageCluster pc : removeClusters) {
	    pkgs.removeAll(pc.getPackages());
	}
	// and finally the customized packages
	pkgs.addAll(addPackages);
	pkgs.removeAll(removePackages);

	return pkgs;
    }

    /*
     * Set the installed geos.
     */
    public void setGeos(Set <String> geos) {
	this.geos = geos;
    }

    /**
     * Write out the installation profile.
     */
    public String getProfile() {
	normalizeSelection();
	StringBuilder sb = new StringBuilder();
	// geographic regions, if defined
	if (gpanel != null) {
	    setGeos(gpanel.selectedGeos());
	}
	for (String s : geos) {
	    sb.append(geostr).append("\t").append(s).append("\n");
	}
	// start software with the metacluster
	sb.append(clstr).append("\t");
	sb.append(mcluster.getClusterName()).append("\n");
	// loop through added and removed clusters
	for (PackageCluster pc : addClusters) {
	    sb.append(clstr).append("\t");
	    sb.append(pc.getClusterName()).append(" add\n");
	}
	for (PackageCluster pc : removeClusters) {
	    sb.append(clstr).append("\t");
	    sb.append(pc.getClusterName()).append(" delete\n");
	}
	// loop through added and removed packages
	for (SolarisPackage pkg : addPackages) {
	    sb.append(pkgstr).append("\t");
	    sb.append(pkg.getName()).append(" add\n");
	}
	for (SolarisPackage pkg : removePackages) {
	    sb.append(pkgstr).append("\t");
	    sb.append(pkg.getName()).append(" delete\n");
	}
	return sb.toString();
    }

    /**
     * Write out a sorted list of packages contained in this profile.
     */
    public String getPkglist() {
	StringBuilder sb = new StringBuilder();
	// getPackages is already sorted
	for (SolarisPackage pkg : getPackages()) {
	    sb.append(pkg.getName()).append("\n");
	}
	return sb.toString();
    }
}
