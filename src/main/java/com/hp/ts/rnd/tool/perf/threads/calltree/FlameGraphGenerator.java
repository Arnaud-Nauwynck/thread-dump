package com.hp.ts.rnd.tool.perf.threads.calltree;

import java.io.OutputStream;

/**
 * cf https://github.com/brendangregg/FlameGraph
 *  Capture stacks
 *  Fold stacks
 *  flamegraph.pl
 *  
 * Part to "fold stacks":  * cf  https://github.com/brendangregg/FlameGraph/blob/master/stackcollapse-jstack.pl
 * <PRE>
 * #  capture stacks:
 * i=0; while (( i++ < 200 )); do jstack PID >> out.jstacks; sleep 10; done
 * # Fold stacks
 * cat out.jstacks | ./stackcollapse-jstack.pl > out.stacks-folded
 * # Flamegraph
 * ./flamegraph.pl out.stacks-folded > stacks.svg
 * </PRE>
 *
 */
public class FlameGraphGenerator {

	public void printUnfoldFlameGraph(ExtStackCallEltTreeNode callTree, OutputStream out) {
		// TODO 
		
	}

}
