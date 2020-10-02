package io.onedev.server.event;

import java.util.Date;

import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

import io.onedev.server.model.User;

@JsonTypeInfo(property="@class", use=Id.CLASS)
public abstract class Event {
	
	private final User user;
	
	private final Date date;
	
	public Event(@Nullable User user, Date date) {
		this.user = user;
		this.date = date;
	}

	@Nullable
	public User getUser() {
		return user;
	}

	public Date getDate() {
		return date;
	}
	
}
