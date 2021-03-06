package com.hp.ts.rnd.tool.perf.threads.sampling.util;

import java.util.concurrent.TimeUnit;

import com.hp.ts.rnd.tool.perf.threads.model.ExtThreadsDump;
import com.hp.ts.rnd.tool.perf.threads.sampling.EndOfSamplingException;
import com.hp.ts.rnd.tool.perf.threads.sampling.ThreadSampler;
import com.hp.ts.rnd.tool.perf.threads.sampling.ThreadSamplingException;
import com.hp.ts.rnd.tool.perf.threads.sampling.ThreadSamplingHandler;
import com.hp.ts.rnd.tool.perf.threads.sampling.ThreadSamplingService;

public class SimpleThreadSamplingService implements ThreadSamplingService {

	private ThreadSampler sampler;

	private int samplingDurationSeconds;

	private int samplingPeriodMillis;

	private volatile Thread currentThread;

	private volatile boolean closed;

	public SimpleThreadSamplingService(ThreadSampler sampler,
			int samplingDurationSeconds, int samplingPeriodMillis) {
		this.sampler = sampler;
		this.samplingDurationSeconds = samplingDurationSeconds;
		this.samplingPeriodMillis = samplingPeriodMillis;
	}

	@Override
	public void executeSampling(ThreadSamplingHandler handler)
			throws ThreadSamplingException {
		closed = false;
		currentThread = Thread.currentThread();
		long samplingTime = samplingDurationSeconds <= 0 ? Long.MAX_VALUE
				: (System.nanoTime() + TimeUnit.SECONDS
						.toNanos(samplingDurationSeconds));
		try {
			while (!closed) {
				long inSampling = System.nanoTime();
				if (samplingTime < inSampling) {
					break;
				}
				try {
					ExtThreadsDump state = sampler.sampling();
					handler.onSampling(state);
				} catch (EndOfSamplingException e) {
					break;
				} catch (ThreadSamplingException e) {
					handler.onError(e);
				}
				inSampling = System.nanoTime() - inSampling;
				long waitSampling = TimeUnit.MILLISECONDS
						.toNanos(samplingPeriodMillis) - inSampling;
				try {
					TimeUnit.NANOSECONDS.sleep(waitSampling);
				} catch (InterruptedException e) {
					if (!closed) {
						Thread.currentThread().interrupt();
					}
					break;
				}
			}
		} finally {
			currentThread = null;
			closed = true;
			handler.onEnd();
		}
	}

	@Override
	public void closeSampling() {
		Thread thread = currentThread;
		currentThread = null;
		if (thread != null) {
			closed = true;
			thread.interrupt();
		}
	}

}
