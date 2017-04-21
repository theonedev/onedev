package com.gitplex.server.model.support;

import java.io.Serializable;
import java.util.Date;

import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import com.gitplex.server.model.Account;

@Embeddable
public class LastEvent implements Serializable {

	private static final long serialVersionUID = 1L;

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="LAST_EVENT_USER")
	private Account user;
	
	@Column(name="LAST_EVENT_USER_NAME")
	private String userName;

	@Column(name="LAST_EVENT_TYPE")
	private String type;

	@Column(name="LAST_EVENT_DATE")
	private Date date;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	@Nullable
	public Account getUser() {
		return user;
	}

	public void setUser(Account user) {
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
