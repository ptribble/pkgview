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

/**
 * Print out the sizes of all the packages.
 */
public class PackageSizes {

    /**
     * Prints out the installed sizes of all packages.
     *
     * @param args Command line arguments, unused
     */
    public static void main(String[] args) {
	ContentsParser cp = ContentsParser.getInstance();
	for (SolarisPackage pkg :  new PkgList().getPackages()) {
	    ContentsPackage cc = cp.getPackage(pkg.getName());
	    if (cc == null) {
		System.out.println("0 | 0 | " + pkg.getName());
	    } else {
		System.out.println(cc.spaceUsed() + " | " + cc.numEntries()
				+ " | " + pkg.getName());
	    }
	}
    }
}
