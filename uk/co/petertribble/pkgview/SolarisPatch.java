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

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;

/**
 * Stores details of a Solaris patch.
 */
public class SolarisPatch implements Comparable<SolarisPatch> {

    private String sid;
    private String srevision;
    private String info;
    private List <SolarisPackage> packages;

    public SolarisPatch(String sid, String srevision, String info) {
	this.sid = sid;
	this.srevision = srevision;
	this.info = info;
	packages = new ArrayList <SolarisPackage> ();
    }

    public void addPackage(SolarisPackage pkg) {
	packages.add(pkg);
    }

    public Set <SolarisPackage> getPackages() {
	return new TreeSet <SolarisPackage> (packages);
    }

    public int getId() {
	return Integer.parseInt(sid);
    }

    public int getRevision() {
	return Integer.parseInt(srevision);
    }

    public String getInfo() {
	return info;
    }

    /**
     * Returns a String representation of this patch, in the form
     * ######-##.
     */
    @Override
    public String toString() {
	return sid + "-" + srevision;
    }

    /**
     * Returns whether the requested Object is equal to this SolarisPatch.
     * Equality implies that the Object is of class SolarisPatch and has
     * the same id and revision.
     *
     * @param o The object to be tested for equality.
     *
     * @return true if the object is a {@code SolarisPatch} with the same
     * id and revision as this {@code SolarisPatch}.
     */
    @Override
    public boolean equals(Object o) {
        if (o instanceof SolarisPatch) {
	    SolarisPatch sp = (SolarisPatch) o;
	    return (getId() == sp.getId())
		&& (getRevision() == sp.getRevision());
	}
	return false;
    }

    /**
     * Returns the hashcode of this object. The actual algorithm is
     * 100*patch+revision, and relies upon the revision always being in the
     * range 1 to 99.
     *
     * @return the hashcode of this SolarisPatch
     */
    @Override
    public int hashCode() {
	return 100*getId() + getRevision();
    }

    public int compareTo(SolarisPatch p2) {
	int lp = getId();
	int lp2 = p2.getId();
	return (lp == lp2) ? getRevision() - p2.getRevision() : lp - lp2;
    }
}
