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

import org.hibernate.annotations.DynamicUpdate;

import io.onedev.server.model.support.pullrequest.actiondata.ActionData;

@Entity
@Table(indexes={@Index(columnList="g_request_id"), @Index(columnList="g_user_id")})
@DynamicUpdate
public class PullRequestAction extends AbstractEntity {

	private static final long serialVersionUID = 1L;

	@ManyToOne
	@JoinColumn(nullable=false)
	private PullRequest request;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn
	private User user;
	
	private String userName;
	
	@Column(nullable=false)
	private Date date;
	
	@Lob
	@Column(length=65535)
	private ActionData data;

	public PullRequest getRequest() {
		return request;
	}

	public void setRequest(PullRequest request) {
		this.request = request;
	}

	public ActionData getData() {
		return data;
	}

	public void setData(ActionData data) {
		this.data = data;
	}

	@Nullable
	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	@Nullable
	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
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
	
}
