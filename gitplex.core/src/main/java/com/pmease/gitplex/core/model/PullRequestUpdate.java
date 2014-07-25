package com.pmease.gitplex.core.model;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;

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
import com.pmease.commons.util.LockUtils;

@SuppressWarnings("serial")
@Entity
public class PullRequestUpdate extends AbstractEntity {

	@ManyToOne
	@JoinColumn(nullable=false)
	private PullRequest request;
	
	@Column(nullable=false)
	private String headCommit;

	@ManyToOne
	private User user;
	
	private Date date = new Date();
	
	@OneToMany(mappedBy="update", cascade=CascadeType.REMOVE)
	private Collection<Vote> votes = new ArrayList<Vote>();
	
	private transient String referentialCommit;
	
	private transient String baseCommit;
	
	private transient Collection<Commit> mergedCommits;
	
	private transient List<Commit> commits;
	
	private transient Collection<String> changedFiles;
	
	public PullRequest getRequest() {
		return request;
	}

	public void setRequest(PullRequest request) {
		this.request = request;
	}

	public String getHeadCommit() {
		return headCommit;
	}
	
	public void setHeadCommit(String headCommit) {
		this.headCommit = headCommit;
	}
	
    public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
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
	
	public String getChangeRef() {
		Preconditions.checkNotNull(getId());
		return Repository.REFS_GITOP + "updates/" + getId() + "/change";
	}
	
	public String getHeadRef() {
		Preconditions.checkNotNull(getId());
		return Repository.REFS_GITOP + "updates/" + getId() + "/head";
	}

	/**
	 * Calculate referential commit for change calculation of this update. Referential commit is merged 
	 * commit of:
	 * 
	 * <li> merge base of update head and target branch head
	 * <li> head of previous update
	 * 
	 * Changed files of this update will be calculated between referential commit and head commit 
	 * and this effectively represents changes made since previous update with merged changes 
	 * from target branch excluded if there is any.  
	 *  
	 * @return
	 * 			referential commit used for change calculation of current update
	 */
	public String getReferentialCommit() {
		if (referentialCommit == null) {
			Git git = getRequest().getTarget().getRepository().git();
			String mergeBase = git.calcMergeBase(getHeadCommit(), getRequest().getTarget().getHeadCommit());

			if (git.isAncestor(getBaseCommit(), mergeBase)) { 
				referentialCommit = mergeBase;
			} else if (git.isAncestor(mergeBase, getBaseCommit())) {
				referentialCommit = getBaseCommit();
			} else {
				Lock lock = LockUtils.lock(getLockName());
				try {
					String changeRef = getChangeRef();
					referentialCommit = git.parseRevision(changeRef, false);
	
					if (referentialCommit != null) {
						Commit commit = git.showRevision(referentialCommit);
						if (!commit.getParentHashes().contains(mergeBase) || !commit.getParentHashes().contains(getBaseCommit())) 
							referentialCommit = null;
					} 
					
					if (referentialCommit == null) {
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
							Preconditions.checkState(tempGit.merge(getBaseCommit(), null, null, "ours", null) != null);
							git.fetch(tempGit, "+HEAD:" + changeRef);
							referentialCommit = git.parseRevision(changeRef, true);
						} finally {
							FileUtils.deleteDir(tempDir);
						}
					}
				} finally {
					lock.unlock();
				}
			}
		}
		
