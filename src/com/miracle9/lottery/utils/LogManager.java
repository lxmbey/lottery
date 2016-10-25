package com.miracle9.lottery.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class LogManager {
	private final static Log logger = LogFactory.getLog(LogManager.class);

	public static void error(Throwable e) {
		logger.error(e, e);
	}

	public static void warn(Object msg) {
		logger.warn(msg);
	}
	
	public static void info(Object msg) {
		logger.info(msg);
	}
}
