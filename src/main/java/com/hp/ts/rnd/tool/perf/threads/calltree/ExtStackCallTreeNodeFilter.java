package com.hp.ts.rnd.tool.perf.threads.calltree;

public interface ExtStackCallTreeNodeFilter {

	public boolean acceptNode(ExtStackCallEltTreeNode item);

	// true: keep the trace (up and all down), otherwise ignore the trace
	public Boolean acceptTrace(ExtStackCallEltTreeNode item);

}