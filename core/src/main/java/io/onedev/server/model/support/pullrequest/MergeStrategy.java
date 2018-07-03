package io.onedev.server.model.support.pullrequest;

import javax.annotation.Nullable;

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;

import io.onedev.server.OneDev;
import io.onedev.server.git.GitUtils;
import io.onedev.server.model.PullRequest;

public enum MergeStrategy {
	ALWAYS_MERGE("Create Merge Commit", 
			"Add all commits from source branch to target branch with a merge commit.") {

		@Override
		public ObjectId merge(PullRequest request) {
			PersonIdent user = new PersonIdent(OneDev.NAME, "");
			Repository repository = request.getTargetProject().getRepository();
			ObjectId requestHead = request.getHeadCommit();
			ObjectId targetHead = request.getTarget().getObjectId();
			return GitUtils.merge(repository, requestHead, targetHead, false, user, 
						request.getCommitMessage());
		}
		
	}, 
	MERGE_IF_NECESSARY("Create Merge Commit If Necessary", 
			"Only create merge commit if target branch can not be fast-forwarded to source branch") {

		@Override
		public ObjectId merge(PullRequest request) {
			Repository repository = request.getTargetProject().getRepository();
			ObjectId requestHead = request.getHeadCommit();
			ObjectId targetHead = request.getTarget().getObjectId();
			if (GitUtils.isMergedInto(repository, null, targetHead, requestHead)) {
				return requestHead;
			} else {
				PersonIdent user = new PersonIdent(OneDev.NAME, "");
				return GitUtils.merge(repository, requestHead, targetHead, false, user, 
							request.getCommitMessage());
			}
		}
		
	},
	SQUASH_MERGE("Squash Source Branch Commits", 
			"Squash all commits from source branch into a single commit in target branch") {

		@Override
		public ObjectId merge(PullRequest request) {
			Repository repository = request.getTargetProject().getRepository();
			ObjectId requestHead = request.getHeadCommit();
			ObjectId targetHead = request.getTarget().getObjectId();
			PersonIdent user = new PersonIdent(OneDev.NAME, "");
			return GitUtils.merge(repository, requestHead, targetHead, true, user, 
						request.getCommitMessage());
		}
		
	},
	REBASE_MERGE("Rebase Source Branch Commits", "Rebase all commits from source branch onto target branch") {

		@Override
		public ObjectId merge(PullRequest request) {
			Repository repository = request.getTargetProject().getRepository();
			ObjectId requestHead = request.getHeadCommit();
			ObjectId targetHead = request.getTarget().getObjectId();
			PersonIdent user = new PersonIdent(OneDev.NAME, "");
			return GitUtils.rebase(repository, requestHead, targetHead, user);
		}
		
	},
	DO_NOT_MERGE("Do Not Merge", "Do not merge now, only for review") {

		@Override
		public ObjectId merge(PullRequest request) {
			throw new UnsupportedOperationException();
		}
		
	};

	private final String displayName;
	
	private final String description;
	
	MergeStrategy(String displayName, String description) {
		this.displayName = displayName;
		this.description = description;
	}
	
	public String getDisplayName() {
		return displayName;
	}

	public String getDescription() {
		return description;
	}

	@Override
	public String toString() {
		return displayName;
	}
	
	@Nullable
	public abstract ObjectId merge(PullRequest request);
	
}