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

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.pmease.commons.git.CalcMergeBaseCommand;
import com.pmease.commons.git.CheckAncestorCommand;
import com.pmease.commons.git.FindChangedFilesCommand;
import com.pmease.commons.git.Git;
import com.pmease.commons.hibernate.AbstractEntity;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.core.gatekeeper.CheckResult;
import com.pmease.gitop.core.gatekeeper.GateKeeper;
import com.pmease.gitop.core.manager.ProjectManager;
import com.pmease.gitop.core.manager.VoteInvitationManager;

@SuppressWarnings("serial")
@Entity
public class MergeRequest extends AbstractEntity {

	public enum Status {OPEN, CLOSED}
	
	private String title;
	
	@ManyToOne
	@JoinColumn(nullable=false)
	private User submitter;
	
	@ManyToOne
	@JoinColumn(nullable=false)
	private Branch destination;
	
	@ManyToOne
	@JoinColumn(nullable=true)
	private Branch source;
	
	@Column(nullable=false)
	private Status status = Status.OPEN;

	private transient Optional<CheckResult> checkResult;
	
	private transient Boolean merged;
	
	private transient String mergeBase;
	
	private transient List<MergeRequestUpdate> sortedUpdates;
	
	private transient List<MergeRequestUpdate> effectiveUpdates;
	
	private transient MergeRequestUpdate baseUpdate;
	
	private transient Collection<User> potentialVoters;
	
	@OneToMany(mappedBy="request", cascade=CascadeType.REMOVE)
	private Collection<MergeRequestUpdate> updates = new ArrayList<MergeRequestUpdate>();
	
	@OneToMany(mappedBy="request", cascade=CascadeType.REMOVE)
	private Collection<VoteInvitation> voteInvitations = new ArrayList<VoteInvitation>();
	
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

	public Collection<VoteInvitation> getVoteInvitations() {
		return voteInvitations;
	}

	public void setVoteInvitations(Collection<VoteInvitation> voteInvitations) {
		this.voteInvitations = voteInvitations;
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
						
			File repoDir = Gitop.getInstance(ProjectManager.class).locateStorage(getDestination().getProject()).ofCode();
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
		File repoDir = Gitop.getInstance(ProjectManager.class).locateStorage(getDestination().getProject()).ofCode();
		CheckAncestorCommand command = new Git(repoDir).checkAncestor();
		command.ancestor(getDestination().getName());
		command.descendant(getLatestUpdate().getRefName());
		return command.call();
	}
	
	public Collection<String> findTouchedFiles() {
		ProjectManager projectManager = Gitop.getInstance(ProjectManager.class);
		File repoDir = projectManager.locateStorage(getDestination().getProject()).ofCode();
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
			ProjectManager projectManager = Gitop.getInstance(ProjectManager.class);
			File repoDir = projectManager.locateStorage(getDestination().getProject()).ofCode();
			CheckAncestorCommand command = new Git(repoDir).checkAncestor();
			command.ancestor(getLatestUpdate().getRefName());
			command.descendant(getDestination().getName());
			merged = command.call();
		} 
		return merged;
	}
	
	public String getMergeBase() {
		if (mergeBase == null) {
			ProjectManager projectManager = Gitop.getInstance(ProjectManager.class);
			File repoDir = projectManager.locateStorage(getDestination().getProject()).ofCode();
			CalcMergeBaseCommand command = new Git(repoDir).calcMergeBase();
			command.rev1(getLatestUpdate().getRefName());
			command.rev2(getDestination().getName());
			mergeBase = command.call();
		}
		return mergeBase;
	}

	/**
	 * Find potential voters for this request. Potential voters will be 
	 * presented with the vote/reject button when they review the request.
	 * However they may not receive vote invitation.
	 * <p>
	 * @return
	 * 			a collection of potential voters
	 */
	public Collection<User> findPotentialVoters() {
		check(false);
		return getPotentialVoters();
	}
	
	private Collection<User> getPotentialVoters() {
		if (potentialVoters == null) 
			potentialVoters = new HashSet<User>();
		return potentialVoters;
	}
	
	/**
	 * Invite specified number of users to vote for this request.
	 * <p>
	 * @param users
	 * 			a collection of users to invite users from
	 * @param count
	 * 			number of users to invite
	 */
	public void inviteToVote(final Collection<User> users, int count) {
		Collection<User> candidates = new HashSet<User>(users);

		// submitter is not allowed to vote for this request 
		candidates.remove(getSubmitter());
		
		// users already voted for latest update should be excluded
		for (Vote vote: getLatestUpdate().getVotes())
			candidates.remove(vote.getVoter());
		
		getPotentialVoters().addAll(candidates);
		
		/* 
		 * users already voted since base update should be excluded from
		 * invitation list as their votes are still valid 
		 */
		for (Vote vote: getBaseUpdate().listVotesOnwards()) {
			candidates.remove(vote.getVoter());
		}
		
		Set<User> invited = new HashSet<User>();
		for (VoteInvitation each: getVoteInvitations())
			invited.add(each.getVoter());

		invited.retainAll(candidates);
		
		for (int i=0; i<count-invited.size(); i++) {
			User selected = Collections.min(candidates, new Comparator<User>() {

				@Override
				public int compare(User user1, User user2) {
					return user1.getVotes().size() - user2.getVotes().size();
				}
				
			});

			candidates.remove(selected);
			
			VoteInvitation invitation = new VoteInvitation();
			invitation.setRequest(this);
			invitation.setVoter(selected);
			
			Gitop.getInstance(VoteInvitationManager.class).save(invitation);
		}

	}

	/**
	 * Check this request with gate keeper of target project.
	 * <p>
	 * @param force
	 * 			whether or not to force the check. Since the check might be time-consuming, Gitop
	 * 			caches the check result, and returns the cached result for subsequent checks if 
	 * 			force flag is not set.
	 * @return
	 * 			check result of gate keeper, or Optional.absent() if no gate keeper is defined
	 */
	public Optional<CheckResult> check(boolean force) {
		if (checkResult == null || force) {
			GateKeeper gateKeeper = getDestination().getProject().getGateKeeper();
			if (gateKeeper != null) {
				checkResult = Optional.of(gateKeeper.check(this));
				
				Collection<VoteInvitation> withdraws = new HashSet<VoteInvitation>();
				for (VoteInvitation invitation: getVoteInvitations()) {
					if (!getPotentialVoters().contains(invitation.getVoter())) {
						withdraws.add(invitation);
					}
				}
				
				for (VoteInvitation invitation: withdraws) {
					Gitop.getInstance(VoteInvitationManager.class).delete(invitation);
				}
			} else {
				checkResult = Optional.absent();
			}
		}
		return checkResult;
	}
	
}
