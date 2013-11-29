package com.pmease.gitop.core.model;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import com.pmease.commons.git.Git;
import com.pmease.commons.git.command.GitCommand;
import com.pmease.commons.loader.AppLoader;
import com.pmease.commons.loader.AppLoaderMocker;
import com.pmease.commons.util.FileUtils;
import com.pmease.gitop.core.manager.StorageManager;
import com.pmease.gitop.core.storage.ProjectStorage;

public class PullRequestTest extends AppLoaderMocker {

    private File projectDir;
    
    private Git git;
    
    @Mock
    private StorageManager storageManager;
    
    @Override
    public void setup() {
        Assert.assertTrue(GitCommand.checkError() == null);
        projectDir = FileUtils.createTempDir();
        
        git = new Git(new File(projectDir, "code"));
        git.init(false);
    }
    
    @Override
    public void teardown() {
        FileUtils.deleteDir(projectDir);
    }

    @Test
    public void shouldReturnAllUpdatesAsEffectiveIfTheyAreFastForward() {
        FileUtils.touchFile(new File(git.repoDir(), "a"));
        git.add("a");
        git.commit("commit", false, false);
        
        git.checkout("dev", true);
        
        FileUtils.touchFile(new File(git.repoDir(), "b"));
        git.add("b");
        git.commit("commit", false, false);
        
        git.updateRef("refs/updates/1", "HEAD", null, null);
        
        FileUtils.touchFile(new File(git.repoDir(), "c"));
        git.add("c");
        git.commit("commit", false, false);
        
        git.updateRef("refs/updates/2", "HEAD", null, null);

        Mockito.when(AppLoader.getInstance(StorageManager.class)).thenReturn(storageManager);
        
        Mockito.when(storageManager.getStorage(Mockito.any(Project.class)))
                .thenReturn(new ProjectStorage(projectDir));
        
        PullRequest pullRequest = new PullRequest();
        Branch destBranch = new Branch();
        destBranch.setName("master");
        destBranch.setProject(new Project());
        pullRequest.setTarget(destBranch);
        
        PullRequestUpdate update = new PullRequestUpdate();
        update.setId(1L);
        update.setRequest(pullRequest);
        pullRequest.getUpdates().add(update);

        update = new PullRequestUpdate();
        update.setId(2L);
        update.setRequest(pullRequest);
        pullRequest.getUpdates().add(update);
        
        Assert.assertEquals(pullRequest.getEffectiveUpdates().size(), 2);
    }

    @Test
    public void shouldReturnLatestUpdateAsEffectiveIfAllOthersHaveBeenMerged() {
        FileUtils.touchFile(new File(git.repoDir(), "a"));
        git.add("a");
        git.commit("commit", false, false);
        
        git.checkout("dev", true);
        
        FileUtils.touchFile(new File(git.repoDir(), "b"));
        git.add("b");
        git.commit("commit", false, false);
        
        git.updateRef("refs/updates/1", "HEAD", null, null);
        
        FileUtils.touchFile(new File(git.repoDir(), "c"));
        git.add("c");
        git.commit("commit", false, false);
        
        git.updateRef("refs/updates/2", "HEAD", null, null);

        FileUtils.touchFile(new File(git.repoDir(), "d"));
        git.add("d");
        git.commit("commit", false, false);
        
        git.updateRef("refs/updates/3", "HEAD", null, null);
        
        git.checkout("master", false);
        
        FileUtils.touchFile(new File(git.repoDir(), "e"));
        git.add("e");
        git.commit("commit", false, false);
        
        git.merge("refs/updates/2", null, null, null);

        Mockito.when(AppLoader.getInstance(StorageManager.class)).thenReturn(storageManager);
        
        Mockito.when(storageManager.getStorage(Mockito.any(Project.class)))
                .thenReturn(new ProjectStorage(projectDir));
        
        PullRequest pullRequest = new PullRequest();
        Branch destBranch = new Branch();
        destBranch.setName("master");
        destBranch.setProject(new Project());
        pullRequest.setTarget(destBranch);
        
        PullRequestUpdate update = new PullRequestUpdate();
        update.setId(1L);
        update.setRequest(pullRequest);
        pullRequest.getUpdates().add(update);

        update = new PullRequestUpdate();
        update.setId(2L);
        update.setRequest(pullRequest);
        pullRequest.getUpdates().add(update);
        
        update = new PullRequestUpdate();
        update.setId(3L);
        update.setRequest(pullRequest);
        pullRequest.getUpdates().add(update);

        Assert.assertEquals(pullRequest.getEffectiveUpdates().size(), 2);
        Assert.assertEquals(pullRequest.getEffectiveUpdates().get(0).getId().longValue(), 3L);
    }

}
