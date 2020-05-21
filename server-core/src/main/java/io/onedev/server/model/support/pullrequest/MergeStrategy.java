package io.onedev.server.model.support.pullrequest;

import javax.annotation.Nullable;

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;

import io.onedev.commons.utils.WordUtils;
import io.onedev.server.OneDev;
import io.onedev.server.git.GitUtils;
import io.onedev.server.model.PullRequest;

public enum MergeStrategy {
	CREATE_MERGE_COMMIT("Add all commits from source branch to target branch with a merge commit.") {

		@Override
		public ObjectId merge(PullRequest request, String commitMessage) {
			PersonIdent user = new PersonIdent(OneDev.NAME, "");
			Repository repository = request.getTargetProject().getRepository();
			ObjectId requestHead = request.getLatestUpdate().getHeadCommit();
			ObjectId targetHead = request.getTarget().getObjectId();
			return GitUtils.merge(repository, targetHead, requestHead, false, user, user,
						commitMessage, false);
		}
		
	}, 
	CREATE_MERGE_COMMIT_IF_NECESSARY("Only create merge commit if target branch can not be fast-forwarded to source branch") {

		@Override
		public ObjectId merge(PullRequest request, String commitMessage) {
			Repository repository = request.getTargetProject().getRepository();
			ObjectId requestHead = request.getLatestUpdate().getHeadCommit();
			ObjectId targetHead = request.getTarget().getObjectId();
			if (GitUtils.isMergedInto(repository, null, targetHead, requestHead)) {
				return requestHead;
			} else {
				PersonIdent user = new PersonIdent(OneDev.NAME, "");
				return GitUtils.merge(repository, targetHead, requestHead, false, user, user,
							commitMessage, false);
			}
		}
		
	},
	SQUASH_SOURCE_BRANCH_COMMITS("Squash all commits from source branch into a single commit in target branch") {

		@Override
		public ObjectId merge(PullRequest request, String commitMessage) {
			Repository repository = request.getTargetProject().getRepository();
			ObjectId requestHead = request.getLatestUpdate().getHeadCommit();
			ObjectId targetHead = request.getTarget().getObjectId();
			PersonIdent committer = new PersonIdent(OneDev.NAME, "");
			PersonIdent author = request.getSubmitter().asPerson();
			return GitUtils.merge(repository, targetHead, requestHead, true, committer, author,
						commitMessage, false);
		}
		
	},
	REBASE_SOURCE_BRANCH_COMMITS("Rebase all commits from source branch onto target branch") {

		@Override
		public ObjectId merge(PullRequest request, String commitMessage) {
			Repository repository = request.getTargetProject().getRepository();
			ObjectId requestHead = request.getLatestUpdate().getHeadCommit();
			ObjectId targetHead = request.getTarget().getObjectId();
			PersonIdent user = new PersonIdent(OneDev.NAME, "");
			return GitUtils.rebase(repository, requestHead, targetHead, user);
		}
		
	};

	private final String description;
	
	MergeStrategy(String description) {
		this.description = description;
	}
	
	public String getDescription() {
		return description;
	}
	
	@Override
	public String toString() {
		return WordUtils.toWords(name());
	}
	
	public static MergeStrategy fromString(String displayName) {
		return MergeStrategy.valueOf(WordUtils.toUnderscored(displayName));
	}

	@Nullable
	public abstract ObjectId merge(PullRequest request, String commitMessage);
	
}