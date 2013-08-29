package com.pmease.gitop.core.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

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

	public enum Status {OPEN, MERGED, CLOSED}
	
	private String title;

	@ManyToOne(fetch=FetchType.EAGER)
	@org.hibernate.annotations.Fetch(FetchMode.SELECT)
	@JoinColumn(nullable=false)
	private User user;
	
	@ManyToOne(fetch=FetchType.EAGER)
	@org.hibernate.annotations.Fetch(FetchMode.SELECT)
	@JoinColumn(nullable=false)
	private Repository targetRepository;
	
	@Column(nullable=false)
	private String targetBranch;

	@ManyToOne(fetch=FetchType.EAGER)
	@org.hibernate.annotations.Fetch(FetchMode.SELECT)
	@JoinColumn(nullable=false)
	private Repository sourceRepository;
	
	@Column(nullable=false)
	private String sourceBranch;
	
	@Column(nullable=false)
	private Status status = Status.OPEN;

	@OneToMany(mappedBy="request")
	private Collection<MergeRequestUpdate> updates = new ArrayList<MergeRequestUpdate>();
	
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Repository getTargetRepository() {
		return targetRepository;
	}

	public void setTargetRepository(Repository targetRepository) {
		this.targetRepository = targetRepository;
	}

	public String getTargetBranch() {
		return targetBranch;
	}

	public void setTargetBranch(String targetBranch) {
		this.targetBranch = targetBranch;
	}

	public Repository getSourceRepository() {
		return sourceRepository;
	}

	public void setSourceRepository(Repository sourceRepository) {
		this.sourceRepository = sourceRepository;
	}

	public String getSourceBranch() {
		return sourceBranch;
	}

	public void setSourceBranch(String sourceBranch) {
		this.sourceBranch = sourceBranch;
	}

	public Collection<MergeRequestUpdate> getUpdates() {
		return updates;
	}

	public void setUpdates(Collection<MergeRequestUpdate> updates) {
		this.updates = updates;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public Collection<String> getTouchedFiles() {
		Collection<String> touchedFiles = new ArrayList<String>();
		MergeRequestUpdate update = getLatestUpdate();
		if (update != null) {
			
		} 
		return touchedFiles;
	}
	
	public MergeRequestUpdate getLatestUpdate() {
		List<MergeRequestUpdate> updates = new ArrayList<MergeRequestUpdate>(getUpdates());
		Collections.sort(updates);
		Collections.reverse(updates);
		if (updates.isEmpty())
			return null;
		else
			return updates.iterator().next();
	}
	
}
