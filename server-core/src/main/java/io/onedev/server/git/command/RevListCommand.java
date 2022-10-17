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

        boolean hasRevisions = false;
        if (!options.revisions().isEmpty()) {
        	for (String revision: options.revisions()) {
        		git.addArgs(revision);
        		if (!revision.startsWith("^"))
        			hasRevisions = true;
        	}
        } 
        if (!hasRevisions)
        	git.addArgs("--branches");
        
        if (options.before() != null) 
        	git.addArgs("--before", options.before());
        
        if (options.after() != null)
        	git.addArgs("--after", options.after());
        
        if (options.count() != 0)
        	git.addArgs("-" + options.count());
        if (options.skip() != 0)
        	git.addArgs("--skip=" + options.skip());
        
        if (options.order() == Order.DATE)
        	git.addArgs("--date-order");
        else if (options.order() == Order.AUTHOR_DATE)
        	git.addArgs("--author-date-order");
        else if (options.order() == Order.TOPO)
        	git.addArgs("--topo-order");

        if (options.firstParent())
        	git.addArgs("--first-parent");
        
        for (String author: options.authors())
        	git.addArgs("--author=" + author);
        
        for (String committer: options.committers())
        	git.addArgs("--committer=" + committer);
        
        for (String message: options.messages())
        	git.addArgs("--grep=" + message);
        
        if (options.ignoreCase())
        	git.addArgs("-i");
        
        git.addArgs("--");
        
        for (String path: options.paths())
        	git.addArgs(path);

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
