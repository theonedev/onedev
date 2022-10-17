package io.onedev.server.git.service;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.revwalk.LastCommitsOfChildren;
import org.eclipse.jgit.revwalk.RevCommit;

import io.onedev.commons.utils.LinearRange;
import io.onedev.server.git.BlameBlock;
import io.onedev.server.git.Blob;
import io.onedev.server.git.BlobEdits;
import io.onedev.server.git.BlobIdent;
import io.onedev.server.git.BlobIdentFilter;
import io.onedev.server.git.command.RevListOptions;
import io.onedev.server.model.Project;

public interface GitService {

	@Nullable
	String getDefaultBranch(Project project);
	
	void setDefaultBranch(Project project, String defaultBranch);
	
	@Nullable
	ObjectId resolve(Project project, String revision, boolean errorIfInvalid);
	
	@Nullable
	RevCommit getCommit(Project project, ObjectId revId);
	
	List<RevCommit> getCommits(Project project, List<ObjectId> revIds);
	
	int getMode(Project project, ObjectId revId, String path);
	
	ObjectId createBranch(Project project, String branchName, String branchRevision);
	
	TaggingResult createTag(Project project, String tagName, String tagRevision, 
			PersonIdent taggerIdent, @Nullable String tagMessage, boolean signRequired);
	
	int countRefs(Long projectId, String prefix);

	@Nullable
	String getClosestPath(Project project, ObjectId revId, String path);
	
	List<RefFacade> getRefs(Project project, String prefix);

	RefFacade getRef(Project project, String revision);
	
	void deleteBranch(Project project, String branchName);
	
	void deleteTag(Project project, String tagName);
	
	void fetch(Project sourceProject, Project targetProject, String... refSpecs);
	
	void push(Project targetProject, Project sourceProject, String... refSpecs);
	
	void updateRef(Project project, String refName, ObjectId newObjectId, 
			@Nullable ObjectId expectedOldObjectId);
	
	<T extends Serializable> Collection<T> filterParents(Project project,  
			ObjectId commitId, Map<ObjectId, T> values, int limit);
	
	List<RevCommit> sortValidCommits(Project project, Collection<ObjectId> commitIds);
	
	List<String> revList(Project project, RevListOptions options);
	
	ObjectId commit(Project project, BlobEdits blobEdits, String refName, 
			ObjectId expectedOldCommitId, ObjectId parentCommitId, 
			PersonIdent authorAndCommitter, String commitMessage, 
			boolean signRequired);

	@Nullable
	PathChange getPathChange(Project project, ObjectId oldRevId, ObjectId newRevId, 
			String path);
	
	@Nullable
	Blob getBlob(Project project, ObjectId revId, String path);
	
	void deleteRefs(Project project, Collection<String> refs);
	
	@Nullable
	ObjectId merge(Project project, ObjectId targetCommitId, ObjectId sourceCommitId,
			boolean squash, PersonIdent committer, PersonIdent author, String commitMessage,
			boolean useOursOnConflict);
	
	boolean isMergedInto(Project project, @Nullable Map<String, String> gitEnvs, 
			ObjectId base, ObjectId tip);

	@Nullable
	ObjectId rebase(Project project, ObjectId source, ObjectId target, PersonIdent committer);
	
	ObjectId amendCommits(Project project, ObjectId startCommitId, ObjectId endCommitId, 
			String oldCommitterName, PersonIdent newCommitter);

	ObjectId amendCommit(Project project, ObjectId commitId, @Nullable PersonIdent author, 
			PersonIdent committer, String commitMessage);
	
	List<RevCommit> getReachableCommits(Project project, Collection<ObjectId> startCommitIds, 
			Collection<ObjectId> uninterestingCommitIds);
	
	Collection<String> getChangedFiles(Project project, ObjectId oldCommitId, ObjectId newCommitId, 
			@Nullable Map<String, String> gitEnvs);

	@Nullable
	ObjectId getMergeBase(Project project1, ObjectId commitId1, Project project2, ObjectId commitId2);
	
	boolean hasObjects(Project project, ObjectId... objIds);

	List<BlobIdent> getChildren(Project project, ObjectId revId, @Nullable String path, 
			BlobIdentFilter filter, boolean expandSingle);
	
	LastCommitsOfChildren getLastCommitsOfChildren(Project project, ObjectId revId, 
			@Nullable String path);
	
	List<DiffEntryFacade> diff(Project project, AnyObjectId oldRevId, AnyObjectId newRevId);

	Map<ObjectId, AheadBehind> getAheadBehinds(Project project, ObjectId baseId, 
			Collection<ObjectId> compareIds);
	
	Collection<BlameBlock> blame(Project project, ObjectId revId, String file, 
			@Nullable LinearRange range);
	
	byte[] getRawCommit(Project project, ObjectId revId, Map<String, String> envs);
	
	byte[] getRawTag(Project project, ObjectId tagId, Map<String, String> envs);
	
}
