package com.pmease.gitop.web.git.command;

import java.util.List;

import com.google.common.collect.Lists;
import com.pmease.commons.util.execution.LineConsumer;

public class LineListConsumer extends LineConsumer {

	private final List<String> lines = Lists.newLinkedList();
	
	@Override
	public void consume(String line) {
		lines.add(line);
	}

	public List<String> getLines() {
		return lines;
	}
}
