package com.hp.ts.rnd.tool.perf.threads.dump.jvm;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class VirtualMachineUtils {

	private static Class<?> VirtualMachineClass;

	private static Class<?> loadVirtualMachineClass() {
		try {
			try {
				return Class.forName("com.sun.tools.attach.VirtualMachine");
			} catch (ClassNotFoundException cnfe) {
				File toolsJarFile = JavaHomeUtils.getToolsJarFile();
				try {
					if (toolsJarFile.exists()) {
						ClassLoader loader = new URLClassLoader(
								new URL[] { toolsJarFile.toURI().toURL() },
								VirtualMachineUtils.class.getClassLoader());
						Class<?> clz = loader
								.loadClass("com.sun.tools.attach.VirtualMachine");
						return clz;
					} else {
						throw cnfe;
					}
				} catch (MalformedURLException e) {
					throw new IllegalArgumentException(e);
				}
			}
		} catch (ClassNotFoundException e) {
			throw new NoClassDefFoundError(e.getMessage());
		}
	}

	public static Object attachJvm(int pid) throws IOException {
		try {
			if (VirtualMachineClass == null) {
				VirtualMachineClass = loadVirtualMachineClass();
			}
			try {
				return VirtualMachineClass.getMethod("attach", String.class)
						.invoke(null, String.valueOf(pid));
			} catch (Exception e) {
				throw new IOException(e);
			}
		} catch (NoClassDefFoundError e) {
			throw new UnsupportedOperationException("Not support attach to JVM");
		}
	}

	public static void detachJvm(Object vm) throws IOException {
		try {
			if (VirtualMachineClass == null) {
				VirtualMachineClass = loadVirtualMachineClass();
			}
			VirtualMachineClass.getMethod("detach").invoke(vm);
		} catch (Exception e) {
			throw new IOException(e);
		}
	}

	public static InputStream remoteDataDump(Object vm) throws IOException {
		try {
			if (VirtualMachineClass == null) {
				VirtualMachineClass = loadVirtualMachineClass();
			}
			Method method = vm.getClass().getMethod("remoteDataDump",
					Object[].class);
			return (InputStream) (method.invoke(vm,
					new Object[] { new Object[0] }));
		} catch (Exception e) {
			throw new IOException(e);
		}
	}


	public static Map<Integer, String> jps() throws IOException {
		try {
			if (VirtualMachineClass == null) {
				VirtualMachineClass = loadVirtualMachineClass();
			}
			Method method = VirtualMachineClass.getMethod("list");
			List<?> vmList = (List<?>) method.invoke(null);
			Map<Integer, String> ret = new LinkedHashMap<Integer, String>();
			for (Object vm : vmList) {
				Integer pid = Integer.parseInt((String) vm.getClass()
						.getMethod("id").invoke(vm));
				String displayName = (String) vm.getClass()
						.getMethod("displayName").invoke(vm);
				ret.put(pid, displayName);
			}
			return ret;
		} catch (Exception e) {
			throw new IOException(e);
		}
	}
}
