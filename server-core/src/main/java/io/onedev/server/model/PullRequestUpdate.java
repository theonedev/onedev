package io.onedev.server.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.RefUpdate;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.revwalk.filter.RevFilter;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.TreeFilter;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;

import io.onedev.commons.utils.ExceptionUtils;
import io.onedev.server.entitymanager.impl.DefaultCodeCommentRelationManager;
import io.onedev.server.git.GitUtils;

@Entity
@Table(indexes={@Index(columnList="o_request_id"), @Index(columnList="date")})
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
public class PullRequestUpdate extends AbstractEntity {

	private static final long serialVersionUID = 1L;

	public static final String REFS_PREFIX = "refs/updates/";
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private PullRequest request;
	
	@Column(nullable=false)
	private String headCommitHash;
	
	@Column(nullable=false)
	private String mergeBaseCommitHash;

	@Column(nullable=false)
	private Date date = new Date();
	
	private transient List<RevCommit> commits;
	
	private transient Collection<String> changedFiles;
	
	public PullRequest getRequest() {
		return request;
	}

	public void setRequest(PullRequest request) {
		this.request = request;
	}

	public String getHeadCommitHash() {
		return headCommitHash;
	}
	
	public void setHeadCommitHash(String headCommitHash) {
		this.headCommitHash = headCommitHash;
	}
	
	public String getMergeBaseCommitHash() {
		return mergeBaseCommitHash;
	}

	public void setMergeBaseCommitHash(String mergeBaseCommitHash) {
		this.mergeBaseCommitHash = mergeBaseCommitHash;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	@JsonProperty
	public String getHeadRef() {
		Preconditions.checkNotNull(getId());
		return REFS_PREFIX + getId();
	}
	
	public void deleteRefs() {
		GitUtils.deleteRef(GitUtils.getRefUpdate(getRequest().getTargetProject().getRepository(), getHeadRef()));
	}	
	
	/**
	 * Get changed files of this update since previous update. This calculation 
	 * excludes changes introduced by commits from target branch (this will 
	 * happen if some commits were merged from target branch to source branch)
	 */
	public Collection<String> getChangedFiles() {
		if (changedFiles == null) {
			changedFiles = new HashSet<>();
			
			Repository repository = getRequest().getWorkProject().getRepository();
			try (	RevWalk revWalk = new RevWalk(repository);
					TreeWalk treeWalk = new TreeWalk(repository)) {
				RevCommit mergeBaseCommit = revWalk.parseCommit(ObjectId.fromString(getMergeBaseCommitHash()));
				RevCommit baseCommit = revWalk.parseCommit(ObjectId.fromString(getBaseCommitHash()));
				RevCommit headCommit = revWalk.parseCommit(ObjectId.fromString(getHeadCommitHash()));
				revWalk.markStart(mergeBaseCommit);
				revWalk.markStart(baseCommit);
				revWalk.setRevFilter(RevFilter.MERGE_BASE);
				RevCommit nextMergeBase = Preconditions.checkNotNull(revWalk.next());
				treeWalk.setRecursive(true);
				if (nextMergeBase.equals(baseCommit)) {
					treeWalk.addTree(mergeBaseCommit.getTree());
					treeWalk.addTree(headCommit.getTree());
					treeWalk.setFilter(TreeFilter.ANY_DIFF);
					while (treeWalk.next())
						changedFiles.add(treeWalk.getPathString());
				} else if (nextMergeBase.equals(mergeBaseCommit)) {
					treeWalk.addTree(baseCommit.getTree());
					treeWalk.addTree(headCommit.getTree());
					treeWalk.setFilter(TreeFilter.ANY_DIFF);
					while (treeWalk.next())
						changedFiles.add(treeWalk.getPathString());
				} else {
					treeWalk.addTree(headCommit.getTree());
					treeWalk.addTree(baseCommit.getTree());
					treeWalk.addTree(mergeBaseCommit.getTree());
					treeWalk.setFilter(new TreeFilter() {

						@Override
						public boolean include(TreeWalk walker)
								throws MissingObjectException, IncorrectObjectTypeException, IOException {
							int m0 = walker.getRawMode(0);
							
							// only include the path if the file is modified in head commit 
							// compared to base commit and that modification is not introduced
							// by commit from target branch
							return (walker.getRawMode(1) != m0 || !walker.idEqual(1, 0)) 
									&& (walker.getRawMode(2) != m0 || !walker.idEqual(2, 0));
						}

						@Override
						public boolean shouldBeRecursive() {
							return false;
						}

						@Override
						public TreeFilter clone() {
							return this;
						}
						
					});
					while (treeWalk.next()) {
						changedFiles.add(treeWalk.getPathString());
					}
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		return changedFiles;
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
	public RevCommit getBaseCommit() {
		PullRequest request = getRequest();

		int index = request.getSortedUpdates().indexOf(this);
		if (index > 0)
			return request.getSortedUpdates().get(index-1).getHeadCommit();
		else
			return request.getBaseCommit();
	}
	
	/**
	 * Get commits belonging to this update, reversely ordered by commit traversing. The list of commit will remain 
	 * unchanged even if tip of target branch changes. This assumption is key to our caching of code comment to
	 * to pull request relations. Check {@link DefaultCodeCommentRelationManager#findCodeComments(PullRequest)} for
	 * details
	 * 
	 * @return
	 * 			commits belonging to this update ordered by commit id
	 */
	public List<RevCommit> getCommits() {
		if (commits == null) {
			commits = new ArrayList<>();
			
			try (RevWalk revWalk = new RevWalk(getRequest().getWorkProject().getRepository())) {
				revWalk.markStart(revWalk.parseCommit(ObjectId.fromString(getHeadCommitHash())));
				revWalk.markUninteresting(revWalk.parseCommit(ObjectId.fromString(getBaseCommitHash())));
				
				/*
				 * Instead of excluding commits reachable from target branch, we exclude commits reachable
				 * from the merge commit to achieve two purposes:
				 * 1. commits merged back into target branch after this update can still be included in this
				 * update
				 * 2. commits of this update will remain unchanged even if tip of the target branch changes     
				 */
				revWalk.markUninteresting(revWalk.parseCommit(ObjectId.fromString(getMergeBaseCommitHash())));
				
				revWalk.forEach(c->commits.add(c));
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			Collections.reverse(commits);
		}
		return commits;
	}
	
	public RevCommit getHeadCommit() {
		return request.getWorkProject().getRevCommit(ObjectId.fromString(getHeadCommitHash()), true);
	}
	
	public void writeRef() {
		ObjectId updateHeadId = ObjectId.fromString(getHeadCommitHash());
		if (!request.getTargetProject().equals(request.getSourceProject())) {
			try {
				request.getTargetProject().git().fetch()
						.setRemote(request.getSourceProject().getGitDir().getAbsolutePath())
						.setRefSpecs(new RefSpec(GitUtils.branch2ref(request.getSourceBranch()) + ":" + getHeadRef()))
						.call();
				if (!request.getTargetProject().getObjectId(getHeadRef(), true).equals(updateHeadId)) {
					RefUpdate refUpdate = GitUtils.getRefUpdate(request.getTargetProject().getRepository(), getHeadRef());
					refUpdate.setNewObjectId(updateHeadId);
					GitUtils.updateRef(refUpdate);
				}
			} catch (Exception e) {
				throw ExceptionUtils.unchecked(e);
			}
		} else {
			RefUpdate refUpdate = GitUtils.getRefUpdate(request.getTargetProject().getRepository(), getHeadRef());
			refUpdate.setNewObjectId(updateHeadId);
			GitUtils.updateRef(refUpdate);
		}
	}
	
}
