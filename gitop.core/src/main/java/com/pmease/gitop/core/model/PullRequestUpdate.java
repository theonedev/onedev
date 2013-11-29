package com.pmease.gitop.core.model;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import com.google.common.base.Preconditions;
import com.pmease.commons.git.Commit;
import com.pmease.commons.git.Git;
import com.pmease.commons.hibernate.AbstractEntity;
import com.pmease.commons.util.FileUtils;

@SuppressWarnings("serial")
@Entity
public class PullRequestUpdate extends AbstractEntity {

	@ManyToOne
	@JoinColumn(nullable=false)
	private PullRequest request;
	
	@Column(nullable=false)
	private String subject;
	
	private Date date = new Date();
	
	@OneToMany(mappedBy="update", cascade=CascadeType.REMOVE)
	private Collection<Vote> votes = new ArrayList<Vote>();
	
	private transient String headCommit;
	
	private transient String baseCommit;
	
	public PullRequest getRequest() {
		return request;
	}

	public void setRequest(PullRequest request) {
		this.request = request;
	}

	public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

    public Collection<Vote> getVotes() {
		return votes;
	}

	public void setVotes(Collection<Vote> votes) {
		this.votes = votes;
	}

	public String getHeadRef() {
		return "refs/gitop/updates/" + getId() + "/head";
	}
	
	public String getHeadCommit() {
		if (headCommit == null) 
			headCommit = getRequest().getTarget().getProject().getCodeRepo().resolveRef(getHeadRef(), true);
		return headCommit;
	}
	
	/**
	 * Calculate base commit for change calculation of this update. Base commit is merged 
	 * commit of:
	 * <li> merge base of head ref and target branch head
	 * <li> head ref of previous update
	 * Changed files of this update will be calculated between change base and head ref
	 * and this effectively represents changes made since previous update with merged 
	 * changes from target branch excluded if there is any.  
	 *  
	 * @return
	 * 			base commit used for change calculation of current update
	 */
	public String getBaseCommit() {
		if (baseCommit == null) {
			Git git = getRequest().getTarget().getProject().getCodeRepo();
			String mergeBase = git.calcMergeBase(getHeadCommit(), getRequest().getTarget().getHeadCommit());
			int index = getRequest().getSortedUpdates().indexOf(this);
			Preconditions.checkState(index != -1);
			
			String previousUpdate;
			if (index == 0) {
				previousUpdate = mergeBase;
			} else {
				previousUpdate = getRequest().getSortedUpdates().get(index-1).getHeadCommit();
			}
	
			if (git.isAncestor(previousUpdate, mergeBase)) { 
				baseCommit = mergeBase;
			} else if (git.isAncestor(mergeBase, previousUpdate)) {
				baseCommit = previousUpdate;
			} else {
				String baseRef = "refs/gitop/updates/" + getId() + "/base";
				baseCommit = git.resolveRef(baseRef, false);

				if (baseCommit != null) {
					Commit commit = git.resolveRevision(baseCommit);
					if (!commit.getParentHashes().contains(mergeBase) || !commit.getParentHashes().contains(previousUpdate)) 
						baseCommit = null;
				} 
				
				if (baseCommit == null) {
					File tempDir = FileUtils.createTempDir();
					try {
						Git tempGit = new Git(tempDir);
						
						/*
						 * Branch name here is not significant, we just use an existing branch
						 * in cloned repository to hold mergeBase, so that we can merge with 
						 * previousUpdate 
						 */
						String branchName = getRequest().getTarget().getName();
						tempGit.clone(git.repoDir().getAbsolutePath(), false, true, true, branchName);
						tempGit.updateRef("HEAD", mergeBase, null, null);
						tempGit.reset(null, null);
						Preconditions.checkState(tempGit.merge(previousUpdate, null, "ours", null));
						git.fetch(tempGit.repoDir().getAbsolutePath(), "+HEAD:" + baseRef);
						baseCommit = git.resolveRef(baseRef, true);
					} finally {
						FileUtils.deleteDir(tempDir);
					}
				}
			}
		}
		
		return baseCommit;
	}
	
	/**
	 * List votes against this update and all subsequent updates.
	 * <p>
	 * @return
	 * 			list of found votes, ordered by associated updates reversely
	 */
	public List<Vote> listVotesOnwards() {
		List<Vote> votes = new ArrayList<Vote>();
		
		List<PullRequestUpdate> updates = getRequest().getEffectiveUpdates();
		for (Iterator<PullRequestUpdate> it = updates.iterator(); it.hasNext();) {
			PullRequestUpdate update = it.next();
			votes.addAll(update.getVotes());
			if (update.equals(this)) {
				break;
			}
		}
		
		return votes;
	}

}
