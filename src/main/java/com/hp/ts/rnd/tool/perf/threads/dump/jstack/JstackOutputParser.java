package com.hp.ts.rnd.tool.perf.threads.dump.jstack;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.Thread.State;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.hp.ts.rnd.tool.perf.threads.model.ExtStackTraceElement;
import com.hp.ts.rnd.tool.perf.threads.model.ExtThreadStack;
import com.hp.ts.rnd.tool.perf.threads.model.ExtThreadStackLockInfo;
import com.hp.ts.rnd.tool.perf.threads.model.ExtThreadsDump;

/**
 * 
 */
public class JstackOutputParser {

	private static enum ThreadParseState {
		StartThread,

		ThreadLine(
				"^\"(.*)\" (?:#[0-9]+ )?(daemon )?prio=([0-9]+) (?:os_prio=-?[0-9]+ )?tid=(0x[0-9a-f]+) nid=(0x[0-9a-f]+) (.*)$"),

		ThreadState(" +java\\.lang\\.Thread\\.State: ([^ ]+)(?: \\((.+)\\))?"),

		StackFrame("\tat (.+)\\.([^(]+)\\((.+)\\)"),

		StackLock("\t- (.*) <(0x[0-9a-f]+)> \\(a (.+)\\)"),

		StackLockUnavailable("\t- (.*) <([^0-9]+)>"),

		Compiling(" +(No compile task|Compiling: .*)"),

		EndThread;

		private final Pattern pattern;

		private ThreadParseState() {
			this.pattern = null;
		}

		private ThreadParseState(String pattern) {
			this.pattern = (pattern != null)? Pattern.compile(pattern) : null;
		}

		public Pattern getPattern() {
			return pattern;
		}
	}

	// guarded by synchronized
	private static final SimpleDateFormat DateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

	private static final ThreadParseState[] ThreadParseStates = ThreadParseState.values();

	public JstackOutputParser() {
	}
	
