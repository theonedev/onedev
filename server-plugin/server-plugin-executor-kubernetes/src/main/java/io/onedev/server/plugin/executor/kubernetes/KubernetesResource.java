package io.onedev.server.plugin.executor.kubernetes;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.StringUtils;
import io.onedev.commons.utils.TarUtils;
import io.onedev.commons.utils.TaskLogger;
import io.onedev.k8shelper.K8sJobData;
import io.onedev.k8shelper.KubernetesHelper;
import io.onedev.server.cluster.ClusterService;
import io.onedev.server.service.JobCacheService;
import io.onedev.server.service.ProjectService;
import io.onedev.server.job.JobContext;
import io.onedev.server.job.JobService;
import io.onedev.server.model.Project;
import io.onedev.server.persistence.SessionService;
import io.onedev.server.rest.annotation.Api;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.IOUtils;
import org.apache.commons.lang.SerializationUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.shiro.authz.UnauthorizedException;
import org.glassfish.jersey.client.ClientProperties;

import org.jspecify.annotations.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.onedev.k8shelper.KubernetesHelper.readInt;
import static io.onedev.k8shelper.KubernetesHelper.readString;
import static io.onedev.server.util.IOUtils.BUFFER_SIZE;
import static org.apache.commons.io.IOUtils.copy;

@Api(internal=true)
@Path("/k8s")
@Consumes(MediaType.WILDCARD)
@Singleton
public class KubernetesResource {

	private final JobService jobService;
	
	private final JobCacheService jobCacheService;
	
	private final SessionService sessionService;
	
	private final ProjectService projectService;
	
	private final ClusterService clusterService;
	
    @Context
    private HttpServletRequest request;
    
    @Inject
    public KubernetesResource(JobService jobService, JobCacheService jobCacheService,
                              SessionService sessionService, ProjectService projectService,
                              ClusterService clusterService) {
    	this.jobService = jobService;
		this.jobCacheService = jobCacheService;
    	this.sessionService = sessionService;
		this.projectService = projectService;
		this.clusterService = clusterService;
	}
    
	@Path("/job-data")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
    @GET
    public byte[] getJobData(@QueryParam("jobToken") String jobToken, 
							 @QueryParam("jobWorkspace") @Nullable String jobWorkspace) {
		JobContext jobContext = jobService.getJobContext(jobToken, true);
		if (StringUtils.isNotBlank(jobWorkspace))
			jobService.reportJobWorkspace(jobContext, jobWorkspace);	
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
			@QueryParam("cacheKey") @Nullable String cacheKey, 
			@QueryParam("loadKeys") @Nullable String joinedLoadKeys, 
			@QueryParam("cachePaths") String joinedCachePaths) {
		return os -> {
			sessionService.closeSession();
			try {
				var jobContext = jobService.getJobContext(jobToken, true);
				Pair<Long, Long> cacheInfo;
				if (cacheKey != null) {
					cacheInfo = jobCacheService.getCacheInfoForDownload(jobContext.getProjectId(), cacheKey);
				} else {
					var loadKeys = Splitter.on('\n').splitToList(joinedLoadKeys);
					cacheInfo = jobCacheService.getCacheInfoForDownload(jobContext.getProjectId(), loadKeys);
				}
				if (cacheInfo != null) {
					var cachePaths = Splitter.on('\n').splitToList(joinedCachePaths); 					
					var activeServer = projectService.getActiveServer(cacheInfo.getLeft(), true);
					if (activeServer.equals(clusterService.getLocalServerAddress())) {
						jobCacheService.downloadCache(cacheInfo.getLeft(), cacheInfo.getRight(), cachePaths, os);
					} else {
						Client client = ClientBuilder.newClient();
						client.property(ClientProperties.REQUEST_ENTITY_PROCESSING, "CHUNKED");
						try {
							String serverUrl = clusterService.getServerUrl(activeServer);
							var target = client.target(serverUrl)
									.path("~api/cluster/cache")
									.queryParam("projectId", cacheInfo.getLeft())
									.queryParam("cacheId", cacheInfo.getRight())
									.queryParam("cachePaths", Joiner.on('\n').join(cachePaths));
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
					os.write(0);					
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
			@QueryParam("cacheKey") String cacheKey, 
			@QueryParam("cachePaths") String joinedCachePaths, 
			InputStream is) {
		var projectId = checkUploadCache(jobToken, projectPath);
		sessionService.closeSession();
		try {
			Long cacheId = jobCacheService.getCacheIdForUpload(projectId, cacheKey);
			var cachePaths = Splitter.on('\n').splitToList(joinedCachePaths);
			var activeServer = projectService.getActiveServer(projectId, true);
			if (activeServer.equals(clusterService.getLocalServerAddress())) {
				jobCacheService.uploadCache(projectId, cacheId, cachePaths, is);
			} else {
				Client client = ClientBuilder.newClient();
				client.property(ClientProperties.REQUEST_ENTITY_PROCESSING, "CHUNKED");
				try {
					String serverUrl = clusterService.getServerUrl(activeServer);
					var target = client.target(serverUrl)
							.path("~api/cluster/cache")
							.queryParam("projectId", projectId)
							.queryParam("cacheId", cacheId)
							.queryParam("cachePaths", joinedCachePaths);
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
