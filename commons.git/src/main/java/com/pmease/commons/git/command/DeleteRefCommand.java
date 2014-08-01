package com.pmease.commons.git.command;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.pmease.commons.util.execution.Commandline;
import com.pmease.commons.util.execution.LineConsumer;

public class DeleteRefCommand extends GitCommand<Void> {

	private static final Logger logger = LoggerFactory.getLogger(DeleteRefCommand.class);
	
    private String refName;
    
    private String oldRevision;
    
    private String reason;
    
	public DeleteRefCommand(File repoDir) {
		super(repoDir);
	}
	
	public DeleteRefCommand refName(String refName) {
	    this.refName = refName;
		return this;
	}
	
	public DeleteRefCommand oldRevision(String oldRevision) {
	    this.oldRevision = oldRevision;
	    return this;
	}
	
	public DeleteRefCommand reason(String reason) {
	    this.reason = reason;
	    return this;
	}

	@Override
	public Void call() {
        Preconditions.checkNotNull(refName, "refName has to be specified.");

        Commandline cmd = cmd().addArgs("update-ref", "-d", refName);
		if (oldRevision != null)
		    cmd.addArgs(oldRevision);
		
		if (reason != null)
		    cmd.addArgs(reason);
		
		cmd.execute(new LineConsumer() {

			@Override
			public void consume(String line) {
				logger.debug(line);
			}
			
		}, new LineConsumer() {

			@Override
			public void consume(String line) {
				logger.debug(line);
			}
			
		}).checkReturnCode();
		
		return null;
	}

}
