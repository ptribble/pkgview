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
 * Display the sizes of install metaclusters.
 */
public class MetaClusterSizes {

    /**
     * Print out the disk space used by different MetaClusters.
     *
     * @param args Command line arguments, ignored.
     */
    public static void main(String[] args) {
	ClusterToc ctoc = new ClusterToc();
	for (String s : ctoc.getMetaClusterNames()) {
	    MetaCluster mc = ctoc.getMetaCluster(s);
	    ContentsPackage cc = new ContentsPackage(mc);
	    System.out.println(cc.spaceUsed() + " | " + cc.numEntries()
			+ " | " + s + " | " + mc.getDescription());
	}
    }
}
