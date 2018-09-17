package io.onedev.server.model.support.pullrequest;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.OptimisticLock;

import io.onedev.server.model.User;
import io.onedev.utils.WordUtils;

@Embeddable
public class CloseInfo implements Serializable {

	private static final long serialVersionUID = 1L;

	public enum Status {
		MERGED, DISCARDED;
	
		@Override
		public String toString() {
			return WordUtils.toWords(name());
		}
		
	};
	
	@OptimisticLock(excluded=true)
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="CLOSE_USER")
	private User user;
	
	@OptimisticLock(excluded=true)
	@Column(name="CLOSE_USER_NAME")
	private String userName;

	@OptimisticLock(excluded=true)
	@Column(name="CLOSE_DATE")
	private Date date;
	
	@OptimisticLock(excluded=true)
	@Column(name="CLOSE_STATUS")
	private Status status;

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

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}
	
}
