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
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.event.*;
import java.util.Vector;

/**
 * Show a list of patches. There's a list of patches on the left, with
 * information about that patch on the right.
 * @author Peter Tribble
 * @version 1.0
 */
public class PatchPanel extends JPanel {

    private PatchInformationPanel pip;

    /**
     * Create a new PatchPanel.
     *
     * @param pkglist a PkgList
     */
    public PatchPanel(PkgList pkglist) {
	PatchList patchlist = new PatchList(pkglist);
	setLayout(new BorderLayout());
	JList jplistpanel = new JList(
			new Vector <SolarisPatch> (patchlist.getPatches()));
	jplistpanel.addMouseListener(mouseListener);
	jplistpanel.addKeyListener(keyListener);
	pip = new PatchInformationPanel();
	JSplitPane psplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
		new JScrollPane(jplistpanel), pip);
	psplit.setOneTouchExpandable(true);
	psplit.setDividerLocation(120);
	add(psplit);
    }

    MouseListener mouseListener = new MouseAdapter() {
	public void mouseClicked(MouseEvent e) {
	    setPatchInfo((JList) e.getSource());
	}
    };

    KeyListener keyListener = new KeyAdapter() {
	public void keyReleased(KeyEvent e) {
	    setPatchInfo((JList) e.getSource());
	}
    };

    private void setPatchInfo(JList source) {
	Cursor c = getCursor();
	setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
	pip.display((SolarisPatch) source.getSelectedValue());
	setCursor(c);
    }
}
