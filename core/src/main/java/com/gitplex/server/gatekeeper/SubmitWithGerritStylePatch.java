package com.gitplex.server.gatekeeper;

import java.io.IOException;

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.revwalk.RevWalkUtils;

import com.gitplex.server.gatekeeper.checkresult.GateCheckResult;
import com.gitplex.server.model.Account;
import com.gitplex.server.model.Depot;
import com.gitplex.server.model.PullRequest;
import com.gitplex.server.util.editable.annotation.Editable;
import com.google.common.collect.Lists;

@Editable(order=3000, icon="fa-ext fa-file-diff", description="This gatekeeper will be passed if the pull request "
		+ "is submitted with gerrit style patch, that is, only one commit is submitted for review at "
		+ "a time, and subsequent updates to the review should be done by amending previous commit")
public class SubmitWithGerritStylePatch extends AbstractGateKeeper {

	private static final long serialVersionUID = 1L;
	
	@Override
	protected GateCheckResult doCheckRequest(PullRequest request) {
		try (RevWalk revWalk = new RevWalk(request.getWorkDepot().getRepository())) {
			RevCommit requestHeadCommit = revWalk.parseCommit(ObjectId.fromString(request.getHeadCommitHash()));
			RevCommit branchHeadCommit = revWalk.parseCommit(ObjectId.fromString(request.getBaseCommitHash()));
			if (RevWalkUtils.count(revWalk, requestHeadCommit, branchHeadCommit) > 1) {
				return failed(Lists.newArrayList("Please squash/rebase your commits"));
			} else {
				return passed(Lists.newArrayList("No more than one commit"));
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected GateCheckResult doCheckFile(Account user, Depot depot, String branch, String file) {
		return ignored();
	}

	@Override
	protected GateCheckResult doCheckPush(Account user, Depot depot, String refName, 
			ObjectId oldObjectId, ObjectId newObjectId) {
		return ignored();
	}

}
