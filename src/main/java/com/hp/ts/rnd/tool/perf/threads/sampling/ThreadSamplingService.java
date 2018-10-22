package com.hp.ts.rnd.tool.perf.threads.sampling;

public interface ThreadSamplingService {

	public void executeSampling(ThreadSamplingHandler handler)
			throws ThreadSamplingException;

	public void closeSampling();

}
