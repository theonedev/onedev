package com.pmease.gitop.model;

import java.io.File;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.pmease.commons.git.Commit;
import com.pmease.commons.git.Git;
import com.pmease.commons.git.command.GitCommand;
import com.pmease.commons.util.FileUtils;

public class PullRequestUpdateTest {

    private File projectDir;
    
    private Git workGit;
    
    private Git bareGit;
    
    private Project project;
    
    @Before
    public void setup() {
        Assert.assertTrue(GitCommand.checkError() == null);
        projectDir = FileUtils.createTempDir();
        
        bareGit = new Git(new File(projectDir, "code"));
        bareGit.init(true);
        
        workGit = new Git(new File(projectDir, "work"));
        workGit.clone(bareGit.repoDir().getAbsolutePath(), false);
        
        project = Mockito.mock(Project.class);
        Mockito.when(project.code()).thenReturn(bareGit);
    }
    
    @After
    public void teardown() {
        FileUtils.deleteDir(projectDir);
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
        update.setHeadCommit(bareGit.resolveRevision("dev~1").getHash());
        bareGit.updateRef(update.getHeadRef(), update.getHeadCommit(), null, null);
        request.getUpdates().add(update);
        
        update = new PullRequestUpdate();
        update.setId(2L);
        update.setRequest(request);
        update.setHeadCommit(bareGit.resolveRevision("dev").getHash());
        bareGit.updateRef(update.getHeadRef(), update.getHeadCommit(), null, null);
        request.getUpdates().add(update);

        Assert.assertEquals(bareGit.resolveRevision("dev~1").getHash(), request.getLatestUpdate().getBaseCommit());
        Assert.assertEquals(bareGit.resolveRevision("master~1").getHash(), request.getSortedUpdates().get(1).getBaseCommit());
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
        update.setHeadCommit(bareGit.resolveRevision("dev~2").getHash());
        bareGit.updateRef(update.getHeadRef(), update.getHeadCommit(), null, null);
        request.getUpdates().add(update);
        
        update = new PullRequestUpdate();
        update.setId(2L);
        update.setRequest(request);
        update.setHeadCommit(bareGit.resolveRevision("dev").getHash());
        bareGit.updateRef(update.getHeadRef(), update.getHeadCommit(), null, null);
        request.getUpdates().add(update);

        Commit baseCommit = bareGit.resolveRevision(request.getLatestUpdate().getBaseCommit());
        Assert.assertTrue(baseCommit.getParentHashes().contains(bareGit.resolveRevision("master").getHash()));
        Assert.assertTrue(baseCommit.getParentHashes().contains(bareGit.resolveRevision("dev~2").getHash()));
        Assert.assertEquals(bareGit.resolveRevision("master~1").getHash(), request.getSortedUpdates().get(1).getBaseCommit());
    }

}