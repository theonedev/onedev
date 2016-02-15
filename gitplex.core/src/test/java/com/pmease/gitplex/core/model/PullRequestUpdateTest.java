package com.pmease.gitplex.core.model;

import java.io.File;

import org.eclipse.jgit.lib.ObjectId;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.google.common.collect.Sets;
import com.pmease.commons.git.AbstractGitTest;
import com.pmease.commons.git.Git;
import com.pmease.commons.loader.AppLoader;
import com.pmease.gitplex.core.manager.StorageManager;

public class PullRequestUpdateTest extends AbstractGitTest {

    private Git bareGit;
    
    private Depot depot;
    
    @Override
    public void setup() {
    	super.setup();
    	
        bareGit = new Git(new File(tempDir, "bare"));
        bareGit.clone(git, true, false, false, null);
        
        depot = new Depot() {

			private static final long serialVersionUID = 1L;

			@Override
			public Git git() {
				return bareGit;
			}
        	
        };
        
		Mockito.when(AppLoader.getInstance(StorageManager.class)).thenReturn(new StorageManager() {
			
			@Override
			public File getDepotDir(Depot depot) {
				throw new UnsupportedOperationException();
			}
			
			@Override
			public File getCacheDir(PullRequestUpdate update) {
				File cacheDir = new File(tempDir, "updates/" + update.getId());
				cacheDir.mkdirs();
				return cacheDir;
			}
			
			@Override
			public File getCacheDir(PullRequest request) {
				throw new UnsupportedOperationException();
			}
			
			@Override
			public File getCacheDir(Depot depot) {
				throw new UnsupportedOperationException();
			}

			@Override
			public File getIndexDir(Depot depot) {
				throw new UnsupportedOperationException();
			}

			@Override
			public File getAttachmentsDir(PullRequest request) {
				throw new UnsupportedOperationException();
			}
		});
    }
    
    @Test
    public void testResolveChangedFilesWhenThereIsNoMerge() {
        PullRequest request = new PullRequest();
        request.setTargetDepot(depot);
        request.setTargetBranch("master");

        addFileAndCommit("a", "", "master:1");
        
        git.checkout("HEAD", "dev");

        addFileAndCommit("b", "", "dev:2");
        
        addFileAndCommit("c", "", "dev:3");
        
        git.checkout("master", null);

        addFileAndCommit("d", "", "master:4");

        git.push(bareGit.depotDir().getAbsolutePath(), "master:master");
        git.push(bareGit.depotDir().getAbsolutePath(), "dev:dev");
        
        request.setBaseCommitHash(bareGit.showRevision("master~1").getHash());

        PullRequestUpdate update1 = new PullRequestUpdate();
        update1.setId(1L);
        update1.setRequest(request);
        update1.setHeadCommitHash(bareGit.showRevision("dev~1").getHash());
        bareGit.updateRef(update1.getHeadRef(), update1.getHeadCommitHash(), null, null);
        request.addUpdate(update1);
        
        PullRequestUpdate update2 = new PullRequestUpdate();
        update2.setId(2L);
        update2.setRequest(request);
        update2.setHeadCommitHash(bareGit.showRevision("dev").getHash());
        bareGit.updateRef(update2.getHeadRef(), update2.getHeadCommitHash(), null, null);
        request.addUpdate(update2);

        depot.cacheObjectId("master", ObjectId.fromString(bareGit.parseRevision("master", true)));
        
        Assert.assertEquals(Sets.newHashSet("c"), update2.getChangedFiles());
        Assert.assertEquals(Sets.newHashSet("b"), update1.getChangedFiles());
    }

    @Test
    public void testResolveChangedFilesWhenThereIsMerge() {
        PullRequest request = new PullRequest();
        request.setTargetDepot(depot);
        request.setTargetBranch("master");

        addFileAndCommit("1", "", "master:1");

        addFileAndCommit("2", "", "master:2");
        
        git.checkout("master~1", "dev");

        addFileAndCommit("3", "", "dev:3");
        
        git.merge("master", null, null, null, null);

        addFileAndCommit("4", "", "dev:4");

        git.push(bareGit.depotDir().getAbsolutePath(), "master:master");
        git.push(bareGit.depotDir().getAbsolutePath(), "dev:dev");

        request.setBaseCommitHash(bareGit.calcMergeBase("dev~2", "master"));

        PullRequestUpdate update1 = new PullRequestUpdate();
        update1.setId(1L);
        update1.setRequest(request);
        update1.setHeadCommitHash(bareGit.showRevision("dev~2").getHash());
        bareGit.updateRef(update1.getHeadRef(), update1.getHeadCommitHash(), null, null);
        request.addUpdate(update1);
        
        PullRequestUpdate update2 = new PullRequestUpdate();
        update2.setId(2L);
        update2.setRequest(request);
        update2.setHeadCommitHash(bareGit.showRevision("dev").getHash());
        bareGit.updateRef(update2.getHeadRef(), update2.getHeadCommitHash(), null, null);
        request.addUpdate(update2);

        depot.cacheObjectId("master", ObjectId.fromString(bareGit.parseRevision("master", true)));
        
        Assert.assertEquals(Sets.newHashSet("4"), update2.getChangedFiles());
    }

