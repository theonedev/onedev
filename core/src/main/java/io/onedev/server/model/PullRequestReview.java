package io.onedev.server.model;

import java.util.Date;
import java.util.Optional;

import javax.annotation.Nullable;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import io.onedev.server.model.support.pullrequest.MergePreview;
import io.onedev.server.model.support.pullrequest.ReviewResult;

@Entity
@Table(
		indexes={@Index(columnList="g_user_id"), @Index(columnList="g_request_id")},
		uniqueConstraints={@UniqueConstraint(columnNames={"g_user_id", "g_request_id"})}
)
public class PullRequestReview extends AbstractEntity {

	private static final long serialVersionUID = 1L;
	
	public static final String ATTR_USER = "user";
	
	public static final String PATH_RESULT = "result";
	
	public static final String ATTR_RESULT_APPROVED = "result.approved";
	
	public static final String ATTR_EXCLUDE_DATE = "excludeDate";
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private User user;

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private PullRequest request;
	
	private Date excludeDate;
	
	@Embedded
	private ReviewResult result;
	
	private transient Optional<PullRequestUpdate> updateOpt;
	
	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}
	
	public PullRequest getRequest() {
		return request;
	}

	public void setRequest(PullRequest request) {
		this.request = request;
	}

	public ReviewResult getResult() {
		return result;
	}

	public void setResult(ReviewResult result) {
		this.result = result;
	}

	public Date getExcludeDate() {
		return excludeDate;
	}

	public void setExcludeDate(Date excludeDate) {
		this.excludeDate = excludeDate;
		result = null;
	}

	@Nullable
	public PullRequestUpdate getUpdate() {
		if (updateOpt == null) {
			PullRequestUpdate update = null;
			if (result != null) {
				MergePreview preview = request.getMergePreview();
				if (preview != null && result.getCommit().equals(preview.getMerged())) {
					update = request.getLatestUpdate();
				} else {
					for (PullRequestUpdate each: request.getUpdates()) {
						if (each.getHeadCommitHash().equals(result.getCommit())) {
							update = each;
							break;
						}
					}
				}
			}
			updateOpt = Optional.ofNullable(update);
		}
		return updateOpt.orElse(null);
	}
	
}
