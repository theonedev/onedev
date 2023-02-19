package io.onedev.server.model;

import io.onedev.server.model.support.pullrequest.changedata.PullRequestChangeData;

import javax.annotation.Nullable;
import javax.persistence.*;
import java.util.Date;

@Entity
@Table(indexes={@Index(columnList="o_request_id"), @Index(columnList="o_user_id")})
public class PullRequestChange extends AbstractEntity {

	private static final long serialVersionUID = 1L;
	
	@ManyToOne
	@JoinColumn(nullable=false)
	private PullRequest request;
	
	@ManyToOne(fetch=FetchType.LAZY)
	private User user;
	
	@Column(nullable=false)
	private Date date = new Date();
	
	@Lob
	@Column(length=65535, nullable=false)
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

	public void setUser(@Nullable User user) {
		this.user = user;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getAnchor() {
		return getClass().getSimpleName() + "-" + getId();
	}
	
	public boolean isMinor() {
		return getData().isMinor();
	}
	
}
