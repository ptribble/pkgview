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

import uk.co.petertribble.jumble.JumbleFile;
import java.io.File;
import java.util.*;

/**
 * ClusterToc - parse a .clustertoc file.
 * @author Peter Tribble
 * @version 1.0
 */
public class ClusterToc {

    private boolean ctocexists;

    private Map <String, PackageCluster> Clusters;
    private Map <String, MetaCluster> MetaClusters;

    /**
     * Constructs a ClusterToc object.
     */
    public ClusterToc() {
	this(new PkgList());
    }

    /**
     * Constructs a ClusterToc object.
     */
    public ClusterToc(PkgList pkglist) {
	this("/var/sadm/system/admin", "/var/sadm/pkg", pkglist);
    }

    /**
     * Constructs a ClusterToc object.
     */
    public ClusterToc(String s, PkgList pkglist) {
	this(s, s, pkglist);
    }

    /*
     * Constructs a ClusterToc object.
     */
    private ClusterToc(String s, String pkgroot, PkgList pkglist) {
	File f = new File(s, ".clustertoc");
	ctocexists = f.exists();
	Clusters = new HashMap <String, PackageCluster> ();
	MetaClusters = new HashMap <String, MetaCluster> ();
	String thiscluster = "";
	PackageCluster thispkgcluster = null;
	MetaCluster thismetacluster = null;
	boolean ismeta = false;
	for (String line : JumbleFile.getLines(f)) {
	    String[] ds = line.split("=", 2);
	    if (ds[0].equals("CLUSTER")) {
		ismeta = false;
		thiscluster = ds[1];
		thispkgcluster = new PackageCluster(thiscluster);
		Clusters.put(thiscluster, thispkgcluster);
	    } else if (ds[0].equals("NAME")) {
		if (ismeta) {
		    thismetacluster.setName(ds[1]);
		} else {
		    thispkgcluster.setName(ds[1]);
		}
	    } else if (ds[0].equals("DESC")) {
		if (ismeta) {
		    thismetacluster.setDescription(ds[1]);
		} else {
		    thispkgcluster.setDescription(ds[1]);
		}
	    } else if (ds[0].equals("SUNW_CSRMEMBER")) {
		if (ismeta) {
		    if (Clusters.containsKey(ds[1])) {
			// it's a cluster
			PackageCluster pc = Clusters.get(ds[1]);
			thismetacluster.addCluster(pc);
		    } else {
			// must be a package
			SolarisPackage pkg = pkglist.getPackage(ds[1]);
			if (pkg == null) {
			    pkg = new SolarisPackage(pkgroot, ds[1]);
			}
			thismetacluster.addPackage(pkg);
		    }
		} else {
		    SolarisPackage pkg = pkglist.getPackage(ds[1]);
		    if (pkg == null) {
			pkg = new SolarisPackage(pkgroot, ds[1]);
		    }
		    thispkgcluster.addPackage(pkg);
		}
	    } else if (ds[0].equals("METACLUSTER")) {
		ismeta = true;
		thiscluster = ds[1];
		thismetacluster = new MetaCluster(thiscluster);
		MetaClusters.put(thiscluster, thismetacluster);
	    }
	}
	/*
	 * For the SUNWCmreq metacluster, mark the contents as required.
	 */
	MetaCluster mreqcluster = MetaClusters.get("SUNWCmreq");
	if (mreqcluster != null) {
	    mreqcluster.setRequired();
	}
    }

    /**
     * Return whether the .clustertoc file referenced by this ClusterToc exists.
     */
    public boolean exists() {
	return ctocexists;
    }

    /**
     * Returns whether the named PackageCluster is a valid cluster
     * that exists in this clustertoc.
     */
    public boolean clusterExists(String s) {
	return Clusters.containsKey(s);
    }

    public Set <String> getMetaClusterNames() {
	return MetaClusters.keySet();
    }

    public PackageCluster getCluster(String cname) {
	return Clusters.get(cname);
    }

    /**
     * Return the MetaCluster of the specified name.
     */
    public MetaCluster getMetaCluster(String s) {
	return MetaClusters.get(s);
    }

    /**
     * Return the cluster(s) that contain the given package.
     * Normally, the way that Solaris is structured into clusters
     * means that only one cluster will be returned, but this code
     * does not assume or require that to be the case.
     */
    public Set <PackageCluster> containingClusters(SolarisPackage pkg) {
	Set <PackageCluster> h = new TreeSet <PackageCluster> ();
	for (PackageCluster pclust : Clusters.values()) {
	    if (pclust.containsPackage(pkg)) {
		h.add(pclust);
	    }
	}
	return h;
    }

    /**
     * Return the cluster(s) that contain the packages in the
     * supplied List.
     */
    public Set <PackageCluster> containingClusters(
			List <SolarisPackage> pkglist) {
	Set <PackageCluster> h = new TreeSet <PackageCluster> ();
	for (SolarisPackage pkg : pkglist) {
	    h.addAll(containingClusters(pkg));
	}
	return h;
    }

    /**
     * Return a Set of PackageClusters that are completely contained in
     * the list of packages specified. This can be used to normalize
     * the list of packages into clusters.
     */
    public Set <PackageCluster> includedClusters(
			Set <SolarisPackage> pkglist) {
	Set <PackageCluster> h = new TreeSet <PackageCluster> ();
	for (PackageCluster pclust : Clusters.values()) {
	    if (pkglist.containsAll(pclust.getPackages())) {
		h.add(pclust);
	    }
	}
	return h;
    }

    /**
     * Return the Metaclusters that contain a given package.
     */
    public Set <MetaCluster> containingMetaClusters(SolarisPackage pkg) {
	Set <MetaCluster> h = new TreeSet <MetaCluster> ();
	for (MetaCluster mclust : MetaClusters.values()) {
	    if (mclust.containsPackage(pkg)) {
		// Metacluster contains this package directly
		h.add(mclust);
	    } else {
		// check all clusters containing this package
		for (PackageCluster pc : containingClusters(pkg)) {
		    if (mclust.containsCluster(pc)) {
			h.add(mclust);
		    }
		}
	    }
	}
	return h;
    }

    /**
     * Return the Metaclusters that contain the packages in the
     * supplied list.
     */
    public Set <MetaCluster> containingMetaClusters(
			List <SolarisPackage> pkglist) {
	Set <MetaCluster> h = new TreeSet <MetaCluster> ();
	for (SolarisPackage pkg : pkglist) {
	    h.addAll(containingMetaClusters(pkg));
	}
	return h;
    }

    /**
     * Return the Metaclusters that contain a given cluster.
     */
    public Set <MetaCluster> containingMetaClusters(PackageCluster pc) {
	Set <MetaCluster> h = new TreeSet <MetaCluster> ();
	for (MetaCluster mclust : MetaClusters.values()) {
	    if (mclust.containsCluster(pc)) {
		h.add(mclust);
	    }
	}
	return h;
    }

}
