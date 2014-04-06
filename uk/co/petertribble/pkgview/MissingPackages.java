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
 * MissingPackages - produce a report of packages that are claimed as being
 * required by other packages but aren't installed.
 */
public class MissingPackages {

    public MissingPackages() {
	PkgList plist = new PkgList();
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
	 * Map the reverse dependencies.
	 */
	plist.createRevDependencies();

	for (String s : deps) {
	    System.out.print("missing package " + s);
	    System.out.println(" needed by " + plist.getDependantSet(s));
	}
    }

    /**
     * Run the application.
     *
     * @param args Command line arguments, ignored
     */
    public static void main(String args[]) {
	new MissingPackages();
    }
}
