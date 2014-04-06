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
 * Generates a view of the software installed on a system. The display
 * is broken into three tabs, which show a list of installed packages,
 * a tree view based on installation clusters, and (optionally) a view
 * based on the files in the filesystem.
 * @author Peter Tribble
 * @version 1.0
 */
public class InstalledSoftwarePanel extends JTabbedPane {

    private ClusterToc ctoc;
    private PkgList plist;
    private PackagePanel ipp;
    private ClusterPanel icp;
    private InstalledFilesPanel ifp;

    public InstalledSoftwarePanel() {
	plist = new PkgList();
	ctoc = new ClusterToc(plist);

	ipp = new PackagePanel(plist, ctoc);
	add(PkgResources.getString("PKG.LIST"), ipp);

	if (ctoc.exists()) {
	    icp = new ClusterPanel(plist, ctoc);
	    add(PkgResources.getString("PKG.TREE"), icp);
	}

	ifp = new InstalledFilesPanel(plist, ctoc);
	add(PkgResources.getString("PKG.FS"), ifp);

	add(PkgResources.getString("PKG.PATCH"), new PatchPanel(plist));

	(new RevDependencyWorker()).execute();
	(new ContentsWorker()).execute();
    }

    /*
     * Parse the contents file in the background. ContentsParser is
     * a singleton, so once we've done it here we can tell the other
     * views to use it.
     */
    class ContentsWorker extends SwingWorker <String, Object> {
	@Override
	public String doInBackground() {
	    ContentsParser.getInstance();
	    return "done";
	}

	@Override
	protected void done() {
	    ipp.showDetailedView();
	    if (ctoc.exists()) {
		icp.showDetailedView();
	    }
	    ifp.showDetailedView();
	}
    }

    /*
     * Generate reverse dependencies in the background, then tell the other
     * views to show them.
     */
    class RevDependencyWorker extends SwingWorker <String, Object> {
	@Override
	public String doInBackground() {
	    plist.createRevDependencies();
	    return "done";
	}

	@Override
	protected void done() {
	    ipp.showRevDependencies();
	    if (ctoc.exists()) {
		icp.showRevDependencies();
	    }
	}
    }
}
