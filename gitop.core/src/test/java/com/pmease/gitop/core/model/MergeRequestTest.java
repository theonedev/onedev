package com.pmease.gitop.core.model;

import java.io.File;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.pmease.commons.git.Git;
import com.pmease.commons.loader.AppLoader;
import com.pmease.commons.util.FileUtils;
import com.pmease.gitop.core.manager.StorageManager;
import com.pmease.gitop.core.storage.ProjectStorage;

@RunWith(PowerMockRunner.class)
@PrepareForTest(AppLoader.class)
public class MergeRequestTest {

    private File projectDir;
    
    private Git git;
    
    @Mock
    private StorageManager storageManager;
    
    @Before
    public void before() {
        Assert.assertTrue(Git.checkError() == null);
        projectDir = FileUtils.createTempDir();
        
        git = new Git(new File(projectDir, "code")).init().call();
        
        MockitoAnnotations.initMocks(this);
    }
    
    @After
    public void after() {
        FileUtils.deleteDir(projectDir);
    }

    @Test
    public void shouldReturnAllUpdatesAsEffectiveIfTheyAreFastForward() {
        FileUtils.touchFile(new File(git.repoDir(), "a"));
        git.add().addPath("a").call();
        git.commit().message("commit").call();
        
        git.branch().branchName("dev").call();
        
        FileUtils.touchFile(new File(git.repoDir(), "b"));
        git.add().addPath("b").call();
        git.commit().message("commit").call();
        
        git.updateRef().refName("refs/updates/1").revision("HEAD").call();
        
        FileUtils.touchFile(new File(git.repoDir(), "c"));
        git.add().addPath("c").call();
        git.commit().message("commit").call();
        
        git.updateRef().refName("refs/updates/2").revision("HEAD").call();

        PowerMockito.mockStatic(AppLoader.class);
        Mockito.when(AppLoader.getInstance(StorageManager.class)).thenReturn(storageManager);
        
        Mockito.when(storageManager.getStorage(Mockito.any(Project.class)))
                .thenReturn(new ProjectStorage(projectDir));
        
        MergeRequest mergeRequest = new MergeRequest();
        Branch destBranch = new Branch();
        destBranch.setName("master");
        destBranch.setProject(new Project());
        mergeRequest.setDestination(destBranch);
        
        MergeRequestUpdate update = new MergeRequestUpdate();
        update.setId(1L);
        update.setRequest(mergeRequest);
        mergeRequest.getUpdates().add(update);

        update = new MergeRequestUpdate();
        update.setId(2L);
        update.setRequest(mergeRequest);
        mergeRequest.getUpdates().add(update);
        
        Assert.assertEquals(mergeRequest.getEffectiveUpdates().size(), 2);
    }

    @Test
    public void shouldReturnLatestUpdateAsEffectiveIfAllOthersHaveBeenMerged() {
        FileUtils.touchFile(new File(git.repoDir(), "a"));
        git.add().addPath("a").call();
        git.commit().message("commit").call();
        
        git.branch().branchName("dev").call();
        
        FileUtils.touchFile(new File(git.repoDir(), "b"));
        git.add().addPath("b").call();
        git.commit().message("commit").call();
        
        git.updateRef().refName("refs/updates/1").revision("HEAD").call();
        
        FileUtils.touchFile(new File(git.repoDir(), "c"));
        git.add().addPath("c").call();
        git.commit().message("commit").call();
        
        git.updateRef().refName("refs/updates/2").revision("HEAD").call();

        FileUtils.touchFile(new File(git.repoDir(), "d"));
        git.add().addPath("d").call();
        git.commit().message("commit").call();
        
        git.updateRef().refName("refs/updates/3").revision("HEAD").call();
        
        git.checkout().revision("master").call();
        
        FileUtils.touchFile(new File(git.repoDir(), "e"));
        git.add().addPath("e").call();
        git.commit().message("commit").call();
        
        git.merge().revision("refs/updates/2").call();

        PowerMockito.mockStatic(AppLoader.class);
        Mockito.when(AppLoader.getInstance(StorageManager.class)).thenReturn(storageManager);
        
        Mockito.when(storageManager.getStorage(Mockito.any(Project.class)))
                .thenReturn(new ProjectStorage(projectDir));
        
        MergeRequest mergeRequest = new MergeRequest();
        Branch destBranch = new Branch();
        destBranch.setName("master");
        destBranch.setProject(new Project());
        mergeRequest.setDestination(destBranch);
        
        MergeRequestUpdate update = new MergeRequestUpdate();
        update.setId(1L);
        update.setRequest(mergeRequest);
        mergeRequest.getUpdates().add(update);

        update = new MergeRequestUpdate();
        update.setId(2L);
        update.setRequest(mergeRequest);
        mergeRequest.getUpdates().add(update);
        
        update = new MergeRequestUpdate();
        update.setId(3L);
        update.setRequest(mergeRequest);
        mergeRequest.getUpdates().add(update);

        Assert.assertEquals(mergeRequest.getEffectiveUpdates().size(), 2);
        Assert.assertEquals(mergeRequest.getEffectiveUpdates().get(0).getId().longValue(), 3L);
    }

}
