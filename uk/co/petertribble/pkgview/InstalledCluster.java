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
import uk.co.petertribble.jumble.JumbleUtils;
import java.io.File;
import java.util.Map;

/**
 * Describes the currently installed software metacluster.
 * @author Peter Tribble
 * @version 1.0
 */
public class InstalledCluster {

    private static String instCluster;

    static {
	Map <String, String> mm = JumbleUtils.stringToPropMap(
		JumbleFile.getStringContents(
			new File("/var/sadm/system/admin/CLUSTER")), "\n");
	instCluster = mm.get("CLUSTER");
    }

    private InstalledCluster() {
    }

    /**
     * Return the name of the installed cluster.
     *
     * @return The name of the installed metacluster, or null if unknown.
     */
    public static String getClusterName() {
	 return instCluster;
    }
}
