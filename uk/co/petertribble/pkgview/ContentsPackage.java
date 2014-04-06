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

import java.util.Set;
import java.util.HashSet;

/**
 * Represent the files contained in a Solaris package.
 *
 * @author Peter Tribble
 * @version 1.0
 */
public class ContentsPackage {

    private Set <ContentsFileDetail> fileset;
    private ContentsParser cp;

    public ContentsPackage() {
	fileset = new HashSet <ContentsFileDetail> ();
    }

    public ContentsPackage(PackageCluster pc) {
	fileset = new HashSet <ContentsFileDetail> ();
	cp = ContentsParser.getInstance();
	addPkgFiles(pc);
    }

    public ContentsPackage(MetaCluster mc) {
	fileset = new HashSet <ContentsFileDetail> ();
	cp = ContentsParser.getInstance();
	for (PackageCluster pc : mc.getClusters()) {
	    addPkgFiles(pc);
	}
	for (SolarisPackage pkg : mc.getPackages()) {
	    addPkgFiles(pkg);
	}
    }

    public void addFile(ContentsFileDetail cfd) {
	fileset.add(cfd);
    }

    private void addPkgFiles(PackageCluster pc) {
	for (SolarisPackage pkg : pc.getPackages()) {
	    addPkgFiles(pkg);
	}
    }

    private void addPkgFiles(SolarisPackage pkg) {
	ContentsPackage cpp = cp.getPackage(pkg.getName());
	if (cpp != null) {
	    fileset.addAll(cpp.getDetails());
	}
    }

    public int numEntries() {
	return fileset.size();
    }

    public int numDirectories() {
	int i = 0;
	for (ContentsFileDetail cfd : fileset) {
	    if (cfd.isDirectory()) {
		i++;
	    }
	}
	return i;
    }

    /*
     * pkginfo -l reports "linked files" just for hard links
     */
    public int numHardLinks() {
	int i = 0;
	for (ContentsFileDetail cfd : fileset) {
	    if (cfd.isHardLink()) {
		i++;
	    }
	}
	return i;
    }

    public int numSymLinks() {
	int i = 0;
	for (ContentsFileDetail cfd : fileset) {
	    if (cfd.isSymLink()) {
		i++;
	    }
	}
	return i;
    }

    /*
     * Devices
     */
    public int numDevices() {
	int i = 0;
	for (ContentsFileDetail cfd : fileset) {
	    if (cfd.isDevice()) {
		i++;
	    }
	}
	return i;
    }

    /*
     * Shared files - contained in more than one package
     */
    public int numShared() {
	int i = 0;
	for (ContentsFileDetail cfd : fileset) {
	    if (cfd.isShared()) {
		i++;
	    }
	}
	return i;
    }

    /*
     * Add up the space used
     */
    public long spaceUsed() {
	long l = 0;
	for (ContentsFileDetail cfd : fileset) {
	    if (cfd.isRegular()) {
		l += Long.parseLong(cfd.getSize());
	    }
	}
	return l;
    }

    public Set <ContentsFileDetail> getDetails() {
	return fileset;
    }
}
