package com.pmease.gitplex.core.model;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.pmease.commons.git.AbstractGitTest;

public class PullRequestTest extends AbstractGitTest {

    private Repository repository;

    @Override
    public void setup() {
    	super.setup();
    	
        repository = Mockito.mock(Repository.class);
        Mockito.when(repository.git()).thenReturn(git);
    }

    @Test
    public void shouldReturnAllUpdatesAsEffectiveIfTheyAreFastForward() {
        PullRequest request = new PullRequest();
        Branch target = new Branch();
        target.setName("master");
        target.setRepository(repository);
        request.setTarget(target);

        addFileAndCommit("a", "", "commit");
        
        git.checkout("head", "dev");

        addFileAndCommit("b", "", "commit");

        target.setHeadCommit(git.parseRevision("master", true));
        
        request.setBaseCommit(git.parseRevision("master", true));

        PullRequestUpdate update1 = new PullRequestUpdate();
        update1.setId(1L);
        update1.setRequest(request);
        git.updateRef(update1.getHeadRef(), "HEAD", null, null);
        update1.setHeadCommit(git.parseRevision(update1.getHeadRef(), true));
        request.getUpdates().add(update1);

        addFileAndCommit("c", "", "commit");
        
        PullRequestUpdate update2 = new PullRequestUpdate();
        update2.setId(2L);
        update2.setRequest(request);
        git.updateRef(update2.getHeadRef(), "HEAD", null, null);
        update2.setHeadCommit(git.parseRevision(update2.getHeadRef(), true));
        request.getUpdates().add(update2);
        
        Assert.assertEquals(request.getEffectiveUpdates().size(), 2);
    }

    @Test
    public void shouldReturnLatestUpdateAsEffectiveIfAllOthersHaveBeenMerged() {
        PullRequest request = new PullRequest();
        Branch target = new Branch();
        target.setName("master");
        target.setRepository(repository);
        request.setTarget(target);

        addFileAndCommit("a", "", "master:1");
        
        git.checkout("head", "dev");

        addFileAndCommit("b", "", "dev:2");
        
        request.setBaseCommit(git.parseRevision("master", true));

        addFileAndCommit("c", "", "dev:3");
        
        PullRequestUpdate update1 = new PullRequestUpdate();
        update1.setId(1L);
        update1.setRequest(request);
        git.updateRef(update1.getHeadRef(), "HEAD", null, null);
        update1.setHeadCommit(git.parseRevision(update1.getHeadRef(), true));
        String secondRef = update1.getHeadRef();
        request.getUpdates().add(update1);

        addFileAndCommit("d", "", "dev:4");
        
        PullRequestUpdate update2 = new PullRequestUpdate();
        update2.setId(2L);
        update2.setRequest(request);
        git.updateRef(update2.getHeadRef(), "HEAD", null, null);
        update2.setHeadCommit(git.parseRevision(update2.getHeadRef(), true));
        request.getUpdates().add(update2);
        
        git.checkout("master", null);

        addFileAndCommit("e", "", "master:5");
        
        git.merge(secondRef, null, null, null, null);

        target.setHeadCommit(git.parseRevision("master", true));

        Assert.assertEquals(1, request.getEffectiveUpdates().size());
        Assert.assertEquals(2L, request.getEffectiveUpdates().get(0).getId().longValue());
    }

}