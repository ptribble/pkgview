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

import javax.swing.JTree;
import javax.swing.tree.*;
import java.util.*;

/**
 * A tree structure representing the hierarchical structure of metaclusters.
 * clusters, and packages, as defined in the .clustertoc file.
 * @author Peter Tribble
 * @version 1.0
 */
public class ClusterTree extends JTree {

    private DefaultTreeModel model;
    private Map <SolarisPackage, NodeSet> pkgmap;
    private Map <PackageCluster, NodeSet> clstrmap;

    /**
     * Create a tree from a clustertoc file and add it to the parent node.
     *
     * @param ctoc A parsed cluster table of contents
     */
    public ClusterTree(ClusterToc ctoc) {
	this(ctoc, (String) null);
    }

    /**
     * Create a tree from a clustertoc file and add it to the parent node.
     *
     * @param ctoc A parsed cluster table of contents
     * @param cluster The metacluster to show. If null, show all metaclusters
     */
    public ClusterTree(ClusterToc ctoc, String cluster) {
	DefaultMutableTreeNode topmenu = new DefaultMutableTreeNode(
				(cluster == null) ? "Solaris" : cluster);
	model = new DefaultTreeModel(topmenu);
	setModel(model);
	pkgmap = new HashMap <SolarisPackage, NodeSet> ();
	clstrmap = new HashMap <PackageCluster, NodeSet> ();
	if (cluster == null) {
	    for (String s : ctoc.getMetaClusterNames()) {
		DefaultMutableTreeNode mitem = new DefaultMutableTreeNode(s);
		topmenu.add(mitem);
		addMC(mitem, ctoc.getMetaCluster(s));
	    }
	} else {
	    MetaCluster mc = ctoc.getMetaCluster(cluster);
	    if (mc != null) {
		addMC(topmenu, mc);
	    }
	}
    }

    public void nodeChanged(SolarisPackage pkg) {
	NodeSet ns = pkgmap.get(pkg);
	if (ns != null) {
	    for (DefaultMutableTreeNode node : ns.getNodes()) {
		model.nodeChanged(node);
	    }
	}
    }

    public void nodeChanged(PackageCluster pc) {
	NodeSet ns = clstrmap.get(pc);
	if (ns != null) {
	    for (DefaultMutableTreeNode node : ns.getNodes()) {
		model.nodeChanged(node);
	    }
	}
    }

    private void addMC(DefaultMutableTreeNode node, MetaCluster mc) {
	for (PackageCluster pc : mc.getClusters()) {
	    node.add(addNode(pc));
	}
	for (SolarisPackage pkg : mc.getPackages()) {
	    node.add(addNode(pkg));
	}
    }

    private DefaultMutableTreeNode addNode(PackageCluster pc) {
	DefaultMutableTreeNode mitem = new DefaultMutableTreeNode(pc);
	for (SolarisPackage pkg : pc.getPackages()) {
	    mitem.add(addNode(pkg));
	}
	NodeSet ns = clstrmap.get(pc);
	if (ns == null) {
	    ns = new NodeSet();
	    clstrmap.put(pc, ns);
	}
	ns.add(mitem);
	return mitem;
    }

    private DefaultMutableTreeNode addNode(SolarisPackage pkg) {
	DefaultMutableTreeNode node = new DefaultMutableTreeNode(pkg);
	NodeSet ns = pkgmap.get(pkg);
	if (ns == null) {
	    ns = new NodeSet();
	    pkgmap.put(pkg, ns);
	}
	ns.add(node);
	return node;
    }

    class NodeSet {
	private Set <DefaultMutableTreeNode> nodes;
	public NodeSet() {
	    nodes = new HashSet <DefaultMutableTreeNode> ();
	}
	public void add(DefaultMutableTreeNode node) {
	    nodes.add(node);
	}
	public Set <DefaultMutableTreeNode> getNodes() {
	    return nodes;
	}
    }
}
