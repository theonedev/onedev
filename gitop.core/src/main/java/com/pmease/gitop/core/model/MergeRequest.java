package com.pmease.gitop.core.model;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.hibernate.annotations.FetchMode;

import com.google.common.base.Preconditions;
import com.pmease.commons.git.CalcMergeBaseCommand;
import com.pmease.commons.git.CheckAncestorCommand;
import com.pmease.commons.git.FindChangedFilesCommand;
import com.pmease.commons.git.Git;
import com.pmease.commons.persistence.AbstractEntity;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.core.manager.PendingVoteManager;
import com.pmease.gitop.core.manager.RepositoryManager;

@SuppressWarnings("serial")
@Entity
public class MergeRequest extends AbstractEntity {

	public enum Status {OPEN, CLOSED}
	
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
	
	private transient Boolean merged;
	
	private transient String mergeBase;
	
	private transient List<MergeRequestUpdate> sortedUpdates;
	
	private transient List<MergeRequestUpdate> effectiveUpdates;
	
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
			return getEffectiveUpdates().iterator().next();
		}
	}

	public void setBaseUpdate(MergeRequestUpdate baseUpdate) {
		this.baseUpdate = baseUpdate;
	}

	/**
	 * Get list of sorted updates.
	 * <p>
	 * @return
	 * 			list of sorted updates ordered by update id reversely
	 */
	public List<MergeRequestUpdate> getSortedUpdates() {
		if (sortedUpdates == null) {
			sortedUpdates = new ArrayList<MergeRequestUpdate>(getUpdates());
			Preconditions.checkState(!sortedUpdates.isEmpty());
			
			Collections.sort(sortedUpdates);
			Collections.reverse(sortedUpdates);
		}
		return sortedUpdates;
	}
	
	/**
	 * Get list of effective updates in reverse order. Update is considered effective 
	 * if it is descendant of merge base of latest update and target branch head.  
	 * <p>
	 * @return
	 * 			list of effective updates in reverse order.
	 */
	public List<MergeRequestUpdate> getEffectiveUpdates() {
		if (effectiveUpdates == null) {
			effectiveUpdates = new ArrayList<MergeRequestUpdate>();
						
			File repoDir = Gitop.getInstance(RepositoryManager.class).locateStorage(getDestination().getRepository());
			Git git = new Git(repoDir);
			CheckAncestorCommand command = git.checkAncestor();
			command.ancestor(getMergeBase());

			for (Iterator<MergeRequestUpdate> it = getSortedUpdates().iterator(); it.hasNext();) {
				MergeRequestUpdate update = it.next();
				command.descendant(update.getRefName());
				if (command.call()) {
					effectiveUpdates.add(update);
				} else {
					break;
				}
			}
		}
		return effectiveUpdates;
	}
	
	public MergeRequestUpdate getLatestUpdate() {
		return getSortedUpdates().iterator().next();
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

	public boolean isMerged() {
		if (merged == null) {
			RepositoryManager repositoryManager = Gitop.getInstance(RepositoryManager.class);
			File repoDir = repositoryManager.locateStorage(getDestination().getRepository());
			CheckAncestorCommand command = new Git(repoDir).checkAncestor();
			command.ancestor(getLatestUpdate().getRefName());
			command.descendant(getDestination().getName());
			merged = command.call();
		} 
		return merged;
	}
	
	public String getMergeBase() {
		if (mergeBase == null) {
			RepositoryManager repositoryManager = Gitop.getInstance(RepositoryManager.class);
			File repoDir = repositoryManager.locateStorage(getDestination().getRepository());
			CalcMergeBaseCommand command = new Git(repoDir).calcMergeBase();
			command.rev1(getLatestUpdate().getRefName());
			command.rev2(getDestination().getName());
			mergeBase = command.call();
		}
		return mergeBase;
	}
	
	public void requestVote(Collection<User> candidates) {
		Set<User> pendingUsers = new HashSet<User>();
		for (PendingVote each: getPendingVotes())
			pendingUsers.add(each.getReviewer());

		pendingUsers.retainAll(candidates);
		
		if (pendingUsers.isEmpty()) {
			User selected = Collections.min(candidates, new Comparator<User>() {

				@Override
				public int compare(User user1, User user2) {
					return user1.getVotes().size() - user2.getVotes().size();
				}
				
			});
			
			PendingVote pendingVote = new PendingVote();
			pendingVote.setRequest(this);
			pendingVote.setReviewer(selected);
	
			Gitop.getInstance(PendingVoteManager.class).save(pendingVote);
		}
		
	}
	
}
