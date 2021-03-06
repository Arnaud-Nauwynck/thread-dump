package com.hp.ts.rnd.tool.perf.threads.sampling;

public class EndOfSamplingException extends ThreadSamplingException {

	private static final long serialVersionUID = -4231104958147072283L;

	public EndOfSamplingException() {
		super();
	}

	public EndOfSamplingException(String message) {
		super(message);
	}

	public EndOfSamplingException(String message, Throwable cause) {
		super(message, cause);
	}

	public EndOfSamplingException(Throwable cause) {
		super(cause);
	}

}
