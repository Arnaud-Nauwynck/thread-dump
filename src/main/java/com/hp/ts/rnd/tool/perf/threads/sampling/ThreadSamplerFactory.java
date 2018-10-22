package com.hp.ts.rnd.tool.perf.threads.sampling;

public interface ThreadSamplerFactory {

	public ThreadSampler getSampler() throws ThreadSamplingException;

	public void close();

}
