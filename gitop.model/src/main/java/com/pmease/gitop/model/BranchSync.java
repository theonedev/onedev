package com.pmease.gitop.model;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.google.common.base.Objects;
import com.pmease.commons.hibernate.AbstractEntity;

@Entity
@Table(uniqueConstraints={
		@UniqueConstraint(columnNames={"from", "to"})
})
@SuppressWarnings("serial")
public class BranchSync extends AbstractEntity {

	@ManyToOne
	@JoinColumn(nullable=false)
	private Branch from;
	
	@ManyToOne
	@JoinColumn(nullable=false)
	private Branch to;
	
	@ManyToOne
	private User pusher;
	
	@ManyToOne
	private User puller;
	
	public Branch getFrom() {
		return from;
	}

	public void setFrom(Branch from) {
		this.from = from;
	}

	public Branch getTo() {
		return to;
	}

	public void setTo(Branch to) {
		this.to = to;
	}
	
	public User getPusher() {
		return pusher;
	}

	public void setPusher(User pusher) {
		this.pusher = pusher;
	}

	public User getPuller() {
		return puller;
	}

	public void setPuller(User puller) {
		this.puller = puller;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
				.add("from", getFrom())
				.add("to", getTo())
				.toString();
	}

}
