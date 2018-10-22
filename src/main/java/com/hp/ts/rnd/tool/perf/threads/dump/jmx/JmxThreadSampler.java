package com.hp.ts.rnd.tool.perf.threads.dump.jmx;

import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;

import com.hp.ts.rnd.tool.perf.threads.dump.lang.JavaLangMgtThreadStackTraceParser;
import com.hp.ts.rnd.tool.perf.threads.model.ExtThreadsDump;
import com.hp.ts.rnd.tool.perf.threads.sampling.ThreadSampler;

/**
 * ThreadSampler implementation using ThreadMXBean.dumpAllthread() 
 */
public class JmxThreadSampler implements ThreadSampler {

	private ThreadMXBean threadMXBean;

	private boolean cachedObjMonitorUsageSupported; // = threadMXBean.isObjectMonitorUsageSupported();
	private boolean cachedSynchronizerUsageSupported; // = threadMXBean.isSynchronizerUsageSupported();
	
	private JavaLangMgtThreadStackTraceParser threadStackParser;
	
	public JmxThreadSampler(ThreadMXBean threadMXBean, boolean ignoreSunManagementThread) {
		this.threadMXBean = threadMXBean;
		this.cachedObjMonitorUsageSupported = threadMXBean.isObjectMonitorUsageSupported();
		this.cachedSynchronizerUsageSupported = threadMXBean.isSynchronizerUsageSupported();
		this.threadStackParser = new JavaLangMgtThreadStackTraceParser(ignoreSunManagementThread);
	}

	public ExtThreadsDump sampling() {
		long samplingTime = System.currentTimeMillis();
		
		long startNanos = System.nanoTime();
		// *** perform JMX Thread Dump ***
		ThreadInfo[] threadInfos = threadMXBean.dumpAllThreads(cachedObjMonitorUsageSupported, cachedSynchronizerUsageSupported);
		long nanos = System.nanoTime() - startNanos;
		
		return threadStackParser.parseExtThreadDump(samplingTime, null, nanos, threadInfos);
	}

}
