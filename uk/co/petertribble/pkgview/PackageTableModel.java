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
import javax.swing.table.AbstractTableModel;

/**
 * A TableModel describing Solaris packages.
 * @author Peter Tribble
 * @version 1.0
 */
public class PackageTableModel extends AbstractTableModel {

    private boolean showselected;
    private DistributionSelectionPanel dsp;

    private List <SolarisPackage> pkgs;

    /*
     * Columns to show
     */
    private String[] col1 = { "Name", "Category", "Description" };
    private String[] col2 = { "Selected", "Name", "Category", "Description" };
    private String[] columnNames;

    /**
     * Create a new PackageTableModel. If a non null jumpstart profile is
     * supplied, then show checkboxes to show the state of package selection.
     *
     * @param plist a PkgList
     * @param profile a jumpstart profile
     */
    public PackageTableModel(PkgList plist, PackageProfile profile) {
	showselected = (profile != null);
	pkgs = new ArrayList <SolarisPackage> (plist.getPackages());
	columnNames = (showselected) ? col2 : col1;
    }

    public void setDsp(DistributionSelectionPanel dsp) {
	this.dsp = dsp;
    }

    public int getColumnCount() {
	return columnNames.length;
    }

    public int getRowCount() {
	return pkgs.size();
    }

    @Override
    public String getColumnName(int col) {
	return columnNames[col];
    }

    /**
     * Return the appropriate data.
     *
     * @see #setValueAt
     *
     * @param row the int row of the selected cell
     * @param col the int column of the selected cell
     *
     * @return the Object at the selected cell
     */
    public Object getValueAt(int row, int col) {
	SolarisPackage pkg = pkgs.get(row);
	if (showselected) {
	    if (col == 0) {
		return Boolean.valueOf(pkg.isSelected());
	    } else {
		col--;
	    }
	}
	if (col == 0) {
	    return pkg.getName();
	} else if (col == 1) {
	    String s =  pkg.getInfoItem("CATEGORY");
	    return (s == null) ? "" : s.split(",")[0];
	} else {
	    return pkg.getDescription();
	}
    }

    @Override
    public Class<?> getColumnClass(int col) {
	return (showselected && (col == 0)) ? Boolean.class : String.class;
    }

    /*
     * The only editable cells are those in the first column and only then
     * if we're showing the selection. Furthermore, if the package cannot
     * be unselected then it shouldn't be editable ether.
     */
    @Override
    public boolean isCellEditable(int row, int col) {
     	if (showselected && (col == 0)) {
	    return !getPackageAtRow(row).isRequired();
     	}
     	return false;
    }

    /*
     * We route all changes to the profile through dsp and get it to do all
     * the work, to avoid duplicating the same code here.
     */
    @Override
    public void setValueAt(Object value, int row, int col) {
	if (showselected && (col == 0)) {
	    Boolean b = (Boolean) value;
	    if (dsp != null) {
		System.out.println("YAYAYA");
		if (b) {
		    dsp.addRecursively(getPackageAtRow(row));
		} else {
		    dsp.removeRecursively(getPackageAtRow(row));
		}
	    }
	}
    }

    /**
     * Return the package at the selected row.
     *
     * @param row the row to select
     *
     * @return the SolarisPackage on the given row
     */
    public SolarisPackage getPackageAtRow(int row) {
	return pkgs.get(row);
    }
}
