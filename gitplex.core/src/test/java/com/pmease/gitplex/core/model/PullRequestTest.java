package com.pmease.gitplex.core.model;

import java.io.IOException;

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryCache;
import org.eclipse.jgit.lib.RepositoryCache.FileKey;
import org.eclipse.jgit.util.FS;
import org.junit.Assert;
import org.junit.Test;

import com.pmease.commons.git.AbstractGitTest;
import com.pmease.commons.git.Git;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.entity.PullRequest;
import com.pmease.gitplex.core.entity.PullRequestUpdate;

public class PullRequestTest extends AbstractGitTest {

    private Depot depot;

    @Override
    public void setup() {
    	super.setup();
    	
        depot = new Depot() {

			private static final long serialVersionUID = 1L;

			@Override
			public Git git() {
				return git;
			}

			@Override
			public Repository getRepository() {
				try {
					return RepositoryCache.open(FileKey.lenient(git.depotDir(), FS.DETECTED));
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
        	
        };
    }

    @Test
    public void shouldReturnAllUpdatesAsEffectiveIfTheyAreFastForward() {
        PullRequest request = new PullRequest();
        request.setTargetDepot(depot);
        request.setTargetBranch("master");

        addFileAndCommit("a", "", "commit");
        
        git.checkout("HEAD", "dev");

        addFileAndCommit("b", "", "commit");

        depot.cacheObjectId("master", ObjectId.fromString(git.parseRevision("master", true)));
        
        request.setBaseCommitHash(git.parseRevision("master", true));

        PullRequestUpdate update1 = new PullRequestUpdate();
        update1.setId(1L);
        update1.setRequest(request);
        git.updateRef(update1.getHeadRef(), "HEAD", null, null);
        update1.setHeadCommitHash(git.parseRevision(update1.getHeadRef(), true));
        request.addUpdate(update1);

        addFileAndCommit("c", "", "commit");
        
        PullRequestUpdate update2 = new PullRequestUpdate();
        update2.setId(2L);
        update2.setRequest(request);
        git.updateRef(update2.getHeadRef(), "HEAD", null, null);
        update2.setHeadCommitHash(git.parseRevision(update2.getHeadRef(), true));
        request.addUpdate(update2);
        
        Assert.assertEquals(request.getEffectiveUpdates().size(), 2);
    }

    @Test
    public void shouldReturnLatestUpdateAsEffectiveIfAllOthersHaveBeenMerged() {
        PullRequest request = new PullRequest();
        request.setTargetDepot(depot);
        request.setTargetBranch("master");

        addFileAndCommit("a", "", "master:1");
        
        git.checkout("HEAD", "dev");

        addFileAndCommit("b", "", "dev:2");
        
        request.setBaseCommitHash(git.parseRevision("master", true));

        addFileAndCommit("c", "", "dev:3");
        
        PullRequestUpdate update1 = new PullRequestUpdate();
        update1.setId(1L);
        update1.setRequest(request);
        git.updateRef(update1.getHeadRef(), "HEAD", null, null);
        update1.setHeadCommitHash(git.parseRevision(update1.getHeadRef(), true));
        String secondRef = update1.getHeadRef();
        request.addUpdate(update1);

        addFileAndCommit("d", "", "dev:4");
        
        PullRequestUpdate update2 = new PullRequestUpdate();
        update2.setId(2L);
        update2.setRequest(request);
        git.updateRef(update2.getHeadRef(), "HEAD", null, null);
        update2.setHeadCommitHash(git.parseRevision(update2.getHeadRef(), true));
        request.addUpdate(update2);
        
        git.checkout("master", null);

        addFileAndCommit("e", "", "master:5");
        
        git.merge(secondRef, null, null, null, null);

        depot.cacheObjectId("master", ObjectId.fromString(git.parseRevision("master", true)));

        Assert.assertEquals(1, request.getEffectiveUpdates().size());
        Assert.assertEquals(2L, request.getEffectiveUpdates().get(0).getId().longValue());
    }

}