package io.onedev.server.plugin.executor.kubernetes;

import static io.onedev.k8shelper.KubernetesHelper.readInt;
import static io.onedev.k8shelper.KubernetesHelper.readString;
import static io.onedev.server.util.IOUtils.BUFFER_SIZE;
import static org.apache.commons.io.IOUtils.copy;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Context;
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
import io.onedev.k8shelper.K8sJobData;
import io.onedev.k8shelper.KubernetesHelper;
import io.onedev.server.cluster.ClusterService;
import io.onedev.server.job.JobContext;
import io.onedev.server.job.JobService;
import io.onedev.server.model.Project;
import io.onedev.server.persistence.SessionService;
import io.onedev.server.rest.annotation.Api;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.service.ProjectService;
import io.onedev.server.service.RunCacheService;
import io.onedev.server.util.IOUtils;

@Api(internal=true)
@Path("/k8s")
@Consumes(MediaType.WILDCARD)
@Singleton
public class KubernetesResource {

	@Inject
	private JobService jobService;
	
	@Inject
	private RunCacheService cacheService;
	
	@Inject
	private SessionService sessionService;
	
	@Inject
	private ProjectService projectService;
	
	@Inject
	private ClusterService clusterService;
	
    @Context
    private HttpServletRequest request;
        
	@Path("/job-data")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
    @GET
    public byte[] getJobData(@QueryParam("jobToken") String jobToken, 
							 @QueryParam("jobWorkDir") @Nullable String jobWorkDir) {
		JobContext jobContext = jobService.getJobContext(jobToken, true);
		if (StringUtils.isNotBlank(jobWorkDir))
			jobService.reportJobWorkDir(jobContext, jobWorkDir);	
		K8sJobData k8sJobData = new K8sJobData(
				jobContext.getJobExecutor().getName(), 
				jobContext.getRefName(),
				jobContext.getCommitId().name(), 
				jobContext.getActions());
		return SerializationUtils.serialize(k8sJobData);
    }
	
	@Path("/run-server-step")
	@Consumes(MediaType.APPLICATION_OCTET_STREAM)
	@POST
	public StreamingOutput runServerStep(@QueryParam("jobToken") String jobToken, InputStream is) {
		JobContext jobContext = jobService.getJobContext(jobToken, true);
		return os -> {
			File filesDir = FileUtils.createTempDir();
			// Make sure we are not occupying a database connection here as we will occupy 
			// database connection when running step at project server side
			sessionService.closeSession();
			try {
				int length = readInt(is);
				List<Integer> stepPosition = new ArrayList<>();
				for (int i=0; i<length; i++) 
					stepPosition.add(readInt(is));
				
				Map<String, String> placeholderValues = new HashMap<>();
				length = readInt(is);
				for (int i=0; i<length; i++) 
					placeholderValues.put(readString(is), readString(is));
				
				TarUtils.untar(is, filesDir, false);
				
				var result = jobService.runServerStep(jobContext, 
						stepPosition, filesDir, placeholderValues, true, new TaskLogger() {

					@Override
					public void log(String message, String sessionId) {
						// While testing, ngrok.io buffers response and build can not get log entries 
						// timely. This won't happen on pagekite however
						KubernetesHelper.writeInt(os, 1);
						KubernetesHelper.writeString(os, message);
						try {
							os.flush();
						} catch (IOException e) {
							throw new RuntimeException(e);
						}
					}
					
				});
				byte[] bytes = SerializationUtils.serialize(result); 
				KubernetesHelper.writeInt(os, 2);
				KubernetesHelper.writeInt(os, bytes.length);
				os.write(bytes);
			} finally {
				sessionService.openSession();
				FileUtils.deleteDir(filesDir);
			}						
	   };
	}
	
