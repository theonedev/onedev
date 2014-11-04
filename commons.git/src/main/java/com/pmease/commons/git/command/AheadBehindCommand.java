package com.pmease.commons.git.command;

import java.io.File;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.pmease.commons.git.AheadBehind;
import com.pmease.commons.util.execution.Commandline;
import com.pmease.commons.util.execution.LineConsumer;

public class AheadBehindCommand extends GitCommand<AheadBehind> {

	private static final Logger logger = LoggerFactory.getLogger(AheadBehindCommand.class);
	
	private String leftRev;

	private String rightRev;
	
	public AheadBehindCommand(File repoDir) {
		super(repoDir);
	}

	public AheadBehindCommand leftRev(String leftRev) {
		this.leftRev = Preconditions.checkNotNull(leftRev);
		return this;
	}
	
	public AheadBehindCommand rightRev(String rightRev) {
		this.rightRev = Preconditions.checkNotNull(rightRev);
		return this;
	}
	
	@Override
	public AheadBehind call() {
		Commandline cmd = cmd();
		cmd.addArgs("rev-list", "--left-right", "--count");
		cmd.addArgs(Preconditions.checkNotNull(leftRev, "leftRev")
				+ "..." + Preconditions.checkNotNull(rightRev, "rightRev"));

		final AheadBehind counter = new AheadBehind();
		cmd.execute(new LineConsumer() {

			@Override
			public void consume(String line) {
				StringTokenizer tokenizer = new StringTokenizer(line);
				counter.setAhead(Integer.parseInt(tokenizer.nextToken()));
				counter.setBehind(Integer.parseInt(tokenizer.nextToken()));
			}
			
		}, new LineConsumer() {

			@Override
			public void consume(String line) {
				logger.error(line);
			}
			
		});
		
		return counter;
	}

}
