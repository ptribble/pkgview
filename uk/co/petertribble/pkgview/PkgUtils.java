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

import java.util.Map;
import java.util.Set;
import java.util.Iterator;
import java.text.DecimalFormat;
import java.io.File;

/**
 * PkgUtils - shows available Solaris packages.
 *
 * @author Peter Tribble
 * @version 1.0
 */
public class PkgUtils {

    /**
     * Returns the pkginfo file as a html table.
     */
    static public String infoTable(SolarisPackage pkg) {
	StringBuilder sb = new StringBuilder();
	Map <String, String> infomap = pkg.infoMap();
	// clean out the junk
	infomap.remove("#FASPACD");
	infomap.remove("PKG");
	infomap.remove("MAXINST");
	infomap.remove("PKGINST");
	infomap.remove("PKGSAV");
	infomap.remove("OAMBASE");
	infomap.remove("ARCH");
	infomap.remove("TZ");
	infomap.remove("PATH");
	infomap.remove("UPDATE");
	infomap.remove("ISTATES");
	infomap.remove("LANG");
	Iterator <String> itr = infomap.keySet().iterator();
	while (itr.hasNext()) {
	    String s = itr.next();
	    if (s.startsWith("LC_") || s.startsWith("PATCH_")
		   || s.startsWith("SUNW_") || s.startsWith("ACTIVE_")
		   || s.startsWith("SCRIPTS_") || s.startsWith("PKG_")) {
		itr.remove();
	    }
	}

	headRow(sb, PkgResources.getString("PKGUTILS.PROPERTY"),
			PkgResources.getString("PKGUTILS.VALUE"));
	for (String s : infomap.keySet()) {
	    String v = infomap.get(s);
	    if (!"".equals(v)) {
		addRow(sb, s, v);
	    }
	}

	return wrapTable(sb);
    }

    static public String dependencyTable(SolarisPackage pkg, PkgList plist,
					boolean showselected) {
	return dependencyTable(pkg.getDependencySet(), pkg.getRDependencySet(),
			pkg.getIncompatibleSet(), plist, showselected);
    }

    static public String dependencyTable(PackageCluster pc, PkgList plist,
					boolean showselected) {
	return dependencyTable(pc.getDependencySet(), pc.getRDependencySet(),
			pc.getIncompatibleSet(), plist, showselected);
    }

    /*
     * Common dependency tree code
     */
    static private String dependencyTable(Set <String> depset,
				Set <String> rdepset, Set <String> idepset,
				PkgList plist, boolean showselected) {
	StringBuilder sb = new StringBuilder();

	headRow(sb, PkgResources.getString("PKGUTILS.PACKAGE"),
		PkgResources.getString("PKGUTILS.DEPENDENCY"));
	innerdeptable(sb, depset, plist, showselected,
		PkgResources.getString("PKGUTILS.PREREQ"));
	innerdeptable(sb, rdepset, plist, showselected,
		PkgResources.getString("PKGUTILS.REQ"));
	innerdeptable(sb, idepset, plist, showselected,
		PkgResources.getString("PKGUTILS.INCOMP"));
	return wrapTable(sb);
    }

    static private void innerdeptable(StringBuilder sb, Set <String> depset,
			PkgList plist, boolean showselected, String deptype) {
	for (String s : depset) {
	    sb.append("<tr><td>").append(s);
	    if (showselected) {
		SolarisPackage pkg = plist.getPackage(s);
		if ((pkg != null) && (!pkg.isSelected())) {
		    sb.append(" (unselected)");
		}
	    }
	    sb.append("</td><td>");
	    sb.append(deptype);
	    sb.append("</td></tr>");
	}
    }

    static public String detailTable(PackageCluster pc, ContentsParser cp) {
	StringBuilder sb = new StringBuilder();
	if (cp != null) {
	    ContentsPackage cc = new ContentsPackage(pc);

	    sb.append(doDetailTable(cc.numEntries(), cc.numShared(),
		cc.numHardLinks(), cc.numDirectories(), cc.spaceUsed()));
	}
	return sb.toString();
    }

    static public String detailTable(SolarisPackage pkg, ContentsParser cp) {
	StringBuilder sb = new StringBuilder();
	if (cp != null) {
	    ContentsPackage cpp = cp.getPackage(pkg.getName());
	    if (cpp != null) {
		sb.append(doDetailTable(cpp.numEntries(), cpp.numShared(),
		    cpp.numHardLinks(), cpp.numDirectories(), cpp.spaceUsed()));
		sb.append(doFileList(cpp));
	    }
	}
	return sb.toString();
    }

    static private String doFileList(ContentsPackage cpp) {
	StringBuilder sb = new StringBuilder();
	headRow(sb, PkgResources.getString("PKGUTILS.FILELIST"));
	StringBuilder sb2 = new StringBuilder();
	sb2.append("<pre>\n");
	for (ContentsFileDetail cfd : cpp.getDetails()) {
	    sb2.append(cfd.getName()).append("\n");
	}
	sb2.append("</pre>\n");
	return wrapTable(sb) + sb2;
    }

