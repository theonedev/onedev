package com.pmease.gitop.core.model;

import java.util.Collection;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.hibernate.annotations.FetchMode;

import com.pmease.commons.persistence.AbstractEntity;

@SuppressWarnings("serial")
@Entity
public class MergeRequest extends AbstractEntity {

	@Column(nullable=false)
	private String title;

	@ManyToOne(fetch=FetchType.EAGER)
	@org.hibernate.annotations.Fetch(FetchMode.SELECT)
	@JoinColumn(nullable=false)
	@org.hibernate.annotations.ForeignKey(name="FK_MR_ACC")
	private Account account;
	
	@ManyToOne(fetch=FetchType.EAGER)
	@org.hibernate.annotations.Fetch(FetchMode.SELECT)
	@JoinColumn(nullable=false)
	@org.hibernate.annotations.ForeignKey(name="FK_MR_TBRCH")
	private InvolvedBranch targetBranch;

	@ManyToOne(fetch=FetchType.EAGER)
	@org.hibernate.annotations.Fetch(FetchMode.SELECT)
	@JoinColumn(nullable=false)
	@org.hibernate.annotations.ForeignKey(name="FK_MR_SBRCH")
	private InvolvedBranch sourceBranch;

	@OneToMany(mappedBy="request")
	private Collection<MergeRequestUpdate> updates;
	
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Account getAccount() {
		return account;
	}

	public void setAccount(Account account) {
		this.account = account;
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

	public Collection<MergeRequestUpdate> getUpdates() {
		return updates;
	}

	public void setUpdates(Collection<MergeRequestUpdate> updates) {
		this.updates = updates;
	}

}
