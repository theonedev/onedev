package com.pmease.gitop.core.model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.FetchMode;

import com.pmease.commons.persistence.AbstractEntity;

@Entity
@SuppressWarnings("serial")
public class PullRequest extends AbstractEntity {
	
	@ManyToOne(fetch=FetchType.EAGER)
	@org.hibernate.annotations.Fetch(FetchMode.SELECT)
	@JoinColumn(nullable=false)
	@org.hibernate.annotations.ForeignKey(name="FK_PR_INI")
	private User initiator;

	public User getInitiator() {
		return initiator;
	}

	public void setInitiator(User initiator) {
		this.initiator = initiator;
	}
	
}
