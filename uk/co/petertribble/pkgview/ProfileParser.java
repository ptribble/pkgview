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
import java.io.File;
import java.util.Set;
import java.util.TreeSet;

/**
 * ProfileParser - import and parse a jumpstart profile.
 * Can also validate it against a distribution directory
 *
 * @author Peter Tribble
 * @version 1.0
 */
public class ProfileParser {

    private static StringBuilder header;
    private static StringBuilder trailer;

    private static String mcluster;
    private static Set <String> addClusters;
    private static Set <String> deleteClusters;
    private static Set <String> addPackages;
    private static Set <String> deletePackages;

    private static final String pkgstr = "package";
    private static final String clstr = "cluster";

    // refers to syntax
    private static boolean isvalid = true;
    // refers to profile content
    private static boolean haswarnings;
    // hold reports
    private static StringBuilder errors;
    private static StringBuilder warnings;

    private static void parse(File f) {
	parse(f, (String) null);
    }

    private static void parse(File profile, String distdir) {
	header = new StringBuilder();
	trailer = new StringBuilder();
	errors = new StringBuilder();
	warnings = new StringBuilder();

	addClusters = new TreeSet <String> ();
	addPackages = new TreeSet <String> ();
	deleteClusters = new TreeSet <String> ();
	deletePackages = new TreeSet <String> ();

	boolean inheader = true;
	for (String line : JumbleFile.getLines(profile)) {
	    String thisline = line.trim();
	    if (thisline.startsWith(clstr)) {
		// cluster selection, so no longer in a header
		inheader = false;
		/*
		 * The algorithm here is that we split the line into words.
		 * If 2 words, then it ought to be defining the metacluster.
		 * If 3 words, then the 3rd word must be add or delete.
		 */
		String[] ds = thisline.split("\\s+");
		if (ds.length == 2) {
		    // define the metacluster
		    if (mcluster != null) {
			addError("Duplicate metacluster entry " + mcluster);
		    }
		    mcluster = ds[1];
		} else if (ds.length == 3) {
		    // add or delete a cluster
		    if (ds[2].equals("add")) {
			addClusters.add(ds[1]);
		    } else if (ds[2].equals("delete")) {
			deleteClusters.add(ds[1]);
		    } else {
			addError("Invalid cluster line:\n" + thisline);
		    }
		} else {
		    addError("Invalid cluster line:\n" + thisline);
		}
	    } else if (thisline.startsWith(pkgstr)) {
		// package selection, so no longer in a header
		inheader = false;
		/*
		 * The algorithm here is that we split the line into words.
		 * If 3 words, then the 3rd word must be add or delete.
		 */
		String[] ds = thisline.split("\\s+");
		if (ds.length == 3) {
		    // add or delete a package
		    if (ds[2].equals("add")) {
			addPackages.add(ds[1]);
		    } else if (ds[2].equals("delete")) {
			deletePackages.add(ds[1]);
		    } else {
			addError("Invalid package line:\n" + thisline);
		    }
		} else {
		    addError("Invalid package line:\n" + thisline);
		}
	    } else {
		if (inheader) {
		    header.append(thisline).append("\n");
		} else {
		    trailer.append(thisline).append("\n");
		}
	    }
	}
	int itype = getInstallType();
	if (itype < 0) {
	    addError("Error in install_type specification");
	}
	if ((itype > 0) && (mcluster != null)) {
	    addError("Can only specify a metacluster for initial installs");
	}
	/*
	 * If we have been given a distribution directory then we can import
	 * that and validate the profile we just read against it.
	 */
	String rdistdir = PkgUtils.findDist(distdir);
	if (rdistdir != null) {
	    checkProfile(rdistdir);
	}
	/*
	 * Output the results.
	 */
	if (isvalid) {
	    printProfile();
	    if (haswarnings) {
		System.out.println("++++++++++++WARNINGS++++++++++++");
		System.out.println(warnings);
		System.out.println("++++++++++END WARNINGS++++++++++");
	    }
	} else {
	    System.out.println("ERROR: invalid profile");
	    System.out.println(errors);
	}
    }

    private static int getInstallType() {
	// only split into 3 as we only want the first two words
	String dh[] = header.toString().split("\\s+", 3);
	if (!"install_type".equals(dh[0])) {
	    return -1;
	}
	if ("initial_install".equals(dh[1])) {
	    return PackageProfile.INSTALL_INITIAL;
	} else if ("upgrade".equals(dh[1])) {
	    return PackageProfile.INSTALL_UPGRADE;
	} else if ("flash_install".equals(dh[1])) {
	    return PackageProfile.INSTALL_FLASH;
	} else if ("initial_install".equals(dh[1])) {
	    return PackageProfile.INSTALL_FLASHUPDATE;
	}
	return -1;
    }

