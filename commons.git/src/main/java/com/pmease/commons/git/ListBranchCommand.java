package com.pmease.commons.git;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.lang3.StringUtils;

import com.pmease.commons.util.execution.Commandline;
import com.pmease.commons.util.execution.LineConsumer;

public class ListBranchCommand extends GitCommand<Collection<String>> {

    public ListBranchCommand(Git git) {
        super(git);
    }

    @Override
    public Collection<String> call() {
        Commandline cmd = git().cmd();
        cmd.addArgs("branch");
        
        final Collection<String> branches = new ArrayList<String>();
        
        cmd.execute(new LineConsumer() {

            @Override
            public void consume(String line) {
                branches.add(StringUtils.stripStart(line, "*").trim());
            }
            
        }, errorLogger());
        
        return branches;
    }

}
