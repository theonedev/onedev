package com.pmease.gitop.model;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.pmease.commons.git.AbstractGitTest;
import com.pmease.commons.git.Git;
import com.pmease.commons.util.FileUtils;

public class PullRequestTest extends AbstractGitTest {

    private File projectDir;
    
    private Git git;
    
    private Repository project;

    @Override
    public void setup() {
    	super.setup();
    	
        projectDir = FileUtils.createTempDir();
        
        git = new Git(new File(projectDir, "code"));
        git.init(false);
        
        project = Mockito.mock(Repository.class);
        Mockito.when(project.git()).thenReturn(git);
    }

    @Override
    public void teardown() {
        FileUtils.deleteDir(projectDir);
        super.teardown();
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