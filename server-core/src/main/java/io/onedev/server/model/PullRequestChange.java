package io.onedev.server.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import io.onedev.server.model.support.pullrequest.changedata.PullRequestChangeData;

@Entity
@Table(indexes={@Index(columnList="o_request_id"), @Index(columnList="o_user_id")})
public class PullRequestChange extends AbstractEntity {

	private static final long serialVersionUID = 1L;
	
	public static final String PROP_REQUEST = "request";
	
	public static final String PROP_USER = "user";
	
	public static final String PROP_DATE = "date";
	
	@ManyToOne
	@JoinColumn(nullable=false)
	private PullRequest request;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private User user;
	
	@Column(nullable=false)
	private Date date = new Date();
	
	@Lob
	@Column(length=1048576, nullable=false)
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

	public String getAnchor() {
		return getClass().getSimpleName() + "-" + getId();
	}
	
	public boolean isMinor() {
		return getData().isMinor();
	}
	
}
