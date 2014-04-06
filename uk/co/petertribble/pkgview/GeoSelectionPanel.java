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
import java.util.*;

/**
 * Interactively select geographic localisations for a jumpstart profile.
 *
 * @author Peter Tribble
 * @version 1.0
 */
public class GeoSelectionPanel extends JPanel {

    private Map <String, JCheckBoxMenuItem> geoMap;

    public GeoSelectionPanel() {
	setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
	initGeos();
    }

    /*
     * Picked out of the documentation, in essentially arbitrary order.
     */
    private void initGeos() {
	geoMap = new HashMap <String, JCheckBoxMenuItem> ();
	addGeo("N_America", "North America");
	addGeo("C_America", "Central America");
	addGeo("S_America", "South America");
	addGeo("W_Europe", "Western Europe");
	addGeo("C_Europe", "Central Europe");
	addGeo("N_Europe", "Northern Europe");
	addGeo("S_Europe", "Southern Europe");
	addGeo("E_Europe", "Eastern Europe");
	addGeo("M_East", "Middle East");
	addGeo("N_Africa", "Northern Africa");
	addGeo("Asia", "Asia");
	addGeo("Ausi", "Australasia");
    }

    private void addGeo(String geo, String text) {
	JCheckBoxMenuItem jcbi = new JCheckBoxMenuItem(text);
	add(jcbi);
	geoMap.put(geo, jcbi);
    }

    /**
     * Clear the selection so that no geos are selected.
     */
    public void clearSelection() {
	for (String s : geoMap.keySet()) {
	    geoMap.get(s).setSelected(false);
	}
    }

    /**
     * Return the selected geos.
     *
     * @return a Set of Strings representing the geos that have been selected.
     *
     */
    public Set <String> selectedGeos() {
	Set <String> geos = new TreeSet <String> ();
	for (String s : geoMap.keySet()) {
	    if (geoMap.get(s).isSelected()) {
		geos.add(s);
	    }
	}
	return geos;
    }
}
