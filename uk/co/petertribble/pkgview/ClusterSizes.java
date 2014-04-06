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
 * Print out the sizes of all the package clusters.
 */
public class ClusterSizes {

    /**
     * Prints out the installed sizes of all package clusters.
     *
     * @param args Command line arguments, unused
     */
    public static void main(String[] args) {
	for (PackageCluster pc :
		new ClusterToc().getMetaCluster("SUNWCXall").getClusters()) {
	    ContentsPackage cc = new ContentsPackage(pc);
	    System.out.println(cc.spaceUsed() + " | " + cc.numEntries()
			+ " | " + pc.getClusterName()
			+ " | " + pc.getDescription());
	}
    }
}
