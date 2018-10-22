package com.hp.ts.rnd.tool.perf.threads.dump.jvm;

import java.io.File;

public class JavaHomeUtils {

	public static File getJavaHome() {
		return new File(System.getProperty("java.home"));
	}

	public static File getToolsJarFile() {
		File javaHome = getJavaHome();
		File toolsJarFile = new File(new File(javaHome, "lib"), "tools.jar");
		if (!toolsJarFile.exists()) {
			toolsJarFile = new File(new File(javaHome.getParentFile(), "lib"),
					"tools.jar");
		}
		return toolsJarFile;
	}

}
