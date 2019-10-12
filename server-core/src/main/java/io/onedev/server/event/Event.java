package io.onedev.server.event;

import java.util.Date;
import java.util.Stack;

import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

import io.onedev.server.model.User;

@JsonTypeInfo(property="@class", use = Id.CLASS)
public abstract class Event {
	
	private static ThreadLocal<Stack<Event>> stack =  new ThreadLocal<Stack<Event>>() {

		@Override
		protected Stack<Event> initialValue() {
			return new Stack<Event>();
		}
	
	};
	
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
	
	public static void push(ProjectEvent event) {
		stack.get().push(event);
	}

	public static void pop() {
		stack.get().pop();
	}
	
	@Nullable
	public static Event get() {
		if (!stack.get().isEmpty()) 
			return stack.get().peek();
		else 
			return null;
	}
	
}
