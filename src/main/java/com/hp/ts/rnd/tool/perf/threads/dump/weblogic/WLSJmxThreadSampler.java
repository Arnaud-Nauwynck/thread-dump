package com.hp.ts.rnd.tool.perf.threads.dump.weblogic;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import com.hp.ts.rnd.tool.perf.threads.model.ExtThreadsDump;
import com.hp.ts.rnd.tool.perf.threads.sampling.ThreadSampler;
import com.hp.ts.rnd.tool.perf.threads.sampling.ThreadSamplingException;

class WLSJmxThreadSampler implements ThreadSampler {

	private MBeanServerConnection mbsc;
	private ObjectName jvmObjName;
	private WLSThreadStackDumpParser parser;

	public WLSJmxThreadSampler(MBeanServerConnection mbsc,
			ObjectName jvmObjName, 
			WLSThreadStackDumpParser parser) {
		this.mbsc = mbsc;
		this.jvmObjName = jvmObjName;
		this.parser = parser;
	}

	public ExtThreadsDump sampling() throws ThreadSamplingException {
		String threadDump;
		long samplingTime = System.currentTimeMillis();
		long nanos;
		try {
			long startNanos = System.nanoTime();
			// *** perform JMX Weblogic ThreadDump ***
			threadDump = (String) mbsc.getAttribute(jvmObjName, "ThreadStackDump");
			nanos = System.nanoTime() - startNanos;
		} catch(Exception ex) {
			throw new ThreadSamplingException(ex);
		}
		
		ExtThreadsDump res;
		BufferedReader reader = new BufferedReader(new StringReader(threadDump));
		try {
			res = parser.parseThreadDump(samplingTime, nanos, reader);
		} catch(IOException ex) {
			res = null; // should not occur
		}
		return res;
	}

}
