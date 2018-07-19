package io.onedev.server.model.support.pullrequest;

import javax.annotation.Nullable;

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;

import io.onedev.server.OneDev;
import io.onedev.server.exception.OneException;
import io.onedev.server.git.GitUtils;
import io.onedev.server.model.PullRequest;

public enum MergeStrategy {
	ALWAYS_MERGE("Add all commits from source branch to target branch with a merge commit.") {

		@Override
		public String toString() {
			return "Create Merge Commit";
		}

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
	MERGE_IF_NECESSARY("Only create merge commit if target branch can not be fast-forwarded to source branch") {

		@Override
		public String toString() {
			return "Create Merge Commit If Necessary";
		}

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
	SQUASH_MERGE("Squash all commits from source branch into a single commit in target branch") {

		@Override
		public String toString() {
			return "Squash Source Branch Commits";
		}

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
	REBASE_MERGE("Rebase all commits from source branch onto target branch") {

		@Override
		public String toString() {
			return "Rebase Source Branch Commits";
		}

		@Override
		public ObjectId merge(PullRequest request) {
			Repository repository = request.getTargetProject().getRepository();
			ObjectId requestHead = request.getHeadCommit();
			ObjectId targetHead = request.getTarget().getObjectId();
			PersonIdent user = new PersonIdent(OneDev.NAME, "");
			return GitUtils.rebase(repository, requestHead, targetHead, user);
		}
		
	},
	DO_NOT_MERGE("Do not merge now, only for review") {

		@Override
		public String toString() {
			return "Do Not Merge";
		}

		@Override
		public ObjectId merge(PullRequest request) {
			throw new UnsupportedOperationException();
		}
		
	};

	private final String description;
	
	MergeStrategy(String description) {
		this.description = description;
	}
	
	public String getDescription() {
		return description;
	}
	
	public static MergeStrategy from(String displayName) {
		for (MergeStrategy each: MergeStrategy.values()) {
			if (each.toString().equals(displayName))
				return each;
		}
		throw new OneException("Unable to find merge strategy with display name: " + displayName);
	}

	@Nullable
	public abstract ObjectId merge(PullRequest request);
	
}