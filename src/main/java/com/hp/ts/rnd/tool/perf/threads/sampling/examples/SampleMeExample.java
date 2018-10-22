package com.hp.ts.rnd.tool.perf.threads.sampling.examples;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import com.hp.ts.rnd.tool.perf.threads.calltree.ExtStackCallEltTreeNode;
import com.hp.ts.rnd.tool.perf.threads.calltree.ExtStackCallEltTreeNodePrinter;
import com.hp.ts.rnd.tool.perf.threads.calltree.FlameGraphGenerator;
import com.hp.ts.rnd.tool.perf.threads.calltree.ThreadDumpToCallTreeAccumulator;
import com.hp.ts.rnd.tool.perf.threads.dump.jvm.JvmThreadSamplerFactory;
import com.hp.ts.rnd.tool.perf.threads.model.ExtThreadsDump;
import com.hp.ts.rnd.tool.perf.threads.sampling.ThreadSampler;
import com.hp.ts.rnd.tool.perf.threads.sampling.ThreadSamplingException;
import com.hp.ts.rnd.tool.perf.threads.sampling.ThreadSamplingHandler;
import com.hp.ts.rnd.tool.perf.threads.sampling.util.SimpleThreadSamplingService;

public class SampleMeExample implements Runnable {

	private Thread thread;

	public SampleMeExample() {
	}

	public void start() {
		thread = new Thread(this);
		thread.start();
	}

	public void stop() {
		thread.interrupt(); // TODO TOCHANGE!
		try {
			thread.join();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	public void run() {
		ThreadSampler threadDumpSampler = new JvmThreadSamplerFactory().getSampler();
		
		int samplingDurationSeconds = 20;
		int samplingPeriodMillis = 10;
		SimpleThreadSamplingService service = new SimpleThreadSamplingService(threadDumpSampler, samplingDurationSeconds, samplingPeriodMillis);

		ExtStackCallEltTreeNode callTree = new ExtStackCallEltTreeNode(null, ExtStackCallEltTreeNode.ROOT_TRACE_ELEMENT);
		ThreadDumpToCallTreeAccumulator callTreeAccumulator = new ThreadDumpToCallTreeAccumulator(callTree);

		ThreadSamplingHandler handler = new ThreadSamplingHandler() {
			@Override
			public void onSampling(ExtThreadsDump state) {
				callTreeAccumulator.addThreadSampling(state);
			}
			@Override
			public void onError(ThreadSamplingException exception) throws ThreadSamplingException {
			}
			@Override
			public void onEnd() {
			}
		};
		
		try {
			service.executeSampling(handler);
		} finally {
			service.closeSampling();
		}

		ExtStackCallEltTreeNodePrinter printer = new ExtStackCallEltTreeNodePrinter();
		printer.print(System.out, callTree);
		
		FlameGraphGenerator flameGraphGenerator = new FlameGraphGenerator();
		OutputStream unfoldOut = null;
		try { 
			unfoldOut = new BufferedOutputStream(new FileOutputStream(new File("flamegraph-unfold.txt")));
			flameGraphGenerator.printUnfoldFlameGraph(callTree, unfoldOut);
		} catch(IOException ex) {
			throw new RuntimeException("failed", ex);
		} finally {
			try {
				unfoldOut.close();
			} catch (IOException e) {
			}
		}
	}

}
