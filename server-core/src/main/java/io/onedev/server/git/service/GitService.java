package io.onedev.server.git.service;

import io.onedev.commons.utils.LinearRange;
import io.onedev.server.git.*;
import io.onedev.server.git.command.RevListOptions;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.revwalk.LastCommitsOfChildren;
import org.eclipse.jgit.revwalk.RevCommit;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

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
	
	List<RefFacade> getCommitRefs(Project project, String prefix);

	RefFacade getRef(Project project, String revision);
	
	void deleteBranch(Project project, String branchName);
	
	void deleteTag(Project project, String tagName);
	
	void fetch(Project targetProject, Project sourceProject, String... refSpecs);
	
	void push(Project sourceProject, String sourceRev, 
			  Project targetProject, String targetRev);

	void pushLfsObjects(Project sourceProject, String sourceRef, 
						Project targetProject, String targetRef,
						ObjectId commitId);
	
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
	
	@Nullable
	BlobIdent getBlobIdent(Project project, ObjectId revId, String path);
	
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

	Collection<ObjectId> filterNonExistants(Project project, Collection<ObjectId> objIds);
	
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
	
	@Nullable
	CommitMessageError checkCommitMessages(Project project, String branch, User user,
							   ObjectId oldId, ObjectId newId, 
							   @Nullable Map<String, String> envs);
	
	@Nullable
	byte[] getRawTag(Project project, ObjectId tagId, Map<String, String> envs);
	
}
