package io.onedev.server.rest;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.codec.binary.Base64;
import org.apache.shiro.authz.UnauthorizedException;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.TreeWalk;

import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.git.Blob;
import io.onedev.server.git.BlobEdits;
import io.onedev.server.git.BlobContent;
import io.onedev.server.git.BlobIdent;
import io.onedev.server.git.GitContribution;
import io.onedev.server.git.GitContributor;
import io.onedev.server.git.GitUtils;
import io.onedev.server.model.Project;
import io.onedev.server.model.UserAuthorization;

import io.onedev.server.rest.annotation.Api;
import io.onedev.server.rest.jersey.InvalidParamException;
import io.onedev.server.security.SecurityUtils;

@Api(order=3450)
@Path("/git-projects")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class GitResource {

	private final ProjectManager projectManager;

	@Inject
	public GitResource(ProjectManager projectManager) {
		this.projectManager = projectManager;
	}

	@Api(order=10, description="Retrieve a specific branch from a repository.")
	@Path("/{projectId}/branches/{branch}")
	@GET
	public RefResponse getBranch(@PathParam("projectId") Long projectId, @PathParam("branch") @Api(example="test-branch") String branchName) {
		Project project = projectManager.load(projectId);
		if (!SecurityUtils.canReadCode(project)) {
			throw new UnauthorizedException();
		}

		String refName = GitUtils.branch2ref(branchName);
		ObjectId commitId = project.getObjectId(refName, true);

		RefResponse response = new RefResponse();
		response.ref = refName;
		response.revision = commitId.name();
		return response;
	}

	public static class RefResponse implements Serializable {

		private static final long serialVersionUID = 1L;

		private String ref;

		private String revision;

		public String getRef() {
			return ref;
		}

		public void setRef(String ref) {
			this.ref = ref;
		}

		public String getRevision() {
			return revision;
		}

		public void setRevision(String revision) {
			this.revision = revision;
		}

	}

	@Api(order=20, description="Create a new git branch")
	@Path("/{projectId}/branches")
	@POST
	public Response createBranch(@PathParam("projectId") Long projectId, @NotNull CreateBranchSettings branch) {
		Project project = projectManager.load(projectId);
		if (!SecurityUtils.canWriteCode(project)) {
			throw new UnauthorizedException();
		}

		if (project.getObjectId(GitUtils.branch2ref(branch.getBranchName()), false) != null) {
			throw new InvalidParamException("Branch '" + branch.getBranchName() + "' already exists");
		} else if (project.getBranchProtection(branch.getBranchName(), SecurityUtils.getUser()).isPreventCreation()) {
			throw new InvalidParamException("Can not create this branch according to branch protection setting");
		} else {
			project.createBranch(branch.getBranchName(), branch.getRevision());
		}

		return Response.ok().build();
	}

	public static class CreateBranchSettings implements Serializable {

		private static final long serialVersionUID = 1L;

		private String branchName;

		private String revision;

		public String getBranchName() {
			return branchName;
		}

		public void setBranchName(String branchName) {
			this.branchName = branchName;
		}

		public String getRevision() {
			return revision;
		}

		public void setRevision(String revision) {
			this.revision = revision;
		}

	}

	@Api(order=30, description="Delete a specific branch from a repository.")
	@Path("/{projectId}/branches/{branch}")
	@DELETE
	public Response deleteBranch(@PathParam("projectId") Long projectId, @PathParam("branch") @Api(example="test-branch") String branchName) {
		Project project = projectManager.load(projectId);
		if (!SecurityUtils.canDeleteBranch(project, branchName)) {
			throw new UnauthorizedException();
		}

		projectManager.deleteBranch(project, branchName);

		return Response.ok().build();
	}

	@Api(order=40, description="Gets the metadata and contents of a file in a repository.")
	@Path("/{projectId}/contents/{branch}/{filepath:.*}")
	@GET
	public ContentsResponse getContents(@PathParam("projectId") Long projectId, @PathParam("branch") @Api(example="test-branch") String branchName, @PathParam("filepath") @Api(example="path/to/file") String filepath) {
		Project project = projectManager.load(projectId);
		if (!SecurityUtils.canReadCode(project)) {
			throw new UnauthorizedException();
		}

		List<String> revisionAndPathSegments = new ArrayList<>();
		revisionAndPathSegments.add(branchName);
		revisionAndPathSegments.add(filepath);
		BlobIdent blobIdent = new BlobIdent(project, revisionAndPathSegments);

		if (!blobIdent.isFile()) {
			throw new InvalidParamException("Blob is not a file");
		}

		Blob blob = project.getBlob(blobIdent, true);

		ContentsResponse response = new ContentsResponse();
		response.path = filepath;
		response.sha = blob.getBlobId().name();
		response.content = new String(Base64.encodeBase64(blob.getBytes()));
		response.size = blob.getSize();
		response.isPartial = blob.isPartial();
		return response;
	}

	public static class ContentsResponse implements Serializable {

		private static final long serialVersionUID = 1L;

		private String path;

		private String sha;

		private String content;

		private long size;

		private boolean isPartial;

		public String getPath() {
			return path;
		}

		public void setPath(String path) {
			this.path = path;
		}

		public String getSha() {
			return sha;
		}

		public void setSha(String sha) {
			this.sha = sha;
		}

		public String getContent() {
			return content;
		}

		public void setContent(String content) {
			this.content = content;
		}

		public long getSize() {
			return size;
		}

		public void setSize(long size) {
			this.size = size;
		}

		public boolean isPartial() {
			return isPartial;
		}

		public void setPartial(boolean partial) {
			isPartial = partial;
		}

	}

	@Api(order=50, description="Create or update a file in a repository.")
	@Path("/{projectId}/contents/{branch}/{filepath:.*}")
	@PUT
	public RefResponse createOrUpdateContents(@PathParam("projectId") Long projectId, @PathParam("branch") @Api(example="test-branch") String branchName, @PathParam("filepath") @Api(example="path/to/file") String filepath, ContentsRequest request) {
		Project project = projectManager.load(projectId);
		if (!SecurityUtils.canWriteCode(project)) {
			throw new UnauthorizedException();
		}

		String refName = GitUtils.branch2ref(branchName);
		ObjectId prevCommitId = project.getObjectId(refName, true);

		Set<String> oldPaths = new HashSet<>();
		RevCommit revCommit = project.getRevCommit(prevCommitId, true);
		try (TreeWalk treeWalk = TreeWalk.forPath(project.getRepository(), filepath, revCommit.getTree())) {
			if (treeWalk != null) {
				oldPaths.add(filepath);
			}
		} catch (IOException e) {
			// ignore
		}

		Repository repository = project.getRepository();

		Map<String, BlobContent> newBlobs = new HashMap<>();
		newBlobs.put(filepath, new BlobContent.Immutable(
			Base64.decodeBase64(request.getContent()),
			FileMode.REGULAR_FILE
		));

		ObjectId newCommitId = new BlobEdits(oldPaths, newBlobs)
			.commit(
				repository,
				refName,
				prevCommitId,
				prevCommitId,
				SecurityUtils.getUser().asPerson(),
				request.getMessage()
			);

		RefResponse response = new RefResponse();
		response.ref = refName;
		response.revision = newCommitId.name();
		return response;
	}

	public static class ContentsRequest implements Serializable {

		private static final long serialVersionUID = 1L;

		private String message;

		private String content;

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}

		public String getContent() {
			return content;
		}

		public void setContent(String content) {
			this.content = content;
		}

	}

}
