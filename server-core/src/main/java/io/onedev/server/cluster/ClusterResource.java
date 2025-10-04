package io.onedev.server.cluster;

import static io.onedev.commons.utils.LockUtils.read;
import static io.onedev.commons.utils.LockUtils.write;
import static io.onedev.server.model.Build.getArtifactsLockName;
import static io.onedev.server.model.Project.SHARE_TEST_DIR;
import static io.onedev.server.util.IOUtils.BUFFER_SIZE;
import static javax.ws.rs.core.Response.ok;
import static javax.ws.rs.core.Response.status;
import static javax.ws.rs.core.Response.Status.NO_CONTENT;
import static org.eclipse.jgit.lib.ObjectId.fromString;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import io.onedev.server.annotation.NoDBAccess;
import org.apache.shiro.authz.UnauthorizedException;
import org.eclipse.jgit.lib.Repository;

import com.google.common.base.Splitter;
import com.google.common.collect.Sets;

import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.TarUtils;
import io.onedev.server.OneDev;
import io.onedev.server.StorageService;
import io.onedev.server.attachment.AttachmentService;
import io.onedev.server.service.BuildService;
import io.onedev.server.service.JobCacheService;
import io.onedev.server.service.PackBlobService;
import io.onedev.server.service.ProjectService;
import io.onedev.server.git.CommandUtils;
import io.onedev.server.git.GitFilter;
import io.onedev.server.git.GitUtils;
import io.onedev.server.git.LfsObject;
import io.onedev.server.git.command.AdvertiseReceiveRefsCommand;
import io.onedev.server.git.command.AdvertiseUploadRefsCommand;
import io.onedev.server.git.hook.HookUtils;
import io.onedev.server.model.PackBlob;
import io.onedev.server.model.Project;
import io.onedev.server.rest.annotation.Api;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.IOUtils;
import io.onedev.server.util.concurrent.WorkExecutionService;
import io.onedev.server.util.patternset.PatternSet;
import io.onedev.server.xodus.CommitInfoService;
import io.onedev.server.xodus.VisitInfoService;

@Api(internal=true)
@Path("/cluster")
@Consumes(MediaType.WILDCARD)
@Singleton
public class ClusterResource {

	private final ProjectService projectService;
	
	private final AttachmentService attachmentService;
	
	private final CommitInfoService commitInfoService;
	
	private final VisitInfoService visitInfoService;
	
	private final StorageService storageService;
	
	private final PackBlobService packBlobService;
	
	private final BuildService buildService;
	
	private final JobCacheService jobCacheService;
	
	private final WorkExecutionService workExecutionService;
	
	@Inject
	public ClusterResource(ProjectService projectService, CommitInfoService commitInfoService,
						   AttachmentService attachmentService, VisitInfoService visitInfoService,
						   WorkExecutionService workExecutionService, StorageService storageService,
						   PackBlobService packBlobService, BuildService buildService,
						   JobCacheService jobCacheService) {
		this.commitInfoService = commitInfoService;
		this.projectService = projectService;
		this.workExecutionService = workExecutionService;
		this.attachmentService = attachmentService;
		this.visitInfoService = visitInfoService;
		this.storageService = storageService;
		this.packBlobService = packBlobService;
		this.buildService = buildService;
		this.jobCacheService = jobCacheService;
	}

	@Path("/project-files")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@GET
	public Response downloadFiles(@QueryParam("projectId") Long projectId,
								  @QueryParam("path") String path,
								  @QueryParam("patterns") String patterns,
								  @QueryParam("readLock") String readLock) {
		if (!SecurityUtils.isSystem())
			throw new UnauthorizedException("This api can only be accessed via cluster credential");

		StreamingOutput output = os -> read(readLock, () -> {
			File directory = new File(projectService.getProjectDir(projectId), path);
			PatternSet patternSet = PatternSet.parse(patterns);
			patternSet.getExcludes().add(SHARE_TEST_DIR + "/**");
			TarUtils.tar(directory, patternSet.getIncludes(), patternSet.getExcludes(), os, false);
		});
		return ok(output).build();
	}

	@Path("/assets")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@GET
	public Response downloadAssets() {
		if (!SecurityUtils.isSystem())
			throw new UnauthorizedException("This api can only be accessed via cluster credential");
		
		StreamingOutput output = os -> TarUtils.tar(OneDev.getAssetsDir(), Sets.newHashSet("**"), null, os, false);
		return ok(output).build();
	}
	
