package com.pmease.commons.git;

import java.util.concurrent.Callable;

import com.pmease.commons.util.execution.LineConsumer;

public abstract class GitCommand<V> implements Callable<V> {

	private final Git git;
	
	private final LineConsumer debugLogger = new LineConsumer.DebugLogger();
	
	private final LineConsumer infoLogger = new LineConsumer.InfoLogger();
	
	private final LineConsumer warnLogger = new LineConsumer.WarnLogger();
	
	private final LineConsumer errorLogger = new LineConsumer.ErrorLogger();
	
	private final LineConsumer traceLogger = new LineConsumer.TraceLogger();
	
	public GitCommand(Git git) {
		this.git = git;
	}

	public Git git() {
		return git;
	}
	
	protected LineConsumer debugLogger() {
		return debugLogger;
	}
	
	protected LineConsumer infoLogger() {
		return infoLogger;
	}
	
	protected LineConsumer warnLogger() {
		return warnLogger;
	}
	
	protected LineConsumer errorLogger() {
		return errorLogger;
	}
	
	protected LineConsumer traceLogger() {
		return traceLogger;
	}
	
	@Override
	public abstract V call();
}
