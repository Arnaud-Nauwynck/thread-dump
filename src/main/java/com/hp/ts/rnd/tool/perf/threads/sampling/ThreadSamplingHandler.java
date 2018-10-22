package com.hp.ts.rnd.tool.perf.threads.sampling;

import com.hp.ts.rnd.tool.perf.threads.model.ExtThreadsDump;

public interface ThreadSamplingHandler {

	public void onSampling(ExtThreadsDump state);

	public void onError(ThreadSamplingException exception)
			throws ThreadSamplingException;

	public void onEnd();

}
