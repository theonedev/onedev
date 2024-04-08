package io.onedev.server.plugin.executor.kubernetes;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.StringUtils;
import io.onedev.commons.utils.TarUtils;
import io.onedev.commons.utils.TaskLogger;
import io.onedev.k8shelper.K8sJobData;
import io.onedev.k8shelper.KubernetesHelper;
import io.onedev.server.cluster.ClusterManager;
import io.onedev.server.entitymanager.JobCacheManager;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.job.JobContext;
import io.onedev.server.job.JobManager;
import io.onedev.server.model.Project;
import io.onedev.server.persistence.SessionManager;
import io.onedev.server.rest.annotation.Api;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.IOUtils;
import org.apache.commons.lang.SerializationUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.shiro.authz.UnauthorizedException;
import org.glassfish.jersey.client.ClientProperties;

import javax.annotation.Nullable;
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

	private final JobManager jobManager;
	
	private final JobCacheManager jobCacheManager;
	
	private final SessionManager sessionManager;
	
	private final ProjectManager projectManager;
	
	private final ClusterManager clusterManager;
	
    @Context
    private HttpServletRequest request;
    
    @Inject
    public KubernetesResource(JobManager jobManager, JobCacheManager jobCacheManager, 
							  SessionManager sessionManager, ProjectManager projectManager, 
							  ClusterManager clusterManager) {
    	this.jobManager = jobManager;
		this.jobCacheManager = jobCacheManager;
    	this.sessionManager = sessionManager;
		this.projectManager = projectManager;
		this.clusterManager = clusterManager;
	}
    
	@Path("/job-data")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
    @GET
    public byte[] getJobData(@QueryParam("jobToken") String jobToken, 
							 @QueryParam("jobWorkspace") @Nullable String jobWorkspace) {
		JobContext jobContext = jobManager.getJobContext(jobToken, true);
		if (StringUtils.isNotBlank(jobWorkspace))
			jobManager.reportJobWorkspace(jobContext, jobWorkspace);	
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
		JobContext jobContext = jobManager.getJobContext(jobToken, true);
		return os -> {
			File filesDir = FileUtils.createTempDir();
			// Make sure we are not occupying a database connection here as we will occupy 
			// database connection when running step at project server side
			sessionManager.closeSession();
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
				
				var result = jobManager.runServerStep(jobContext, 
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
				sessionManager.openSession();
				FileUtils.deleteDir(filesDir);
			}						
	   };
	}
	
	@Path("/download-dependencies")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@GET
	public StreamingOutput downloadDependencies(@QueryParam("jobToken") String jobToken) {
		return os -> {
			JobContext jobContext = jobManager.getJobContext(jobToken, true);
			File tempDir = FileUtils.createTempDir();
			sessionManager.closeSession();
			try {
				jobManager.copyDependencies(jobContext, tempDir);
				TarUtils.tar(tempDir, os, false);
			} finally {
				sessionManager.openSession();
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
			sessionManager.closeSession();
			try {
				var jobContext = jobManager.getJobContext(jobToken, true);
				Pair<Long, Long> cacheInfo;
				if (cacheKey != null) {
					cacheInfo = jobCacheManager.getCacheInfoForDownload(jobContext.getProjectId(), cacheKey);
				} else {
					var loadKeys = Splitter.on('\n').splitToList(joinedLoadKeys);
					cacheInfo = jobCacheManager.getCacheInfoForDownload(jobContext.getProjectId(), loadKeys);
				}
				if (cacheInfo != null) {
					var cachePaths = Splitter.on('\n').splitToList(joinedCachePaths); 					
					var activeServer = projectManager.getActiveServer(cacheInfo.getLeft(), true);
					if (activeServer.equals(clusterManager.getLocalServerAddress())) {
						jobCacheManager.downloadCache(cacheInfo.getLeft(), cacheInfo.getRight(), cachePaths, os);
					} else {
						Client client = ClientBuilder.newClient();
						client.property(ClientProperties.REQUEST_ENTITY_PROCESSING, "CHUNKED");
						try {
							String serverUrl = clusterManager.getServerUrl(activeServer);
							var target = client.target(serverUrl)
									.path("~api/cluster/cache")
									.queryParam("projectId", cacheInfo.getLeft())
									.queryParam("cacheId", cacheInfo.getRight())
									.queryParam("cachePaths", Joiner.on('\n').join(cachePaths));
							Invocation.Builder builder = target.request();
							builder.header(HttpHeaders.AUTHORIZATION,
									KubernetesHelper.BEARER + " " + clusterManager.getCredential());
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
				sessionManager.openSession();
			}
		};
	}

	@Path("/upload-cache")
	@GET
	public Long checkUploadCache(
			@QueryParam("jobToken") String jobToken,
			@QueryParam("projectPath") @Nullable String projectPath) {
		var jobContext = jobManager.getJobContext(jobToken, true);
		Project uploadProject;
		if (projectPath == null) {
			uploadProject = projectManager.load(jobContext.getProjectId());
		} else {
			uploadProject = projectManager.findByPath(projectPath);
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
		sessionManager.closeSession();
		try {
			Long cacheId = jobCacheManager.getCacheIdForUpload(projectId, cacheKey);
			var cachePaths = Splitter.on('\n').splitToList(joinedCachePaths);
			var activeServer = projectManager.getActiveServer(projectId, true);
			if (activeServer.equals(clusterManager.getLocalServerAddress())) {
				jobCacheManager.uploadCache(projectId, cacheId, cachePaths, is);
			} else {
				Client client = ClientBuilder.newClient();
				client.property(ClientProperties.REQUEST_ENTITY_PROCESSING, "CHUNKED");
				try {
					String serverUrl = clusterManager.getServerUrl(activeServer);
					var target = client.target(serverUrl)
							.path("~api/cluster/cache")
							.queryParam("projectId", projectId)
							.queryParam("cacheId", cacheId)
							.queryParam("cachePaths", joinedCachePaths);
					Invocation.Builder builder = target.request();
					builder.header(HttpHeaders.AUTHORIZATION,
							KubernetesHelper.BEARER + " " + clusterManager.getCredential());
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
			sessionManager.openSession();
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