	@Path("/project-file")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@GET
	public Response downloadProjectFile(@QueryParam("projectId") Long projectId,
										@QueryParam("path") String path,
										@QueryParam("readLock") String readLock) {
		if (!SecurityUtils.isSystem())
			throw new UnauthorizedException("This api can only be accessed via cluster credential");

		File file = new File(projectService.getProjectDir(projectId), path);
		if (read(readLock, file::exists)) {
			StreamingOutput os = output -> read(readLock, () -> {
				try (output; InputStream is = new FileInputStream(file)) {
					IOUtils.copy(is, output, BUFFER_SIZE);
				}
				return null;
			});
			return ok(os).build();
		} else {
			return status(NO_CONTENT).build();
		}
	}
	
	@Path("/artifacts")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@GET
	public Response downloadArtifacts(@QueryParam("projectId") Long projectId,
			@QueryParam("buildNumber") Long buildNumber,
			@QueryParam("artifacts") String artifacts) {
		if (!SecurityUtils.isSystem()) 
			throw new UnauthorizedException("This api can only be accessed via cluster credential");
		
		StreamingOutput output = os -> read(getArtifactsLockName(projectId, buildNumber), () -> {
			File artifactsDir = buildService.getArtifactsDir(projectId, buildNumber);
			PatternSet patternSet = PatternSet.parse(artifacts);
			patternSet.getExcludes().add(SHARE_TEST_DIR + "/**");
			TarUtils.tar(artifactsDir, patternSet.getIncludes(), patternSet.getExcludes(), os, false);
			return null;
		});
		return ok(output).build();
	}

	@Path("/artifact")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@GET
	public Response downloadArtifact(@QueryParam("projectId") Long projectId,
									  @QueryParam("buildNumber") Long buildNumber,
									  @QueryParam("artifactPath") String artifactPath) {
		if (!SecurityUtils.isSystem())
			throw new UnauthorizedException("This api can only be accessed via cluster credential");

		StreamingOutput os = output -> read(getArtifactsLockName(projectId, buildNumber), () -> {
			File artifactsDir = buildService.getArtifactsDir(projectId, buildNumber);
			File artifactFile = new File(artifactsDir, artifactPath);
			try (output; InputStream is = new FileInputStream(artifactFile)) {
				IOUtils.copy(is, output, BUFFER_SIZE);
			}
			return null;
		});
		return ok(os).build();
	}
	
	@Path("/blob")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@GET
	public Response downloadBlob(@QueryParam("projectId") Long projectId, @QueryParam("revId") String revId, 
			@QueryParam("path") String path) {
		if (!SecurityUtils.isSystem()) 
			throw new UnauthorizedException("This api can only be accessed via cluster credential");
		
		StreamingOutput os = output -> {
			Repository repository = projectService.getRepository(projectId);
			try (output; InputStream is = GitUtils.getInputStream(repository, fromString(revId), path)) {
				IOUtils.copy(is, output, BUFFER_SIZE);
			}
	   };
		return ok(os).build();
	}

	@NoDBAccess
	@Path("/pack-blob")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@GET
	public Response downloadPackBlob(@QueryParam("projectId") Long projectId, 
									 @QueryParam("hash") String hash) {
		if (!SecurityUtils.isSystem())
			throw new UnauthorizedException("This api can only be accessed via cluster credential");
		
		StreamingOutput out = os -> {
			read(PackBlob.getFileLockName(projectId, hash), () -> {
				try (os; var is = new FileInputStream(packBlobService.getPackBlobFile(projectId, hash))) {
					IOUtils.copy(is, os, BUFFER_SIZE);
				}
				return null;
			});
		};
		return ok(out).build();
	}
	
	@Path("/cache")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@GET
	public Response downloadCache(
			@QueryParam("projectId") Long projectId, 
			@QueryParam("cacheId") Long cacheId, 
			@QueryParam("cachePaths") String joinedCachePaths) {
		if (!SecurityUtils.isSystem())
			throw new UnauthorizedException("This api can only be accessed via cluster credential");
		var cachePaths = Splitter.on('\n').splitToList(joinedCachePaths);
		StreamingOutput out = os -> jobCacheService.downloadCache(projectId, cacheId, cachePaths, os);
		return ok(out).build();
	}

