package com.hp.ts.rnd.tool.perf.threads.model;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * data model for a full stack ThreadDump
 * 
 */
public class ExtThreadsDump {

	private long samplingTime;
	
	private Date remoteDumpTime;
	
	private long durationTimeNanos;

	private List<ExtThreadStack> threads;

	private Map<String, Object> extraData;

	// --------------------------------------------------------------------------------------------
	
	public ExtThreadsDump(long samplingTime, Date remoteDumpTime, long durationTimeNanos, List<ExtThreadStack> threads, Map<String, Object> extraData) {
		this.samplingTime = samplingTime;
		this.remoteDumpTime = (remoteDumpTime != null)? remoteDumpTime : new Date(samplingTime);
		this.durationTimeNanos = durationTimeNanos;
		this.threads = threads;
		this.extraData = (extraData != null)? new HashMap<String,Object>(extraData) : null;;
	}

	// --------------------------------------------------------------------------------------------
	
	public long getSamplingTime() {
		return samplingTime;
	}
	
	public Date getRemoteDumpTime() {
		return remoteDumpTime;
	}

	public long getDurationTimeNanos() {
		return durationTimeNanos;
	}

	public void setDurationTimeNanos(long durationTimeNanos) {
		this.durationTimeNanos = durationTimeNanos;
	}

	public List<ExtThreadStack> getThreads() {
		return threads;
	}

	@SuppressWarnings("unchecked")
	public <T> T getExtraData(String key) {
		if (extraData == null) {
			return null;
		}
		return (T) extraData.get(key);
	}

	@SuppressWarnings("unchecked")
	public <T> T putExtraData(String key, T value) {
		if (extraData == null) {
			extraData = new HashMap<String,Object>();
		}
		return (T) extraData.put(key, value);
	}

	// --------------------------------------------------------------------------------------------
	
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("\n\n");
		for (ExtThreadStack thread : threads) {
			builder.append(thread);
			builder.append("\n");
		}
		builder.append("Sampling at: " + new Date(samplingTime)
				+ ", in: " + TimeUnit.NANOSECONDS.toMillis(durationTimeNanos)
				+ " ms");
		return builder.toString();
	}

}
