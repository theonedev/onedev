package com.gitplex.server.gatekeeper;

import java.io.IOException;

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.TreeFilter;
import org.hibernate.validator.constraints.NotEmpty;

import com.google.common.collect.Lists;
import com.gitplex.server.gatekeeper.checkresult.GateCheckResult;
import com.gitplex.server.model.Account;
import com.gitplex.server.model.Depot;
import com.gitplex.server.model.PullRequest;
import com.gitplex.server.model.PullRequestUpdate;
import com.gitplex.server.util.editable.annotation.Editable;
import com.gitplex.server.util.editable.annotation.PathMatch;
import com.gitplex.server.util.includeexclude.IncludeExcludeUtils;
import com.gitplex.server.util.match.ChildAwareMatcher;
import com.gitplex.server.util.match.ParentAwareMatcher;
import com.gitplex.server.util.match.PatternMatcher;

@Editable(order=300, icon="fa-file-text", description=
		"This gatekeeper will be passed if specified files are modified")
public class TouchSpecifiedFiles extends AbstractGateKeeper {

	private static final long serialVersionUID = 1L;
	
	private static final PatternMatcher INCLUDE_MATCHER = new ParentAwareMatcher();
	
	private static final PatternMatcher EXCLUDE_MATCHER = new ChildAwareMatcher();
	
	private String pathMatch;
	
	@Editable(name="Files to Match")
	@PathMatch
	@NotEmpty
	public String getPathMatch() {
		return pathMatch;
	}

	public void setPathMatch(String pathMatch) {
		this.pathMatch = pathMatch;
	}

	private boolean matches(String value) {
		return IncludeExcludeUtils.getIncludeExclude(pathMatch).matches(INCLUDE_MATCHER, EXCLUDE_MATCHER, value);
	}
	
	@Override
	public GateCheckResult doCheckRequest(PullRequest request) {
		for (PullRequestUpdate update: request.getEffectiveUpdates()) {
			for (String file: update.getChangedFiles()) {
				if (matches(file)) {
					request.setReferentialUpdate(update);
					return passed(Lists.newArrayList("Touched files matches '" + pathMatch + "'"));
				}
			}
		}

		return failed(Lists.newArrayList("No touched files match '" + pathMatch + "'"));
	}

	@Override
	protected GateCheckResult doCheckFile(Account user, Depot depot, String branch, String file) {
		if (file != null) {
			if (matches(file)) 
				return passed(Lists.newArrayList("Touched files match '" + pathMatch + "'"));
			else
				return failed(Lists.newArrayList("No touched files match '" + pathMatch + "'"));
		} else {
			return ignored();
		}
	}
	
	@Override
	protected GateCheckResult doCheckPush(Account user, Depot depot, String refName, 
			ObjectId oldObjectId, ObjectId newObjectId) {
		if (!oldObjectId.equals(ObjectId.zeroId()) && !newObjectId.equals(ObjectId.zeroId())) {
			try (TreeWalk treeWalk = new TreeWalk(depot.getRepository())) {
				treeWalk.setFilter(TreeFilter.ANY_DIFF);
				treeWalk.setRecursive(true);
				RevCommit oldCommit = depot.getRevCommit(oldObjectId, false);
				if (oldCommit == null)
					return ignored();
				RevCommit newCommit = depot.getRevCommit(newObjectId, false);
				if (newCommit == null)
					return ignored();
				treeWalk.addTree(oldCommit.getTree());
				treeWalk.addTree(newCommit.getTree());
				while (treeWalk.next()) {
					if (matches(treeWalk.getPathString()))
						return passed(Lists.newArrayList("Touched files match '" + pathMatch + "'"));
				}
				return failed(Lists.newArrayList("No touched files match '" + pathMatch + "'"));
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		} else {
			return ignored();
		}
	}

}
