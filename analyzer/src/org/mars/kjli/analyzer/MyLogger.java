package org.mars.kjli.analyzer;

import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MyLogger {
	
	public static Logger get() {
		return sLogger;
	}
	
	public static void severe(String msg) {
		sLogger.severe(msg);
	}
	
	public static void warning(String msg) {
		sLogger.warning(msg);
	}
	
	public static void info(String msg) {
		sLogger.info(msg);
	}
	
	public static void fine(String msg) {
		sLogger.fine(msg);
	}
	
	public static void finer(String msg) {
		sLogger.finer(msg);
	}
	
	public static void finest(String msg) {
		sLogger.finest(msg);
	}
	
	public static void loge(String msg, Exception e) {
		sLogger.log(Level.WARNING, msg, e);
	}
	
	private static Logger sLogger;
	
	static {
		sLogger = Logger.getLogger("org.mars.kjli.analyzer");
		try {
			sLogger.addHandler(new FileHandler());
		} catch (Exception e) {
			sLogger.log(Level.SEVERE, "Failed to record log to file.", e);
		}
	}

}
