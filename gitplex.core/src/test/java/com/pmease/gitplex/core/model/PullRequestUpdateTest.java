package com.pmease.gitplex.core.model;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.pmease.commons.git.AbstractGitTest;
import com.pmease.commons.git.Commit;
import com.pmease.commons.git.Git;

public class PullRequestUpdateTest extends AbstractGitTest {

    private Git bareGit;
    
    private Repository repository;
    
    @Override
    public void setup() {
    	super.setup();
    	
        bareGit = new Git(new File(tempDir, "bare"));
        bareGit.clone(git, true, false, false, null);
        
        repository = Mockito.mock(Repository.class);
        Mockito.when(repository.git()).thenReturn(bareGit);
    }
    
    @Test
    public void testResolveChangeCommitWhenThereIsNoMerge() {
        PullRequest request = new PullRequest();
        Branch target = new Branch();
        target.setRepository(repository);
        target.setName("master");
        request.setTarget(target);

        addFileAndCommit("a", "", "master:1");
        
        git.checkout("head", "dev");

        addFileAndCommit("b", "", "dev:2");
        
        addFileAndCommit("c", "", "dev:3");
        
        git.checkout("master", null);

        addFileAndCommit("d", "", "master:4");

        git.push(bareGit.repoDir().getAbsolutePath(), "master:master");
        git.push(bareGit.repoDir().getAbsolutePath(), "dev:dev");
        
        request.setBaseCommit(bareGit.showRevision("master~1").getHash());

        PullRequestUpdate update1 = new PullRequestUpdate();
        update1.setId(1L);
        update1.setRequest(request);
        update1.setHeadCommit(bareGit.showRevision("dev~1").getHash());
        bareGit.updateRef(update1.getHeadRef(), update1.getHeadCommit(), null, null);
        request.getUpdates().add(update1);
        
        PullRequestUpdate update2 = new PullRequestUpdate();
        update2.setId(2L);
        update2.setRequest(request);
        update2.setHeadCommit(bareGit.showRevision("dev").getHash());
        bareGit.updateRef(update2.getHeadRef(), update2.getHeadCommit(), null, null);
        request.getUpdates().add(update2);

        target.setHeadCommit(bareGit.parseRevision("master", true));
        
        Assert.assertEquals(bareGit.showRevision("dev~1").getHash(), request.getLatestUpdate().getReferentialCommit());
        Assert.assertEquals(bareGit.showRevision("master~1").getHash(), request.getSortedUpdates().get(0).getReferentialCommit());
    }

    @Test
    public void testResolveChangeCommitWhenThereIsMerge() {
        PullRequest request = new PullRequest();
        Branch target = new Branch();
        target.setRepository(repository);
        target.setName("master");
        request.setTarget(target);

        addFileAndCommit("1", "", "master:1");

        addFileAndCommit("2", "", "master:2");
        
        git.checkout("master~1", "dev");

        addFileAndCommit("3", "", "dev:3");
        
        git.merge("master", null, null, null, null);

        addFileAndCommit("4", "", "dev:4");

        git.push(bareGit.repoDir().getAbsolutePath(), "master:master");
        git.push(bareGit.repoDir().getAbsolutePath(), "dev:dev");

        request.setBaseCommit(bareGit.calcMergeBase("dev~2", "master"));

        PullRequestUpdate update1 = new PullRequestUpdate();
        update1.setId(1L);
        update1.setRequest(request);
        update1.setHeadCommit(bareGit.showRevision("dev~2").getHash());
        bareGit.updateRef(update1.getHeadRef(), update1.getHeadCommit(), null, null);
        request.getUpdates().add(update1);
        
        PullRequestUpdate update2 = new PullRequestUpdate();
        update2.setId(2L);
        update2.setRequest(request);
        update2.setHeadCommit(bareGit.showRevision("dev").getHash());
        bareGit.updateRef(update2.getHeadRef(), update2.getHeadCommit(), null, null);
        request.getUpdates().add(update2);

        target.setHeadCommit(bareGit.parseRevision("master", true));
        
        Commit referentialCommit = bareGit.showRevision(request.getLatestUpdate().getReferentialCommit());
        Assert.assertTrue(referentialCommit.getParentHashes().contains(bareGit.showRevision("master").getHash()));
        Assert.assertTrue(referentialCommit.getParentHashes().contains(bareGit.showRevision("dev~2").getHash()));
        Assert.assertEquals(bareGit.showRevision("master~1").getHash(), request.getSortedUpdates().get(0).getReferentialCommit());
    }

