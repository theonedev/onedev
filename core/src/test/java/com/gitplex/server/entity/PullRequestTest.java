package com.gitplex.server.entity;

import java.io.File;

import org.eclipse.jgit.lib.Repository;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

import com.gitplex.launcher.loader.AppLoader;
import com.gitplex.server.git.AbstractGitTest;
import com.gitplex.server.manager.DepotManager;
import com.gitplex.server.model.Depot;
import com.gitplex.server.model.PullRequest;
import com.gitplex.server.model.PullRequestUpdate;

public class PullRequestTest extends AbstractGitTest {

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
        DepotManager depotManager = Mockito.mock(DepotManager.class);    
        Mockito.when(depotManager.load(Matchers.any())).thenReturn(depot);
        Mockito.when(AppLoader.getInstance(DepotManager.class)).thenReturn(depotManager);
    }

    @Test
    public void shouldReturnAllUpdatesAsEffectiveIfTheyAreFastForward() throws Exception {
        PullRequest request = new PullRequest();
        request.setTargetDepot(depot);
        request.setTargetBranch("master");

        addFileAndCommit("a", "", "commit");
        
        git.checkout().setName("dev").setCreateBranch(true).call();

        addFileAndCommit("b", "", "commit");

        depot.cacheObjectId("master", git.getRepository().resolve("master"));
        
        request.setBaseCommitHash(git.getRepository().resolve("master").name());

        PullRequestUpdate update1 = new PullRequestUpdate();
        update1.setId(1L);
        update1.setRequest(request);
        updateRef(update1.getHeadRef(), "HEAD", null);
        update1.setHeadCommitHash(git.getRepository().resolve(update1.getHeadRef()).name());
        request.addUpdate(update1);

        addFileAndCommit("c", "", "commit");
        
        PullRequestUpdate update2 = new PullRequestUpdate();
        update2.setId(2L);
        update2.setRequest(request);
        updateRef(update2.getHeadRef(), "HEAD", null);
        update2.setHeadCommitHash(git.getRepository().resolve(update2.getHeadRef()).name());
        request.addUpdate(update2);
        
        Assert.assertEquals(request.getEffectiveUpdates().size(), 2);
    }

    @Test
    public void shouldReturnLatestUpdateAsEffectiveIfAllOthersHaveBeenMerged() throws Exception {
        PullRequest request = new PullRequest();
        request.setTargetDepot(depot);
        request.setTargetBranch("master");

        addFileAndCommit("a", "", "master:1");
        
        git.checkout().setName("dev").setCreateBranch(true).call();

        addFileAndCommit("b", "", "dev:2");
        
        request.setBaseCommitHash(git.getRepository().resolve("master").name());

        addFileAndCommit("c", "", "dev:3");
        
        PullRequestUpdate update1 = new PullRequestUpdate();
        update1.setId(1L);
        update1.setRequest(request);
        updateRef(update1.getHeadRef(), "HEAD", null);
        update1.setHeadCommitHash(git.getRepository().resolve(update1.getHeadRef()).name());
        String secondRef = update1.getHeadRef();
        request.addUpdate(update1);

        addFileAndCommit("d", "", "dev:4");
        
        PullRequestUpdate update2 = new PullRequestUpdate();
        update2.setId(2L);
        update2.setRequest(request);
        updateRef(update2.getHeadRef(), "HEAD", null);
        update2.setHeadCommitHash(git.getRepository().resolve(update2.getHeadRef()).name());
        request.addUpdate(update2);
        
        git.checkout().setName("master").call();

        addFileAndCommit("e", "", "master:5");
        
        git.merge().include(git.getRepository().resolve(secondRef)).setCommit(true).call();

        depot.cacheObjectId("master", git.getRepository().resolve("master"));

        Assert.assertEquals(1, request.getEffectiveUpdates().size());
        Assert.assertEquals(2L, request.getEffectiveUpdates().get(0).getId().longValue());
    }

}