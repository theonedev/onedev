package com.pmease.commons.git.command;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.pmease.commons.util.execution.Commandline;
import com.pmease.commons.util.execution.LineConsumer;

public class UpdateRefCommand extends GitCommand<Void> {

	private static final Logger logger = LoggerFactory.getLogger(UpdateRefCommand.class);
	
    private String refName;
    
    private String revision;
    
    private String oldRevision;
    
    private String reason;
    
	public UpdateRefCommand(File repoDir) {
		super(repoDir);
	}
	
	public UpdateRefCommand refName(String refName) {
	    this.refName = refName;
		return this;
	}
	
	public UpdateRefCommand revision(String revision) {
	    this.revision = revision;
	    return this;
	}
	
	public UpdateRefCommand oldRevision(String oldRevision) {
	    this.oldRevision = oldRevision;
	    return this;
	}
	
	public UpdateRefCommand reason(String reason) {
	    this.reason = reason;
	    return this;
	}

	@Override
	public Void call() {
	    Preconditions.checkNotNull(refName, "refName has to be specified.");
	    Preconditions.checkNotNull(revision, "revision has to be specified.");
	    
		Commandline cmd = cmd().addArgs("update-ref", refName, revision);
		if (oldRevision != null)
		    cmd.addArgs(oldRevision);

		if (reason != null)
            cmd.addArgs("-m", reason);
		
		cmd.execute(new LineConsumer() {

			@Override
			public void consume(String line) {
				logger.debug(line);
			}
			
		}, new LineConsumer() {

			@Override
			public void consume(String line) {
				if (line.startsWith(" * [new ref]"))
					logger.info(line);
				else
					logger.error(line);
			} 
			
		}).checkReturnCode();
		
		return null;
	}

}
