package com.pmease.commons.git.command;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.pmease.commons.util.execution.Commandline;
import com.pmease.commons.util.execution.ExecuteResult;
import com.pmease.commons.util.execution.LineConsumer;

public class UpdateRefCommand extends GitCommand<Boolean> {

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
	public Boolean call() {
	    Preconditions.checkNotNull(refName, "refName has to be specified.");
	    Preconditions.checkNotNull(revision, "revision has to be specified.");
	    
		Commandline cmd = cmd().addArgs("update-ref", refName, revision);
		if (oldRevision != null)
		    cmd.addArgs(oldRevision);

		if (reason != null)
            cmd.addArgs("-m", reason);
		
		final AtomicBoolean refLockError = new AtomicBoolean(false);
		ExecuteResult result = cmd.execute(new LineConsumer() {

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
				
				if (line.startsWith("fatal: Cannot lock the ref")) 
					refLockError.set(true);
			} 
			
		});
		
		if (refLockError.get())
			return false;
		
		result.checkReturnCode();
		
		return true;
	}

}
