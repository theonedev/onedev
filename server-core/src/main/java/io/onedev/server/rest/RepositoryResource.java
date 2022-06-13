package io.onedev.server.rest;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.codec.binary.Base64;
import org.apache.shiro.authz.UnauthorizedException;
import org.bouncycastle.openpgp.PGPSecretKeyRing;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.hibernate.validator.constraints.NotEmpty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;

import io.onedev.commons.loader.ListenerRegistry;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.StringUtils;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.event.RefUpdated;
import io.onedev.server.git.Blob;
import io.onedev.server.git.BlobContent;
import io.onedev.server.git.BlobEdits;
import io.onedev.server.git.BlobIdent;
import io.onedev.server.git.GitUtils;
import io.onedev.server.git.command.RevListCommand;
import io.onedev.server.git.exception.ObjectNotFoundException;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.persistence.SessionManager;
import io.onedev.server.rest.annotation.Api;
import io.onedev.server.rest.exception.InvalidParamException;
import io.onedev.server.rest.support.FileCreateOrUpdateRequest;
import io.onedev.server.rest.support.FileEditRequest;
import io.onedev.server.search.commit.CommitQuery;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.RevisionAndPath;

@Api(order=1100)
@Path("/repositories")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class RepositoryResource {

	private static final int MAX_COMMITS = 10000;
	
	private final ProjectManager projectManager;
	
	private final SettingManager settingManager;
	
    private final ListenerRegistry listenerRegistry;
    
    private final SessionManager sessionManager;
    
    private static final Logger logger = LoggerFactory.getLogger(RepositoryResource.class);

	@Inject
	public RepositoryResource(ProjectManager projectManager, SessionManager sessionManager, 
			ListenerRegistry listenerRegistry, SettingManager settingManager) {
		this.projectManager = projectManager;
		this.settingManager = settingManager;
		this.sessionManager = sessionManager;
		this.listenerRegistry = listenerRegistry;
	}

	@Api(order=10, description="List all branches")
	@Path("/{projectId}/branches")
	@GET
	public List<String> getBranches(@PathParam("projectId") Long projectId) {
		Project project = projectManager.load(projectId);
		if (!SecurityUtils.canReadCode(project)) {
			throw new UnauthorizedException();
		}

		List<String> branchNames = new ArrayList<>();
		try {
			for (Ref ref: project.getRepository().getRefDatabase().getRefsByPrefix(Constants.R_HEADS))
				branchNames.add(GitUtils.ref2branch(ref.getName()));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return branchNames;
	}
	
	@Api(order=20, description="Get specified branch")
	@Path("/{projectId}/branches/{branch:.*}")
	@GET
	public RefResponse getBranch(
			@PathParam("projectId") Long projectId, 
			@PathParam("branch") @Api(example="test-branch") String branchName) {
		Project project = projectManager.load(projectId);
		if (!SecurityUtils.canReadCode(project)) {
			throw new UnauthorizedException();
		}

		Ref ref = project.getBranchRef(branchName);
		if (ref == null)
			throw new ObjectNotFoundException("Branch not found: " + branchName);

		RefResponse response = new RefResponse();
		
		response.refName = ref.getName();
		response.commitHash = project.getRevCommit(ref.getObjectId(), true).getName();
		
		return response;
	}

	@Api(order=30, description="Create a new branch")
	@Path("/{projectId}/branches")
	@POST
	public Response createBranch(@PathParam("projectId") Long projectId, @NotNull CreateBranchRequest request) {
		Project project = projectManager.load(projectId);
		User user = SecurityUtils.getUser();
		if (!SecurityUtils.canWriteCode(project)) 
			throw new UnauthorizedException();
		else if (project.getBranchRef(request.getBranchName()) != null) 
			throw new InvalidParamException("Branch '" + request.getBranchName() + "' already exists");
		else if (project.getHierarchyBranchProtection(request.getBranchName(), user).isPreventCreation()) 
			throw new ExplicitException("Branch creation prohibited by branch protection rule");
		
		if (!project.isCommitSignatureRequirementSatisfied(
				user, request.getBranchName(), 
				project.getRevCommit(request.getRevision(), true))) {
			throw new ExplicitException("Can not create this branch as branch protection setting "
					+ "requires valid signature on head commit");
		}
		
		project.createBranch(request.getBranchName(), request.getRevision());

		return Response.ok().build();
	}

	@Api(order=40, description="Delete specified branch")
	@Path("/{projectId}/branches/{branch:.*}")
	@DELETE
	public Response deleteBranch(@PathParam("projectId") Long projectId, 
			@PathParam("branch") @Api(example="test-branch") String branchName) {
		Project project = projectManager.load(projectId);
		if (!SecurityUtils.canDeleteBranch(project, branchName)) {
			throw new UnauthorizedException();
		}

		projectManager.deleteBranch(project, branchName);

		return Response.ok().build();
	}

	@Api(order=50, description="List all tags")
	@Path("/{projectId}/tags")
	@GET
	public List<String> getTags(@PathParam("projectId") Long projectId) {
		Project project = projectManager.load(projectId);
		if (!SecurityUtils.canReadCode(project)) {
			throw new UnauthorizedException();
		}

		List<String> tagNames = new ArrayList<>();
		try {
			for (Ref ref: project.getRepository().getRefDatabase().getRefsByPrefix(Constants.R_TAGS))
				tagNames.add(GitUtils.ref2tag(ref.getName()));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return tagNames;
	}
	
	@Api(order=60, description="Get specified tag")
	@Path("/{projectId}/tags/{tag:.*}")
	@GET
	public RefResponse getTag(
			@PathParam("projectId") Long projectId, 
			@PathParam("tag") @Api(example="test-tag") String tagName) {
		Project project = projectManager.load(projectId);
		
		if (!SecurityUtils.canReadCode(project)) {
			throw new UnauthorizedException();
		}

		Ref ref = project.getTagRef(tagName);
		if (ref == null)
			throw new ObjectNotFoundException("Tag not found: " + tagName);

		RefResponse response = new RefResponse();
		
		response.refName = ref.getName();
		response.commitHash = project.getRevCommit(ref.getObjectId(), true).getName();
		
		return response;
	}
	
	@Api(order=70, description="Create a new tag")
	@Path("/{projectId}/tags")
	@POST
	public Response createTag(@PathParam("projectId") Long projectId, @NotNull CreateTagRequest request) {
		Project project = projectManager.load(projectId);
		if (!SecurityUtils.canCreateTag(project, request.getTagName())) {
			throw new UnauthorizedException();
		}

		if (project.getTagRef(request.getTagName()) != null) {
			throw new InvalidParamException("Tag '" + request.getTagName() + "' already exists");
		} else {
			PGPSecretKeyRing signingKey = settingManager.getGpgSetting().getSigningKey();
			
			User user = SecurityUtils.getUser();
			if (project.isTagSignatureRequired(user, request.getTagName()) && signingKey == null) {
				throw new ExplicitException("Tag signature required per tag protection rule, "
						+ "please generate system GPG signing key first");
			}
			
			project.createTag(request.getTagName(), request.getRevision(), 
					SecurityUtils.getUser().asPerson(), request.getTagMessage(), signingKey);
		}

		return Response.ok().build();
	}
	
	@Api(order=80, description="Delete specified tag")
	@Path("/{projectId}/tags/{tag:.*}")
	@DELETE
	public Response deleteTag(
			@PathParam("projectId") Long projectId, 
			@PathParam("tag") @Api(example="test-tag") String tagName) {
		Project project = projectManager.load(projectId);
		if (!SecurityUtils.canDeleteTag(project, tagName)) {
			throw new UnauthorizedException();
		}

		projectManager.deleteTag(project, tagName);

		return Response.ok().build();
	}

	@Api(order=83, description="Query commits of specified project. Will return list of matching commit hashes")
	@Path("/{projectId}/commits")
	@GET
    public List<String> queryCommits(
			@PathParam("projectId") Long projectId, 
    		@QueryParam("query") @Api(description="Syntax of this query is the same as query box in commits page", example="since tag(v4.0.0) until tag(v4.7.0)") String query, 
    		@QueryParam("count") @Api(example="100", description="Number of commits to return") int count) {
		Project project = projectManager.load(projectId);
		if (!SecurityUtils.canReadCode(project)) {
			throw new UnauthorizedException();
		}
		
    	if (count > MAX_COMMITS)
    		throw new InvalidParamException("Count should not be greater than " + MAX_COMMITS);

    	CommitQuery parsedQuery;
		try {
			parsedQuery = CommitQuery.parse(project, query);
		} catch (Exception e) {
			throw new InvalidParamException("Error parsing query", e);
		}
    	
		RevListCommand command = new RevListCommand(project.getGitDir());
		command.ignoreCase(true);
		
		command.count(count);
		parsedQuery.fill(project, command);
		
		return command.call();
    }
	
	@Api(order=86, description="Get specified commit")
	@Path("/{projectId}/commits/{commitHash}")
	@GET
    public CommitResponse getCommit(
			@PathParam("projectId") Long projectId,
			@PathParam("commitHash") @Api(example="8cbec3d9eda2050a4ca0676767be3b6bf20251b8") String commitHash) {
		Project project = projectManager.load(projectId);
		if (!SecurityUtils.canReadCode(project)) {
			throw new UnauthorizedException();
		}

		RevCommit commit = project.getRevCommit(ObjectId.fromString(commitHash), true);
		CommitResponse response = new CommitResponse();
		response.commitHash = commit.name();
		response.author = commit.getAuthorIdent();
		response.committer = commit.getCommitterIdent();
		response.commitMessage = commit.getFullMessage();
		
		return response;
    }
	
	@Api(order=90, description="Get children of specified directory")
	@Path("/{projectId}/directories/{revisionAndDirectory:.*}")
	@GET
	public List<DirectoryChild> getDirectory(
			@PathParam("projectId") Long projectId, 
			@PathParam("revisionAndDirectory") @NotEmpty @Api(example="some-branch-or-tag/path/to/directory") String revisionAndDirectory) {
		Project project = projectManager.load(projectId);
		if (!SecurityUtils.canReadCode(project)) {
			throw new UnauthorizedException();
		}

		List<String> revisionAndPathSegments = Splitter.on('/').splitToList(revisionAndDirectory);
		BlobIdent blobIdent = new BlobIdent(project, revisionAndPathSegments);

		if (!blobIdent.isTree()) {
			throw new InvalidParamException("Specified path is not a directory: " + blobIdent.path);
		}

		ObjectId commitId = project.getRevCommit(blobIdent.revision, true);
		
		List<DirectoryChild> children = new ArrayList<>();
		
		Repository repository = project.getRepository();			
		try (RevWalk revWalk = new RevWalk(repository)) {
			RevTree revTree = revWalk.parseCommit(commitId).getTree();
			TreeWalk treeWalk;
			if (blobIdent.path != null) {
				treeWalk = Preconditions.checkNotNull(TreeWalk.forPath(repository, blobIdent.path, revTree));
				treeWalk.enterSubtree();
			} else {
				treeWalk = new TreeWalk(repository);
				treeWalk.addTree(revTree);
			}
			while (treeWalk.next()) {
				DirectoryChild child = new DirectoryChild();
				child.path = treeWalk.getPathString();
				child.isFile = (FileMode.TYPE_MASK & treeWalk.getRawMode(0)) == FileMode.TYPE_FILE;
				children.add(child);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		} 
		
		return children;
	}
	
	@Api(order=100, description="Get metadata and content of specified file")
	@Path("/{projectId}/files/{revisionAndFile:.*}")
	@GET
	public FileResponse getFile(
			@PathParam("projectId") Long projectId, 
			@PathParam("revisionAndFile") @NotEmpty @Api(example="some-branch-or-tag/path/to/file") String revisionAndFile) {
		Project project = projectManager.load(projectId);
		if (!SecurityUtils.canReadCode(project)) {
			throw new UnauthorizedException();
		}

		List<String> revisionAndPathSegments = Splitter.on('/').splitToList(revisionAndFile);
		BlobIdent blobIdent = new BlobIdent(project, revisionAndPathSegments);

		if (!blobIdent.isFile()) {
			throw new InvalidParamException("Specified path is not a file: " + blobIdent.path);
		}

		Blob blob = project.getBlob(blobIdent, true);

		FileResponse response = new FileResponse();
		response.path = blobIdent.path;
		response.sha = blob.getBlobId().name();
		response.base64Content = new String(Base64.encodeBase64(blob.getBytes()));
		response.size = blob.getSize();
		response.isPartial = blob.isPartial();
		return response;
	}
	
	@Api(order=110, description="Create, update, or delete specified file. Return hash of resulting commit", 
			example="46c001b04cba0ca41588841f1ca32f50b582ee9b")
	@Path("/{projectId}/files/{branchAndFile:.*}")
	@POST
	public FileEditResponse editFile(
			@PathParam("projectId") Long projectId, 
			@PathParam("branchAndFile") @NotEmpty @Api(example="test-branch/path/to/file") String branchAndFile, 
			@NotNull FileEditRequest request) {
		Project project = projectManager.load(projectId);
		
		List<String> revisionAndPathSegments = Splitter.on('/').splitToList(branchAndFile);
		RevisionAndPath revisionAndPath;
		String refName;
		ObjectId oldCommitId;
		if (project.getDefaultBranch() != null) {
			revisionAndPath = RevisionAndPath.parse(project, revisionAndPathSegments);
			Ref ref = project.getBranchRef(revisionAndPath.getRevision());
			if (ref == null) 
				throw new InvalidParamException("Not a branch: " + revisionAndPath.getRevision());
			refName = ref.getName();
			oldCommitId = ref.getObjectId();
			if (revisionAndPath.getPath() == null)
				throw new InvalidParamException("Branch and file should be specified");
		} else {
			if (revisionAndPathSegments.size() < 2)
				throw new InvalidParamException("Branch and file should be specified");
			revisionAndPath = new RevisionAndPath(
					revisionAndPathSegments.get(0), 
					StringUtils.join(revisionAndPathSegments.subList(1, revisionAndPathSegments.size())));
			refName = GitUtils.branch2ref(revisionAndPath.getRevision());
			oldCommitId = ObjectId.zeroId();
		}

		if (!SecurityUtils.canModify(project, revisionAndPath.getRevision(), revisionAndPath.getPath())) 
			throw new UnauthorizedException();

		Repository repository = project.getRepository();
		
		Map<String, BlobContent> newBlobs = new HashMap<>();
		
		Set<String> oldPaths = new HashSet<>();
		if (!oldCommitId.equals(ObjectId.zeroId())) {
			RevCommit revCommit = project.getRevCommit(oldCommitId, true);
			try (TreeWalk treeWalk = TreeWalk.forPath(project.getRepository(), revisionAndPath.getPath(), revCommit.getTree())) {
				if (treeWalk != null) {
					oldPaths.add(revisionAndPath.getPath());
				}
			} catch (IOException e) {
				// ignore
			}
		}
		
		if (request instanceof FileCreateOrUpdateRequest) {
			newBlobs.put(revisionAndPath.getPath(), new BlobContent.Immutable(
					Base64.decodeBase64(((FileCreateOrUpdateRequest)request).getBase64Content()),
					FileMode.REGULAR_FILE
				));
		}

		User user = SecurityUtils.getUser();
		PGPSecretKeyRing signingKey = settingManager.getGpgSetting().getSigningKey();
		
		if (project.isCommitSignatureRequired(user, revisionAndPath.getRevision()) && signingKey == null) {
			throw new ExplicitException("Signature required per branch protection rule, "
					+ "please generate system GPG signing key first");
		}
		
		ObjectId newCommitId = new BlobEdits(oldPaths, newBlobs).commit(
				repository, refName, oldCommitId, oldCommitId,
				user.asPerson(), request.getCommitMessage(), signingKey);

		if (project.getDefaultBranch() == null)
			project.setDefaultBranch(revisionAndPath.getRevision());
		
        sessionManager.runAsync(new Runnable() {

			@Override
			public void run() {
		        try {
		            Project project = projectManager.load(projectId);

		        	if (!newCommitId.equals(ObjectId.zeroId()))
		        		project.cacheObjectId(refName, newCommitId);
		        	else 
		        		project.cacheObjectId(refName, null);
		        	listenerRegistry.post(new RefUpdated(project, refName, oldCommitId, newCommitId));
		        } catch (Exception e) {
		        	logger.error("Error posting ref updated event", e);
				}
			}
        	
        });
		
		FileEditResponse response = new FileEditResponse();
		response.commitHash = newCommitId.name();
		return response;
	}

	// Wrap string inside an object to make return value a valid json for some third party 
	// applications to consume
	public static class FileEditResponse implements Serializable {

		private static final long serialVersionUID = 1L;
		
		String commitHash;
		
	}

	public static class RefResponse implements Serializable {

		private static final long serialVersionUID = 1L;

		String refName;

		@Api(example="46c001b04cba0ca41588841f1ca32f50b582ee9b")
		String commitHash;

	}

	public static class CreateBranchRequest implements Serializable {

		private static final long serialVersionUID = 1L;

		private String branchName;

		@Api(example="46c001b04cba0ca41588841f1ca32f50b582ee9b")
		private String revision;

		@NotEmpty
		public String getBranchName() {
			return branchName;
		}

		public void setBranchName(String branchName) {
			this.branchName = branchName;
		}

		@NotEmpty
		public String getRevision() {
			return revision;
		}

		public void setRevision(String revision) {
			this.revision = revision;
		}

	}

	public static class CreateTagRequest implements Serializable {

		private static final long serialVersionUID = 1L;

		private String tagName;
		
		private String tagMessage;

		@Api(example="46c001b04cba0ca41588841f1ca32f50b582ee9b")
		private String revision;

		@NotEmpty
		public String getTagName() {
			return tagName;
		}

		public void setTagName(String tagName) {
			this.tagName = tagName;
		}

		public String getTagMessage() {
			return tagMessage;
		}

		public void setTagMessage(String tagMessage) {
			this.tagMessage = tagMessage;
		}

		@NotEmpty
		public String getRevision() {
			return revision;
		}

		public void setRevision(String revision) {
			this.revision = revision;
		}

	}

	public static class DirectoryChild implements Serializable {

		private static final long serialVersionUID = 1L;

		String path;

		boolean isFile;
		
	}
	
	public static class FileResponse implements Serializable {

		private static final long serialVersionUID = 1L;

		String path;

		@Api(example="46c001b04cba0ca41588841f1ca32f50b582ee9b")
		String sha;

		@Api(description="Base64 encoding of file content")
		String base64Content;

		long size;

		@Api(description="True if the content is not fully loaded due to too large. "
				+ "The partial content can still be useful for instance to decide "
				+ "mime type of the file")
		boolean isPartial;

	}
	
	public static class CommitResponse implements Serializable {

		private static final long serialVersionUID = 1L;

		String commitHash;
		
		PersonIdent author;
		
		PersonIdent committer;
		
		String commitMessage;
		
	}
	
}
