package com.hp.ts.rnd.tool.perf.threads.sampling;

import com.hp.ts.rnd.tool.perf.threads.model.ExtThreadsDump;

public interface ThreadSampler {

	public ExtThreadsDump sampling() throws ThreadSamplingException,
			EndOfSamplingException;

}
