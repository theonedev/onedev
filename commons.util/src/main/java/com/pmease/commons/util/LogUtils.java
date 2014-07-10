package com.pmease.commons.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogUtils {
	
	private static final Logger logger = LoggerFactory.getLogger(LogUtils.class);
	
	private static final Map<String, Logger> loggers = new ConcurrentHashMap<>();
	
	public static Logger getLogger(int stackBacktrackCount) {
		if (stackBacktrackCount == -1) {
			return logger;
		} else {
			String className = StringUtils.substringBefore(
					new Throwable().getStackTrace()[stackBacktrackCount].getClassName(), "$");
			Logger logger = loggers.get(className);
			if (logger == null) {
				logger = LoggerFactory.getLogger(className);
				loggers.put(className, logger);
			}
			return logger;
		}
	}

}
