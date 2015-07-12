package com.pmease.gitplex.core.model;

import org.eclipse.jgit.lib.ObjectId;
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
        request.setTargetRepo(repository);
        request.setTargetBranch("master");

        addFileAndCommit("a", "", "commit");
        
        git.checkout("HEAD", "dev");

        addFileAndCommit("b", "", "commit");

        repository.cacheObjectId("master", ObjectId.fromString(git.parseRevision("master", true)));
        
        request.setBaseCommitHash(git.parseRevision("master", true));

        PullRequestUpdate update1 = new PullRequestUpdate();
        update1.setId(1L);
        update1.setRequest(request);
        git.updateRef(update1.getHeadRef(), "HEAD", null, null);
        update1.setHeadCommitHash(git.parseRevision(update1.getHeadRef(), true));
        request.addUpdate(update1);

        addFileAndCommit("c", "", "commit");
        
        PullRequestUpdate update2 = new PullRequestUpdate();
        update2.setId(2L);
        update2.setRequest(request);
        git.updateRef(update2.getHeadRef(), "HEAD", null, null);
        update2.setHeadCommitHash(git.parseRevision(update2.getHeadRef(), true));
        request.addUpdate(update2);
        
        Assert.assertEquals(request.getEffectiveUpdates().size(), 2);
    }

    @Test
    public void shouldReturnLatestUpdateAsEffectiveIfAllOthersHaveBeenMerged() {
        PullRequest request = new PullRequest();
        request.setTargetRepo(repository);
        request.setTargetBranch("master");

        addFileAndCommit("a", "", "master:1");
        
        git.checkout("HEAD", "dev");

        addFileAndCommit("b", "", "dev:2");
        
        request.setBaseCommitHash(git.parseRevision("master", true));

        addFileAndCommit("c", "", "dev:3");
        
        PullRequestUpdate update1 = new PullRequestUpdate();
        update1.setId(1L);
        update1.setRequest(request);
        git.updateRef(update1.getHeadRef(), "HEAD", null, null);
        update1.setHeadCommitHash(git.parseRevision(update1.getHeadRef(), true));
        String secondRef = update1.getHeadRef();
        request.addUpdate(update1);

        addFileAndCommit("d", "", "dev:4");
        
        PullRequestUpdate update2 = new PullRequestUpdate();
        update2.setId(2L);
        update2.setRequest(request);
        git.updateRef(update2.getHeadRef(), "HEAD", null, null);
        update2.setHeadCommitHash(git.parseRevision(update2.getHeadRef(), true));
        request.addUpdate(update2);
        
        git.checkout("master", null);

        addFileAndCommit("e", "", "master:5");
        
        git.merge(secondRef, null, null, null, null);

        repository.cacheObjectId("master", ObjectId.fromString(git.parseRevision("master", true)));

        Assert.assertEquals(1, request.getEffectiveUpdates().size());
        Assert.assertEquals(2L, request.getEffectiveUpdates().get(0).getId().longValue());
    }

}