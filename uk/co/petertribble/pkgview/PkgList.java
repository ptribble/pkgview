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

import java.io.File;
import java.util.*;

/**
 * PkgList - shows available Solaris packages in a JList.
 * @author Peter Tribble
 * @version 1.0
 */
public class PkgList {

    private Set <SolarisPackage> pkglist;
    private Map <String, SolarisPackage> pkgMap;
    private Map <String, Set <SolarisPackage>> revDependencies;

    /**
     * Create a package list.
     */
    public PkgList() {
	this("/var/sadm/pkg");
    }

    /**
     * Create a package list.
     *
     * Packages are in directories, so ignore files
     * and anything hidden (starting with a dot) and
     * also the locale directory
     */
    public PkgList(String pkgroot) {
	pkgMap = new HashMap <String, SolarisPackage> ();
	pkglist = new TreeSet <SolarisPackage> ();

	File pkgrootf = new File(pkgroot);

	for (File f : pkgrootf.listFiles()) {
	    if (f.isDirectory() &&
		!f.isHidden() &&
		!f.getName().equals("locale") &&
		(new File(f, "pkginfo")).exists()) {
		SolarisPackage sp = new SolarisPackage(pkgroot, f.getName());
		pkglist.add(sp);
		pkgMap.put(sp.getName(), sp);
	    }
	}
    }

    public Set <SolarisPackage> getPackages() {
	return pkglist;
    }

    public Set <String> getPackageNames() {
	Set <String> pkgnames = new TreeSet <String> ();
	for (SolarisPackage pkg : pkglist) {
	    pkgnames.add(pkg.getName());
	}
	return pkgnames;
    }

    public SolarisPackage getPackage(String name) {
	return pkgMap.get(name);
    }

    public Set <SolarisPackage> getDependantSet(String pkg) {
	return (revDependencies == null) ? null : revDependencies.get(pkg);
    }

    /**
     * Create a reverse dependency tree. Pull the dependencies out
     * and populate another Map.
     */
    public void createRevDependencies() {
	revDependencies = new HashMap <String, Set <SolarisPackage>> ();
	for (SolarisPackage pkg : pkglist) {
	    for (String pkgdep : pkg.getDependencySet()) {
		Set <SolarisPackage> revSet = revDependencies.get(pkgdep);
		if (revSet == null) {
		    revSet = new HashSet <SolarisPackage> ();
		    revDependencies.put(pkgdep, revSet);
		}
		revSet.add(pkg);
	    }
	}
	/*
	 * Now we've built the tree, tell the packages what their
	 * dependants are.
	 */
	for (SolarisPackage pkg : pkglist) {
	    pkg.setDependantSet(revDependencies.get(pkg.getName()));
	}
    }
}
