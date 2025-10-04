package io.onedev.server.model;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import io.onedev.server.OneDev;
import io.onedev.server.service.PullRequestService;
import io.onedev.server.git.service.GitService;

@Entity
@Table(indexes={@Index(columnList="o_request_id"), @Index(columnList="date")})
public class PullRequestUpdate extends AbstractEntity {

	private static final long serialVersionUID = 1L;

	public static final String REFS_PREFIX = "refs/updates/";
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private PullRequest request;
	
	@Column(nullable=false)
	private String headCommitHash;
	
	@Column(nullable=false)
	private String targetHeadCommitHash;
	
	@Column(nullable=false)
	private Date date = new Date();
	
	private transient List<RevCommit> commits;
	
	private transient Collection<String> changedFiles;
	
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
	
	public String getTargetHeadCommitHash() {
		return targetHeadCommitHash;
	}

	public void setTargetHeadCommitHash(String targetHeadCommitHash) {
		this.targetHeadCommitHash = targetHeadCommitHash;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getHeadRef() {
		Preconditions.checkNotNull(getId());
		return REFS_PREFIX + getId();
	}
		
	public Collection<String> getChangedFiles() {
		if (changedFiles == null) {
			ObjectId headCommitId = ObjectId.fromString(getHeadCommitHash());
			ObjectId comparisonBaseCommitId = getPullRequestService().getComparisonBase(
					getRequest(), ObjectId.fromString(getBaseCommitHash()), headCommitId);
			changedFiles = getGitService().getChangedFiles(
					getRequest().getWorkProject(), headCommitId, comparisonBaseCommitId, null);
		}
		return changedFiles;
	}
	
	public String getBaseCommitHash() {
		PullRequest request = getRequest();

		int index = request.getSortedUpdates().indexOf(this);
		if (index > 0)
			return request.getSortedUpdates().get(index-1).getHeadCommitHash();
		else
			return request.getBaseCommitHash();
	}
	
	public RevCommit getHeadCommit() {
		return request.getWorkProject().getRevCommit(ObjectId.fromString(getHeadCommitHash()), true);
	}
	
	public List<RevCommit> getCommits() {
		if (commits == null) {
			Collection<ObjectId> uninterestingCommitIds = Lists.newArrayList(
					ObjectId.fromString(getBaseCommitHash()), 
					ObjectId.fromString(getTargetHeadCommitHash()));
			commits = getGitService().getReachableCommits(
					getRequest().getWorkProject(), 
					Lists.newArrayList(ObjectId.fromString(getHeadCommitHash())), 
					uninterestingCommitIds);
			if (commits.isEmpty()) // in case source branch reverted to a previous update
				commits.add(getGitService().getCommit(getRequest().getWorkProject(), ObjectId.fromString(getHeadCommitHash())));
			Collections.reverse(commits);
		}
		return commits;
	}
	
	private GitService getGitService() {
		return OneDev.getInstance(GitService.class);
	}
	
	private PullRequestService getPullRequestService() {
		return OneDev.getInstance(PullRequestService.class);
	}
	
}
