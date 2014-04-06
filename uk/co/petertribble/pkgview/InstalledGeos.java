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

import uk.co.petertribble.jumble.*;
import java.io.File;
import java.util.*;

/**
 * Returns the list of installed geos, if known.
 * @author Peter Tribble
 * @version 1.0
 */
public class InstalledGeos {

    private static Set <String> geos;

    static {
	geos = new HashSet <String> ();
	Map <String, String> mm = JumbleUtils.stringToPropMap(
	    JumbleFile.getStringContents(
		new File("/var/sadm/system/data/locales_installed")), "\n");
	String geolist = mm.get("GEOS");
	if (geolist != null) {
	    for (String geo : geolist.split(",")) {
		geos.add(geo);
	    }
	}
    }

    private InstalledGeos() {
    }

    /**
     * Return the names of installed geos.
     *
     * @return The names of the installed geos, empty if unknown.
     */
    public static Set <String> getGeos() {
	 return geos;
    }
}
