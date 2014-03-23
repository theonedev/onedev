package com.pmease.gitop.model;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.pmease.commons.git.AbstractGitTest;
import com.pmease.commons.git.Commit;
import com.pmease.commons.git.Git;
import com.pmease.commons.util.FileUtils;

public class PullRequestUpdateTest extends AbstractGitTest {

    private File projectDir;
    
    private Git workGit;
    
    private Git bareGit;
    
    private Repository project;
    
    @Override
    public void setup() {
    	super.setup();
    	
        projectDir = FileUtils.createTempDir();
        
        bareGit = new Git(new File(projectDir, "code"));
        bareGit.init(true);
        
        workGit = new Git(new File(projectDir, "work"));
        workGit.clone(bareGit.repoDir().getAbsolutePath(), false);
        
        project = Mockito.mock(Repository.class);
        Mockito.when(project.code()).thenReturn(bareGit);
    }
    
    @Override
    public void teardown() {
        FileUtils.deleteDir(projectDir);
        super.teardown();
    }

    @Test
    public void testResolveBaseCommitWhenThereIsNoMerge() {
        PullRequest request = new PullRequest();
        Branch target = new Branch();
        target.setProject(project);
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
        
        PullRequestUpdate update = new PullRequestUpdate();
        update.setId(1L);
        update.setRequest(request);
        update.setHeadCommit(bareGit.showRevision("dev~1").getHash());
        bareGit.updateRef(update.getHeadRef(), update.getHeadCommit(), null, null);
        request.getUpdates().add(update);
        
        update = new PullRequestUpdate();
        update.setId(2L);
        update.setRequest(request);
        update.setHeadCommit(bareGit.showRevision("dev").getHash());
        bareGit.updateRef(update.getHeadRef(), update.getHeadCommit(), null, null);
        request.getUpdates().add(update);

        Assert.assertEquals(bareGit.showRevision("dev~1").getHash(), request.getLatestUpdate().getBaseCommit());
        Assert.assertEquals(bareGit.showRevision("master~1").getHash(), request.getSortedUpdates().get(1).getBaseCommit());
    }

    @Test
    public void testResolveBaseCommitWhenThereIsMerge() {
        PullRequest request = new PullRequest();
        Branch target = new Branch();
        target.setProject(project);
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
        
        workGit.merge("master", null, null, null);

        FileUtils.touchFile(new File(workGit.repoDir(), "4"));
        workGit.add("4");
        workGit.commit("dev:4", false, false);

        workGit.push(bareGit.repoDir().getAbsolutePath(), "master:master");
        workGit.push(bareGit.repoDir().getAbsolutePath(), "dev:dev");

        PullRequestUpdate update = new PullRequestUpdate();
        update.setId(1L);
        update.setRequest(request);
        update.setHeadCommit(bareGit.showRevision("dev~2").getHash());
        bareGit.updateRef(update.getHeadRef(), update.getHeadCommit(), null, null);
        request.getUpdates().add(update);
        
        update = new PullRequestUpdate();
        update.setId(2L);
        update.setRequest(request);
        update.setHeadCommit(bareGit.showRevision("dev").getHash());
        bareGit.updateRef(update.getHeadRef(), update.getHeadCommit(), null, null);
        request.getUpdates().add(update);

        Commit baseCommit = bareGit.showRevision(request.getLatestUpdate().getBaseCommit());
        Assert.assertTrue(baseCommit.getParentHashes().contains(bareGit.showRevision("master").getHash()));
        Assert.assertTrue(baseCommit.getParentHashes().contains(bareGit.showRevision("dev~2").getHash()));
        Assert.assertEquals(bareGit.showRevision("master~1").getHash(), request.getSortedUpdates().get(1).getBaseCommit());
    }

}