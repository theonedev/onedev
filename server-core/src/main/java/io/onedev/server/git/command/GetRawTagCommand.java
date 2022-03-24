package io.onedev.server.git.command;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import io.onedev.commons.utils.command.Commandline;
import io.onedev.commons.utils.command.ExecutionResult;
import io.onedev.commons.utils.command.LineConsumer;

public class GetRawTagCommand extends GitCommand<byte[]> {

	private static final Logger logger = LoggerFactory.getLogger(GetRawTagCommand.class);
	
	private String tag;
	
	public GetRawTagCommand(File gitDir, Map<String, String> gitEnvs) {
		super(gitDir, gitEnvs);
	}
	
	public GetRawTagCommand revision(String tag) {
		this.tag = tag;
		return this;
	}
	
	@Override
	public byte[] call() {
		Preconditions.checkNotNull(tag, "tag has to be specified.");
		
		Commandline cmd = cmd();
		
		cmd.addArgs("cat-file", "tag", tag);
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
	
		AtomicBoolean badFile = new AtomicBoolean(false);
		ExecutionResult result = cmd.execute(baos, new LineConsumer() {

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