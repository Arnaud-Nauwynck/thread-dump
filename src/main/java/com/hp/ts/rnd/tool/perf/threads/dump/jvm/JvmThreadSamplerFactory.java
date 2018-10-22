package com.hp.ts.rnd.tool.perf.threads.dump.jvm;

import com.hp.ts.rnd.tool.perf.threads.sampling.ThreadSampler;
import com.hp.ts.rnd.tool.perf.threads.sampling.ThreadSamplerFactory;

public class JvmThreadSamplerFactory implements ThreadSamplerFactory {

	private boolean ignoreSamplingThread = true;

	@Override
	public ThreadSampler getSampler() {
		return new JvmThreadSampler(new JvmThreadParser(ignoreSamplingThread));
	}

	public boolean isIgnoreSamplingThread() {
		return ignoreSamplingThread;
	}

	public void setIgnoreSamplingThread(boolean ignoreSamplingThread) {
		this.ignoreSamplingThread = ignoreSamplingThread;
	}

	@Override
	public void close() {
	}

}
