package com.hp.ts.rnd.tool.perf.threads.model;

import java.beans.ConstructorProperties;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 
 */
public class ExtThreadStackLockInfo implements Serializable {

	private static final long serialVersionUID = 1L;

	private String lockState;

	private String details;
	
	private boolean ownLock;

	private long lockIdentityHashCode;

	private String lockClassName;

	private Map<String,Object> extraData;

	// --------------------------------------------------------------------------------------------
	
	@ConstructorProperties({ "lockClassName", "lockIdentityHashCode", "lockState", "details", "ownLock", "extraData" })
	public ExtThreadStackLockInfo(String lockClassName, long lockIdentityHashCode,
			String lockState, String details, boolean ownLock,
			Map<String,Object> extraData) {
		this.lockClassName = lockClassName;
		this.lockIdentityHashCode = lockIdentityHashCode;
		this.lockState = lockState;
		this.details = details;
		this.ownLock = ownLock;
		this.extraData = (extraData != null)? new HashMap<String,Object>(extraData) : null;
	}

	public String getLockState() {
		return lockState;
	}

	public String getDetails() {
		return details;
	}
	
	public boolean isOwnLock() {
		return ownLock;
	}

	public long getLockIdentityHashCode() {
		return lockIdentityHashCode;
	}

	public String getLockClassName() {
		return lockClassName;
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
		builder.append(getLockState());
		if (lockIdentityHashCode != 0) {
			builder.append(" <0x");
			String idString = String.valueOf(Long
					.toHexString(getLockIdentityHashCode()));
			for (int i = idString.length(); i < 16; i++) {
				builder.append('0');
			}
			builder.append(idString);
			builder.append("> (a ");
			builder.append(getLockClassName());
			builder.append(")");
		} else {
			builder.append(" <");
			builder.append(lockClassName);
			builder.append(">");
		}
	}

}
