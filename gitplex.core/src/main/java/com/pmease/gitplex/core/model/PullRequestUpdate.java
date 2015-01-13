package com.pmease.gitplex.core.model;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.Callable;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.pmease.commons.git.Commit;
import com.pmease.commons.git.Git;
import com.pmease.commons.hibernate.AbstractEntity;
import com.pmease.commons.util.FileUtils;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.manager.StorageManager;

@SuppressWarnings("serial")
@Entity
public class PullRequestUpdate extends AbstractEntity {

	@ManyToOne
	@JoinColumn(nullable=false)
	private PullRequest request;
	
	@ManyToOne
	private User user;
	
	@Column(nullable=false)
	private String headCommitHash;

	@Column(nullable=false)
	private Date date;
	
	@OneToMany(mappedBy="update", cascade=CascadeType.REMOVE)
	private Collection<Review> reviews = new ArrayList<Review>();
	
	private transient List<Commit> commits;

	private transient CachedInfo cachedInfo;
	
	public PullRequest getRequest() {
		return request;
	}

	public void setRequest(PullRequest request) {
		this.request = request;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public String getHeadCommitHash() {
		return headCommitHash;
	}
	
	public void setHeadCommitHash(String headCommitHash) {
		this.headCommitHash = headCommitHash;
	}
	
	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

    public Collection<Review> getReviews() {
		return getRequest().getReviews(this);
	}

	@JsonProperty
	public String getHeadRef() {
		Preconditions.checkNotNull(getId());
		return Repository.REFS_GITPLEX + "updates/" + getId();
	}
	
	/**
	 * List reviews against this update and all subsequent updates.
	 * <p>
	 * @return
	 * 			list of found reviews, ordered by associated updates reversely
	 */
	public List<Review> listReviewsOnwards() {
		List<Review> reviews = new ArrayList<Review>();
		
		for (PullRequestUpdate update: getRequest().getEffectiveUpdates()) {
			reviews.addAll(update.getReviews());
			if (update.equals(this))
				break;
		}
		
		return reviews;
	}

	public void deleteRefs() {
		Git git = getRequest().getTarget().getRepository().git();
		git.deleteRef(getHeadRef(), null, null);
	}	
	
	public Collection<String> getChangedFiles() {
		return getCachedInfo().getChangedFiles();
	}
	
	public String getBaseCommitHash() {
		PullRequest request = getRequest();

		int index = request.getSortedUpdates().indexOf(this);
		if (index > 0)
			return request.getSortedUpdates().get(index-1).getHeadCommitHash();
		else
			return request.getBaseCommitHash();
	}
	
	/**
	 * Base commit represents head commit of last update, or base commit of the request 
	 * for the first update. Base commit is used to calculate commits belonging to 
	 * current update.
	 * 
	 * @return
	 * 			base commit of this update
	 */
	public Commit getBaseCommit() {
		PullRequest request = getRequest();

		int index = request.getSortedUpdates().indexOf(this);
		if (index > 0)
			return request.getSortedUpdates().get(index-1).getHeadCommit();
		else
			return request.getBaseCommit();
	}
	
	private CachedInfo getCachedInfo() {
		if (cachedInfo == null) {
			Callable<CachedInfo> callable = new Callable<CachedInfo>() {

				@Override
				public CachedInfo call() throws Exception {
					CachedInfo cachedInfo = new CachedInfo();

					Git git = getRequest().getTarget().getRepository().git();
					List<Commit> log = git.log(getBaseCommitHash(), getHeadCommitHash(), null, 0, 0);
					if (log.isEmpty())
						log = Lists.newArrayList(getRequest().getTarget().getRepository().getCommit(getHeadCommitHash()));
					cachedInfo.setLogCommits(log);
					
					String mergeBase = git.calcMergeBase(getHeadCommitHash(), getRequest().getTarget().getHeadCommitHash());

					if (git.isAncestor(getBaseCommitHash(), mergeBase)) { 
						cachedInfo.setChangedFiles(git.listChangedFiles(mergeBase, getHeadCommitHash(), null));					
					} else if (git.isAncestor(mergeBase, getBaseCommitHash())) {
						cachedInfo.setChangedFiles(git.listChangedFiles(getBaseCommitHash(), getHeadCommitHash(), null));					
					} else {
						File tempDir = FileUtils.createTempDir();
						try {
							Git tempGit = new Git(tempDir);
							
							/*
							 * Calculate changed files of this update since merged commit of:
							 * 
							 * 1. merge base of update head and target branch head
							 * 2. head of previous update
							 * 
							 * This way changed files of this update will exclude merged changes 
							 * from target branch if there is any.  
							 */
							String branchName = getRequest().getTarget().getName();
							tempGit.clone(git.repoDir().getAbsolutePath(), false, true, true, branchName);
							tempGit.updateRef("HEAD", mergeBase, null, null);
							tempGit.reset(null, null);
							Preconditions.checkNotNull(tempGit.merge(getBaseCommitHash(), null, null, "ours", null));

							cachedInfo.setChangedFiles(tempGit.listChangedFiles(
									tempGit.parseRevision("HEAD", true), getHeadCommitHash(), null));					
						} finally {
							FileUtils.deleteDir(tempDir);
						}
					}

					return cachedInfo;
				}
				
			};
			if (isNew()) {
				try {
					cachedInfo = callable.call();
				} catch (Exception e) {
					Throwables.propagate(e);
				}
			} else {
				StorageManager storageManager = GitPlex.getInstance(StorageManager.class);
				File cacheFile = new File(storageManager.getCacheDir(this), "cachedInfo");
				cachedInfo = FileUtils.readFile(cacheFile, callable);
			}
		}
		return cachedInfo;
	}

	/**
	 * Get commits belonging to this update, ordered by commit id
	 * 
	 * @return
	 * 			commits belonging to this update ordered by commit id
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
			for (Commit commit: getCachedInfo().getLogCommits()) {
				ScoreAwareCommit scoreAwareCommit = new ScoreAwareCommit();
				scoreAwareCommit.setCommit(commit);
				allCommits.put(commit.getHash(), scoreAwareCommit);
			}

			ScoreAwareCommit headCommit = allCommits.get(getHeadCommitHash());
			if (headCommit != null) {
				headCommit.setScore(0);
				updateAffinityScores(allCommits, headCommit);
			}
			
			Map<String, ScoreAwareCommit> mergedCommits = new HashMap<>();
			for (Commit commit: getRequest().getMergedCommits()) {
				ScoreAwareCommit scoredCommit = new ScoreAwareCommit();
				scoredCommit.setCommit(commit);
				mergedCommits.put(commit.getHash(), scoredCommit);
			}

			headCommit = mergedCommits.get(getRequest().getTarget().getHeadCommitHash());
			if (headCommit != null) {
				headCommit.setScore(0);
				updateAffinityScores(mergedCommits, headCommit);
			}
			
			for (ScoreAwareCommit commit: allCommits.values()) {
				ScoreAwareCommit mergedCommit = mergedCommits.get(commit.getCommit().getHash());
				if (mergedCommit == null || mergedCommit.getScore() >= commit.getScore())
					commits.add(commit.getCommit());
			}
			
			getRequest().getTarget().getRepository().cacheCommits(commits);
			
			Collections.reverse(commits);
		}
		return commits;
	}
	
	public Commit getHeadCommit() {
		Commit headCommit = getCachedInfo().getLogCommits().get(0);
		Preconditions.checkState(headCommit.getHash().equals(getHeadCommitHash()));
		return headCommit;
	}
	
	/*
	 * This method calculates affinity scores of a set of commits against specified head commit (or branch). 
	 * The affinity score reflects the distance between a commit and first-parent chain of the head commit. 
	 * For instance, if a commit can be reached recursively via first-parent of the head commit, its 
	 * affinity score will be 0.     
	 */
	private void updateAffinityScores(Map<String, ScoreAwareCommit> commits, ScoreAwareCommit headCommit) {
		Stack<ScoreAwareCommit> stack = new Stack<>();
		stack.push(headCommit);
		while (!stack.isEmpty()) {
			ScoreAwareCommit currentCommit = stack.pop();
			for (int i=0; i<currentCommit.getCommit().getParentHashes().size(); i++) {
				String parentCommitHash = currentCommit.getCommit().getParentHashes().get(i);
				ScoreAwareCommit parentCommit = commits.get(parentCommitHash);
				if (parentCommit != null && parentCommit.getScore() == Integer.MAX_VALUE) {
					/*
					 * Parent hashes of a commit is ordered in parent number. For instance first-parent is at index 0. 
					 * So the parent score is calculated simply by adding current parent index to the head commit score.
					 */
					parentCommit.setScore(i + currentCommit.getScore());
					stack.push(parentCommit);
				}
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
	
	private static class CachedInfo implements Serializable {
		
		private Collection<String> changedFiles;
		
		private List<Commit> logCommits;

		public Collection<String> getChangedFiles() {
			return changedFiles;
		}

		public void setChangedFiles(Collection<String> changedFiles) {
			this.changedFiles = changedFiles;
		}

		public List<Commit> getLogCommits() {
			return logCommits;
		}

		public void setLogCommits(List<Commit> logCommits) {
			this.logCommits = logCommits;
		}
		
	}
}
