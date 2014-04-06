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
import java.io.File;
import uk.co.petertribble.jumble.*;

/**
 * Describe a Solaris (SVR4) package.
 */
public class SolarisPackage implements Comparable<SolarisPackage> {

    private File pkgrootf;

    private String name;
    private Map <String, String> infomap;
    private Set <String> dependson;
    private Set <String> rdepends;
    private Set <String> incompatibles;
    private Set <SolarisPackage> depSet;

    private boolean selected;
    private boolean required;

    /**
     * Create a Solaris package container.
     *
     * @param name  The name of the package
     */
    public SolarisPackage(String name) {
	this("/var/sadm/pkg", name);
    }

    /**
     * Create a Solaris package container.
     *
     * @param pkgroot  The root directory for package metadata
     * @param name  The name of the package.
     */
    public SolarisPackage(String pkgroot, String name) {
	pkgrootf = new File(pkgroot);
	this.name = name;
    }

    /**
     * Return the name of this package.
     */
    public String getName() {
	return name;
    }

    /**
     * The name of this package.
     */
    public String toString() {
	return name;
    }

    /**
     * The long name, or description of this package. If the NAME field is
     * present, then that; else the DESC field or, if that is not
     * present, just returns the name of the package.
     *
     * @return the name or description of this package
     */
    public String getDescription() {
	String s = getInfoItem("NAME");
	if (s == null) {
	    s = getInfoItem("DESC");
	}
	return (s == null) ? name : s;
    }

    /*
     * Actually parse the depend file
     */
    private void parseDepend() {
	dependson = new TreeSet <String> ();
	rdepends = new TreeSet <String> ();
	incompatibles = new TreeSet <String> ();
	for (String s : getDepend()) {
	    String[] ds = s.split("\\s+", 3);
	    // Must have at least 2 words
	    if ((ds.length > 1) && (ds[0].equals("P") || ds[0].equals("I") ||
						ds[0].equals("R"))) {
		if (ds[0].equals("P")) {
		    dependson.add(ds[1]);
		} else if (ds[0].equals("R")) {
		    rdepends.add(ds[1]);
		} else if (ds[0].equals("I")) {
		    incompatibles.add(ds[1]);
		}
	    }
	}
    }

    /**
     * Return the Set of package names that this package is dependent on.
     */
    public Set <String> getDependencySet() {
	if (dependson == null) {
	    parseDepend();
	}
	return dependson;
    }

    /**
     * Return the Set of package names that this package declares to be
     * dependent on it.
     */
    public Set <String> getRDependencySet() {
	if (rdepends == null) {
	    parseDepend();
	}
	return rdepends;
    }

    /**
     * Return the Set of package names that this package is incompatible with.
     */
    public Set <String> getIncompatibleSet() {
	if (incompatibles == null) {
	    parseDepend();
	}
	return incompatibles;
    }

    /**
     * Define the set of packages that are dependant on this package.
     *
     * @see #getDependantSet
     */
    public void setDependantSet(Set <SolarisPackage> depSet) {
	this.depSet = depSet;
    }

    /**
     * Return the set of packages that are dependant on this package.
     *
     * @see #setDependantSet
     */
    public Set <SolarisPackage> getDependantSet() {
	return depSet;
    }

    /**
     * Return the Set of packages that are dependant on this package
     * and are selected.
     */
    public Set <SolarisPackage> getSelectedDependantSet() {
	Set <SolarisPackage> seldepSet = new TreeSet <SolarisPackage> ();
	if (depSet != null) {
	    for (SolarisPackage pkg : depSet) {
		if (pkg.isSelected()) {
		    seldepSet.add(pkg);
		}
	    }
	}
	return seldepSet;
    }

    /**
     * Parse the pkginfo file.
     */
    public Map <String, String> infoMap() {
	if (infomap == null) {
	    parseInfo();
	}
	// defensive copy, as PkgUtils mangles it
	return new HashMap <String, String> (infomap);
    }

    /**
     * Get the specified property from the pkginfo file.
     */
    public String getInfoItem(String s) {
	if (infomap == null) {
	    parseInfo();
	}
	return infomap.get(s);
    }

    /*
     * Parse the pkginfo file.
     */
    private void parseInfo() {
	infomap = JumbleUtils.stringToPropMap(getInfo(), "\n");
    }

    /**
     * Returns whether this package exists, by seeing whether the
     * directory corresponding to its name exists.
     */
    public boolean exists() {
	return new File(pkgrootf, name).exists();
    }

    /*
     * Returns the pkginfo file associated with this package as a String.
     */
    private String getInfo() {
	return JumbleFile.getStringContents(
		new File(pkgrootf, name+"/pkginfo"));
    }

    /*
     * Returns the depend file associated with this package as a String array.
     */
    private String[] getDepend() {
	return JumbleFile.getLines(new File(pkgrootf, name+"/install/depend"));
    }

    /**
     * Sets whether this package is selected. If the package is required,
     * the selected status will be forced to true.
     *
     * @see #isSelected
     */
    public void setSelected(boolean selected) {
	this.selected = required ? true : selected;
    }

    /**
     * Gets whether this package is selected.
     *
     * @see #setSelected
     */
    public boolean isSelected() {
	return selected;
    }

    /**
     * Sets whether this package must be selected.
     */
    public void setRequired() {
	required = true;
    }

    /**
     * Gets whether this package is required. Packages and clusters in the
     * SUNWCmreq metacluster are required.
     */
    public boolean isRequired() {
	return required;
    }

    /**
     * For Comparable.
     */
    public int compareTo(SolarisPackage p) {
	return name.compareTo(p.getName());
    }
}
