package com.hp.ts.rnd.tool.perf.threads.calltree;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import com.hp.ts.rnd.tool.perf.threads.model.ExtStackTraceElement;

public class ExtStackCallEltTreeNode {

	public static final ExtStackTraceElement ROOT_TRACE_ELEMENT = new ExtStackTraceElement("", "", "", 0, "", null, null);
	
	public static ExtStackTraceElement createRootThreadIdElement(long threadId, String threadName) {
		String idOrName = (threadId != 0)? "" + threadId : threadName;
		return new ExtStackTraceElement(idOrName, "", "", 0, "", null, null);
	}

	private final Object key;
	
	private final ExtStackTraceElement stackTraceElt;

	private CallCount value;

	private Map<Object, ExtStackCallEltTreeNode> children;

	public ExtStackCallEltTreeNode(Object key, ExtStackTraceElement stackTraceElt) {
		this.key = key;
		this.stackTraceElt = stackTraceElt;
	}

	public Object getKey() {
		return this.key;
	}

	public ExtStackTraceElement getStackTraceElt() {
		return this.stackTraceElt;
	}

	public ExtStackCallEltTreeNode getChild(Object key, ExtStackTraceElement stackTraceElt, boolean createIfNotFound) {
		if (children == null) {
			if (!createIfNotFound) {
				return null;
			} else {
				children = new LinkedHashMap<>();
			}
		}
		ExtStackCallEltTreeNode child = children.get(key);
		if (child == null) {
			if (!createIfNotFound) {
				return null;
			} else {
				child = new ExtStackCallEltTreeNode(key, stackTraceElt);
				children.put(key, child);
				return child;
			}
		} else {
			return child;
		}
	}

	public Collection<ExtStackCallEltTreeNode> listChildren() {
		if (children == null) {
			return Collections.emptyList();
		}
		return children.values();
	}

	public CallCount getValue() {
		return value;
	}

	public void setValue(CallCount value) {
		this.value = value;
	}

	public boolean hasChildren() {
		return children != null && !children.isEmpty();
	}

	public void removeChild(Object key) {
		if (children != null) {
			children.remove(key);
		}
	}

}