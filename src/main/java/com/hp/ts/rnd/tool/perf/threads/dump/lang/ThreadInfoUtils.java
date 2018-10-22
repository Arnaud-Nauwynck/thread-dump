package com.hp.ts.rnd.tool.perf.threads.dump.lang;

import java.lang.management.ThreadInfo;

public class ThreadInfoUtils {


	public static boolean isSunManagementThread(ThreadInfo threadInfo) {
		// suppose we can find it in last 3 methods call
		int max = 3;
		for (StackTraceElement elt : threadInfo.getStackTrace()) {
			if (max-- < 0) {
				break;
			}
			if (elt.getClassName().equals("sun.management.ThreadImpl")
					&& elt.getMethodName().equals("dumpAllThreads")) {
				return true;
			}
		}
		return false;
	}
}
