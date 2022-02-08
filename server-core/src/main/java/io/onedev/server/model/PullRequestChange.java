package io.onedev.server.model;

import java.util.Date;

import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.apache.commons.lang3.StringUtils;

import io.onedev.server.model.support.pullrequest.changedata.PullRequestChangeData;

@Entity
@Table(indexes={@Index(columnList="o_request_id"), @Index(columnList="o_user_id")})
public class PullRequestChange extends AbstractEntity {

	private static final long serialVersionUID = 1L;

	private static final int MAX_COMMENT_LEN = 14000;
	
	public static final int DIFF_CONTEXT_SIZE = 3;
	
	public static final String PROP_COMMENT = "comment";
	
	@ManyToOne
	@JoinColumn(nullable=false)
	private PullRequest request;
	
	@ManyToOne(fetch=FetchType.LAZY)
	private User user;
	
	@Column(nullable=false)
	private Date date = new Date();
	
	@Column(length=MAX_COMMENT_LEN)
	private String comment;
	
	@Lob
	@Column(length=65535)
	private PullRequestChangeData data;

	public PullRequest getRequest() {
		return request;
	}

	public void setRequest(PullRequest request) {
		this.request = request;
	}

	public PullRequestChangeData getData() {
		return data;
	}

	public void setData(PullRequestChangeData data) {
		this.data = data;
	}

	@Nullable
	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	@Nullable
	public String getComment() {
		return comment;
	}

	public void setComment(@Nullable String comment) {
		if (comment != null)
			this.comment = StringUtils.abbreviate(comment, MAX_COMMENT_LEN);
		else
			this.comment = null;
	}
	
	public String getAnchor() {
		return getClass().getSimpleName() + "-" + getId();
	}
	
}