	public ExtThreadsDump parseThreadDump(InputStream input, long samplingTime, long nanos) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(input), 256 * 1024);

		Date dumpTime = readDumpTime(reader);
		if (!readTillEmptyLine(reader)) {
			throw new IllegalStateException("expect empty line");
		}

		List<ExtThreadStack> threads = new ArrayList<>();
		ExtThreadStack thread;
		while ((thread = parseThreadStack(reader)) != null) {
			threads.add(thread);
		}
		return new ExtThreadsDump(samplingTime, dumpTime, nanos, threads, null);
	}
		
	
	private Date readDumpTime(BufferedReader reader) throws IOException {
		Date sampleTime;
		String dumpTimeLine = reader.readLine();
		if (dumpTimeLine == null) {
			throw new IllegalStateException("expect dump data time line");
		}
		try {
			synchronized (DateFormat) {
				sampleTime = DateFormat.parse(dumpTimeLine);
			}
		} catch (ParseException e) {
			throw new IllegalStateException("parse dump date time error: "
					+ dumpTimeLine, e);
		}
		return sampleTime;
	}

	private boolean readTillEmptyLine(BufferedReader reader) throws IOException {
		String line;
		while ((line = reader.readLine()) != null) {
			if (line.length() == 0) {
				return true;
			}
		}
		return false;
	}

	public ExtThreadStack parseThreadStack(BufferedReader reader) throws IOException {
		boolean threadAlreadySeen = false;
		String threadName = null;
		long threadId = 0;
		State threadState = null;
		String detailState = null;
		boolean daemon = false;
		int priority = 0;
		long nid = 0;
		String status = null;
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
						threadAlreadySeen = true;
						threadName = matcher.group(1);
						daemon = matcher.group(2) != null;
						priority = Integer.parseInt(matcher.group(3));
						threadId = Long.parseLong(matcher.group(4).substring(2), 16);
						nid = Long.parseLong(matcher.group(5).substring(2), 16);
						status = matcher.group(6);
						possibleStates.clear();
						possibleStates.set(ThreadParseState.ThreadState.ordinal());
						possibleStates.set(ThreadParseState.EndThread.ordinal());
						continue NEXTLINE;
					} else {
						// End of threads
						return null;
					}
				case ThreadState:
					matcher = state.getPattern().matcher(line);
					if (matcher.matches()) {
						threadState = Thread.State.valueOf(matcher.group(1));
						detailState = matcher.group(2);
						possibleStates.clear();
						possibleStates.set(ThreadParseState.StackFrame.ordinal());
						possibleStates.set(ThreadParseState.Compiling.ordinal());
						possibleStates.set(ThreadParseState.EndThread.ordinal());
						continue NEXTLINE;
					}
					break;
				case Compiling:
					matcher = state.getPattern().matcher(line);
					if (matcher.matches()) {
						possibleStates.clear();
						possibleStates.set(ThreadParseState.EndThread.ordinal());
						continue NEXTLINE;
					}
					break;
				case StackFrame:
					matcher = state.getPattern().matcher(line);
					if (matcher.matches()) {
						String fileInfo = matcher.group(3);
						int lineNoIndex = fileInfo.lastIndexOf(':');
						int lineNo = -1;
						if (fileInfo.equals("Native Method")) {
							lineNo = -2;
						} else {
							if (lineNoIndex > 0) {
								try {
									lineNo = Integer.parseInt(fileInfo.substring(lineNoIndex + 1));
								} catch (NumberFormatException e) {
								}
								fileInfo = fileInfo.substring(0, lineNoIndex);
							}
						}
						String fileName = (lineNo == -1 && "Unknown Source".equals(fileInfo)) ? null : fileInfo;
						String stackFrameId = null; //?
						ExtStackTraceElement stackTraceElement = new ExtStackTraceElement(
								matcher.group(1), matcher.group(2), fileName, lineNo, stackFrameId, null, null);
						stackTraceElts.add(stackTraceElement);
						
						possibleStates.clear();
						possibleStates.set(ThreadParseState.StackFrame.ordinal());
						possibleStates.set(ThreadParseState.StackLock.ordinal());
						possibleStates.set(ThreadParseState.StackLockUnavailable.ordinal());
						possibleStates.set(ThreadParseState.EndThread.ordinal());
						continue NEXTLINE;
					}
					break;
				case StackLock:
					matcher = state.getPattern().matcher(line);
					if (matcher.matches()) {
						String lockState = matcher.group(1);
						long lockIdentityHashCode = Long.parseLong(matcher.group(2).substring(2), 16);
						String lockClassName = matcher.group(3);
						boolean ownLock = "locked".equals(lockState);
						ExtThreadStackLockInfo lockInfo = new ExtThreadStackLockInfo(
								lockClassName, lockIdentityHashCode, lockState, null,
								ownLock, null);
						stackTraceElts.get(stackTraceElts.size()-1).addLockInfo(lockInfo);
						
						possibleStates.clear();
						possibleStates.set(ThreadParseState.StackFrame.ordinal());
						possibleStates.set(ThreadParseState.StackLock.ordinal());
						possibleStates.set(ThreadParseState.StackLockUnavailable.ordinal());
						possibleStates.set(ThreadParseState.EndThread.ordinal());
						continue NEXTLINE;
					}
					break;
				case StackLockUnavailable:
					matcher = state.getPattern().matcher(line);
					if (matcher.matches()) {
						String lockState = matcher.group(1);
						String lockClassName = matcher.group(2);
						boolean ownLock = "locked".equals(lockState);
						ExtThreadStackLockInfo lockInfo = new ExtThreadStackLockInfo(lockClassName, 0, lockState, null,
								ownLock, null);
						stackTraceElts.get(stackTraceElts.size()-1).addLockInfo(lockInfo);

						possibleStates.clear();
						possibleStates.set(ThreadParseState.StackFrame.ordinal());
						possibleStates.set(ThreadParseState.StackLock.ordinal());
						possibleStates.set(ThreadParseState.StackLockUnavailable.ordinal());
						possibleStates.set(ThreadParseState.EndThread.ordinal());
						continue NEXTLINE;
					}
					break;
				case EndThread:
					if (line.length() == 0) {
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
//		boolean daemon = false;
//		int priority = 0;
//		long nid = 0;
//		String status = null;
		
		return new ExtThreadStack(threadId, threadName, threadState, detailState, stackTraceElts, null);
	}
}
