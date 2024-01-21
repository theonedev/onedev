package io.onedev.server.event.project;

import java.util.Date;

import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;

import io.onedev.server.OneDev;
import io.onedev.server.web.UrlManager;
import io.onedev.server.git.GitUtils;
import io.onedev.server.model.Project;
import io.onedev.server.util.CommitAware;
import io.onedev.server.util.ProjectScopedCommit;
import io.onedev.server.util.commenttext.CommentText;
import io.onedev.server.util.commenttext.PlainText;

public class RefUpdated extends ProjectEvent implements CommitAware {
	
	private static final long serialVersionUID = 1L;

	private final String refName;
	
	private final ObjectId oldCommitId;
	
	private final ObjectId newCommitId;
	
	private transient ProjectScopedCommit commit;
	
	public RefUpdated(Project project, String refName, ObjectId oldCommitId, ObjectId newCommitId) {
		super(null, new Date(), project);
		this.refName = refName;
		this.oldCommitId = oldCommitId;
		this.newCommitId = newCommitId;
	}
	
	public static boolean isValidRef(String refName) {
		return refName.startsWith(Constants.R_HEADS) || refName.startsWith(Constants.R_TAGS);
	}

	public String getRefName() {
		return refName;
	}

	public ObjectId getOldCommitId() {
		return oldCommitId;
	}

	public ObjectId getNewCommitId() {
		return newCommitId;
	}

	@Override
	public Project getProject() {
		Project project = super.getProject();
		if (!newCommitId.equals(ObjectId.zeroId())) 
			project.cacheObjectId(refName, newCommitId);
		else
			project.cacheObjectId(refName, null);
		return project;
	}

	@Override
	public ProjectScopedCommit getCommit() {
		if (commit == null)
			commit = new ProjectScopedCommit(getProject(), newCommitId);
		return commit;
	}

	@Override
	public String getActivity() {
		return "Git ref updated";
	}

	@Override
	protected CommentText newCommentText() {
		if (!newCommitId.equals(ObjectId.zeroId())) { 
			RevCommit commit = getProject().getRevCommit(newCommitId, false);
			if (commit != null) {
				String detailMessage = GitUtils.getDetailMessage(commit);
				if (detailMessage != null)
					return new PlainText(detailMessage);
			}
		}
		return null;
	}

	@Override
	public String getUrl() {
		if (newCommitId != null)
			return OneDev.getInstance(UrlManager.class).urlFor(getProject(), newCommitId);
		else
			throw new UnsupportedOperationException();
	}
	
}
