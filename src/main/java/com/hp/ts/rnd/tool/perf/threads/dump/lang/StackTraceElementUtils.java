package com.hp.ts.rnd.tool.perf.threads.dump.lang;

public class StackTraceElementUtils {

	public static boolean isUnsafePark(StackTraceElement e) {
		return "sun.misc.Unsafe".equals(e.getClassName()) && "park".equals(e.getMethodName());
	}

	// only support following
	// at java.lang.Thread.dumpThreads(Native Method)
	// at java.lang.Thread.getAllStackTraces(Thread.java:?)
	public static boolean isDumpThreadStackTrace(StackTraceElement[] elements) {
		if (elements.length > 3) {
			if (Thread.class.getName().equals(elements[0].getClassName())
					&& "dumpThreads".equals(elements[0].getMethodName())
					&& Thread.class.getName().equals(elements[1].getClassName())
					&& "getAllStackTraces".equals(elements[1].getMethodName())) {
				return true;
			}
		}
		return false;
	}
}
