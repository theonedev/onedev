package io.onedev.server.git.command;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.onedev.commons.utils.command.Commandline;
import io.onedev.commons.utils.command.LineConsumer;

public class RevListCommand extends GitCommand<List<String>> {

	private static final Logger logger = LoggerFactory.getLogger(RevListCommand.class); 
	
	public enum Order {DATE, AUTHOR_DATE, TOPO};
	
    private List<String> revisions = new ArrayList<>();
    
    private List<String> paths = new ArrayList<>();
    
    private String after;
    
    private String before;
    
    private int count;
    
    private int skip;
    
    private Order order;
    
    private boolean firstParent;
    
    private boolean ignoreCase;
    
    private List<String> messages = new ArrayList<>();
    
    private List<String> authors = new ArrayList<>();
    
    private List<String> committers = new ArrayList<>();
    
    public RevListCommand(File gitDir) {
        super(gitDir);
    }

	public List<String> revisions() {
		return revisions;
	}

	public RevListCommand revisions(List<String> revisions) {
		this.revisions = revisions;
		return this;
	}

	public List<String> paths() {
		return paths;
	}

	public RevListCommand paths(List<String> paths) {
		this.paths = paths;
		return this;
	}

	public String after() {
		return after;
	}

	public RevListCommand after(String after) {
		this.after = after;
		return this;
	}
	
	public Order order() {
		return order;
	}
	
	public RevListCommand order(Order order) {
		this.order = order;
		return this;
	}
	
	public boolean firstParent() {
		return firstParent;
	}
	
	public RevListCommand firstParent(boolean firstParent) {
		this.firstParent = firstParent;
		return this;
	}
	
	public RevListCommand after(Date after) {
		this.after = DateFormatUtils.ISO_8601_EXTENDED_DATE_FORMAT.format(after);
		return this;
	}

	public String before() {
		return before;
	}

	public RevListCommand before(String before) {
		this.before = before;
		return this;
	}
	
	public RevListCommand before(Date before) {
		this.before = DateFormatUtils.ISO_8601_EXTENDED_DATE_FORMAT.format(before);		
		return this;
	}
	
	public int count() {
		return count;
	}

	public RevListCommand count(int count) {
		this.count = count;
		return this;
	}

	public int skip() {
		return skip;
	}

	public RevListCommand skip(int skip) {
		this.skip = skip;
		return this;
	}
	
	public boolean ignoreCase() {
		return ignoreCase;
	}
	
	public RevListCommand ignoreCase(boolean ignoreCase) {
		this.ignoreCase = ignoreCase;
		return this;
	}
	
	public List<String> messages() {
		return messages;
	}

	public RevListCommand messages(List<String> messages) {
		this.messages = messages;
		return this;
	}

	public List<String> authors() {
		return authors;
	}
	
	public RevListCommand authors(List<String> authors) {
		this.authors = authors;
		return this;
	}
	
	public List<String> committers() {
		return committers;
	}
	
	public RevListCommand commmitters(List<String> committers) {
		this.committers = committers;
		return this;
	}
	
	@Override
    public List<String> call() {
        Commandline cmd = cmd();
        cmd.addArgs("rev-list");

        boolean hasRevisions = false;
        if (!revisions.isEmpty()) {
        	for (String revision: revisions) {
        		cmd.addArgs(revision);
        		if (!revision.startsWith("^"))
        			hasRevisions = true;
        	}
        } 
        if (!hasRevisions)
        	cmd.addArgs("--branches");
        
        if (before != null) 
        	cmd.addArgs("--before", before);
        
        if (after != null)
        	cmd.addArgs("--after", after);
        
        if (count != 0)
        	cmd.addArgs("-" + count);
        if (skip != 0)
        	cmd.addArgs("--skip=" + skip);
        
        if (order == Order.DATE)
        	cmd.addArgs("--date-order");
        else if (order == Order.AUTHOR_DATE)
        	cmd.addArgs("--author-date-order");
        else if (order == Order.TOPO)
        	cmd.addArgs("--topo-order");

        if (firstParent)
        	cmd.addArgs("--first-parent");
        
        for (String author: authors)
        	cmd.addArgs("--author=" + author);
        
        for (String committer: committers)
        	cmd.addArgs("--committer=" + committer);
        
        for (String message: messages)
        	cmd.addArgs("--grep=" + message);
        
        if (ignoreCase)
        	cmd.addArgs("-i");
        
        cmd.addArgs("--");
        
        for (String path: paths)
        	cmd.addArgs(path);

        List<String> commitHashes = new ArrayList<>();
        cmd.execute(new LineConsumer() {

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