	@Path("/download-dependencies")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@GET
	public StreamingOutput downloadDependencies(@QueryParam("jobToken") String jobToken) {
		return os -> {
			JobContext jobContext = jobService.getJobContext(jobToken, true);
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

	@Path("/download-cache")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@GET
	public StreamingOutput downloadCache(
			@QueryParam("jobToken") String jobToken,
			@QueryParam("key") String key, 
			@QueryParam("checksum") @Nullable String checksum, 
			@QueryParam("cachePathsString") String cachePathsString) {
		return os -> {
			sessionService.closeSession();
			try {
				var jobContext = jobService.getJobContext(jobToken, true);
				var cacheQueryResult = cacheService.queryCache(jobContext.getProjectId(), key, checksum);
				if (cacheQueryResult != null) {
					var activeServer = projectService.getActiveServer(cacheQueryResult.getProjectId(), true);
					if (activeServer.equals(clusterService.getLocalServerAddress())) {
						cacheService.downloadCache(cacheQueryResult, cachePathsString, os);
					} else {
						Client client = ClientBuilder.newClient();
						client.property(ClientProperties.REQUEST_ENTITY_PROCESSING, "CHUNKED");
						try {
							String serverUrl = clusterService.getServerUrl(activeServer);
							var target = client.target(serverUrl)
									.path("~api/cluster/cache")
									.queryParam("projectId", cacheQueryResult.getProjectId())
									.queryParam("cacheId", cacheQueryResult.getCacheId())
									.queryParam("exactMatch", cacheQueryResult.isExactMatch())
									.queryParam("cachePathsString", cachePathsString);
							Invocation.Builder builder = target.request();
							builder.header(HttpHeaders.AUTHORIZATION,
									KubernetesHelper.BEARER + " " + clusterService.getCredential());
							try (Response response = builder.get()) {
								KubernetesHelper.checkStatus(response);
								try (var is = response.readEntity(InputStream.class)) {
									IOUtils.copy(is, os, BUFFER_SIZE);
								} catch (IOException e) {
									throw new RuntimeException(e);
								}
							}
						} finally {
							client.close();
						}
					}						
				} else {
					os.write(CacheAvailability.NOT_FOUND.ordinal());					
				}
			} finally {
				sessionService.openSession();
			}
		};
	}

	@Path("/upload-cache")
	@GET
	public Long checkUploadCache(
			@QueryParam("jobToken") String jobToken,
			@QueryParam("projectPath") @Nullable String projectPath) {
		var jobContext = jobService.getJobContext(jobToken, true);
		Project uploadProject;
		if (projectPath == null) {
			uploadProject = projectService.load(jobContext.getProjectId());
		} else {
			uploadProject = projectService.findByPath(projectPath);
			if (uploadProject == null)
				throw new NotFoundException("Project not found: " + projectPath);
		}
		if (jobContext.canManageProject(uploadProject)) 
			return uploadProject.getId();
		else if (SecurityUtils.canUploadCache(uploadProject)) 
			return uploadProject.getId();
		else 
			throw new UnauthorizedException();
	}

	@Path("/upload-cache")
	@Consumes(MediaType.APPLICATION_OCTET_STREAM)
	@POST
	public Response uploadCache(
			@QueryParam("jobToken") String jobToken,
			@QueryParam("projectPath") @Nullable String projectPath,
			@QueryParam("key") String key, 
			@QueryParam("checksum") @Nullable String checksum,
			@QueryParam("cachePathsString") String cachePathsString, 
			InputStream is) {
		var projectId = checkUploadCache(jobToken, projectPath);
		sessionService.closeSession();
		try {
			Long cacheId = cacheService.createCache(projectId, key, checksum);
			var activeServer = projectService.getActiveServer(projectId, true);
			if (activeServer.equals(clusterService.getLocalServerAddress())) {
				cacheService.uploadCache(projectId, cacheId, cachePathsString, is);
			} else {
				Client client = ClientBuilder.newClient();
				client.property(ClientProperties.REQUEST_ENTITY_PROCESSING, "CHUNKED");
				try {
					String serverUrl = clusterService.getServerUrl(activeServer);
					var target = client.target(serverUrl)
							.path("~api/cluster/cache")
							.queryParam("projectId", projectId)
							.queryParam("cacheId", cacheId)
							.queryParam("cachePathsString", cachePathsString);
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
	
	@GET
	@Path("/test")
	public Response test(@QueryParam("jobToken") String jobToken) {
		if (jobToken != null) 
			return Response.ok().build();
		else 
			return Response.status(400).entity("Missing job token").build();
	}
	
}