		return referentialCommit;
	}
	
	/**
	 * List votes against this update and all subsequent updates.
	 * <p>
	 * @return
	 * 			list of found votes, ordered by associated updates reversely
	 */
	public List<Vote> listVotesOnwards() {
		List<Vote> votes = new ArrayList<Vote>();
		
		for (PullRequestUpdate update: getRequest().getEffectiveUpdates()) {
			votes.addAll(update.getVotes());
			if (update.equals(this)) {
				break;
			}
		}
		
		return votes;
	}

	public void deleteRefs() {
		Git git = getRequest().getTarget().getRepository().git();
		git.deleteRef(getHeadRef(), null, null);
		git.deleteRef(getChangeRef(), null, null);
	}	
	
	public String getLockName() {
		Preconditions.checkNotNull(getId());
		return "pull request update: " + getId();
	}
	
	public Collection<String> getChangedFiles() {
		if (changedFiles == null) 
			changedFiles = getRequest().git().listChangedFiles(getReferentialCommit(), getHeadCommit());
		return changedFiles;
	}
	
	/**
	 * Base commit represents head commit of last update, or base commit of the request 
	 * for the first update. Base commit is used to calculate commits belonging to 
	 * current update.
	 * 
	 * @return
	 * 			base commit of this update
	 */
	public String getBaseCommit() {
		if (baseCommit == null) {
			PullRequest request = getRequest();

			int index = request.getSortedUpdates().indexOf(this);
			if (index > 0)
				baseCommit = request.getSortedUpdates().get(index-1).getHeadCommit();
			else
				baseCommit = request.getBaseUpdate().getHeadCommit();
		}
		return baseCommit;
	}
	
	/**
	 * Merged commits represent commits already merged to target branch since base commit.
	 * 
	 * @return
	 * 			commits already merged to target branch since base commit
	 */
	public Collection<Commit> getMergedCommits() {
		if (mergedCommits == null) {
			mergedCommits = new HashSet<>();

			Branch target = getRequest().getTarget();
			Repository repo = target.getRepository();
			for (Commit commit: repo.git().log(getBaseCommit(), target.getHeadCommit(), null, 0, 0)) {
				mergedCommits.add(commit);
			}
		}
		return mergedCommits;
	}
	
	/**
	 * Get commits belonging to this update, descendantly ordered by commit date 
	 * 
	 * @return
	 * 			commits belonging to this update
	 */
	public List<Commit> getCommits() {
		if (commits == null) {
			/* 
			 * This method calculates all commits belonging to this update. You might think that 
			 * the result is obvious, just take log output between last update and current head. 
			 * But that result may include noise commits, for instance, if you merges target 
			 * branch into source branch to resolve merge conflicts, all commits in target 
			 * branch will also appear in log output between last update and current update. 
			 * Now, you might suggest that let's just exclude from the log output those commits 
			 * being ancestor of target branch. But this still can cause issues as target branch
			 * may merge some intermediate commits from our source branch, and these 
			 * intermediate commits will be ancestor of target branch. We certainly do not want 
			 * to exclude these intermediate commits as it originally belongs to our source 
			 * branch. To solve this contradiction, for those commits belong to both source 
			 * branch and target branch, we calculate affinity score of the commit to each 
			 * branch, and consider the commit to be belonging to a branch if the associated 
			 * affinity score is less.
			 */ 
			commits = new ArrayList<>();

			Map<String, ScoreAwareCommit> allCommits = new LinkedHashMap<>(); 
			Git git = getRequest().getTarget().getRepository().git();
			for (Commit commit: git.log(getBaseCommit(), getHeadCommit(), null, 0, 0)) {
				ScoreAwareCommit scoreAwareCommit = new ScoreAwareCommit();
				scoreAwareCommit.setCommit(commit);
				allCommits.put(commit.getHash(), scoreAwareCommit);
			}

			ScoreAwareCommit headCommit = allCommits.get(getHeadCommit());
			if (headCommit != null) {
				headCommit.setScore(0);
				updateAffinityScores(allCommits, headCommit);
			}
			
			Map<String, ScoreAwareCommit> mergedCommits = new HashMap<>();
			for (Commit commit: getMergedCommits()) {
				ScoreAwareCommit scoredCommit = new ScoreAwareCommit();
				scoredCommit.setCommit(commit);
				mergedCommits.put(commit.getHash(), scoredCommit);
			}
			
			headCommit = mergedCommits.get(getRequest().getTarget().getHeadCommit());
			if (headCommit != null) {
				headCommit.setScore(0);
				updateAffinityScores(mergedCommits, headCommit);
			}
			
			for (ScoreAwareCommit commit: allCommits.values()) {
				ScoreAwareCommit mergedCommit = mergedCommits.get(commit.getCommit().getHash());
				if (mergedCommit == null || mergedCommit.getScore() >= commit.getScore()) {
					commits.add(commit.getCommit());
				} 
			}
			
			Collections.reverse(commits);
		}
		return commits;
	}
	
	/*
	 * This method calculates affinity scores of a set of commits against specified head commit (or branch). 
	 * The affinity score reflects the distance between a commit and first-parent chain of the head commit. 
	 * For instance, if a commit can be reached recursively via first-parent of the head commit, its 
	 * affinity score will be 0.     
	 */
	private void updateAffinityScores(Map<String, ScoreAwareCommit> commits, ScoreAwareCommit headCommit) {
		for (int i=0; i<headCommit.getCommit().getParentHashes().size(); i++) {
			String parentCommitHash = headCommit.getCommit().getParentHashes().get(i);
			ScoreAwareCommit parentCommit = commits.get(parentCommitHash);
			if (parentCommit != null) {
				/*
				 * Parent hashes of a commit is ordered in parent number. For instance first-parent is at index 0. 
				 * So the parent score is calculated simply by adding current parent index to the head commit score.
				 */
				int parentScore = i + headCommit.getScore();
				if (parentScore < parentCommit.getScore()) {
					parentCommit.setScore(parentScore);
				}
				
				updateAffinityScores(commits, parentCommit);
			}
		}
	}

	private static class ScoreAwareCommit {
		
		private Commit commit;
		
		private int score = Integer.MAX_VALUE;

		public Commit getCommit() {
			return commit;
		}

		public void setCommit(Commit commit) {
			this.commit = commit;
		}

		public int getScore() {
			return score;
		}

		public void setScore(int score) {
			this.score = score;
		}
		
	}
	
}
