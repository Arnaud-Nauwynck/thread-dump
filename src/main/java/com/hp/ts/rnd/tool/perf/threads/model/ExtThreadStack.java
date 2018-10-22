package com.hp.ts.rnd.tool.perf.threads.model;

import java.beans.ConstructorProperties;
import java.io.Serializable;
import java.lang.Thread.State;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * data model for a dump of a Thread
 *  
 */
public class ExtThreadStack implements Serializable {

	private static final long serialVersionUID = 1L;

	private final String threadName;

	private final long threadId;
	
	private Thread.State threadState;
	
	private String detailState;
	
	private List<ExtStackTraceElement> stackFrames;

// cf from JStack thread dump
	private boolean daemon;
	private int priority;
	private long nid;
	
	
// TODO cf from java.lang.management.ThreadInfo	
//    private long         blockedTime;
//    private long         blockedCount;
//    private long         waitedTime;
//    private long         waitedCount;
//    private LockInfo     lock;
//    private String       lockName;
//    private long         lockOwnerId;
//    private String       lockOwnerName;
//    private boolean      inNative;
//    private boolean      suspended;
//    private MonitorInfo[]       lockedMonitors;
//    private LockInfo[]          lockedSynchronizers;

	
	private Map<String, Object> extraData;

	// --------------------------------------------------------------------------------------------
	
	@ConstructorProperties({ "threadId", "threadName", "threadState", "detailState", "stackFrames", "extraData" })
	public ExtThreadStack(
			long threadId, String threadName, 
			State threadState,
			String detailState,
			List<ExtStackTraceElement> stackFrames,
			Map<String, Object> extraData) {
		this.threadId = threadId;
		this.threadName = threadName;
		this.threadState = threadState;
		this.detailState = detailState;
		this.stackFrames = stackFrames;
		this.extraData = (extraData != null)? new HashMap<String,Object>(extraData) : null;;
	}

	// --------------------------------------------------------------------------------------------
	
	public long getThreadId() {
		return threadId;
	}

	public String getThreadName() {
		return threadName;
	}

	public State getThreadState() {
		return threadState;
	}
	
	public String getDetailState() {
		return detailState;
	}

	
	public boolean isDaemon() {
		return daemon;
	}

	public void setDaemon(boolean daemon) {
		this.daemon = daemon;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public long getNid() {
		return nid;
	}

	public void setNid(long nid) {
		this.nid = nid;
	}

	public List<ExtStackTraceElement> getStackFrames() {
		return stackFrames;
	}
	
	
	
	public Map<String, Object> getExtraData() {
		return extraData;
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
		toString(builder);
		return builder.toString();
	}
	
	public void toString(StringBuilder builder) {
		builder.append('"').append(threadName).append('"');
		if (threadId != 0) {
			builder.append(" id=").append(threadId);
		}
		builder.append('\n');
		if (threadState != null) {
			builder.append("   java.lang.Thread.State: ").append(threadState);
			if (detailState != null && detailState.length() > 0) {
				builder.append(" (").append(detailState).append(")");
			}
			builder.append('\n');
		}
		for (ExtStackTraceElement elt : stackFrames) {
			elt.toString(builder);
			builder.append('\n');
		}
	}

}
