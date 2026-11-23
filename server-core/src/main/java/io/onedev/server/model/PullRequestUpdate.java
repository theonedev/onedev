package io.onedev.server.model;

import io.onedev.server.OneDev;
import io.onedev.server.git.service.GitService;
import jakarta.persistence.*;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.Serial;
import java.util.*;

@Entity
@Table(indexes={@Index(columnList="request_id"), @Index(columnList="date")})
public class PullRequestUpdate extends AbstractEntity {

	@Serial
	private static final long serialVersionUID = 1L;

	public static final String REFS_PREFIX = "refs/updates/";
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private PullRequest request;
	
	@Column(nullable=false)
	private String headCommitHash;

	@Column(nullable=false)
	private Date date = new Date();
	
	private transient Collection<String> changedFiles;

	private transient List<RevCommit> commits;
	
	public PullRequest getRequest() {
		return request;
	}

	public void setRequest(PullRequest request) {
		this.request = request;
	}

	public String getHeadCommitHash() {
		return headCommitHash;
	}
	
	public void setHeadCommitHash(String headCommitHash) {
		this.headCommitHash = headCommitHash;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getHeadRef() {
		return REFS_PREFIX + Objects.requireNonNull(getId());
	}
		
	public Collection<String> getChangedFiles() {
		if (changedFiles == null) {
			var project = getRequest().getProject();
			var targetHeadCommitId = getRequest().getTarget().getObjectId();
			var baseCommitId = getBaseCommitId();
			var headCommitId = getHeadCommitId();
			if (project.getRevCommit(headCommitId, false) == null) {
				changedFiles = new ArrayList<>();
			} else {
				if (project.getRevCommit(baseCommitId, false) == null)
					baseCommitId = getRequest().getMergeBase();
				changedFiles = getGitService().getChangedFilesAfterMerge(
						project, baseCommitId, headCommitId, targetHeadCommitId);
			}
		}
		return changedFiles;
	}

	public List<RevCommit> getCommits() {
		if (commits == null) {
			var project = getRequest().getProject();
			var baseCommitId = getBaseCommitId();
			var headCommitId = getHeadCommitId();
			var reachableCommits = getGitService().getReachableCommits(
					project, Set.of(headCommitId), Set.of(baseCommitId));
			commits = new ArrayList<>(getRequest().getCommits());
			commits.retainAll(reachableCommits);
		}
		return commits;
	}

	private ObjectId getBaseCommitId() {
		var updates = getRequest().getSortedUpdates();
		var index = updates.indexOf(this);
		if (index > 0)
			return updates.get(index - 1).getHeadCommitId();
		else
			return getRequest().getMergeBase();
	}
	
	public ObjectId getHeadCommitId() {
		return ObjectId.fromString(getHeadCommitHash());
	}
	
	public RevCommit getHeadCommit() {
		return request.getTargetProject().getRevCommit(ObjectId.fromString(getHeadCommitHash()), true);
	}

	private GitService getGitService() {
		return OneDev.getInstance(GitService.class);
	}
	
}
