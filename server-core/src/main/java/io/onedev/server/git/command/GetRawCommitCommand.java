package io.onedev.server.git.command;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import io.onedev.commons.utils.command.Commandline;
import io.onedev.commons.utils.command.LineConsumer;

public class GetRawCommitCommand extends GitCommand<byte[]> {

	private static final Logger logger = LoggerFactory.getLogger(GetRawCommitCommand.class);
	
	private String revision;
	
	public GetRawCommitCommand(File gitDir, Map<String, String> gitEnvs) {
		super(gitDir, gitEnvs);
	}
	
	public GetRawCommitCommand revision(String revision) {
		this.revision = revision;
		return this;
	}
	
	@Override
	public byte[] call() {
		Preconditions.checkNotNull(revision, "revision has to be specified.");
		
		Commandline cmd = cmd();
		
		cmd.addArgs("cat-file", "commit", revision);
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		cmd.execute(baos, new LineConsumer() {

			@Override
			public void consume(String line) {
				logger.error(line);
			}
			
		}).checkReturnCode();

		return baos.toByteArray();
	}

}