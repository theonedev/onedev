package io.onedev.server.rest.resource;

import static io.onedev.k8shelper.KubernetesHelper.readInt;
import static io.onedev.k8shelper.KubernetesHelper.readString;
import static io.onedev.k8shelper.KubernetesHelper.writeInt;
import static io.onedev.k8shelper.KubernetesHelper.writeString;
import static io.onedev.server.util.IOUtils.BUFFER_SIZE;
import static org.apache.commons.io.IOUtils.copy;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.lang.SerializationUtils;
import org.apache.shiro.authz.UnauthorizedException;
import org.glassfish.jersey.client.ClientProperties;
import org.jspecify.annotations.Nullable;

import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.StringUtils;
import io.onedev.commons.utils.TarUtils;
import io.onedev.commons.utils.TaskLogger;
import io.onedev.k8shelper.CacheAvailability;
import io.onedev.k8shelper.KubernetesHelper;
import io.onedev.k8shelper.KubernetesJobData;
import io.onedev.k8shelper.KubernetesWorkspaceData;
import io.onedev.server.cluster.ClusterService;
import io.onedev.server.job.JobContext;
import io.onedev.server.job.JobService;
import io.onedev.server.model.Project;
import io.onedev.server.persistence.SessionService;
import io.onedev.server.rest.annotation.Api;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.service.ProjectService;
import io.onedev.server.service.RunCacheService;
import io.onedev.server.service.UserService;
import io.onedev.server.service.support.CacheFindResult;
import io.onedev.server.util.IOUtils;
import io.onedev.server.workspace.WorkspaceContext;
import io.onedev.server.workspace.WorkspaceService;

@Api(internal = true)
@Path("/worker")
@Consumes(MediaType.WILDCARD)
@Singleton
public class WorkerResource {

	@Inject
	private JobService jobService;

	@Inject
	private WorkspaceService workspaceService;

	@Inject
	private RunCacheService cacheService;

	@Inject
	private SessionService sessionService;

	@Inject
	private ProjectService projectService;

	@Inject
	private ClusterService clusterService;

	@Inject
	private UserService userService;

	@Path("job-data")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@GET
	public byte[] getJobData(@QueryParam("token") String token,
			@QueryParam("workDir") @Nullable String workDir) {
		JobContext jobContext = jobService.getJobContext(token, true);
		if (StringUtils.isNotBlank(workDir))
			jobService.reportJobWorkDir(jobContext, workDir);
		KubernetesJobData k8sJobData = new KubernetesJobData(
				jobContext.getJobExecutor().getName(),
				jobContext.getRefName(),
				jobContext.getCommitId().name(),
				jobContext.getActions());
		return SerializationUtils.serialize(k8sJobData);
	}

	@Path("run-server-step")
	@Consumes(MediaType.APPLICATION_OCTET_STREAM)
	@POST
	public StreamingOutput runServerStep(@QueryParam("token") String token, InputStream is) {
		JobContext jobContext = jobService.getJobContext(token, true);
		return os -> {
			File filesDir = FileUtils.createTempDir();
			// Make sure we are not occupying a database connection here as we will occupy
			// database connection when running step at project server side
			sessionService.closeSession();
			try {
				int length = readInt(is);
				List<Integer> stepPosition = new ArrayList<>();
				for (int i = 0; i < length; i++)
					stepPosition.add(readInt(is));

				Map<String, String> placeholderValues = new HashMap<>();
				length = readInt(is);
				for (int i = 0; i < length; i++)
					placeholderValues.put(readString(is), readString(is));

				TarUtils.untar(is, filesDir, false);

				var result = jobService.runServerStep(jobContext,
						stepPosition, filesDir, placeholderValues, true, new TaskLogger() {

					@Override
					public void log(String message, String sessionId) {
						// While testing, ngrok.io buffers response and build cannot get log entries
						// timely. This won't happen on pagekite however
						writeInt(os, 1);
						writeString(os, message);
						try {
							os.flush();
						} catch (IOException e) {
							throw new RuntimeException(e);
						}
					}

				});
				byte[] bytes = SerializationUtils.serialize(result);
				writeInt(os, 2);
				writeInt(os, bytes.length);
				os.write(bytes);
			} finally {
				sessionService.openSession();
				FileUtils.deleteDir(filesDir);
			}
		};
	}

	@Path("dependencies")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@GET
	public StreamingOutput downloadDependencies(@QueryParam("token") String token) {
		return os -> {
			JobContext jobContext = jobService.getJobContext(token, true);
			File tempDir = FileUtils.createTempDir();
			sessionService.closeSession();
			try {
				jobService.copyDependencies(jobContext, tempDir);
				TarUtils.tar(tempDir, os, false);
			} finally {
				sessionService.openSession();
				FileUtils.deleteDir(tempDir);
			}
		};
	}