    private static void checkProfile(String distdir) {
	PkgList plist = new PkgList(distdir);
	ClusterToc ctoc = new ClusterToc(distdir, plist);
	/*
	 * For each cluster added or deleted by the profile, we can
	 * check whether it was in the specified metacluster or not
	 * and do the same for packages
	 */
	MetaCluster mc = ctoc.getMetaCluster(mcluster);
	if (mc == null) {
	    addWarning("Invalid metacluster " + mcluster);
	}
	// Check for duplicates
	Set <String> dupClusters = new TreeSet <String> (addClusters);
	dupClusters.retainAll(deleteClusters);
	Set <String> dupPackages = new TreeSet <String> (addPackages);
	dupPackages.retainAll(deletePackages);
	for (String s : dupClusters) {
	    addWarning("Cannot both add and delete cluster " + s);
	}
	for (String s : dupPackages) {
	    addWarning("Cannot both add and delete package " + s);
	}

	// if deleted, check it's present
	for (String cname : deleteClusters) {
	    if (ctoc.clusterExists(cname)) {
		if (!mc.containsCluster(cname)) {
		    addWarning("Cluster " + cname + " deleted unnecessarily");
		}
	    } else {
		addWarning("Unrecognized cluster " + cname);
	    }
	}
	// if added, check it wasn't already present
	for (String cname : addClusters) {
	    if (ctoc.clusterExists(cname)) {
		if (mc.containsCluster(cname)) {
		    addWarning("Cluster " + cname + " added unnecessarily");
		}
	    } else {
		addWarning("Unrecognized cluster " + cname);
	    }
	}

	/*
	 * Checking packages is a little more complicated. We not only need
	 * to check them against the metacluster, but also against the list
	 * of added and deleted clusters.
	 */
	for (String pname : deletePackages) {
	    // check it exists
	    if (plist.getPackage(pname) == null) {
		addWarning("Unrecognized package " + pname);
	    } else {
		for (String cname : deleteClusters) {
		    PackageCluster pc = ctoc.getCluster(cname);
		    if ((pc != null) && pc.containsPackage(pname)) {
			addWarning("Package " + pname + " already deleted via "
							+ cname);
		    }
		}
		if (!mc.includesPackage(pname)) {
		    /*
		     * Removing this package is wrong unless we added a cluster
		     * that pulled it in.
		     */
		    boolean packwarn = true;
		    for (String cname : addClusters) {
			PackageCluster pc = ctoc.getCluster(cname);
			if ((pc != null) && pc.containsPackage(pname)) {
			    packwarn = false;
			}
		    }
		    if (packwarn) {
			addWarning("Package " + pname +
						" deleted unnecessarily");
		    }
		}
	    }
	}
	for (String pname : addPackages) {
	    // check it exists
	    if (plist.getPackage(pname) == null) {
		addWarning("Unrecognized package " + pname);
	    } else {
		for (String cname : addClusters) {
		    PackageCluster pc = ctoc.getCluster(cname);
		    if ((pc != null) && pc.containsPackage(pname)) {
			addWarning("Package " + pname + " already included via "
+ cname);
		    }
		}
		if (mc.includesPackage(pname)) {
		    /*
		     * Adding this package is wrong unless we deleted a cluster
		     * that took it away and we're putting it back.
		     */
		    boolean packwarn = true;
		    for (String cname : deleteClusters) {
			PackageCluster pc = ctoc.getCluster(cname);
			if ((pc != null) && pc.containsPackage(pname)) {
			    packwarn = false;
			}
		    }
		    if (packwarn) {
			addWarning("Package " + pname + " added unnecessarily");
		    }
		}
	    }
	}
    }

    private static void addError(String s) {
	isvalid = false;
	errors.append(s).append("\n");
    }

    private static void addWarning(String s) {
	haswarnings = true;
	warnings.append(s).append("\n");
    }

    private static void printProfile() {
	StringBuilder sb = new StringBuilder();
	// all the header from the original profile
	sb.append(header.toString());
	// walk through the software selection
	sb.append(clstr).append("\t").append(mcluster).append("\n");
	for (String s : addClusters) {
	    sb.append(clstr).append("\t").append(s).append("\tadd\n");
	}
	for (String s : deleteClusters) {
	    sb.append(clstr).append("\t").append(s).append("\tdelete\n");
	}
	for (String s : addPackages) {
	    sb.append(pkgstr).append("\t").append(s).append("\tadd\n");
	}
	for (String s : deletePackages) {
	    sb.append(pkgstr).append("\t").append(s).append("\tdelete\n");
	}
	// and the rest
	sb.append(trailer.toString());
	System.out.print(sb);
    }

    /**
     * Parse a jumpstart profile and check it for validity.
     *
     * @param args Command line arguments. The first, required, is the name
     * of the file containing the jumpstart profile. The second, optional, is
     * the name of a directory containing a Solaris distribution.
     */
    public static void main(String args[]) {
	if (args.length == 1) {
	    File f = new File(args[0]);
	    if (f.exists() && !f.isDirectory()) {
		parse(f);
	    } else {
		System.out.println("ERROR: Unable to find input profile");
	    }
	} else if (args.length == 2) {
	    File f = new File(args[0]);
	    File ff = new File(args[1]);
	    if (f.exists() && ff.exists() && ff.isDirectory()) {
		parse(f, args[1]);
	    } else {
		if (!f.exists()) {
		    System.out.println("ERROR: Unable to find input profile");
		}
		if (ff.exists()) {
		    if (!ff.isDirectory()) {
			System.out.println(
				"ERROR: Product directory isn't a directory");
		    }
		} else {
		    System.out.println("ERROR: Cannot find Product directory");
		}
	    }
	} else {
	    System.out.println("Usage: parseprofile profile [product_dir]");
	}
    }
}
