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
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import java.awt.BorderLayout;
import java.awt.event.*;
import uk.co.petertribble.jingle.JingleInfoFrame;

/**
 * Interactively construct a jumpstart profile.
 *
 * @author Peter Tribble
 * @version 1.0
 */
public class ProfileBuilder extends JFrame implements ActionListener {

    private DistributionSoftwarePanel cpanel;
    private GeoSelectionPanel gpanel;

    private JMenuItem exitItem;
    private JMenuItem resetItem;
    private JMenuItem helpItem;
    private JMenuItem licenseItem;
    private JButton confirmButton;
    private JButton useLocalButton;
    private JTabbedPane jtp;
    private JRadioButton dummyButton;

    private String useCluster;

    private static final int CHOOSE_TABID = 0;
    private static final int GEO_TABID = 1;
    private static final int PACKAGE_TABID = 2;
    private static final int PROFILE_TABID = 3;

    /**
     * Create a new ProfileBuilder.
     *
     * @param distdir the name of a directory containing a Solaris distribution
     */
    public ProfileBuilder(String distdir) {
	super("ProfileBuilder");

	addWindowListener(new winExit());

	jtp = new JTabbedPane();
	setContentPane(jtp);

	JMenu jme = new JMenu(PkgResources.getString("FILE.TEXT"));
	jme.setMnemonic(KeyEvent.VK_F);
	resetItem = new JMenuItem(PkgResources.getString("JS.RESET"),
				KeyEvent.VK_R);
	resetItem.addActionListener(this);
	resetItem.setEnabled(false);
	exitItem = new JMenuItem(PkgResources.getString("FILE.EXIT.TEXT"),
				KeyEvent.VK_X);
	exitItem.addActionListener(this);
	jme.add(resetItem);
	jme.add(exitItem);

	JMenu jmh = new JMenu(PkgResources.getString("HELP.TEXT"));
	jmh.setMnemonic(KeyEvent.VK_H);
	helpItem = new JMenuItem(PkgResources.getString("HELP.ABOUT.TEXT")
				+ " pkgview", KeyEvent.VK_A);
	helpItem.addActionListener(this);
	jmh.add(helpItem);
	licenseItem = new JMenuItem(PkgResources.getString("HELP.LICENSE.TEXT"),
				KeyEvent.VK_L);
	licenseItem.addActionListener(this);
	jmh.add(licenseItem);

	JMenuBar jm = new JMenuBar();
	jm.add(jme);
	jm.add(jmh);
	setJMenuBar(jm);

	PkgList plist = new PkgList(distdir);
	ClusterToc ctoc = new ClusterToc(distdir, plist);

	PackageProfile profile = new PackageProfile(ctoc);

	JPanel mp = new JPanel(new BorderLayout());

	/*
	 * The tabs are used for workflow, hence the need to specify the index.
	 */
	jtp.insertTab(PkgResources.getString("JS.TAB.MC"), (Icon) null, mp,
		(String) null, CHOOSE_TABID);

	gpanel = new GeoSelectionPanel();
	jtp.insertTab(PkgResources.getString("JS.TAB.GEO"), (Icon) null, gpanel,
		(String) null, GEO_TABID);
	profile.setgeo(gpanel);

	cpanel = new DistributionSoftwarePanel(ctoc, plist, profile);
	jtp.insertTab(PkgResources.getString("JS.TAB.PKG"), (Icon) null, cpanel,
		(String) null, PACKAGE_TABID);

	final ProfileDisplayPanel pdp = new ProfileDisplayPanel(profile);
	jtp.insertTab(PkgResources.getString("JS.TAB.OUT"), (Icon) null,
		pdp, (String) null, PROFILE_TABID);

	/*
	 * Add a change listener so that, when the profile display tab is
	 * selected, the profile is automatically displayed.
	 */
	jtp.addChangeListener(new ChangeListener() {
	    public void stateChanged(ChangeEvent ce) {
		JTabbedPane jtpc = (JTabbedPane) ce.getSource();
		if (jtpc.getSelectedIndex() == PROFILE_TABID) {
		    pdp.doDisplay();
		}
	    }
	});

	/*
	 * Initially the selection and output screens are disabled,
	 * and remain so until the user selects a starting metacluster.
	 */
	jtp.setEnabledAt(PACKAGE_TABID, false);
	jtp.setEnabledAt(GEO_TABID, false);
	jtp.setEnabledAt(PROFILE_TABID, false);

	setSize(720, 540);
	setVisible(true);

	/*
	 * Now we start initialising.
	 */
	mp.add(new JLabel(PkgResources.getString("JS.MSG.SEL")),
		BorderLayout.NORTH);

	JPanel buttonPanel = new JPanel();
	buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.PAGE_AXIS));
	ButtonGroup bg = new ButtonGroup();
	for (String s : ctoc.getMetaClusterNames()) {
	    JRadioButton jb = new JRadioButton(s);
	    jb.addActionListener(this);
	    bg.add(jb);
	    buttonPanel.add(jb);
	}
	/*
	 * This is to work around the stupidity of forcing at least one
	 * button to be selected - the javadoc for ButtonGroup suggests this
	 * particular workaround.
	 */
	dummyButton = new JRadioButton();
	bg.add(dummyButton);

	mp.add(new JScrollPane(buttonPanel));

	JPanel confirmPanel = new JPanel();
	confirmButton = new JButton(PkgResources.getString("JS.MSG.CUSTOM"));
	confirmButton.addActionListener(this);
	confirmButton.setEnabled(false);
	confirmPanel.add(confirmButton);
	String instcluster = InstalledCluster.getClusterName();
	if (instcluster != null) {
	    String sclust = PkgResources.getString("JS.MSG.USELOCAL") + " ("
				+ instcluster + ")";
	    useLocalButton = new JButton(sclust);
	    useLocalButton.addActionListener(this);
	    useLocalButton.setEnabled(true);
	    confirmPanel.add(useLocalButton);
	}
	mp.add(confirmPanel, BorderLayout.SOUTH);
	mp.validate();
    }

    static class winExit extends WindowAdapter {
	public void windowClosing(WindowEvent we) {
	    System.exit(0);
	}
    }

    private void reset() {
	jtp.setEnabledAt(PACKAGE_TABID, false);
	jtp.setEnabledAt(GEO_TABID, false);
	jtp.setEnabledAt(PROFILE_TABID, false);
	jtp.setSelectedIndex(CHOOSE_TABID);
	jtp.setEnabledAt(CHOOSE_TABID, true);
	/*
	 * Java 6 has clearSelection, but we use the dummmyButton we created
	 * earlier to clear the visible selection.
	 */
	dummyButton.setSelected(true);
	confirmButton.setEnabled(false);
	useCluster = null;
	resetItem.setEnabled(false);
	gpanel.clearSelection();
    }

    private void customize(String s) {
	cpanel.setMetaCluster(s);
	jtp.setEnabledAt(PACKAGE_TABID, true);
	jtp.setEnabledAt(GEO_TABID, true);
	jtp.setEnabledAt(PROFILE_TABID, true);
	jtp.setSelectedIndex(PACKAGE_TABID);
	jtp.setEnabledAt(CHOOSE_TABID, false);
	resetItem.setEnabled(true);
    }

    public void actionPerformed(ActionEvent e) {
	if (e.getSource() == confirmButton) {
	    customize(useCluster);
	} else if (e.getSource() == useLocalButton) {
	    customize(InstalledCluster.getClusterName());
	} else if (e.getSource() == resetItem) {
	    reset();
	} else if (e.getSource() == exitItem) {
	    System.exit(0);
	} else if (e.getSource() == helpItem) {
	    new JingleInfoFrame(this.getClass().getClassLoader(),
				"help/profile.html", "text/html");
	} else if (e.getSource() == licenseItem) {
	    new JingleInfoFrame(this.getClass().getClassLoader(),
				"help/CDDL.txt", "text/plain");
	} else if (e.getSource() instanceof JRadioButton) {
	    confirmButton.setEnabled(true);
	    useCluster = ((JRadioButton) e.getSource()).getText();
	}
    }

    /**
     * Run the application.
     *
     * @param args Command line arguments. One argument is expected, naming
     * the location of a Solaris distribution.
     */
    public static void main(String args[]) {
	if (args.length != 1) {
	    System.out.println("Usage: ProfileBuilder product_dir");
	    System.exit(1);
	}
	String sf = PkgUtils.findDist(args[0]);
	if (sf == null) {
	    System.out.println("Usage: ProfileBuilder product_dir");
	    System.exit(1);
	} else {
	    new ProfileBuilder(sf);
	}
    }
}
