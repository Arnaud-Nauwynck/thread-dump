package com.hp.ts.rnd.tool.perf.threads.calltree;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ExtStackCallEltTreeNodePrinter {

	private static final String INDENT = " ";


	public void print(PrintStream out, ExtStackCallEltTreeNode item) {
		print(0, "", item, out);
	}

	private void print(int level, String prefix,
			ExtStackCallEltTreeNode item, PrintStream out) {
		printTreeNode(prefix, item, out);
		Collection<ExtStackCallEltTreeNode> children = listTreeChildren(level, item);
		int i = 0;
		int n = children.size();
		boolean printChildren = false;
		level++;
		for (ExtStackCallEltTreeNode child : children) {
			String np = prefix + INDENT.substring(0, INDENT.length() - 1) + (i < n - 1 ? "|" : " ");
			print(level, np, child, out);
			printChildren = true;
			i++;
		}
		if (!printChildren) {
			printTreeNode(prefix + INDENT, null, out);
		}
	}

	protected Collection<ExtStackCallEltTreeNode> listTreeChildren(
			int level, ExtStackCallEltTreeNode item) {
		Collection<ExtStackCallEltTreeNode> list = item.listChildren();
		List<ExtStackCallEltTreeNode> children = new ArrayList<ExtStackCallEltTreeNode>();
		if (level == 0) {
			for (ExtStackCallEltTreeNode child : list) {
				if (child.hasChildren()) {
					children.add(child);
				}
			}
		} else {
			children.addAll(list);
		}
		// TODO 
//		Collections.sort(children,
//				new Comparator<ExtStackCallEltTreeNode>() {
//					public int compare(ExtStackCallEltTreeNode t1, ExtStackCallEltTreeNode t2) {
//						ExtStackTraceElement elt1 = t1.getKey(), elt2 = t2.getKey();
//						if (elt1 instanceof Long) {
//							return Long.compare((Long) elt1, (Long) elt2);
//						} else {
//							return -Long.compare(t1.getValue().count, t2.getValue().count);
//						}
//					}
//				});
		return children;
	}

	protected void printTreeNode(String prefix,
			ExtStackCallEltTreeNode item, PrintStream out) {
		if (prefix.length() > 0) {
			out.print(prefix);
			if (item != null) {
				out.print("\\- ");
			}
		}
		if (item != null) {
			CallCount callCount = item.getValue();
			if (callCount != null && callCount.name != null) {
				out.print(callCount.name + " " + callCount.count);
			}
		}
		out.println();
	}

	public void printFilter(ExtStackCallEltTreeNode node, PrintStream out, ExtStackCallTreeNodeFilter filter) {
		if (filter != null) {
			ExtStackCallEltTreeNode newRoot = new ExtStackCallEltTreeNode(null, ExtStackCallEltTreeNode.ROOT_TRACE_ELEMENT);
			copyFilterNode(newRoot, node, filter, false);
			node = newRoot;
		}
		print(0, "", node, out);
	}

	private boolean copyFilterNode(ExtStackCallEltTreeNode newNode,
			ExtStackCallEltTreeNode node, ExtStackCallTreeNodeFilter filter,
			boolean acceptDownstream) {
		boolean acceptNode = acceptDownstream;
		for (ExtStackCallEltTreeNode child : node.listChildren()) {
			if (!filter.acceptNode(child)) {
				// not accept this child node and subs
				continue;
			} else {
				Boolean acceptTrace = filter.acceptTrace(child);
				if (acceptTrace == null) {
					ExtStackCallEltTreeNode newChild = newNode.getChild(child.getKey(), child.getStackTraceElt(), true);
					newChild.setValue(child.getValue());
					boolean accept = copyFilterNode(newChild, child, filter, acceptDownstream);
					if (accept) {
						acceptNode = true;
					} else {
						newNode.removeChild(child.getKey());
					}
				} else if (acceptTrace.booleanValue()) {
					ExtStackCallEltTreeNode newChild = newNode.getChild(child.getKey(), child.getStackTraceElt(), true);
					newChild.setValue(child.getValue());
					copyFilterNode(newChild, child, filter, true);
					acceptNode = true;
				} else {
					acceptNode = false;
					continue;
				}
			}
		}
		return acceptNode;
	}
}
