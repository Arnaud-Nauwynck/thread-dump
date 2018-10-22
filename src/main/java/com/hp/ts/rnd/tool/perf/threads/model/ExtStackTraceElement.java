package com.hp.ts.rnd.tool.perf.threads.model;

import java.beans.ConstructorProperties;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * data model for 1 method element of a Thread dump
 * extension of java.lang.StackTraceElement .. but java.lang class is final!
 *
 */
public class ExtStackTraceElement implements Serializable {

	private static final long serialVersionUID = 1L;

	private final String fileName;

	private final int lineNumber;

	private final String className;

	private final String methodName;

	private final String stackFrameId;

	private List<ExtThreadStackLockInfo> lockInfos;
	
	private Map<String,Object> extraData;

	// example of extraData..
	// private ClassPackagingData classPackagingData; // cf logback StackTraceElementProxy
	
	// --------------------------------------------------------------------------------------------
	
	@ConstructorProperties({ "className", "methodName", "fileName", "lineNumber", "stackFrameId", "lockInfos", "extraData" })
	public ExtStackTraceElement (
			String className, String methodName,
			String fileName, int lineNumber, String stackFrameId,
			List<ExtThreadStackLockInfo> lockInfos,
			Map<String,Object> extraData) {
		this.className = className;
		this.methodName = methodName;
		this.fileName = fileName;
		this.lineNumber = lineNumber;
		this.stackFrameId = stackFrameId;
		this.lockInfos = lockInfos;
		this.extraData = (extraData != null)? new HashMap<String,Object>(extraData) : null; 
	}

	public String getFileName() {
		return fileName;
	}

	public int getLineNumber() {
		return lineNumber;
	}

	public String getClassName() {
		return className;
	}

	public String getMethodName() {
		return methodName;
	}

	public String getStackFrameId() {
		return stackFrameId;
	}

	// rename toStackTraceElement()
	public StackTraceElement toTraceElement() {
		return new StackTraceElement(className, methodName, fileName, lineNumber);
	}
	
	public List<ExtThreadStackLockInfo> getLockInfos() {
		return lockInfos;
	}
	

	public void addLockInfo(ExtThreadStackLockInfo lockInfo) {
		if (lockInfos == null) {
			lockInfos = new ArrayList<>();
		}
		lockInfos.add(lockInfo);
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

	public void locksInfoToString(StringBuilder builder) {
		if (lockInfos != null) {
			for (ExtThreadStackLockInfo lockInfo : lockInfos) {
				builder.append("\n\t- ");
				builder.append(lockInfo);
			}
		}
	}

	public void toString(StringBuilder builder) {
		// TODO !!
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		toString(builder);
		return builder.toString();
	}

}
