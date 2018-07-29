package io.onedev.server.model.support;

import java.io.Serializable;
import java.util.Date;

import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Embeddable;

import io.onedev.server.model.User;

@Embeddable
public class LastActivity implements Serializable {

	private static final long serialVersionUID = 1L;

	@Column(name="LAST_ACT_USER")
	private String userName;

	@Column(name="LAST_ACT_ACTION")
	private String description;

	@Column(name="LAST_ACT_DATE")
	private Date date;

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	@Nullable
	public String getUserName() {
		return userName;
	}

	public void setUserName(@Nullable String userName) {
		this.userName = userName;
	}

	public void setUser(@Nullable User user) {
		userName = user!=null?user.getDisplayName():null;
	}
	
}