    @Test
    public void testGetCommitsWhenTargetBranchIsMergedToSourceBranch() {
        PullRequest request = new PullRequest();
        Branch target = new Branch();
        target.setRepository(repository);
        target.setName("master");
        request.setTarget(target);

        addFileAndCommit("0", "", "0");
        
        git.checkout("head", "dev");
        git.checkout("master", null);

        addFileAndCommit("m1", "", "m1");
        
        git.checkout("dev", null);

        addFileAndCommit("d1", "", "d1");

        addFileAndCommit("d2", "", "d2");

        git.push(bareGit.repoDir().getAbsolutePath(), "master:master");
        git.push(bareGit.repoDir().getAbsolutePath(), "dev:dev");
        
        request.setBaseCommit(bareGit.calcMergeBase("master", "dev"));

        PullRequestUpdate update1 = new PullRequestUpdate();
        update1.setId(1L);
        update1.setRequest(request);
        update1.setHeadCommit(bareGit.showRevision("dev").getHash());
        bareGit.updateRef(update1.getHeadRef(), update1.getHeadCommit(), null, null);
        request.getUpdates().add(update1);

        git.merge("master", null, null, null, "merge master to dev");
        git.push(bareGit.repoDir().getAbsolutePath(), "dev:dev");
        
        PullRequestUpdate update2 = new PullRequestUpdate();
        update2.setId(2L);
        update2.setRequest(request);
        update2.setHeadCommit(bareGit.showRevision("dev").getHash());
        bareGit.updateRef(update2.getHeadRef(), update2.getHeadCommit(), null, null);
        request.getUpdates().add(update2);
        
        target.setHeadCommit(bareGit.parseRevision("master", true));
        
        Assert.assertEquals(2, update1.getCommits().size());
        Assert.assertEquals("d1", update1.getCommits().get(0).getMessage());
        Assert.assertEquals("d2", update1.getCommits().get(1).getMessage());
        
        Assert.assertEquals(1, update2.getCommits().size());
        Assert.assertTrue(update2.getCommits().get(0).getMessage().startsWith("merge master to dev"));
    }

    @Test
    public void testGetCommitsWhenSourceBranchIsMergedToTargetBranch() {
        PullRequest request = new PullRequest();
        Branch target = new Branch();
        target.setRepository(repository);
        target.setName("master");
        request.setTarget(target);

        addFileAndCommit("0", "", "0");
        
        git.checkout("head", "dev");
        git.checkout("master", null);

        addFileAndCommit("m1", "", "m1");
        
        git.checkout("dev", null);

        addFileAndCommit("d1", "", "d1");
        
        request.setBaseCommit(git.parseRevision("master", true));

        PullRequestUpdate update1 = new PullRequestUpdate();
        update1.setId(1L);
        update1.setRequest(request);
        update1.setHeadCommit(git.showRevision("dev").getHash());
        request.getUpdates().add(update1);

        addFileAndCommit("d2", "", "d2");

        git.checkout("master", null);
        git.merge("dev", null, null, null, null);
        
        git.checkout("dev", null);
        
        addFileAndCommit("d3", "", "d3");

        git.push(bareGit.repoDir().getAbsolutePath(), "master:master");
        git.push(bareGit.repoDir().getAbsolutePath(), "dev:dev");
        
        bareGit.updateRef(update1.getHeadRef(), update1.getHeadCommit(), null, null);

        PullRequestUpdate update2 = new PullRequestUpdate();
        update2.setId(2L);
        update2.setRequest(request);
        update2.setHeadCommit(bareGit.showRevision("dev").getHash());
        bareGit.updateRef(update2.getHeadRef(), update2.getHeadCommit(), null, null);
        
        request.getUpdates().add(update2);
        
        Assert.assertEquals(2, update2.getCommits().size());
        Assert.assertEquals("d2", update2.getCommits().get(0).getMessage());
        Assert.assertEquals("d3", update2.getCommits().get(1).getMessage());
    }

}