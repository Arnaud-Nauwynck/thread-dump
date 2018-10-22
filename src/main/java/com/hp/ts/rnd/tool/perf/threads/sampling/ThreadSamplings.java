package com.hp.ts.rnd.tool.perf.threads.sampling;

import java.io.IOException;
import java.io.PrintStream;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.management.remote.JMXServiceURL;

import com.hp.ts.rnd.tool.perf.threads.calltree.ExtStackCallEltTreeNode;
import com.hp.ts.rnd.tool.perf.threads.calltree.ExtStackCallEltTreeNodePrinter;
import com.hp.ts.rnd.tool.perf.threads.calltree.ThreadDumpToCallTreeAccumulator;
import com.hp.ts.rnd.tool.perf.threads.dump.jmx.JmxThreadSamplerFactory;
import com.hp.ts.rnd.tool.perf.threads.dump.jstack.JstackThreadSamplerFactory;
import com.hp.ts.rnd.tool.perf.threads.dump.jvm.JvmThreadSamplerFactory;
import com.hp.ts.rnd.tool.perf.threads.dump.weblogic.WLSJmxThreadSamplerFactory;
import com.hp.ts.rnd.tool.perf.threads.model.ExtThreadsDump;
import com.hp.ts.rnd.tool.perf.threads.sampling.util.CompositeThreadSamplingHandler;
import com.hp.ts.rnd.tool.perf.threads.sampling.util.ScheduledThreadSamplingService;
import com.hp.ts.rnd.tool.perf.threads.sampling.util.SimpleThreadSamplingService;

/*
 * The facade class to sampler factory, sampling service
 */
final public class ThreadSamplings {

	/* Factory */

	public static ThreadSamplerFactory createWLSJmxThreadSamplerFactory(
			String hostport, String username, String password) {
		return new WLSJmxThreadSamplerFactory(hostport, username, password);
	}

	public static ThreadSamplerFactory createJvmThreadSamplerFactory() {
		return new JvmThreadSamplerFactory();
	}

	public static ThreadSamplerFactory createJmxThreadSamplerFactory(
			JMXServiceURL jmxURL, String[] userInfo) throws IOException {
		return new JmxThreadSamplerFactory(jmxURL, userInfo);
	}

	public static ThreadSamplerFactory createJstackThreadSamplerFactory(int pid) {
		return new JstackThreadSamplerFactory(pid);
	}

//	public static ThreadSamplerFactory createDiskReplayThreadSamplerFactory(
//			InputStream input) throws IOException {
//		return new DiskStoreThreadSamplerReplay(input);
//	}
//
//	public static ProxyThreadSamplerFactoryBuilder createProxyFactoryBuilder(
//			MBeanServerConnection mbsc) {
//		return new ProxyThreadSamplerFactoryBuilder(mbsc);
//	}

	public static ThreadSamplerFactory createHandlerPassThroughFactory(
			final ThreadSamplerFactory factory,
			final ThreadSamplingHandler handler) {
		return new ThreadSamplerFactory() {

			private ThreadSampler sampler;
			private AtomicBoolean unprocessEnd = new AtomicBoolean(true);

			@Override
			public ThreadSampler getSampler() throws ThreadSamplingException {
				if (sampler == null) {
					final ThreadSampler interanlSampler = factory.getSampler();
					sampler = new ThreadSampler() {

						@Override
						public ExtThreadsDump sampling()
								throws ThreadSamplingException,
								EndOfSamplingException {
							try {
								ExtThreadsDump sampling = interanlSampler
										.sampling();
								handler.onSampling(sampling);
								return sampling;
							} catch (EndOfSamplingException e) {
								if (unprocessEnd.getAndSet(false)) {
									handler.onEnd();
								}
								throw e;
							} catch (ThreadSamplingException e) {
								handler.onError(e);
								throw e;
							}
						}
					};
				}
				return sampler;
			}

			@Override
			public void close() {
				factory.close();
				if (unprocessEnd.getAndSet(false)) {
					handler.onEnd();
				}
			}
		};
	}

	/* Sampling Service */

	public static ThreadSamplingService createSimpleSamplingService(
			ThreadSampler sampler, int samplingDurationSeconds,
			int samplingPeriodMillis) {
		return new SimpleThreadSamplingService(sampler,
				samplingDurationSeconds, samplingPeriodMillis);
	}

	public static ThreadSamplingService createScheduledSamplingService(
			ThreadSampler sampler, ScheduledExecutorService executor,
			int samplingDurationSeconds, int samplingPeriodMillis) {
		return new ScheduledThreadSamplingService(sampler, executor,
				samplingDurationSeconds, samplingPeriodMillis);
	}

//	public static ThreadSamplingService createDiskReplaySamplingService(
//			InputStream input) throws IOException {
//		return new DiskStoreThreadSamplerReplay(input);
//	}

	/* Service Handler */

//	public static ThreadSamplingHandler createDiskStoreSamplingHandler(
//			DataOutput output) throws IOException {
//		return new ThreadSamplingWriter(output);
//	}

	public static ThreadSamplingHandler createChainedSamplingHandler(
			ThreadSamplingHandler... handlers) {
		return new CompositeThreadSamplingHandler(handlers);
	}

	public static ThreadSamplingHandler createCallTree(final PrintStream out) {
		final ExtStackCallEltTreeNode callTree = new ExtStackCallEltTreeNode(null, ExtStackCallEltTreeNode.ROOT_TRACE_ELEMENT);
		final ThreadDumpToCallTreeAccumulator callTreeAccumulator = new ThreadDumpToCallTreeAccumulator(callTree);
		
		return new ThreadSamplingHandler() {
			@Override
			public void onSampling(ExtThreadsDump state) {
				callTreeAccumulator.addThreadSampling(state);
			}

			@Override
			public void onError(ThreadSamplingException exception) {
			}

			@Override
			public void onEnd() {
				ExtStackCallEltTreeNodePrinter printer = new ExtStackCallEltTreeNodePrinter();
				printer.print(out, callTree);
			}
		};
	}

}
