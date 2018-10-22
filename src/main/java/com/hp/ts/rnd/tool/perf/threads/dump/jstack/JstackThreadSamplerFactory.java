package com.hp.ts.rnd.tool.perf.threads.dump.jstack;

import java.io.IOException;

import com.hp.ts.rnd.tool.perf.threads.dump.jvm.VirtualMachineUtils;
import com.hp.ts.rnd.tool.perf.threads.sampling.ThreadSampler;
import com.hp.ts.rnd.tool.perf.threads.sampling.ThreadSamplerFactory;
import com.hp.ts.rnd.tool.perf.threads.sampling.ThreadSamplingException;

public class JstackThreadSamplerFactory implements ThreadSamplerFactory {

	private int pid;
	private Object vm;
	private JstackThreadSampler sampler;
	protected JstackOutputParser parser = new JstackOutputParser();

	public JstackThreadSamplerFactory(int pid) {
		this.pid = pid;
	}

	@Override
	public ThreadSampler getSampler() throws ThreadSamplingException {
		if (sampler == null) {
			try {
				checkAccess();
			} catch (IOException e) {
				throw new ThreadSamplingException(e);
			}
			sampler = new JstackThreadSampler(vm, parser);
		}
		return sampler;
	}

	@Override
	public void close() {
		if (vm != null) {
			try {
				VirtualMachineUtils.detachJvm(vm);
			} catch (IOException e) {
				// ignored
			}
			vm = null;
			sampler = null;
		}
	}

	public void checkAccess() throws IOException {
		if (vm == null) {
			vm = VirtualMachineUtils.attachJvm(pid);
		}
	}

}
