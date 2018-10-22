package com.hp.ts.rnd.tool.perf.threads.dump.weblogic;

import java.io.BufferedReader;
import java.io.IOException;
import java.lang.Thread.State;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.hp.ts.rnd.tool.perf.threads.model.ExtStackTraceElement;
import com.hp.ts.rnd.tool.perf.threads.model.ExtThreadStack;
import com.hp.ts.rnd.tool.perf.threads.model.ExtThreadStackLockInfo;
import com.hp.ts.rnd.tool.perf.threads.model.ExtThreadsDump;

public class WLSThreadStackDumpParser {

	private enum ThreadParseState {
		StartThread,

		ThreadLine(
				"^\"(.*)\" (waiting for lock ([^@]+)@([0-9a-f]+) )?(WAITING|RUNNABLE|BLOCKED|TIMED_WAITING)(.*)$"),

		StackFrame("\t(.+)\\.([^(]+)\\((.+)\\)"),

		EndThread;

		private Pattern pattern;

		private ThreadParseState() {
		}

		private ThreadParseState(String pattern) {
			this.pattern = Pattern.compile(pattern);
		}

		public Pattern getPattern() {
			return pattern;
		}
	}

	private static ThreadParseState[] ThreadParseStates = ThreadParseState.values();

	private boolean ignoreSamplingThread;
	
	public WLSThreadStackDumpParser(boolean ignoreSamplingThread) {
		this.ignoreSamplingThread = ignoreSamplingThread;
	}

	public ExtThreadsDump parseThreadDump(long samplingTime, long nanos, BufferedReader reader) throws IOException {
		ExtThreadStack threadStackTrace;
		List<ExtThreadStack> threads = new ArrayList<>();
		while ((threadStackTrace = nextThreadStackTrace(reader)) != null) {
			if (ignoreSamplingThread && isSamplingStackTrace(threadStackTrace)) {
				continue;
			}
			threads.add(threadStackTrace);
		}
		return new ExtThreadsDump(samplingTime, null, nanos, threads, null);
	}

	
	public ExtThreadStack nextThreadStackTrace(BufferedReader reader) throws IOException {
		boolean threadAlreadySeen = false;
		String threadName = null;
		long threadId = 0;
		State threadState = null;
		String detailState = null;
		String lockClassName = null;
		int lockHashIdentifier = 0;
		List<ExtThreadStackLockInfo> lockInfos = null;
		List<ExtStackTraceElement> stackTraceElts = new ArrayList<>();
		
		BitSet possibleStates = new BitSet();
		possibleStates.set(ThreadParseState.StartThread.ordinal());
		ThreadParseState[] states = ThreadParseStates;
		StringBuilder history = new StringBuilder();
		
		String line;
		NEXTLINE: while ((line = reader.readLine()) != null) {
			history.append(line).append('\n');
			int nextState;
			while ((nextState = possibleStates.nextSetBit(0)) >= 0) {
				possibleStates.clear(nextState);
				ThreadParseState state = states[nextState];
				Matcher matcher;
				switch (state) {
				case StartThread:
					if (threadAlreadySeen) {
						throw new IllegalStateException("unexpect state: "
								+ nextState + "\nHistory:\n" + history);
					}
					possibleStates.set(ThreadParseState.ThreadLine.ordinal());
					threadAlreadySeen = true;
					break;
				case ThreadLine:
					matcher = state.getPattern().matcher(line);
					if (matcher.matches()) {
						threadName = matcher.group(1);
						if (matcher.group(3) != null) {
							lockClassName = matcher.group(3);
						}
						if (matcher.group(4) != null) {
							lockHashIdentifier = Integer.parseInt(matcher.group(4), 16);
						}
						threadState = Thread.State.valueOf(matcher.group(5));
						detailState = matcher.group(6);
						
						possibleStates.clear();
						possibleStates.set(ThreadParseState.StackFrame.ordinal());
						possibleStates.set(ThreadParseState.EndThread.ordinal());
						continue NEXTLINE;
					} else {
						// End of threads
						return null;
					}
				case StackFrame:
					matcher = state.getPattern().matcher(line);
					if (matcher.matches()) {
						String className = matcher.group(1), methodName = matcher.group(2);
						String fileInfo = matcher.group(3);
						int lineNo = -1;
						if ("Unknown Source".equals(fileInfo)) {
							fileInfo = null;
							lineNo =  -2;
						} else {
							int lineNoIndex = fileInfo.lastIndexOf(':');
							if (lineNoIndex > 0) {
								try {
									lineNo = Integer.parseInt(fileInfo
											.substring(lineNoIndex + 1));
								} catch (NumberFormatException e) {
								}
								fileInfo = fileInfo.substring(0, lineNoIndex);
							}
						}
						
						ExtStackTraceElement traceElement = new ExtStackTraceElement(
								className, methodName, fileInfo, lineNo, null,
								lockInfos, null); 
						stackTraceElts.add(traceElement);
						
						if (lockInfos != null) {
							lockInfos = null;
						}
						possibleStates.clear();
						possibleStates.set(ThreadParseState.StackFrame.ordinal());
						possibleStates.set(ThreadParseState.EndThread.ordinal());
						continue NEXTLINE;
					}
					break;
				case EndThread:
					if (line.length() == 0 || line.equals("null")) {
						break NEXTLINE;
					} else {
						throw new IllegalStateException(
								"should be empty line on end thread state"
										+ "\nHistory:\n" + history);
					}
				default:
					throw new IllegalStateException("unknown state: "
							+ nextState + "\nHistory:\n" + history);
				}
			}
			throw new IllegalStateException("no next state process"
					+ "\nHistory:\n" + history);
		}
		
		// TODO 
//		String lockClassName = null;
//		int lockHashIdentifier = 0;

		return new ExtThreadStack(threadId, threadName, threadState, detailState, stackTraceElts, null);
	}

	

	private static boolean isSamplingStackTrace(ExtThreadStack thread) {
		// suppose we can find it in last 8 methods call
		int max = 8;
		for (ExtStackTraceElement elt : thread.getStackFrames()) {
			if (max-- < 0) {
				break;
			}
			if (elt.getClassName().endsWith("JVMRuntime")
					&& elt.getMethodName().equals("getThreadStackDump")) {
				return true;
			}
		}
		return false;
	}

}