    @Test
    public void testGetCommitsWhenTargetBranchIsMergedToSourceBranch() {
        PullRequest request = new PullRequest();
        request.setTargetDepot(depot);
        request.setTargetBranch("master");

        addFileAndCommit("0", "", "0");
        
        git.checkout("HEAD", "dev");
        git.checkout("master", null);

        addFileAndCommit("m1", "", "m1");
        
        git.checkout("dev", null);

        addFileAndCommit("d1", "", "d1");

        addFileAndCommit("d2", "", "d2");

        git.push(bareGit.depotDir().getAbsolutePath(), "master:master");
        git.push(bareGit.depotDir().getAbsolutePath(), "dev:dev");
        
        request.setBaseCommitHash(bareGit.calcMergeBase("master", "dev"));

        PullRequestUpdate update1 = new PullRequestUpdate();
        update1.setId(1L);
        update1.setRequest(request);
        update1.setHeadCommitHash(bareGit.showRevision("dev").getHash());
        bareGit.updateRef(update1.getHeadRef(), update1.getHeadCommitHash(), null, null);
        request.addUpdate(update1);

        git.merge("master", null, null, null, "merge master to dev");
        git.push(bareGit.depotDir().getAbsolutePath(), "dev:dev");
        
        PullRequestUpdate update2 = new PullRequestUpdate();
        update2.setId(2L);
        update2.setRequest(request);
        update2.setHeadCommitHash(bareGit.showRevision("dev").getHash());
        bareGit.updateRef(update2.getHeadRef(), update2.getHeadCommitHash(), null, null);
        request.addUpdate(update2);
        
        depot.cacheObjectId("master", ObjectId.fromString(bareGit.parseRevision("master", true)));
        
        Assert.assertEquals(2, update1.getCommits().size());
        Assert.assertEquals("d1", update1.getCommits().get(0).getMessage());
        Assert.assertEquals("d2", update1.getCommits().get(1).getMessage());
        
        Assert.assertEquals(1, update2.getCommits().size());
        Assert.assertTrue(update2.getCommits().get(0).getMessage().startsWith("merge master to dev"));
    }

    @Test
    public void testGetCommitsWhenSourceBranchIsMergedToTargetBranch() {
        PullRequest request = new PullRequest();
        request.setTargetDepot(depot);
        request.setTargetBranch("master");

        addFileAndCommit("0", "", "0");
        
        git.checkout("HEAD", "dev");
        git.checkout("master", null);

        addFileAndCommit("m1", "", "m1");
        
        git.checkout("dev", null);

        addFileAndCommit("d1", "", "d1");
        
        request.setBaseCommitHash(git.parseRevision("master", true));

        PullRequestUpdate update1 = new PullRequestUpdate();
        update1.setId(1L);
        update1.setRequest(request);
        update1.setHeadCommitHash(git.showRevision("dev").getHash());
        request.addUpdate(update1);

        addFileAndCommit("d2", "", "d2");

        git.checkout("master", null);
        git.merge("dev", null, null, null, null);
        
        git.checkout("dev", null);
        
        addFileAndCommit("d3", "", "d3");

        git.push(bareGit.depotDir().getAbsolutePath(), "master:master");
        git.push(bareGit.depotDir().getAbsolutePath(), "dev:dev");
        
        bareGit.updateRef(update1.getHeadRef(), update1.getHeadCommitHash(), null, null);

        PullRequestUpdate update2 = new PullRequestUpdate();
        update2.setId(2L);
        update2.setRequest(request);
        update2.setHeadCommitHash(bareGit.showRevision("dev").getHash());
        bareGit.updateRef(update2.getHeadRef(), update2.getHeadCommitHash(), null, null);
        
        request.addUpdate(update2);
        
        depot.cacheObjectId("master", ObjectId.fromString(bareGit.parseRevision("master", true)));
        
        Assert.assertEquals(2, update2.getCommits().size());
        Assert.assertEquals("d2", update2.getCommits().get(0).getMessage());
        Assert.assertEquals("d3", update2.getCommits().get(1).getMessage());
    }

}