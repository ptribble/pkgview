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

import javax.swing.JTabbedPane;
import org.jdesktop.swingworker.SwingWorker;

/**
 * Generates a view of the software available on a distribution media.
 * It is broken into two tabs, which show a list of available packages,
 * and a tree view based on installation clusters.
 * @author Peter Tribble
 * @version 1.0
 */
public class DistributionSoftwarePanel extends JTabbedPane {

    private ClusterToc ctoc;
    private PkgList plist;
    private PackagePanel dpp;
    private ClusterPanel dcp;
    private PackageProfile profile;

    public DistributionSoftwarePanel(ClusterToc ctoc, PkgList plist,
				PackageProfile profile) {
	this.ctoc = ctoc;
	this.plist = plist;
	this.profile = profile;

	dpp = new PackagePanel(plist, ctoc, profile);
	add(PkgResources.getString("PKG.LIST"), dpp);

	if (ctoc.exists()) {
	    dcp = new ClusterPanel(plist, ctoc, profile);
	    add(PkgResources.getString("PKG.TREE"), dcp);
	    setSelectedComponent(dcp);
	}

	(new RevDependencyWorker()).execute();
    }

    class RevDependencyWorker extends SwingWorker <String, Object> {
	@Override
	public String doInBackground() {
	    plist.createRevDependencies();
	    return "done";
	}

	@Override
	protected void done() {
	    dpp.showRevDependencies();
	    if (ctoc.exists()) {
		dcp.showRevDependencies();
	    }
	}
    }

    public void setMetaCluster(String metacluster) {
	dpp.setMetaCluster(metacluster);
	profile.initialize(ctoc.getMetaCluster(metacluster));
	if (dcp != null) {
	    dcp.reset();
	    setSelectedComponent(dcp);
	}
    }
}
