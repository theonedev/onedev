package io.onedev.server.model.support.pullrequest;

import javax.annotation.Nullable;

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;

import io.onedev.commons.utils.WordUtils;
import io.onedev.server.OneDev;
import io.onedev.server.git.service.GitService;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.User;

public enum MergeStrategy {
	
	CREATE_MERGE_COMMIT("Add all commits from source branch to target branch with a merge commit.") {

		@Override
		public ObjectId merge(PullRequest request, String commitMessage) {
			PersonIdent user = new PersonIdent(User.SYSTEM_NAME, User.SYSTEM_EMAIL_ADDRESS);
			ObjectId requestHead = request.getLatestUpdate().getHeadCommit();
			ObjectId targetHead = request.getTarget().getObjectId();
			return getGitService().merge(request.getTargetProject(), targetHead, requestHead, 
					false, user, user, commitMessage, false);
		}
		
	}, 
	CREATE_MERGE_COMMIT_IF_NECESSARY("Only create merge commit if target branch can not be fast-forwarded to source branch") {

		@Override
		public ObjectId merge(PullRequest request, String commitMessage) {
			ObjectId requestHead = request.getLatestUpdate().getHeadCommit();
			ObjectId targetHead = request.getTarget().getObjectId();
			Project project = request.getTargetProject();
			if (getGitService().isMergedInto(project, null, targetHead, requestHead)) {
				return requestHead;
			} else {
				PersonIdent user = new PersonIdent(User.SYSTEM_NAME, User.SYSTEM_EMAIL_ADDRESS);
				return getGitService().merge(project, targetHead, requestHead, false, user, user,
							commitMessage, false);
			}
		}
		
	},
	SQUASH_SOURCE_BRANCH_COMMITS("Squash all commits from source branch into a single commit in target branch") {

		@Override
		public ObjectId merge(PullRequest request, String commitMessage) {
			ObjectId requestHead = request.getLatestUpdate().getHeadCommit();
			ObjectId targetHead = request.getTarget().getObjectId();
			PersonIdent committer = new PersonIdent(User.SYSTEM_NAME, User.SYSTEM_EMAIL_ADDRESS);
			return getGitService().merge(request.getTargetProject(), targetHead, requestHead, true, 
					committer, request.getSubmitter().asPerson(), commitMessage, false);
		}
		
	},
	REBASE_SOURCE_BRANCH_COMMITS("Rebase all commits from source branch onto target branch") {

		@Override
		public ObjectId merge(PullRequest request, String commitMessage) {
			ObjectId requestHead = request.getLatestUpdate().getHeadCommit();
			ObjectId targetHead = request.getTarget().getObjectId();
			PersonIdent user = new PersonIdent(User.SYSTEM_NAME, User.SYSTEM_EMAIL_ADDRESS);
			return getGitService().rebase(request.getTargetProject(), requestHead, targetHead, user);
		}
		
	};

	private final String description;
	
	MergeStrategy(String description) {
		this.description = description;
	}
	
	public String getDescription() {
		return description;
	}
	
	private static GitService getGitService() {
		return OneDev.getInstance(GitService.class);
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