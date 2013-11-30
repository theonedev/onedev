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
public class AutoPush extends AbstractEntity {

	@ManyToOne
	@JoinColumn(nullable=false)
	private Branch from;
	
	@ManyToOne
	@JoinColumn(nullable=false)
	private Branch to;
	
	public Branch getFrom() {
		return from;
	}

	public void setFrom(Branch from) {
		this.from = from;
	}

	public Branch getTo() {
		return to;
	}

	public void setBranch(Branch to) {
		this.to = to;
	}
	
	@Override
	public String toString() {
		return Objects.toStringHelper(this)
				.add("from", getFrom())
				.add("to", getTo())
				.toString();
	}

}
