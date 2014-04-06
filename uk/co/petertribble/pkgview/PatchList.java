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

import java.util.*;

/**
 * Stores a list of patches applied to the system.
 */
public class PatchList {

    private Set <SolarisPatch> patchSet;

    /**
     * Create a new PatchList, which searches through the supplied packages
     * to find which patches have been applied.
     *
     * @param pkglist The packages to be searched for patches
     */
    public PatchList(PkgList pkglist) {
	Map <String, SolarisPatch> patchMap =
				new HashMap <String, SolarisPatch> ();
	for (SolarisPackage pkg : pkglist.getPackages()) {
	    String s = pkg.getInfoItem("PATCHLIST");
	    if (s != null) {
		for (String ss : s.split(" ")) {
		    if (ss.matches("\\d\\d\\d\\d\\d\\d-\\d\\d")) {
			SolarisPatch sp = patchMap.get(ss);
			if (sp == null) {
			    String ds[] = ss.split("-");
			    sp = new SolarisPatch(ds[0], ds[1],
					pkg.getInfoItem("PATCH_INFO_" + ss));
			    patchMap.put(ss, sp);
			}
			sp.addPackage(pkg);
		    }
		}
	    }
	}

	patchSet = new TreeSet <SolarisPatch> (patchMap.values());
    }

    /**
     * Return all applied patches.
     *
     * @return The Set of all applied patches
     */
    public Set <SolarisPatch> getPatches() {
	return patchSet;
    }
}
