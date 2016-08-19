package com.pmease.gitplex.core.gatekeeper;

import java.io.IOException;

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.revwalk.RevWalkUtils;

import com.google.common.collect.Lists;
import com.pmease.commons.wicket.editable.annotation.Editable;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.entity.PullRequest;
import com.pmease.gitplex.core.gatekeeper.checkresult.GateCheckResult;

@Editable(order=3000, icon="fa-ext fa-file-diff", description="This gate keeper will be passed if the pull request "
		+ "is submitted with gerrit style patch, that is, only one commit is submitted for review at "
		+ "a time, and subsequent updates to the review should be done by amending previous commit")
public class IfSubmitWithGerritStylePatch extends AbstractGateKeeper {

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
	protected GateCheckResult doCheckPush(Account user, Depot depot, String refName, ObjectId oldCommit, ObjectId newCommit) {
		return ignored();
	}

}
