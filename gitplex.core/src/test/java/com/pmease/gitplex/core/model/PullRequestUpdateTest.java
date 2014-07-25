package com.pmease.gitplex.core.model;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.pmease.commons.git.AbstractGitTest;
import com.pmease.commons.git.Commit;
import com.pmease.commons.git.Git;
import com.pmease.commons.util.FileUtils;

public class PullRequestUpdateTest extends AbstractGitTest {

    private File repoDir;
    
    private Git workGit;
    
    private Git bareGit;
    
    private Repository repository;
    
    @Override
    public void setup() {
    	super.setup();
    	
        repoDir = FileUtils.createTempDir();
        
        bareGit = new Git(new File(repoDir, "code"));
        bareGit.init(true);
        
        workGit = new Git(new File(repoDir, "work"));
        workGit.clone(bareGit.repoDir().getAbsolutePath(), false, false, false, null);
        
        repository = Mockito.mock(Repository.class);
        Mockito.when(repository.git()).thenReturn(bareGit);
    }
    
    @Override
    public void teardown() {
        FileUtils.deleteDir(repoDir);
        super.teardown();
    }

    @Test
    public void testResolveChangeCommitWhenThereIsNoMerge() {
        PullRequest request = new PullRequest();
        Branch target = new Branch();
        target.setRepository(repository);
        target.setName("master");
        request.setTarget(target);

        FileUtils.touchFile(new File(workGit.repoDir(), "a"));
        workGit.add("a");
        workGit.commit("master:1", false, false);
        
        workGit.checkout("head", "dev");
        
        FileUtils.touchFile(new File(workGit.repoDir(), "b"));
        workGit.add("b");
        workGit.commit("dev:2", false, false);
        
        FileUtils.touchFile(new File(workGit.repoDir(), "c"));
        workGit.add("c");
        workGit.commit("dev:3", false, false);
        
        workGit.checkout("master", null);

        FileUtils.touchFile(new File(workGit.repoDir(), "d"));
        workGit.add("d");
        workGit.commit("master:4", false, false);

        workGit.push(bareGit.repoDir().getAbsolutePath(), "master:master");
        workGit.push(bareGit.repoDir().getAbsolutePath(), "dev:dev");
        
        PullRequestUpdate update0 = new PullRequestUpdate();
        update0.setId(1L);
        update0.setRequest(request);
        update0.setHeadCommit(bareGit.showRevision("master~1").getHash());
        bareGit.updateRef(update0.getHeadRef(), update0.getHeadCommit(), null, null);
        request.getUpdates().add(update0);

        PullRequestUpdate update1 = new PullRequestUpdate();
        update1.setId(2L);
        update1.setRequest(request);
        update1.setHeadCommit(bareGit.showRevision("dev~1").getHash());
        bareGit.updateRef(update1.getHeadRef(), update1.getHeadCommit(), null, null);
        request.getUpdates().add(update1);
        
        PullRequestUpdate update2 = new PullRequestUpdate();
        update2.setId(3L);
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

        FileUtils.touchFile(new File(workGit.repoDir(), "1"));
        workGit.add("1");
        workGit.commit("master:1", false, false);
        
        FileUtils.touchFile(new File(workGit.repoDir(), "2"));
        workGit.add("2");
        workGit.commit("master:2", false, false);
        
        workGit.checkout("master~1", "dev");
        
        FileUtils.touchFile(new File(workGit.repoDir(), "3"));
        workGit.add("3");
        workGit.commit("dev:3", false, false);
        
        workGit.merge("master", null, null, null, null);

        FileUtils.touchFile(new File(workGit.repoDir(), "4"));
        workGit.add("4");
        workGit.commit("dev:4", false, false);

        workGit.push(bareGit.repoDir().getAbsolutePath(), "master:master");
        workGit.push(bareGit.repoDir().getAbsolutePath(), "dev:dev");

        PullRequestUpdate update0 = new PullRequestUpdate();
        update0.setId(1L);
        update0.setRequest(request);
        update0.setHeadCommit(bareGit.calcMergeBase("dev~2", "master"));
        bareGit.updateRef(update0.getHeadRef(), update0.getHeadCommit(), null, null);
        request.getUpdates().add(update0);

        PullRequestUpdate update1 = new PullRequestUpdate();
        update1.setId(2L);
        update1.setRequest(request);
        update1.setHeadCommit(bareGit.showRevision("dev~2").getHash());
        bareGit.updateRef(update1.getHeadRef(), update1.getHeadCommit(), null, null);
        request.getUpdates().add(update1);
        
        PullRequestUpdate update2 = new PullRequestUpdate();
        update2.setId(3L);
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

        FileUtils.touchFile(new File(workGit.repoDir(), "0"));
        workGit.add("0");
        workGit.commit("0", false, false);
        
        workGit.checkout("head", "dev");
        workGit.checkout("master", null);
        
        FileUtils.touchFile(new File(workGit.repoDir(), "m1"));
        workGit.add("m1");
        workGit.commit("m1", false, false);
        
        workGit.checkout("dev", null);

        FileUtils.touchFile(new File(workGit.repoDir(), "d1"));
        workGit.add("d1");
        workGit.commit("d1", false, false);

        FileUtils.touchFile(new File(workGit.repoDir(), "d2"));
        workGit.add("d2");
        workGit.commit("d2", false, false);

        workGit.push(bareGit.repoDir().getAbsolutePath(), "master:master");
        workGit.push(bareGit.repoDir().getAbsolutePath(), "dev:dev");
        
        PullRequestUpdate update0 = new PullRequestUpdate();
        update0.setId(1L);
        update0.setRequest(request);
        update0.setHeadCommit(bareGit.calcMergeBase("master", "dev"));
        bareGit.updateRef(update0.getHeadRef(), update0.getHeadCommit(), null, null);
        request.getUpdates().add(update0);

        PullRequestUpdate update1 = new PullRequestUpdate();
        update1.setId(2L);
        update1.setRequest(request);
        update1.setHeadCommit(bareGit.showRevision("dev").getHash());
        bareGit.updateRef(update1.getHeadRef(), update1.getHeadCommit(), null, null);
        request.getUpdates().add(update1);

        workGit.merge("master", null, null, null, "merge master to dev");
        workGit.push(bareGit.repoDir().getAbsolutePath(), "dev:dev");
        
        PullRequestUpdate update2 = new PullRequestUpdate();
        update2.setId(3L);
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

        FileUtils.touchFile(new File(workGit.repoDir(), "0"));
        workGit.add("0");
        workGit.commit("0", false, false);
        
        workGit.checkout("head", "dev");
        workGit.checkout("master", null);
        
        FileUtils.touchFile(new File(workGit.repoDir(), "m1"));
        workGit.add("m1");
        workGit.commit("m1", false, false);
        
        workGit.checkout("dev", null);

        FileUtils.touchFile(new File(workGit.repoDir(), "d1"));
        workGit.add("d1");
        workGit.commit("d1", false, false);
        
        PullRequestUpdate update0 = new PullRequestUpdate();
        update0.setId(1L);
        update0.setRequest(request);
        update0.setHeadCommit(workGit.parseRevision("master", true));
        request.getUpdates().add(update0);

        PullRequestUpdate update1 = new PullRequestUpdate();
        update1.setId(1L);
        update1.setRequest(request);
        update1.setHeadCommit(workGit.showRevision("dev").getHash());
        request.getUpdates().add(update1);

        FileUtils.touchFile(new File(workGit.repoDir(), "d2"));
        workGit.add("d2");
        workGit.commit("d2", false, false);

        workGit.checkout("master", null);
        workGit.merge("dev", null, null, null, null);
        
        workGit.checkout("dev", null);
        FileUtils.touchFile(new File(workGit.repoDir(), "d3"));
        workGit.add("d3");
        workGit.commit("d3", false, false);

        workGit.push(bareGit.repoDir().getAbsolutePath(), "master:master");
        workGit.push(bareGit.repoDir().getAbsolutePath(), "dev:dev");
        
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