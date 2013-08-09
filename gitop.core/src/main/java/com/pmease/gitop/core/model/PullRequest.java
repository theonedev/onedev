package com.pmease.gitop.core.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.FetchMode;

import com.pmease.commons.persistence.AbstractEntity;

@SuppressWarnings("serial")
@Entity
public class PullRequest extends AbstractEntity {

	@Column(nullable=false)
	private String title;
	
	@ManyToOne(fetch=FetchType.EAGER)
	@org.hibernate.annotations.Fetch(FetchMode.SELECT)
	@JoinColumn(nullable=false)
	@org.hibernate.annotations.ForeignKey(name="FK_PR_TBRCH")
	private InvolvedBranch targetBranch;

	@ManyToOne(fetch=FetchType.EAGER)
	@org.hibernate.annotations.Fetch(FetchMode.SELECT)
	@JoinColumn(nullable=true)
	@org.hibernate.annotations.ForeignKey(name="FK_PR_SBRCH")
	private InvolvedBranch sourceBranch;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public InvolvedBranch getTargetBranch() {
		return targetBranch;
	}

	public void setTargetBranch(InvolvedBranch targetBranch) {
		this.targetBranch = targetBranch;
	}

	public InvolvedBranch getSourceBranch() {
		return sourceBranch;
	}

	public void setSourceBranch(InvolvedBranch sourceBranch) {
		this.sourceBranch = sourceBranch;
	}

}
