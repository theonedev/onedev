package com.pmease.gitop.web.git.command;

import java.io.IOException;

import com.google.common.io.LineProcessor;
import com.pmease.commons.util.execution.LineConsumer;

public abstract class ForEachRefOutputHandler<T> extends LineConsumer implements LineProcessor<T> {

	@Override
	public boolean processLine(String line) throws IOException {
		consume(line);
		return true;
	}
}