	@Path("/site")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@GET
	public Response downloadSiteFile(@QueryParam("projectId") Long projectId, @QueryParam("filePath") String filePath) {
		if (!SecurityUtils.isSystem()) 
			throw new UnauthorizedException("This api can only be accessed via cluster credential");
		
		StreamingOutput os = output -> read(Project.getSiteLockName(projectId), () -> {
			File file = new File(projectService.getSiteDir(projectId), filePath);
			try (output; InputStream is = new FileInputStream(file)) {
				IOUtils.copy(is, output, BUFFER_SIZE);
			}
			return null;
		});
		return ok(os).build();
	}
	
	@Path("/git-advertise-refs")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@GET
	public Response gitAdvertiseRefs(@QueryParam("projectId") Long projectId, 
			@QueryParam("protocol") String protocol, @QueryParam("upload") boolean upload) {
		if (!SecurityUtils.isSystem()) 
			throw new UnauthorizedException("This api can only be accessed via cluster credential");
		
		StreamingOutput os = output -> {
			File gitDir = projectService.getGitDir(projectId);
			if (upload)
				new AdvertiseUploadRefsCommand(gitDir, output).protocol(protocol).run();
			else
				new AdvertiseReceiveRefsCommand(gitDir, output).protocol(protocol).run();
	   };
		return ok(os).build();
	}
		
	@Path("/git-pack")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@POST
	public Response gitPack(InputStream is, @QueryParam("projectId") Long projectId, 
			@QueryParam("principal") String principal, @QueryParam("protocol") String protocol, 
			@QueryParam("upload") boolean upload) {
		if (!SecurityUtils.isSystem()) 
			throw new UnauthorizedException("This api can only be accessed via cluster credential");
		
			StreamingOutput os = output -> {
				Map<String, String> hookEnvs = HookUtils.getHookEnvs(projectId, principal);
				
				try {
					File gitDir = projectService.getGitDir(projectId);
					if (upload) {
						workExecutionService.submit(GitFilter.PACK_PRIORITY, new Runnable() {
							
							@Override
							public void run() {
								CommandUtils.uploadPack(gitDir, hookEnvs, protocol, is, output);
							}
							
						}).get();
					} else {
						workExecutionService.submit(GitFilter.PACK_PRIORITY, new Runnable() {
							
							@Override
							public void run() {
								CommandUtils.receivePack(gitDir, hookEnvs, protocol, is, output);
							}
							
						}).get();
					}
				} catch (InterruptedException | ExecutionException e) {
					throw new RuntimeException(e);
				}
		};
		return ok(os).build();
	}
	
	@Path("/commit-info")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@GET
	public Response downloadCommitInfo(@QueryParam("projectId") Long projectId) {
		if (!SecurityUtils.isSystem()) 
			throw new UnauthorizedException("This api can only be accessed via cluster credential");
		
		StreamingOutput output = os -> {
			File tempDir = FileUtils.createTempDir("commit-info"); 
			try {
				commitInfoService.export(projectId, tempDir);
				TarUtils.tar(tempDir, os, false);
			} finally {
				FileUtils.deleteDir(tempDir);
			}
	   };
		return ok(output).build();
	}

	@Path("/visit-info")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@GET
	public Response downloadVisitInfo(@QueryParam("projectId") Long projectId) {
		if (!SecurityUtils.isSystem())
			throw new UnauthorizedException("This api can only be accessed via cluster credential");

		StreamingOutput output = os -> {
			File tempDir = FileUtils.createTempDir("visit-info");
			try {
				visitInfoService.export(projectId, tempDir);
				TarUtils.tar(tempDir, os, false);
			} finally {
				FileUtils.deleteDir(tempDir);
			}
		};
		return ok(output).build();
	}
	
	@Path("/lfs")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@GET
	public Response downloadLfs(@QueryParam("projectId") Long projectId, @QueryParam("objectId") String objectId) {
		if (!SecurityUtils.isSystem()) 
			throw new UnauthorizedException("This api can only be accessed via cluster credential");
		
		StreamingOutput os = output -> {
			try (output; InputStream is = new LfsObject(projectId, objectId).getInputStream()) {
				IOUtils.copy(is, output, BUFFER_SIZE);
			}
	   };
		return ok(os).build();
	}
	
