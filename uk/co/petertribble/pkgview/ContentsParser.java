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
import java.util.HashMap;
import java.io.*;

/**
 * We parse the Solaris contents file and create two hashes.
 *
 * The first hash is by file, or equivalently by line.
 * The key is the filename, the value is a ContentsFileDetail
 * which stores the metadata and the list of packages that contain
 * this file.
 *
 * The second hash is by package. The key is the package name,
 * and the value is a List of ContentsFileDetail's.
 */
public class ContentsParser {

    private static ContentsParser cpinstance;

    private Map <String, ContentsFileDetail> fileHash;
    private Map <String, ContentsPackage> pkgHash;

    private static final String CONTENTS_FILE = "/var/sadm/install/contents";

    /*
     * Parse the default contents file.
     */
    private ContentsParser() {
	this(CONTENTS_FILE);
    }

    /*
     * Parse a contents file.
     */
    private ContentsParser(String contents) {
	fileHash = new HashMap <String, ContentsFileDetail> (65536);
	pkgHash = new HashMap <String, ContentsPackage> (2048);
	parse(contents);
    }

    public static synchronized ContentsParser getInstance() {
	if (cpinstance == null) {
	    cpinstance = new ContentsParser();
	}
	return cpinstance;
    }

    /*
     * Oddly, using this version is significantly slower, although it does
     * consume rather less memory. And timing of the actual reading of the
     * file indicates that this is a lot faster, so I'm not sure why the
     * overall time gets worse.
     *
     * On my machine, the time breakdown is like:
     *  - just reading every line, 0.9s
     *  - just parsing every line, 1.5s, so the parsing adds 0.6s
     *  - populating the fileHash adds about 1s
     *  - poulating the pkgHash and its contents adds another 1s
     *
     * So the actual parse is pretty quick - it's populating the maps
     * that really adds to the cost.
     */
    private void parse(String contents) {
	try {
	    BufferedReader in
		= new BufferedReader(new FileReader(contents));
	    String s = null;
	    while ((s = in.readLine()) != null) {
		ContentsFileDetail cfd = new ContentsFileDetail(s);
		fileHash.put(cfd.getName(), cfd);
		for (String pkgname : cfd.getPackageNames()) {
		    ContentsPackage cp = pkgHash.get(pkgname);
		    if (cp == null) {
			cp = new ContentsPackage();
			pkgHash.put(pkgname, cp);
		    }
		    cp.addFile(cfd);
		}
	    }
	} catch (IOException ioe) {}
    }

    public ContentsFileDetail getFileDetail(String s) {
	return fileHash.get(s);
    }

    public ContentsPackage getPackage(String pkgname) {
	return pkgHash.get(pkgname);
    }
}
