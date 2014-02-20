package com.pmease.gitop.web.git.command;

import java.io.File;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import org.parboiled.common.Preconditions;

import com.pmease.commons.git.command.GitCommand;
import com.pmease.commons.util.execution.Commandline;
import com.pmease.commons.util.execution.LineConsumer;

public class AheadBehindCommand extends GitCommand<AheadBehind> {

	public AheadBehindCommand(File repoDir) {
		super(repoDir);
	}

	public AheadBehindCommand(File repoDir, @Nullable Map<String, String> environments) {
		super(repoDir, environments);
	}
	
	private String leftBranch;
	private String rightBranch;
	
	public AheadBehindCommand leftBranch(String leftBranch) {
		this.leftBranch = Preconditions.checkNotNull(leftBranch);
		return this;
	}
	
	public AheadBehindCommand rightBranch(String rightBranch) {
		this.rightBranch = Preconditions.checkNotNull(rightBranch);
		return this;
	}
	
	static final Pattern pLeftRight = Pattern.compile("(\\d+)\\s+(\\d+)");
	@Override
	public AheadBehind call() {
		Commandline cmd = cmd();
		cmd.addArgs("rev-list", "--left-right", "--count");
		cmd.addArgs(Preconditions.checkArgNotNull(leftBranch, "leftBranch")
				+ "..." + Preconditions.checkArgNotNull(rightBranch, "rightBranch"));

		final AheadBehind counter = new AheadBehind();
		cmd.execute(new LineConsumer() {

			@Override
			public void consume(String line) {
				Matcher m = pLeftRight.matcher(line);
				if (m.find()) {
					counter.setAhead(Integer.valueOf(m.group(1)));
					counter.setBehind(Integer.valueOf(m.group(2)));
				}
			}
			
		}, new LineConsumer.ErrorLogger());
		
		return counter;
	}

	public static void main(String[] args) {
		String str = "68	4";
		Matcher m = pLeftRight.matcher(str);
		if (m.find()) {
			System.out.println(m.group(1) + "\t" + m.group(2));
		}
	}
}