	@Path("/lfs")
	@Consumes(MediaType.APPLICATION_OCTET_STREAM)
	@POST
	public Response uploadLfs(InputStream input, @QueryParam("projectId") Long projectId, 
			@QueryParam("objectId") String objectId) {
		if (!SecurityUtils.isSystem()) 
			throw new UnauthorizedException("This api can only be accessed via cluster credential");

		try (input; OutputStream os = new LfsObject(projectId, objectId).getOutputStream()) {
			IOUtils.copy(input, os, BUFFER_SIZE);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return ok().build();
	}

	@Path("/attachment")
	@Consumes(MediaType.APPLICATION_OCTET_STREAM)
	@POST
	public Response uploadAttachment(InputStream input, @QueryParam("projectId") Long projectId, 
			@QueryParam("attachmentGroup") String attachmentGroup, 
			@QueryParam("suggestedAttachmentName") String suggestedAttachmentName) {
		if (!SecurityUtils.isSystem()) 
			throw new UnauthorizedException("This api can only be accessed via cluster credential");

		String attachmentName = attachmentService.saveAttachmentLocal(
				projectId, attachmentGroup, suggestedAttachmentName, input);
		return ok(attachmentName).build();
	}
	
	@Path("/attachments")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@GET
	public Response downloadAttachments(@QueryParam("projectId") Long projectId, 
			@QueryParam("attachmentGroup") String attachmentGroup) {
		if (!SecurityUtils.isSystem()) 
			throw new UnauthorizedException("This api can only be accessed via cluster credential");

		StreamingOutput output = os -> read(attachmentService.getAttachmentLockName(projectId, attachmentGroup), () -> {
			TarUtils.tar(attachmentService.getAttachmentGroupDir(projectId, attachmentGroup),
					Sets.newHashSet("**"), Sets.newHashSet(), os, false);
			return null;					
		});
		return ok(output).build();
	}
	
	@Path("/artifact")
	@Consumes(MediaType.APPLICATION_OCTET_STREAM)
	@POST
	public Response uploadArtifact(InputStream input, @QueryParam("projectId") Long projectId, 
			@QueryParam("buildNumber") Long buildNumber,  @QueryParam("artifactPath") String artifactPath) {
		if (!SecurityUtils.isSystem()) 
			throw new UnauthorizedException("This api can only be accessed via cluster credential");

		write(getArtifactsLockName(projectId, buildNumber), () -> {
			var artifactsDir = storageService.initArtifactsDir(projectId, buildNumber);
			File artifactFile = new File(artifactsDir, artifactPath);
			FileUtils.createDir(artifactFile.getParentFile());
			try (input; var os = new BufferedOutputStream(new FileOutputStream(artifactFile), BUFFER_SIZE)) {
				IOUtils.copy(input, os, BUFFER_SIZE);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			projectService.directoryModified(projectId, artifactsDir);
			return null;
		});
		
		return ok().build();
	}

	@Path("/pack-blob")
	@Consumes(MediaType.APPLICATION_OCTET_STREAM)
	@POST
	public Long uploadPackBlob(InputStream input, @QueryParam("projectId") Long projectId,
								   @QueryParam("uuid") String uuid) {
		if (!SecurityUtils.isSystem())
			throw new UnauthorizedException("This api can only be accessed via cluster credential");
		var uploadFile = packBlobService.getUploadFile(projectId, uuid);
		try (input; var os = new BufferedOutputStream(new FileOutputStream(uploadFile, true), BUFFER_SIZE)) {
			return IOUtils.copy(input, os, BUFFER_SIZE);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Path("/cache")
	@Consumes(MediaType.APPLICATION_OCTET_STREAM)
	@POST
	public Response uploadCache(
			@QueryParam("projectId") Long projectId,
			@QueryParam("cacheId") Long cacheId,
			@QueryParam("cachePaths") String cachePaths,
			InputStream cacheStream) {
		if (!SecurityUtils.isSystem())
			throw new UnauthorizedException("This api can only be accessed via cluster credential");
		jobCacheService.uploadCache(projectId, cacheId, Splitter.on('\n').splitToList(cachePaths), cacheStream);
		return ok().build();
	}
	
}
