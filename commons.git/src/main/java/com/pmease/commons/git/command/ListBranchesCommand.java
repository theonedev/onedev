package com.pmease.commons.git.command;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.commons.lang3.StringUtils;

import com.pmease.commons.util.execution.Commandline;
import com.pmease.commons.util.execution.LineConsumer;

public class ListBranchesCommand extends GitCommand<Map<String, String>> {

    public ListBranchesCommand(File repoDir) {
        super(repoDir);
    }

    @Override
    public Map<String, String> call() {
        Commandline cmd = cmd();
        cmd.addArgs("branch", "--verbose", "--no-abbrev");
        
        final Map<String, String> branches = new HashMap<String, String>();
        
        cmd.execute(new LineConsumer() {

            @Override
            public void consume(String line) {
            	line = StringUtils.stripStart(line, "*").trim();
            	StringTokenizer tokenizer = new StringTokenizer(line);
            	branches.put(tokenizer.nextToken(), tokenizer.nextToken());
            }
            
        }, errorLogger);
        
        return branches;
    }

}
