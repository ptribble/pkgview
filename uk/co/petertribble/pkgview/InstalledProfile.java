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
 * InstalledProfile - produce a jumpstart profile matching the current system.
 */
public class InstalledProfile {

    public InstalledProfile() {
	PkgList plist = new PkgList();
	ClusterToc ctoc = new ClusterToc(plist);
	MetaCluster mc = ctoc.getMetaCluster(InstalledCluster.getClusterName());
	PackageProfile pp = new PackageProfile(ctoc, mc);

	// construct a Set of packages contained in the installed Cluster
	Set <SolarisPackage> cpkgs = new TreeSet <SolarisPackage> ();
	for (PackageCluster pc : mc.getClusters()) {
	    cpkgs.addAll(pc.getPackages());
	}
	cpkgs.addAll(mc.getPackages());

	/*
	 * Remove all actually installed packages, what is then left in cpkgs
	 * are the packages in the metacluster that aren't installed, so that
	 * gives us the list of packages to remove from the profile.
	 */
	cpkgs.removeAll(plist.getPackages());
	for (SolarisPackage pkg : cpkgs) {
	    pp.removePackage(pkg);
	}

	/*
	 * Now, if the installed cluster isn't SUNWCXall, are there any
	 * installed packages in SUNWCXall that aren't listed in the
	 * cluster that is installed. If so, need to add those to the
	 * profile.
	 */
	if (!"SUNWCXall".equals(InstalledCluster.getClusterName())) {
	    MetaCluster mc2 = ctoc.getMetaCluster("SUNWCXall");
	    Set <SolarisPackage> apkgs = new TreeSet <SolarisPackage> ();
	    for (PackageCluster pc : mc2.getClusters()) {
		apkgs.addAll(pc.getPackages());
	    }
	    apkgs.addAll(mc.getPackages());
	    /*
	     * Remove all the packages in our installed cluster.
	     */
	    for (PackageCluster pc : mc.getClusters()) {
		apkgs.removeAll(pc.getPackages());
	    }
	    apkgs.removeAll(mc.getPackages());
	    /*
	     * apkgs is now the difference between SUNWCXall and our
	     * currently installed cluster. Are any packages in apkgs
	     * actually installed? If so, add them to the profile
	     */
	    for (SolarisPackage pkg : plist.getPackages()) {
		if (apkgs.contains(pkg)) {
		    pp.addPackage(pkg);
		}
	    }
	}

	/*
	 * Get installed geos, if any
	 */
	pp.setGeos(InstalledGeos.getGeos());

	System.out.print(pp.getProfile());

	/*
	 * Go through installed packages and create a complete list
	 * of their dependencies.
	 */
	Set <String> deps = new TreeSet <String> ();
	for (SolarisPackage pkg : plist.getPackages()) {
	    deps.addAll(pkg.getDependencySet());
	}
	/*
	 * Remove the installed packages from the list, this should give
	 * the set of unsatisfied dependencies.
	 */
	for (SolarisPackage pkg : plist.getPackages()) {
	    deps.remove(pkg.getName());
	}
	/*
	 * Get the list of deleted package names.
	 */
	Set <String> delnames = new TreeSet <String> ();
	for (SolarisPackage pkg : cpkgs) {
	    delnames.add(pkg.getName());
	}
	/*
	 * Add as comments. If it's one that we deleted, say so, else
	 * just note it.
	 */
	plist.createRevDependencies();
	for (String s : deps) {
	    if (delnames.contains(s)) {
		System.out.print("# deleted required package " + s);
		System.out.println(" needed by " + plist.getDependantSet(s));
	    } else {
		System.out.println("# missing package " + s);
	    }
	}
    }

    /**
     * Run the application.
     *
     * @param args Command line arguments, ignored
     */
    public static void main(String args[]) {
	new InstalledProfile();
    }
}
