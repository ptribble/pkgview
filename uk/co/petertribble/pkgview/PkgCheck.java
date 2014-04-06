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

import java.io.File;
import java.util.Set;
import java.util.HashSet;

/**
 * PkgCheck - check that what should be installed actually is.
 */
public class PkgCheck {

    private boolean check = true;
    private boolean list;
    private boolean verbose;
    private boolean debug;
    private boolean dopaths;
    private boolean partpaths;
    private boolean allpkgs;

    private ContentsParser cp;

    /**
     * Check package integrity.
     *
     * @param arguments, a list of package names
     */
    public PkgCheck(String[] args) {
	PkgList plist = new PkgList();
	Set <String> names = parseArgs(args);
	cp = ContentsParser.getInstance();
	if (dopaths || partpaths) {
	    doPathNames(names);
	} else {
	    for (String pkg : allpkgs ? plist.getPackageNames() : names) {
		if (plist.getPackage(pkg) == null) {
		    System.out.println("Invalid package " + pkg);
		} else {
		    System.out.println("Valid package " + pkg);
		    doProcess(pkg);
		}
	    }
	}
    }

    // FIXME -a for all packages
    private Set <String> parseArgs(String[] args) {
	Set <String> names = new HashSet <String> ();
	for (String arg : args) {
	    if ("-l".equals(arg)) {
		check = false;
		list = true;
	    } else if ("-v".equals(arg)) {
		verbose = true;
	    } else if ("-a".equals(arg)) {
		allpkgs = true;
	    } else if ("-V".equals(arg)) {
		verbose = true;
		debug = true;
	    } else if ("-p".equals(arg)) {
		dopaths = true;
		partpaths = false;
	    } else if ("-P".equals(arg)) {
		dopaths = false;
		partpaths = true;
	    } else if (arg.startsWith("-")) {
		usage();
	    } else {
		names.add(arg);
	    }
	}
	return names;
    }

    private void doPathNames(Set <String> names) {
	for (String name : names) {
	    showFile(cp.getFileDetail(name));
	}
    }

    /*
     * Shows a nicely formatted list of packages that own the given file.
     */
    private void showOwningPkgs(ContentsFileDetail cfd) {
	System.out.print("Path " + cfd.getName() + " belongs to the following");
	System.out.println(cfd.isShared() ? " packages:" : " package:");
	int i = 0;
	for (String s : cfd.getPackageNames()) {
	    i += 2;
	    i += s.length();
	    if (i > 79) {
		System.out.println();
		i = s.length() + 2;
	    }
	    System.out.print("  " + s);
	}
	System.out.println();
    }

    private void showFile(ContentsFileDetail cfd) {
	if (dopaths || partpaths) {
	    showOwningPkgs(cfd);
	}
	if (list) {
	    System.out.print("  " + cfd.getName());
	    if (verbose) {
		System.out.print(" owner=" + cfd.getOwner());
		System.out.print(" group=" + cfd.getGroup());
		System.out.print(" mode=" + cfd.getMode());
		if (cfd.isRegular()) {
		    System.out.print(" size=" + cfd.getSize());
		}
	    }
	    System.out.println();
	}
	if (check) {
	    File f = new File(cfd.getName());
	    if (f.exists()) {
		if (cfd.isRegular()) {
		    if (f.isFile()) {
			long fmodtime = f.lastModified()/1000;
			long pmodtime = cfd.lastModified();
			if (debug) {
			    System.out.println("    File " +
					cfd.getName() +
					" confirmed present");
			}
			if (f.length() == Long.parseLong(cfd.getSize())) {
			    if (debug) {
				System.out.println("    File " +
						cfd.getName() +
						" has correct size");
			    }
			} else {
			    if (cfd.isEditable()) {
				if (verbose) {
				    System.out.println("   WARNING: File " +
					cfd.getName() +
					" has incorrect size");
				}
			    } else {
				System.out.println("   ERROR: File " +
					cfd.getName() +
					" has incorrect size");
			    }
			}
			// allow a little rounding error
			if (Math.abs(fmodtime - pmodtime) < 2) {
			    if (debug) {
				System.out.println("      Timestamp verified.");
			    }
			} else {
			    if (cfd.isEditable()) {
				if (verbose) {
				    System.out.println("   WARNING: File " +
					cfd.getName() +
					" has incorrect modification time");
				}
			    } else {
				System.out.println("   ERROR: File " +
					cfd.getName() +
					" has incorrect modification time");
			    }
			}
		    } else {
			System.out.println("   ERROR: Path " +
					cfd.getName() +
					" is not a file");
		    }
		}
		if (cfd.isDirectory()) {
		    if (f.isDirectory()) {
			if (debug) {
			    System.out.println("    Directory " +
					cfd.getName() +
					" confirmed present");
			}
		    } else {
			System.out.println("   ERROR: Path " +
					cfd.getName() +
					" is not a directory");
		    }
		}
	    } else {
		System.err.println("Missing or unreadable path "
			+ cfd.getName());
	    }
	}
    }

    private void doProcess(String pkg) {
	ContentsPackage cpp = cp.getPackage(pkg);
	if (cpp == null) {
	    if (debug) {
		System.out.println("    Package " + pkg + " is empty.");
	    }
	} else {
	    for (ContentsFileDetail cfd : cpp.getDetails()) {
		showFile(cfd);
	    }
	}
    }

    private static void usage() {
	System.err.println("Usage: check [-v|-V] [-l | -p path ... | "
		+ "-P partial-path ...] name ...");
	System.exit(1);
    }

    /**
     * Run the application.
     *
     * @param args Command line arguments
     */
    public static void main(String args[]) {
	if (args.length == 0) {
	    usage();
	}
	new PkgCheck(args);
    }
}
