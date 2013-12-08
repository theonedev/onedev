package com.pmease.gitop.model;

import java.io.File;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.pmease.commons.git.Git;
import com.pmease.commons.git.command.GitCommand;
import com.pmease.commons.util.FileUtils;

public class PullRequestTest {

    private File projectDir;
    
    private Git git;
    
    private Project project;
    
    @Before
    public void setup() {
        Assert.assertTrue(GitCommand.checkError() == null);
        projectDir = FileUtils.createTempDir();
        
        git = new Git(new File(projectDir, "code"));
        git.init(false);
        
        project = Mockito.mock(Project.class);
        Mockito.when(project.code()).thenReturn(git);
    }
    
    @After
    public void teardown() {
        FileUtils.deleteDir(projectDir);
    }

    @Test
    public void shouldReturnAllUpdatesAsEffectiveIfTheyAreFastForward() {
        PullRequest request = new PullRequest();
        Branch target = new Branch();
        target.setName("master");
        target.setProject(project);
        request.setTarget(target);

        FileUtils.touchFile(new File(git.repoDir(), "a"));
        git.add("a");
        git.commit("commit", false, false);
        
        git.checkout("head", "dev");
        
        FileUtils.touchFile(new File(git.repoDir(), "b"));
        git.add("b");
        git.commit("commit", false, false);
        
        PullRequestUpdate update = new PullRequestUpdate();
        update.setId(1L);
        update.setRequest(request);
        git.updateRef(update.getHeadRef(), "HEAD", null, null);
        update.setHeadCommit(git.parseRevision(update.getHeadRef(), true));
        request.getUpdates().add(update);

        FileUtils.touchFile(new File(git.repoDir(), "c"));
        git.add("c");
        git.commit("commit", false, false);
        
        update = new PullRequestUpdate();
        update.setId(2L);
        update.setRequest(request);
        git.updateRef(update.getHeadRef(), "HEAD", null, null);
        update.setHeadCommit(git.parseRevision(update.getHeadRef(), true));
        request.getUpdates().add(update);
        
        Assert.assertEquals(request.getEffectiveUpdates().size(), 2);
    }

    @Test
    public void shouldReturnLatestUpdateAsEffectiveIfAllOthersHaveBeenMerged() {
        PullRequest request = new PullRequest();
        Branch target = new Branch();
        target.setName("master");
        target.setProject(project);
        request.setTarget(target);

        FileUtils.touchFile(new File(git.repoDir(), "a"));
        git.add("a");
        git.commit("master:1", false, false);
        
        git.checkout("head", "dev");
        
        FileUtils.touchFile(new File(git.repoDir(), "b"));
        git.add("b");
        git.commit("dev:2", false, false);
        
        PullRequestUpdate update = new PullRequestUpdate();
        update.setId(1L);
        update.setRequest(request);
        git.updateRef(update.getHeadRef(), "HEAD", null, null);
        update.setHeadCommit(git.parseRevision(update.getHeadRef(), true));
        request.getUpdates().add(update);
        
        FileUtils.touchFile(new File(git.repoDir(), "c"));
        git.add("c");
        git.commit("dev:3", false, false);
        
        update = new PullRequestUpdate();
        update.setId(2L);
        update.setRequest(request);
        git.updateRef(update.getHeadRef(), "HEAD", null, null);
        update.setHeadCommit(git.parseRevision(update.getHeadRef(), true));
        String secondRef = update.getHeadRef();
        request.getUpdates().add(update);

        FileUtils.touchFile(new File(git.repoDir(), "d"));
        git.add("d");
        git.commit("dev:4", false, false);
        
        update = new PullRequestUpdate();
        update.setId(3L);
        update.setRequest(request);
        git.updateRef(update.getHeadRef(), "HEAD", null, null);
        update.setHeadCommit(git.parseRevision(update.getHeadRef(), true));
        request.getUpdates().add(update);
        
        git.checkout("master", null);
        
        FileUtils.touchFile(new File(git.repoDir(), "e"));
        git.add("e");
        git.commit("master:5", false, false);
        
        git.merge(secondRef, null, null, null);

        Assert.assertEquals(1, request.getEffectiveUpdates().size());
        Assert.assertEquals(3L, request.getEffectiveUpdates().get(0).getId().longValue());
    }

}