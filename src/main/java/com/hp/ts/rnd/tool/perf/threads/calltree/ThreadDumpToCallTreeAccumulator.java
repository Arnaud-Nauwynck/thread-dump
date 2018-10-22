package com.hp.ts.rnd.tool.perf.threads.calltree;

import java.util.List;

import com.hp.ts.rnd.tool.perf.threads.model.ExtStackTraceElement;
import com.hp.ts.rnd.tool.perf.threads.model.ExtThreadStack;
import com.hp.ts.rnd.tool.perf.threads.model.ExtThreadsDump;

public class ThreadDumpToCallTreeAccumulator {
	
	private ExtStackCallEltTreeNode callTree; // = new ExtStackCallEltTreeNode(null, ExtStackCallEltTreeNode.ROOT_TRACE_ELEMENT);
	
	public ThreadDumpToCallTreeAccumulator(ExtStackCallEltTreeNode callTree) {
		this.callTree = callTree;
	}

	public void addThreadSampling(ExtThreadsDump samplingState) {
		for (ExtThreadStack stackTrace : samplingState.getThreads()) {
			addThreadStackTrace(stackTrace);
		}
	}

	public void addThreadStackTrace(ExtThreadStack stackTrace) {
		long threadId = stackTrace.getThreadId();
		ExtStackTraceElement rootThreadElt = ExtStackCallEltTreeNode.createRootThreadIdElement(threadId, stackTrace.getThreadName());
		
		ExtStackCallEltTreeNode tree = callTree.getChild(rootThreadElt.getClassName(), rootThreadElt, true);
		CallCount callCount = tree.getValue();
		if (callCount == null) {
			callCount = new CallCount();
			tree.setValue(callCount);
			callCount.name = stackTrace.getThreadName();
		}
		callCount.count++;
		List<ExtStackTraceElement> stackFrames = stackTrace.getStackFrames();
		// from bottom to top
		for (int i = stackFrames.size() - 1; i >= 0; i--) {
			ExtStackTraceElement stackFrame = stackFrames.get(i);
			Object childId = stackFrame.getStackFrameId();
			if (childId == null) {
				childId = stackFrame.toTraceElement();
			}
			tree = tree.getChild(childId, stackFrame, true);
			callCount = tree.getValue();
			if (callCount == null) {
				callCount = new CallCount();
				tree.setValue(callCount);
				callCount.name = stackFrame;
			}
			callCount.count++;
		}
	}

}