    static private String doDetailTable(long instpath, long shpath,
					long lnfiles, long dirs, long space) {
	StringBuilder sb = new StringBuilder();

	headRow2(sb, PkgResources.getString("PKGUTILS.DETAILS"));
	addRow(sb, PkgResources.getString("PKGUTILS.INSTPATH"), instpath);
	addRow(sb, PkgResources.getString("PKGUTILS.SHPATH"), shpath);
	addRow(sb, PkgResources.getString("PKGUTILS.LNFILES"), lnfiles);
	addRow(sb, PkgResources.getString("PKGUTILS.DIRS"), dirs);
	addRow(sb, PkgResources.getString("PKGUTILS.SPC"),
						niceSpaceUsed(space));

	return wrapTable(sb);
    }

    static public void headRow(StringBuilder sb, String s1, String s2) {
	sb.append("<tr bgcolor=\"#eeeeee\"><th>");
	sb.append(s1);
	sb.append("</th><th>");
	sb.append(s2);
	sb.append("</th></tr>\n");
    }

    static public void headRow2(StringBuilder sb, String s) {
	sb.append("<tr bgcolor=\"#eeeeee\"><th colspan=\"2\">");
	sb.append(s);
	sb.append("</th></tr>\n");
    }

    static public void headRow(StringBuilder sb, String s) {
	sb.append("<tr bgcolor=\"#eeeeee\"><th>");
	sb.append(s);
	sb.append("</th></tr>\n");
    }

    static public void addRow(StringBuilder sb, String s) {
	sb.append("<tr><td>");
	sb.append(s);
	sb.append("</td></tr>\n");
    }

    static public void addRow(StringBuilder sb, String s1, long l) {
	sb.append("<tr><td>");
	sb.append(s1);
	sb.append("</td><td>");
	sb.append(l);
	sb.append("</td></tr>\n");
    }

    static public void addRow(StringBuilder sb, String s1, String s2) {
	sb.append("<tr><td>");
	sb.append(s1);
	sb.append("</td><td>");
	sb.append(s2);
	sb.append("</td></tr>\n");
    }

    static public String wrapTable(StringBuilder sb) {
	sb.insert(0, "<table width=\"100%\">");
	sb.append("</table>\n");
	return sb.toString();
    }

    static private String niceSpaceUsed(long space) {
        DecimalFormat df = new DecimalFormat("##0.0#");
	StringBuilder sb = new StringBuilder();
	double dspace = (double) space;
	int iscale = 0;
	while ((dspace > 1024.0) && (iscale < 3)) {
	    iscale++;
	    dspace /= 1024.0;
	}
	sb.append(df.format(dspace));
	sb.append(" ");
	switch (iscale) {
	    case 0:
		sb.append(PkgResources.getString("PKGUTILS.BYTES"));
		break;
	    case 1:
		sb.append(PkgResources.getString("PKGUTILS.KBYTES"));
		break;
	    case 2:
		sb.append(PkgResources.getString("PKGUTILS.MBYTES"));
		break;
	    case 3:
		sb.append(PkgResources.getString("PKGUTILS.GBYTES"));
		break;
	}
	return sb.toString();
    }

    static public String clusterMembership(SolarisPackage pkg,
		ClusterToc ctoc) {
	StringBuilder sb = new StringBuilder();

	headRow2(sb, PkgResources.getString("PKGUTILS.CL"));
	for (PackageCluster pclust : ctoc.containingClusters(pkg)) {
	    addRow(sb, pclust.toString(), pclust.getName());
	}

	headRow2(sb, PkgResources.getString("PKGUTILS.MC"));
	for (MetaCluster mclust : ctoc.containingMetaClusters(pkg)) {
	    addRow(sb, mclust.toString(), mclust.getName());
	}

	return wrapTable(sb);
    }

    static public String metaClusterMembership(PackageCluster pc,
		ClusterToc ctoc) {
	StringBuilder sb = new StringBuilder();

	headRow2(sb, "This cluster is part of the following Metaclusters:");
	for (MetaCluster mclust : ctoc.containingMetaClusters(pc)) {
	    addRow(sb, mclust.toString(), mclust.getName());
	}

	return wrapTable(sb);
    }

    static public String findDist(String s) {
	if (s == null) {
	    return null;
	}
	File froot = new File(s);
	if (!froot.exists()) {
	    return null;
	}
	File f = new File(froot, ".clustertoc");
	if (f.exists()) {
	    return s;
	}
	/*
	 * Structure should be .../Solaris_XX/Product/.clustertoc
	 */
	for (File ff : froot.listFiles()) {
	    File f2 = new File(ff, ".clustertoc");
	    if (f2.exists()) {
		return ff.getPath();
	    }
	    File fp = new File(ff, "Product");
	    File f3 = new File(fp, ".clustertoc");
	    if (f3.exists()) {
		return fp.getPath();
	    }
	}
	return null;
    }
}
