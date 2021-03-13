package io.onedev.server.model.support;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.OptimisticLock;

import io.onedev.server.model.User;

@Embeddable
public class LastUpdate implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String COLUMN_USER = "LAST_UPDATE_USER";
	
	public static final String COLUMN_USER_NAME = "LAST_UPDATE_USER_NAME";
	
	public static final String COLUMN_DATE = "LAST_UPDATE_DATE";
	
	public static final String COLUMN_ACTIVITY = "LAST_UPDATE_ACTIVITY";

	public static final String PROP_DATE = "date";
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name=COLUMN_USER)
	@OptimisticLock(excluded=true)
	private User user;
	
	@Column(name=COLUMN_USER_NAME)
	@OptimisticLock(excluded=true)
	private String userName;

	@Column(name=COLUMN_DATE, nullable=false)
	@OptimisticLock(excluded=true)
	private Date date;
	
	@Column(name=COLUMN_ACTIVITY, nullable=false)
	@OptimisticLock(excluded=true)
	private String activity;

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

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

	public String getActivity() {
		return activity;
	}

	public void setActivity(String activity) {
		this.activity = activity;
	}

}