	@Path("job-cache")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@GET
	public StreamingOutput downloadJobCache(
			@QueryParam("token") String token,
			@QueryParam("key") String key,
			@QueryParam("checksum") @Nullable String checksum,
			@QueryParam("path") String path) {
		var context = jobService.getJobContext(token, true);
		return downloadCache(context.getProjectId(), key, checksum, path);
	}

	@Path("workspace-cache")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@GET
	public StreamingOutput downloadWorkspaceCache(
			@QueryParam("token") String token,
			@QueryParam("key") String key,
			@QueryParam("checksum") @Nullable String checksum,
			@QueryParam("path") String path) {
		var context = workspaceService.getWorkspaceContext(token, true);
		return downloadCache(context.getProjectId(), key, checksum, path);
	}

	@Path("job-cache")
	@HEAD
	public void checkUploadJobCache(
			@QueryParam("token") String token,
			@QueryParam("projectPath") @Nullable String projectPath) {
		checkUploadCache(jobService.getJobContext(token, true), projectPath);
	}		

	@Path("workspace-cache")
	@HEAD
	public void checkUploadWorkspaceCache(
			@QueryParam("token") String token,
			@QueryParam("projectPath") @Nullable String projectPath) {
		checkUploadCache(workspaceService.getWorkspaceContext(token, true), projectPath);
	}	

	public Long checkUploadCache(JobContext context, @Nullable String projectPath) {
		var project = projectService.load(context.getProjectId());
		var uploadProject = getUploadProject(project, projectPath);
		if (context.canManageProject(uploadProject) || SecurityUtils.canUploadCache(uploadProject))
			return uploadProject.getId();
		else
			throw new UnauthorizedException();
	}		

	public Long checkUploadCache(WorkspaceContext context, @Nullable String projectPath) {
		var project = projectService.load(context.getProjectId());
		var uploadProject = getUploadProject(project, projectPath);
		if (project.isSelfOrAncestorOf(uploadProject) || SecurityUtils.canUploadCache(uploadProject))
			return uploadProject.getId();
		else
			throw new UnauthorizedException();
	}		

	@Path("job-cache")
	@Consumes(MediaType.APPLICATION_OCTET_STREAM)
	@POST
	public Response uploadJobCache(
			@QueryParam("token") String token,
			@QueryParam("projectPath") @Nullable String projectPath,
			@QueryParam("key") String key,
			@QueryParam("checksum") @Nullable String checksum,
			@QueryParam("path") String path,
			InputStream is) {
		var projectId = checkUploadCache(jobService.getJobContext(token, true), projectPath);
		return uploadCache(projectId, key, checksum, path, is);
	}

	@Path("workspace-cache")
	@Consumes(MediaType.APPLICATION_OCTET_STREAM)
	@POST
	public Response uploadWorkspaceCache(
			@QueryParam("token") String token,
			@QueryParam("projectPath") @Nullable String projectPath,
			@QueryParam("key") String key,
			@QueryParam("checksum") @Nullable String checksum,
			@QueryParam("path") String path,
			InputStream is) {
		var projectId = checkUploadCache(workspaceService.getWorkspaceContext(token, true), projectPath);
		return uploadCache(projectId, key, checksum, path, is);
	}

	@GET
	@Path("test")
	public Response test(@QueryParam("token") String token) {
		if (token != null)
			return Response.ok().build();
		else
			return Response.status(400).entity("Missing token").build();
	}

	@Path("workspace-data")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@GET
	public byte[] getWorkspaceData(@QueryParam("token") String token) {
		WorkspaceContext context = workspaceService.getWorkspaceContext(token, true);
		var data = new KubernetesWorkspaceData(
				context.getUserName(),
				context.getUserEmail(),
				context.getCloneInfo(),
				context.getCommitHash(),
				context.getBranch(),
				context.getSpec().isRetrieveLfs(),
				context.getCacheConfigFacades(),
				context.getUserDataFacades(),
				context.getConfigFileFacades(),
				context.getSetupScriptConfig());
		return SerializationUtils.serialize(data);
	}

