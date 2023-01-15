package io.onedev.server.model.support;

import java.io.Serializable;
import java.util.Date;

import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.OptimisticLock;

import io.onedev.server.model.User;

@Embeddable
public class LastActivity implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String COLUMN_USER = "LAST_ACTIVITY_USER";
	
	public static final String COLUMN_DATE = "LAST_ACTIVITY_DATE";
	
	public static final String COLUMN_DESCRIPTION = "LAST_ACTIVITY_DESCRIPTION";

	public static final String PROP_DATE = "date";
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name=COLUMN_USER)
	@OptimisticLock(excluded=true)
	private User user;
	
	@Column(name=COLUMN_DATE, nullable=false)
	@OptimisticLock(excluded=true)
	private Date date;
	
	@Column(name= COLUMN_DESCRIPTION, nullable=false)
	@OptimisticLock(excluded=true)
	private String description;

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

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}
