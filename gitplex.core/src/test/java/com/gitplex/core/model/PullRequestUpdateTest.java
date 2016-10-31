package com.gitplex.core.model;

import java.io.File;

import org.eclipse.jgit.lib.Repository;
import org.junit.Assert;
import org.junit.Test;

import com.gitplex.core.entity.Depot;
import com.gitplex.core.entity.PullRequest;
import com.gitplex.core.entity.PullRequestUpdate;
import com.google.common.collect.Sets;
import com.gitplex.commons.git.AbstractGitTest;
import com.gitplex.commons.git.GitUtils;

public class PullRequestUpdateTest extends AbstractGitTest {

    private Depot depot;
    
    @Override
    public void before() {
    	super.before();
    	
        depot = new Depot() {

			private static final long serialVersionUID = 1L;

			@Override
			public File getDirectory() {
				return git.getRepository().getDirectory();
			}

			@Override
			public Repository getRepository() {
				return git.getRepository();
			}
        	
        };
        
    }
    
    @Test
    public void testResolveChangedFilesWhenThereIsNoMerge() throws Exception {
        PullRequest request = new PullRequest();
        request.setTargetDepot(depot);
        request.setSourceDepot(depot);
        request.setTargetBranch("master");

        addFileAndCommit("a", "", "master:1");
        
        git.checkout().setCreateBranch(true).setName("dev").call();

        addFileAndCommit("b", "", "dev:2");
        
        addFileAndCommit("c", "", "dev:3");
        
        git.checkout().setName("master").call();

        addFileAndCommit("d", "", "master:4");

        request.setBaseCommitHash(git.getRepository().resolve("master~1").name());

        PullRequestUpdate update1 = new PullRequestUpdate();
        update1.setId(1L);
        update1.setRequest(request);
        update1.setHeadCommitHash(git.getRepository().resolve("dev~1").name());
        update1.setMergeCommitHash(request.getBaseCommitHash());
        updateRef(update1.getHeadRef(), update1.getHeadCommitHash(), null);
        request.addUpdate(update1);
        
        PullRequestUpdate update2 = new PullRequestUpdate();
        update2.setId(2L);
        update2.setRequest(request);
        update2.setHeadCommitHash(git.getRepository().resolve("dev").name());
        update2.setMergeCommitHash(request.getBaseCommitHash());
        updateRef(update2.getHeadRef(), update2.getHeadCommitHash(), null);
        request.addUpdate(update2);

        Assert.assertEquals(Sets.newHashSet("c"), update2.getChangedFiles());
        Assert.assertEquals(Sets.newHashSet("b"), update1.getChangedFiles());
    }

    @Test
    public void testResolveChangedFilesWhenThereIsMerge() throws Exception {
        PullRequest request = new PullRequest();
        request.setTargetDepot(depot);
        request.setSourceDepot(depot);
        request.setTargetBranch("master");

        addFileAndCommit("1", "", "master:1");

        addFileAndCommit("2", "", "master:2");
        
        git.checkout().setStartPoint("master~1").setName("dev").setCreateBranch(true).call();

        addFileAndCommit("3", "", "dev:3");

        git.merge().include(git.getRepository().resolve("master")).setCommit(true).call();

        addFileAndCommit("4", "", "dev:4");

        Repository repository = depot.getRepository();
        request.setBaseCommitHash(GitUtils.getMergeBase(repository, repository.resolve("dev~2"), repository.resolve("master")).name());

        PullRequestUpdate update1 = new PullRequestUpdate();
        update1.setId(1L);
        update1.setRequest(request);
        update1.setHeadCommitHash(git.getRepository().resolve("dev~2").name());
        update1.setMergeCommitHash(request.getBaseCommitHash());
        updateRef(update1.getHeadRef(), update1.getHeadCommitHash(), null);
        request.addUpdate(update1);
        
        PullRequestUpdate update2 = new PullRequestUpdate();
        update2.setId(2L);
        update2.setRequest(request);
        update2.setHeadCommitHash(git.getRepository().resolve("dev").name());
        update2.setMergeCommitHash(git.getRepository().resolve("master").name());
        updateRef(update2.getHeadRef(), update2.getHeadCommitHash(), null);
        request.addUpdate(update2);

        Assert.assertEquals(Sets.newHashSet("4"), update2.getChangedFiles());
    }

