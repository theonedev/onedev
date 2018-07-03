package io.onedev.server.model.support;

import java.io.Serializable;
import java.util.Date;

import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import io.onedev.server.model.User;

@Embeddable
public class LastActivity implements Serializable {

	private static final long serialVersionUID = 1L;

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="LAST_ACT_USER")
	private User user;
	
	@Column(name="LAST_ACT_USER_NAME")
	private String userName;

	@Column(name="LAST_ACT_ACTION")
	private String description;

	@Column(name="LAST_ACT_DATE")
	private long date;

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Date getDate() {
		return new Date(date);
	}

	public void setDate(Date date) {
		this.date = date.getTime();
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
	
}
