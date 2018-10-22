package com.hp.ts.rnd.tool.perf.threads.dump.jstack;

import java.io.IOException;
import java.io.InputStream;

import com.hp.ts.rnd.tool.perf.threads.dump.jvm.VirtualMachineUtils;
import com.hp.ts.rnd.tool.perf.threads.model.ExtThreadsDump;
import com.hp.ts.rnd.tool.perf.threads.sampling.ThreadSampler;
import com.hp.ts.rnd.tool.perf.threads.sampling.ThreadSamplingException;

public class JstackThreadSampler implements ThreadSampler {

	private Object vm;
	protected JstackOutputParser parser;

	public JstackThreadSampler(Object vm, JstackOutputParser parser) {
		this.vm = vm;
		this.parser = parser;
	}

	public ExtThreadsDump sampling() {
		InputStream input = null;
		long samplingTime = System.currentTimeMillis();
		long startNanos = System.nanoTime();
		try {
			// *** perform VM Thread Dump ***
			input = VirtualMachineUtils.remoteDataDump(vm);
		} catch(IOException | RuntimeException e) {
			throw new ThreadSamplingException(e);
		}
		long nanos = System.nanoTime() - startNanos;

		if (input == null) {
			return null;
		}
		ExtThreadsDump res;
		try {
			res = parser.parseThreadDump(input, samplingTime, nanos);
		} catch (IOException | RuntimeException e) {
			throw new ThreadSamplingException(e);
		} finally {
			try {
				input.close();
			} catch (IOException e) {
				// ignore!
			}
		}
		return res;
	}

}
