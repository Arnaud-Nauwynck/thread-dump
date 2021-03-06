package com.hp.ts.rnd.tool.perf.threads.dump.weblogic;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import com.hp.ts.rnd.tool.perf.threads.sampling.ThreadSampler;
import com.hp.ts.rnd.tool.perf.threads.sampling.ThreadSamplerFactory;
import com.hp.ts.rnd.tool.perf.threads.sampling.ThreadSamplingException;

public class WLSJmxThreadSamplerFactory implements ThreadSamplerFactory {

	private String hostport;

	private String username;

	private String password;

	private WLSJmxThreadSampler sampler;
	
	private JMXConnector jmxConnector;
	
	private boolean ignoreSamplingThread = true;

	public WLSJmxThreadSamplerFactory(String hostport, String username,
			String password) {
		this.hostport = hostport;
		this.username = username;
		this.password = password;
	}
	
	public boolean isIgnoreSamplingThread() {
		return ignoreSamplingThread;
	}

	public void setIgnoreSamplingThread(boolean ignoreSamplingThread) {
		this.ignoreSamplingThread = ignoreSamplingThread;
	}

	@Override
	public ThreadSampler getSampler() throws ThreadSamplingException {
		if (sampler == null) {
			checkAccess();
			MBeanServerConnection mbsc;
			Set<ObjectName> objectNames;
			try {
				mbsc = jmxConnector.getMBeanServerConnection();
				objectNames = mbsc.queryNames(
						ObjectName.getInstance("com.bea:Type=JVMRuntime,*"),
						null);
			} catch (IOException e) {
				throw new ThreadSamplingException(e);
			} catch (MalformedObjectNameException e) {
				throw new RuntimeException(e);
			}
			if (objectNames.size() != 1) {
				throw new ThreadSamplingException(
						"expect JVMRuntime mbean, but get: " + objectNames);
			}
			Iterator<ObjectName> iterator = objectNames.iterator();
			sampler = new WLSJmxThreadSampler(mbsc, iterator.next(), new WLSThreadStackDumpParser(ignoreSamplingThread));
		}
		return sampler;
	}

	private JMXConnector createWLJMXConnector() {
		String serviceURL = "service:jmx:t3://" + hostport
				+ "/jndi/weblogic.management.mbeanservers.runtime";
		try {
			JMXServiceURL jmxURL = new JMXServiceURL(serviceURL);

			Map<String, Object> env = new HashMap<String, Object>();
			env.put(JMXConnector.CREDENTIALS,
					new String[] { username, password });
			env.put(JMXConnectorFactory.PROTOCOL_PROVIDER_PACKAGES,
					"weblogic.management.remote");
			return JMXConnectorFactory.connect(jmxURL, env);
		} catch (IOException e) {
			throw new ThreadSamplingException(
					"connect to weblogic jmx url fail: " + serviceURL, e);
		}
	}

	public void checkAccess() {
		if (jmxConnector == null) {
			jmxConnector = createWLJMXConnector();
		}
	}

	@Override
	public void close() {
		if (jmxConnector != null) {
			try {
				jmxConnector.close();
			} catch (IOException ignored) {
			}
			jmxConnector = null;
			sampler = null;
		}
	}

}
