package com.pmease.commons.git.command;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.pmease.commons.util.execution.Commandline;
import com.pmease.commons.util.execution.LineConsumer;

public class ListCherriesCommand extends GitCommand<List<String>> {

	private static final Logger logger = LoggerFactory.getLogger(ListCherriesCommand.class);
	
    private String fromRev;
    
    private String toRev;
    
	public ListCherriesCommand(final File repoDir) {
		super(repoDir);
	}

	public ListCherriesCommand fromRev(String fromRev) {
	    this.fromRev = fromRev;
	    return this;
	}
	
	public ListCherriesCommand toRev(String toRev) {
		this.toRev = toRev;
		return this;
	}
	
	@Override
	public List<String> call() {
	    Preconditions.checkNotNull(fromRev, "fromRev has to be specified.");
	    Preconditions.checkNotNull(toRev, "toRev has to be specified.");
	    
	    final List<String> cherries = new ArrayList<>();
	    Commandline cmd = cmd();
	    cmd.addArgs("rev-list", "--cherry-pick", "--right-only", "--no-merges", "--reverse", fromRev + "..." + toRev);
	    cmd.execute(new LineConsumer() {

			@Override
			public void consume(String line) {
				cherries.add(line);
			}
	    	
	    }, new LineConsumer() {

			@Override
			public void consume(String line) {
				logger.error(line);
			}
	    	
	    }).checkReturnCode();
	    
	    return cherries;
	}

}
