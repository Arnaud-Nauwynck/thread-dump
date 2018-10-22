package com.hp.ts.rnd.tool.perf.threads.dump.jvm;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.hp.ts.rnd.tool.perf.threads.dump.lang.StackTraceElementUtils;
import com.hp.ts.rnd.tool.perf.threads.model.ExtThreadStack;
import com.hp.ts.rnd.tool.perf.threads.model.ExtThreadsDump;
import com.hp.ts.rnd.tool.perf.threads.sampling.ThreadSampler;

public class JvmThreadSampler implements ThreadSampler {

	private JvmThreadParser parser;
	
	JvmThreadSampler(JvmThreadParser parser) {
		this.parser = parser;
	}

	public ExtThreadsDump sampling() {
		long samplingTime = System.currentTimeMillis();
		long startNanos = System.nanoTime();
		// *** perform self JVM Thread Dump ***
		Map<Thread, StackTraceElement[]> allStackTraces = Thread.getAllStackTraces();
		long nanos = System.nanoTime() - startNanos;

		return parser.parseThreadDump(samplingTime, nanos, allStackTraces);
	}

}
