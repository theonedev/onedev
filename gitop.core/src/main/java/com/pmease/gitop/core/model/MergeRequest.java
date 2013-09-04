package com.pmease.gitop.core.model;

import java.io.File;
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

import com.google.common.base.Preconditions;
import com.pmease.commons.git.CheckAncestorCommand;
import com.pmease.commons.git.FindChangedFilesCommand;
import com.pmease.commons.git.Git;
import com.pmease.commons.persistence.AbstractEntity;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.core.manager.RepositoryManager;

@SuppressWarnings("serial")
@Entity
public class MergeRequest extends AbstractEntity {

	public enum Status {OPEN, MERGED, CLOSED}
	
	private String title;
	
	@ManyToOne(fetch=FetchType.EAGER)
	@org.hibernate.annotations.Fetch(FetchMode.SELECT)
	@JoinColumn(nullable=false)
	private User submitter;
	
	@ManyToOne(fetch=FetchType.EAGER)
	@org.hibernate.annotations.Fetch(FetchMode.SELECT)
	@JoinColumn(nullable=false)
	private Branch destination;
	
	@ManyToOne(fetch=FetchType.EAGER)
	@org.hibernate.annotations.Fetch(FetchMode.SELECT)
	@JoinColumn(nullable=true)
	private Branch source;
	
	@Column(nullable=false)
	private Status status = Status.OPEN;
	
	private transient MergeRequestUpdate baseUpdate;

	@OneToMany(mappedBy="request")
	private Collection<MergeRequestUpdate> updates = new ArrayList<MergeRequestUpdate>();
	
	@OneToMany(mappedBy="request")
	private Collection<PendingVote> pendingVotes = new ArrayList<PendingVote>();
	
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public boolean isAutoCreated() {
		return getTitle() == null;
	}
	
	public User getSubmitter() {
		return submitter;
	}

	public void setSubmitter(User submitter) {
		this.submitter = submitter;
	}

	public Branch getDestination() {
		return destination;
	}

	public void setDestination(Branch destination) {
		this.destination = destination;
	}

	public Branch getSource() {
		return source;
	}

	public void setSource(Branch source) {
		this.source = source;
	}

	public Collection<MergeRequestUpdate> getUpdates() {
		return updates;
	}

	public void setUpdates(Collection<MergeRequestUpdate> updates) {
		this.updates = updates;
	}

	public Collection<PendingVote> getPendingVotes() {
		return pendingVotes;
	}

	public void setPendingVotes(Collection<PendingVote> pendingVotes) {
		this.pendingVotes = pendingVotes;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}
	
	public MergeRequestUpdate getBaseUpdate() {
		if (baseUpdate != null) {
			return baseUpdate;
		} else {
			baseUpdate = getFirstUpdate();
			return baseUpdate;
		}
	}

	public void setBaseUpdate(MergeRequestUpdate baseUpdate) {
		this.baseUpdate = baseUpdate;
	}

	public MergeRequestUpdate getLatestUpdate() {
		Preconditions.checkState(!getUpdates().isEmpty());
		
		List<MergeRequestUpdate> updates = new ArrayList<MergeRequestUpdate>(getUpdates());
		Collections.sort(updates);
		Collections.reverse(updates);
		return updates.iterator().next();
	}
	
	public MergeRequestUpdate getFirstUpdate() {
		Preconditions.checkState(!getUpdates().isEmpty());
		
		List<MergeRequestUpdate> updates = new ArrayList<MergeRequestUpdate>(getUpdates());
		Collections.sort(updates);
		return updates.iterator().next();
	}

	public boolean isFastForward() {
		File repoDir = Gitop.getInstance(RepositoryManager.class).locateStorage(getDestination().getRepository());
		CheckAncestorCommand command = new Git(repoDir).checkAncestor();
		command.ancestor(getDestination().getName());
		command.descendant(getLatestUpdate().getRefName());
		return command.call();
	}
	
	public Collection<String> findTouchedFiles() {
		RepositoryManager repositoryManager = Gitop.getInstance(RepositoryManager.class);
		File repoDir = repositoryManager.locateStorage(getDestination().getRepository());
		MergeRequestUpdate update = getLatestUpdate();
		if (update != null) {
			FindChangedFilesCommand command = new Git(repoDir).findChangedFiles();
			command.fromRev(getDestination().getName());
			command.toRev(update.getRefName());
			return command.call();
		} else {
			return new ArrayList<String>();
		}
	}

}