    @Test
    public void testGetCommitsWhenTargetBranchIsMergedToSourceBranch() throws Exception {
        PullRequest request = new PullRequest();
        request.setTargetDepot(depot);
        request.setSourceDepot(depot);
        request.setTargetBranch("master");

        addFileAndCommit("0", "", "0");
        
        git.checkout().setName("dev").setCreateBranch(true).call();
        git.checkout().setName("master").call();

        addFileAndCommit("m1", "", "m1");
        
        git.checkout().setName("dev").call();

        addFileAndCommit("d1", "", "d1");

        addFileAndCommit("d2", "", "d2");

        Repository repository = depot.getRepository();
        request.setBaseCommitHash(GitUtils.getMergeBase(repository, repository.resolve("master"), repository.resolve("dev")).name());

        PullRequestUpdate update1 = new PullRequestUpdate();
        update1.setId(1L);
        update1.setRequest(request);
        update1.setHeadCommitHash(git.getRepository().resolve("dev").name());
        update1.setMergeCommitHash(request.getBaseCommitHash());
        updateRef(update1.getHeadRef(), update1.getHeadCommitHash(), null);
        request.addUpdate(update1);

        git.merge().include(git.getRepository().resolve("master")).setCommit(true).setMessage("merge master to dev").call();
        
        PullRequestUpdate update2 = new PullRequestUpdate();
        update2.setId(2L);
        update2.setRequest(request);
        update2.setHeadCommitHash(git.getRepository().resolve("dev").name());
        update2.setMergeCommitHash(git.getRepository().resolve("master").name());
        updateRef(update2.getHeadRef(), update2.getHeadCommitHash(), null);
        request.addUpdate(update2);
        
        Assert.assertEquals(2, update1.getCommits().size());
        Assert.assertEquals("d1", update1.getCommits().get(0).getFullMessage().trim());
        Assert.assertEquals("d2", update1.getCommits().get(1).getFullMessage().trim());
        
        Assert.assertEquals(1, update2.getCommits().size());
        Assert.assertTrue(update2.getCommits().get(0).getFullMessage().startsWith("merge master to dev"));
    }

    @Test
    public void testGetCommitsWhenSourceBranchIsMergedToTargetBranch() throws Exception {
        PullRequest request = new PullRequest();
        request.setTargetDepot(depot);
        request.setSourceDepot(depot);
        request.setTargetBranch("master");

        addFileAndCommit("0", "", "0");
        
        git.checkout().setName("dev").setCreateBranch(true).call();
        git.checkout().setName("master").call();

        addFileAndCommit("m1", "", "m1");
        
        git.checkout().setName("dev").call();

        addFileAndCommit("d1", "", "d1");
        
        request.setBaseCommitHash(git.getRepository().resolve("master~1").name());

        PullRequestUpdate update1 = new PullRequestUpdate();
        update1.setId(1L);
        update1.setRequest(request);
        update1.setHeadCommitHash(git.getRepository().resolve("dev").name());
        update1.setMergeCommitHash(request.getBaseCommitHash());
        request.addUpdate(update1);

        addFileAndCommit("d2", "", "d2");

        git.checkout().setName("master").call();
        git.merge().include(git.getRepository().resolve("dev")).setCommit(true).call();
        
        git.checkout().setName("dev").call();
        
        addFileAndCommit("d3", "", "d3");

        updateRef(update1.getHeadRef(), update1.getHeadCommitHash(), null);

        PullRequestUpdate update2 = new PullRequestUpdate();
        update2.setId(2L);
        update2.setRequest(request);
        update2.setHeadCommitHash(git.getRepository().resolve("dev").name());
        update2.setMergeCommitHash(git.getRepository().resolve("dev~1").name());
        updateRef(update2.getHeadRef(), update2.getHeadCommitHash(), null);
        
        request.addUpdate(update2);
        
        Assert.assertEquals(1, update2.getCommits().size());
        Assert.assertEquals("d3", update2.getCommits().get(0).getFullMessage().trim());
    }

}