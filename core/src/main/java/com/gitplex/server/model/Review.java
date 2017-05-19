package com.gitplex.server.model;

import java.util.Optional;

import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.gitplex.server.model.support.MergePreview;

@Entity
@Table(
		indexes={@Index(columnList="g_user_id"), @Index(columnList="g_request_id")},
		uniqueConstraints={@UniqueConstraint(columnNames={"g_user_id", "g_request_id", "commit"})}
)
public class Review extends AbstractEntity {

	private static final long serialVersionUID = 1L;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private Account user;

	@Column(nullable=false)
	private String commit;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private PullRequest request;
	
	private boolean checkMerged;
	
	private boolean autoCheck;
	
	private boolean approved;
	
	private transient Optional<PullRequestUpdate> updateOpt;
	
	private String note;
	
	public String getCommit() {
		return commit;
	}

	public void setCommit(String commit) {
		this.commit = commit;
	}

	public Account getUser() {
		return user;
	}

	public void setUser(Account user) {
		this.user = user;
	}
	
	public PullRequest getRequest() {
		return request;
	}

	public void setRequest(PullRequest request) {
		this.request = request;
	}

	public boolean isApproved() {
		return approved;
	}

	public void setApproved(boolean approved) {
		this.approved = approved;
	}

	public boolean isCheckMerged() {
		return checkMerged;
	}

	public void setCheckMerged(boolean checkMerged) {
		this.checkMerged = checkMerged;
	}

	public boolean isAutoCheck() {
		return autoCheck;
	}

	public void setAutoCheck(boolean autoCheck) {
		this.autoCheck = autoCheck;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}
	
	@Nullable
	public PullRequestUpdate getUpdate() {
		if (updateOpt == null) {
			PullRequestUpdate update = null;
			MergePreview preview = request.getMergePreview();
			if (preview != null && commit.equals(preview.getMerged())) {
				update = request.getLatestUpdate();
			} else {
				for (PullRequestUpdate each: request.getUpdates()) {
					if (each.getHeadCommitHash().equals(commit)) {
						update = each;
						break;
					}
				}
			}
			updateOpt = Optional.ofNullable(update);
		}
		return updateOpt.orElse(null);
	}
	
}
