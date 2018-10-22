package com.hp.ts.rnd.tool.perf.threads.dump.jvm;

import java.lang.Thread.State;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.hp.ts.rnd.tool.perf.threads.dump.lang.StackTraceElementUtils;
import com.hp.ts.rnd.tool.perf.threads.model.ExtStackTraceElement;
import com.hp.ts.rnd.tool.perf.threads.model.ExtThreadStack;
import com.hp.ts.rnd.tool.perf.threads.model.ExtThreadsDump;

public class JvmThreadParser {

	boolean ignoreSamplingThread;

	public JvmThreadParser(boolean ignoreSamplingThread) {
		this.ignoreSamplingThread = ignoreSamplingThread;
	}

	public ExtThreadsDump parseThreadDump(long samplingTime, long nanos, Map<Thread, StackTraceElement[]> stacks) {
		List<ExtThreadStack> threads = new ArrayList<>(stacks.size());
		for (Entry<Thread, StackTraceElement[]> entry : stacks.entrySet()) {
			if (ignoreSamplingThread && StackTraceElementUtils.isDumpThreadStackTrace(entry.getValue())) {
				continue;
			}
			threads.add(parseThread(entry.getKey(), entry.getValue()));
		}
		return new ExtThreadsDump(samplingTime, null, nanos, threads, null);
	}

	private ExtThreadStack parseThread(Thread thread, StackTraceElement[] stackTraceElts) {
		List<ExtStackTraceElement> stackFrames = new ArrayList<>();
		for(StackTraceElement elt : stackTraceElts) {
			stackFrames.add(new ExtStackTraceElement(elt.getClassName(), elt.getMethodName(), elt.getFileName(), 
					elt.getLineNumber(), null, // stackFrameId, 
					null, // lockInfos,
					null));
		}
		return new ExtThreadStack(thread.getId(), thread.getName(), thread.getState(), null, // detailState,
				stackFrames, null);
	}
}
