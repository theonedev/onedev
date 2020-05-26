package io.onedev.server.model;

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

import io.onedev.server.model.support.pullrequest.ReviewResult;

@Entity
@Table(
		indexes={@Index(columnList="o_user_id"), @Index(columnList="o_request_id")},
		uniqueConstraints={@UniqueConstraint(columnNames={"o_user_id", "o_request_id"})}
)
public class PullRequestReview extends AbstractEntity {

	private static final long serialVersionUID = 1L;
	
	public static final String PROP_REQUEST = "request";
	
	public static final String PROP_USER = "user";
	
	public static final String PROP_RESULT = "result";
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private User user;

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private PullRequest request;
	
	@Embedded
	private ReviewResult result;
	
	private transient Optional<PullRequestUpdate> updateOptional;
	
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

	@Nullable
	public ReviewResult getResult() {
		return result;
	}

	public void setResult(@Nullable ReviewResult result) {
		this.result = result;
	}

	@Nullable
	public PullRequestUpdate getUpdate() {
		if (updateOptional == null) {
			PullRequestUpdate update = null;
			if (result != null) {
				for (PullRequestUpdate each: request.getUpdates()) {
					if (each.getHeadCommitHash().equals(result.getCommit())) {
						update = each;
						break;
					}
				}
			}
			updateOptional = Optional.ofNullable(update);
		}
		return updateOptional.orElse(null);
	}
	
}
