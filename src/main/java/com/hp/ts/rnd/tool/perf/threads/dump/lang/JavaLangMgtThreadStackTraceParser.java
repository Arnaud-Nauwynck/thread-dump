package com.hp.ts.rnd.tool.perf.threads.dump.lang;

import java.lang.management.LockInfo;
import java.lang.management.MonitorInfo;
import java.lang.management.ThreadInfo;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.hp.ts.rnd.tool.perf.threads.model.ExtStackTraceElement;
import com.hp.ts.rnd.tool.perf.threads.model.ExtThreadStack;
import com.hp.ts.rnd.tool.perf.threads.model.ExtThreadStackLockInfo;
import com.hp.ts.rnd.tool.perf.threads.model.ExtThreadsDump;

/**
 * parser for java.lang.management.ThreadInfo / java.lang.management. 
 *
 */
public class JavaLangMgtThreadStackTraceParser {

	private boolean ignoreSunManagementThread;
	
	public JavaLangMgtThreadStackTraceParser(boolean ignoreSunManagementThread) {
		this.ignoreSunManagementThread = ignoreSunManagementThread;
	}

	public ExtThreadsDump parseExtThreadDump(long samplingTime, Date remoteDumpTime, long nanos, ThreadInfo[] threadInfos) {
		List<ExtThreadStack> threads = parseExtThreadStacks(threadInfos);
		return new ExtThreadsDump(samplingTime, remoteDumpTime, nanos, threads, null);
	}
	
	public List<ExtThreadStack> parseExtThreadStacks(ThreadInfo[] threadInfos) {
		final int threadCount = threadInfos.length;
		List<ExtThreadStack> threads = new ArrayList<ExtThreadStack>(threadCount);
		for (int i = 0; i < threadCount; i++) {
			ThreadInfo threadInfo = threadInfos[i];
			if (ignoreSunManagementThread && ThreadInfoUtils.isSunManagementThread(threadInfo)) {
				continue;
			}
			ExtThreadStack threadRes = parseExtThreadStack(threadInfo);
			if (threadRes != null) {
				threads.add(threadRes);
			}
		}
		return threads;
	}

	/**
	 * parse java.lang.management.ThreadInfo -> ExtThreadStack
	 */
	public ExtThreadStack parseExtThreadStack(ThreadInfo src) {
		StackTraceElement[] stackTrace = src.getStackTrace();
		List<ExtStackTraceElement> stackFrames = new ArrayList<ExtStackTraceElement>(stackTrace.length);
		ExtThreadStackLockInfo extTopLock = parseLockInfo(src);
		for (int i = 0, n = stackTrace.length; i < n; i++) {
			StackTraceElement srcElt = stackTrace[i];
			List<ExtThreadStackLockInfo> locks = new ArrayList<>();
			if (i == 0 && extTopLock != null) {
				locks.add(extTopLock);
			}
			for (MonitorInfo mi : src.getLockedMonitors()) {
				if (mi.getLockedStackDepth() == i) {
					locks.add(new ExtThreadStackLockInfo(mi.getClassName(), mi.getIdentityHashCode(), 
							"locked", null, true, null));
				}
			}
			stackFrames.add(new ExtStackTraceElement(srcElt.getClassName(), srcElt.getMethodName(),
					srcElt.getFileName(), srcElt.getLineNumber(), 
					null, // stackFrameId,
					locks, null));
		}
		
		String detailState = (extTopLock != null)? extTopLock.getDetails() : null;
		return new ExtThreadStack(src.getThreadId(), src.getThreadName(), src.getThreadState(), detailState, stackFrames, null);
	}

	/**
	 * parse java.lang.management.ThreadInfo.lockInfo -> ExtThreadStackLockInfo
	 */
	public ExtThreadStackLockInfo parseLockInfo(ThreadInfo src) {
		LockInfo lockInfo = src.getLockInfo();
		if (lockInfo == null) {
			return null;
		}
		String lockState;
		String detailState = "on object monitor";
		// boolean parking = false;
		if (StackTraceElementUtils.isUnsafePark(src.getStackTrace()[0])) {
			// parking = true;
			lockState = "parking to wait for ";
			detailState = "parking";
		} else {
			switch (src.getThreadState()) {
			case BLOCKED:
				lockState = "blocked on";
				break;
			case WAITING:
				lockState = "waiting on";
				break;
			case TIMED_WAITING:
				lockState = "waiting on";
				break;
			default:
				lockState = null;
				break;
			}
		}
		return new ExtThreadStackLockInfo(
				lockInfo.getClassName(), lockInfo.getIdentityHashCode(), lockState, detailState, false, null);
	}

}
