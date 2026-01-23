package io.onedev.server.git.command;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.onedev.commons.utils.command.Commandline;
import io.onedev.commons.utils.command.LineConsumer;
import io.onedev.server.git.CommandUtils;

public class RevListCommand {

	private static final Logger logger = LoggerFactory.getLogger(RevListCommand.class); 
	
	public enum Order {DATE, AUTHOR_DATE, TOPO};
	
	private final File workingDir;
	
	private RevListOptions options = new RevListOptions();
    
    public RevListCommand(File workingDir) {
    	this.workingDir = workingDir;
    }

    public RevListOptions options() {
    	return options;
    }
    
    public RevListCommand options(RevListOptions options) {
    	this.options = options;
    	return this;
    }
    
	protected Commandline newGit() {
		return CommandUtils.newGit();
	}
	
    public List<String> run() {
        Commandline git = newGit().workingDir(workingDir);
        git.addArgs("rev-list");

		options.configure(git);

        List<String> commitHashes = new ArrayList<>();
        git.execute(new LineConsumer() {

            @Override
            public void consume(String line) {
            	commitHashes.add(line);
            }
            
        }, new LineConsumer() {

			@Override
			public void consume(String line) {
				logger.error(line);
			}
        	
        }).checkReturnCode();
        
        return commitHashes;
	}
}
