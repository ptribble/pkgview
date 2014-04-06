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

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;

/**
 * Adds tooltips and custom icons to a cluster/package tree.
 *
 * @author Peter Tribble
 * @version 1.0
 */
public class ClusterTreeCellRenderer extends DefaultTreeCellRenderer {

    public static final int ICON_INSTALLED = 1;
    public static final int ICON_SELECTED = 2;

    private static ImageIcon noneIcon;
    private static ImageIcon selectedIcon;
    private static ImageIcon partIcon;
    private static ImageIcon forcedIcon;
    private static ImageIcon partforcedIcon;
    private static ImageIcon selectedpartforcedIcon;

    private int icontype;

    public ClusterTreeCellRenderer(int icontype) {
	this.icontype = icontype;
	initIcons();
    }

    public Component getTreeCellRendererComponent(
						  JTree tree,
						  Object value,
						  boolean sel,
						  boolean expanded,
						  boolean leaf,
						  int row,
						  boolean hasFocus) {

	super.getTreeCellRendererComponent(
					   tree, value, sel,
					   expanded, leaf, row,
					   hasFocus);
	Object o = ((DefaultMutableTreeNode) value).getUserObject();
	if (leaf) {
	    /*
	     * It would be nice if the description were more than the name.
	     */
	    SolarisPackage p = (SolarisPackage) o;
	    setToolTipText(p.getDescription());
	    if ((icontype == ICON_SELECTED) && p.isSelected()) {
		setIcon(p.isRequired() ? forcedIcon : selectedIcon);
	    } else if ((icontype == ICON_INSTALLED) && p.exists()) {
		setIcon(p.isRequired() ? forcedIcon : selectedIcon);
	    } else {
		setIcon(noneIcon);
	    }
	} else {
	    if (o instanceof PackageCluster) {
		PackageCluster pc = (PackageCluster) o;
		setToolTipText(pc.getDescription());
		if (icontype > 0) {
		    int istat = (icontype == ICON_SELECTED)
			? pc.selectedStatus() : pc.installedStatus();
		    if (istat == PackageCluster.FULLY_INSTALLED) {
			if (pc.isRequired()) {
			    setIcon(forcedIcon);
			} else if (pc.isPartiallyRequired()) {
			    setIcon(selectedpartforcedIcon);
			} else {
			    setIcon(selectedIcon);
			}
		    } else if (istat == PackageCluster.NOT_INSTALLED) {
			setIcon(noneIcon);
		    } else {
			setIcon(pc.isPartiallyRequired() ? partforcedIcon
				: partIcon);
		    }
		}
	    } else {
		setToolTipText(null);
	    }
	}
	return this;
    }

    /*
     * Absolute paths, otherwise they get resolved relative to this
     * class itself which is deep down in the hierarchy.
     */
    private void initIcons() {
	noneIcon = createImageIcon("/images/none.png");
	selectedIcon = createImageIcon("/images/selected.png");
	partIcon = createImageIcon("/images/part.png");
	forcedIcon = createImageIcon("/images/forced.png");
	partforcedIcon = createImageIcon("/images/partforced.png");
	selectedpartforcedIcon =
	    createImageIcon("/images/selectedpartforced.png");
    }

    /*
     * Based on the Java Swing tutorial examples.
     */
    private ImageIcon createImageIcon(String s) {
	java.net.URL imgURL = getClass().getResource(s);
	return (imgURL == null) ? null : new ImageIcon(imgURL);
    }
}
