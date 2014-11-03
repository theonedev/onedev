package com.pmease.commons.git.command;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pmease.commons.util.execution.Commandline;
import com.pmease.commons.util.execution.LineConsumer;

public class ListBranchCommand extends GitCommand<Collection<String>> {

	private static final Logger logger = LoggerFactory.getLogger(ListBranchCommand.class);
	
    public ListBranchCommand(File repoDir) {
        super(repoDir);
    }

    @Override
    public Collection<String> call() {
        Commandline cmd = cmd();
        cmd.addArgs("branch");
        
        final Collection<String> branches = new ArrayList<String>();
        
        cmd.execute(new LineConsumer() {

            @Override
            public void consume(String line) {
            	branches.add(StringUtils.stripStart(line, "*").trim());
            }
            
        }, new LineConsumer() {

			@Override
			public void consume(String line) {
				logger.error(line);
			}
        	
        });
        
        return branches;
    }

}
