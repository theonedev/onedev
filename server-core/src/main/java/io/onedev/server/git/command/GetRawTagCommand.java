package io.onedev.server.git.command;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.onedev.commons.utils.command.Commandline;
import io.onedev.commons.utils.command.ExecutionResult;
import io.onedev.commons.utils.command.LineConsumer;
import io.onedev.server.git.CommandUtils;

import javax.annotation.Nullable;

public class GetRawTagCommand {

	private static final Logger logger = LoggerFactory.getLogger(GetRawTagCommand.class);
	
	private final File workingDir;
	
	private final String tag;
	
	private final Map<String, String> envs;
	
	public GetRawTagCommand(File workingDir, String tag, Map<String, String> envs) {
		this.workingDir = workingDir;
		this.tag = tag;
		this.envs = envs;
	}

	protected Commandline newGit() {
		return CommandUtils.newGit();
	}
	
	@Nullable
	public byte[] run() {
		Commandline git = newGit().workingDir(workingDir);
		git.environments().putAll(envs);
		
		git.addArgs("cat-file", "tag", tag);
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
	
		AtomicBoolean badFile = new AtomicBoolean(false);
		ExecutionResult result = git.execute(baos, new LineConsumer() {

			@Override
			public void consume(String line) {
				if (line.endsWith(": bad file"))
					badFile.set(true);
				else
					logger.error(line);
			}
			
		});
		
		if (badFile.get())
			return null;
		
		result.checkReturnCode();
		return baos.toByteArray();
	}

}