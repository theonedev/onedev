package com.pmease.gitop.web.git.command;

import com.pmease.commons.util.execution.LineConsumer;

public abstract class ForEachRefOutputHandler<T> extends LineConsumer {

	abstract public T getOutput();
	
}