	@Path("workspace-user-data")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@GET
	public Response downloadWorkspaceUserData(
			@QueryParam("token") String token,
			@QueryParam("key") String key,
			@QueryParam("path") String path) {
		WorkspaceContext context = workspaceService.getWorkspaceContext(token, true);
		Long userId = context.getUserId();

		StreamingOutput output = new StreamingOutput() {

			@Override
			public void write(OutputStream os) throws IOException {
				var found = userService.downloadWorkspaceData(userId, key, path, is -> {
					try {
						writeInt(os, 1);
						copy(is, os, BUFFER_SIZE);
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				});
				if (!found)
					writeInt(os, 0);
			}

		};
		return Response.ok(output, MediaType.APPLICATION_OCTET_STREAM).build();
	}

	@Path("workspace-user-data")
	@Consumes(MediaType.APPLICATION_OCTET_STREAM)
	@POST
	public Response uploadWorkspaceUserData(
			@QueryParam("token") String token,
			@QueryParam("key") String key,
			@QueryParam("path") String path,
			InputStream is) {
		WorkspaceContext context = workspaceService.getWorkspaceContext(token, true);
		Long userId = context.getUserId();

		userService.uploadWorkspaceData(userId, key, path, os -> {
			try {
				IOUtils.copy(is, os, BUFFER_SIZE);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		});
		return Response.ok().build();
	}

	@Path("workspace-user-data")
	@PUT
	public Response notifyWorkspaceUserDataUploaded(
			@QueryParam("token") String token,
			@QueryParam("key") String key) {
		WorkspaceContext context = workspaceService.getWorkspaceContext(token, true);
		Long userId = context.getUserId();
		userService.notifyWorkspaceDataUploaded(userId, key);
		return Response.ok().build();
	}
	
	private Project getUploadProject(Project project, @Nullable String projectPath) {
		if (projectPath != null) {
			var uploadProject = projectService.findByPath(projectPath);
			if (uploadProject == null)
				throw new NotFoundException("Upload project not found: " + projectPath);
			return uploadProject;
		} else {
			return project;
		}
	}

	private StreamingOutput downloadCache(Long projectId, String key, @Nullable String checksum, String path) {
		return os -> {
			sessionService.closeSession();
			try {
				var cacheFindResult = cacheService.findCache(projectId, key, checksum, path);
				if (cacheFindResult != null)
					streamCache(cacheFindResult, os);
				else
					os.write(CacheAvailability.NOT_FOUND.ordinal());
			} finally {
				sessionService.openSession();
			}
		};
	}

	private void streamCache(CacheFindResult cacheFindResult, OutputStream os) throws IOException {
		var activeServer = projectService.getActiveServer(cacheFindResult.getProjectId(), true);
		if (activeServer.equals(clusterService.getLocalServerAddress())) {
			cacheService.downloadCache(cacheFindResult, os);
		} else {
			Client client = ClientBuilder.newClient();
			client.property(ClientProperties.REQUEST_ENTITY_PROCESSING, "CHUNKED");
			try {
				String serverUrl = clusterService.getServerUrl(activeServer);
				var target = client.target(serverUrl)
						.path("~api/cluster/cache")
						.queryParam("projectId", cacheFindResult.getProjectId())
						.queryParam("dirName", cacheFindResult.getDirName())
						.queryParam("pathIndex", cacheFindResult.getPathIndex())
						.queryParam("exactMatch", cacheFindResult.isExactMatch());
				Invocation.Builder builder = target.request();
				builder.header(HttpHeaders.AUTHORIZATION,
						KubernetesHelper.BEARER + " " + clusterService.getCredential());
				try (Response response = builder.get()) {
					KubernetesHelper.checkStatus(response);
					try (var input = response.readEntity(InputStream.class)) {
						IOUtils.copy(input, os, BUFFER_SIZE);
					}
				}
			} finally {
				client.close();
			}
		}
	}

	private Response uploadCache(Long projectId, String key, @Nullable String checksum, String path,
			InputStream is) {
		sessionService.closeSession();
		try {
			var activeServer = projectService.getActiveServer(projectId, true);
			if (activeServer.equals(clusterService.getLocalServerAddress())) {
				cacheService.uploadCache(projectId, key, checksum, path, is);
			} else {
				Client client = ClientBuilder.newClient();
				client.property(ClientProperties.REQUEST_ENTITY_PROCESSING, "CHUNKED");
				try {
					String serverUrl = clusterService.getServerUrl(activeServer);
					var target = client.target(serverUrl)
							.path("~api/cluster/cache")
							.queryParam("projectId", projectId)
							.queryParam("key", key)
							.queryParam("checksum", checksum)
							.queryParam("path", path);
					Invocation.Builder builder = target.request();
					builder.header(HttpHeaders.AUTHORIZATION,
							KubernetesHelper.BEARER + " " + clusterService.getCredential());
					StreamingOutput output = os -> copy(is, os, BUFFER_SIZE);
					try (Response response = builder.post(Entity.entity(output, MediaType.APPLICATION_OCTET_STREAM))) {
						KubernetesHelper.checkStatus(response);
					}
				} finally {
					client.close();
				}
			}
			return Response.ok().build();
		} finally {
			sessionService.openSession();
		}
	}

}
