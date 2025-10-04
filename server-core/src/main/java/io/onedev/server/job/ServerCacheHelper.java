package io.onedev.server.job;

import com.google.common.base.Joiner;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.TaskLogger;
import io.onedev.k8shelper.CacheHelper;
import io.onedev.k8shelper.KubernetesHelper;
import io.onedev.k8shelper.SetupCacheFacade;
import io.onedev.server.OneDev;
import io.onedev.server.cluster.ClusterService;
import io.onedev.server.service.AccessTokenService;
import io.onedev.server.service.JobCacheService;
import io.onedev.server.service.ProjectService;
import io.onedev.server.model.Project;
import io.onedev.server.persistence.SessionService;
import io.onedev.server.security.SecurityUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.glassfish.jersey.client.ClientProperties;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class ServerCacheHelper extends CacheHelper {
	
	private final JobContext jobContext;
	
	public ServerCacheHelper(File buildHome, JobContext jobContext, TaskLogger logger) {
		super(buildHome, logger);
		this.jobContext = jobContext;
	}

	@Override
	protected boolean downloadCache(String cacheKey, List<String> cachePaths, List<File> cacheDirs) {
		var cacheInfo = getCacheService().getCacheInfoForDownload(jobContext.getProjectId(), cacheKey);
		return downloadCache(cacheInfo, cachePaths, cacheDirs);
	}

	@Override
	protected boolean downloadCache(List<String> loadKeys, List<String> cachePaths, List<File> cacheDirs) {
		var cacheInfo = getCacheService().getCacheInfoForDownload(jobContext.getProjectId(), loadKeys);
		return downloadCache(cacheInfo, cachePaths, cacheDirs);		
	}

	private boolean downloadCache(Pair<Long, Long> cacheInfo, List<String> cachePaths, List<File> cacheDirs) {
		if (cacheInfo != null) {
			var projectId = cacheInfo.getLeft();
			var cacheId = cacheInfo.getRight();
			var activeServer = getProjectService().getActiveServer(projectId, true);
			if (activeServer.equals(getClusterService().getLocalServerAddress())) {
				return getCacheService().downloadCache(projectId, cacheId, cachePaths, is -> untar(cacheDirs, is));
			} else {
				Client client = ClientBuilder.newClient();
				client.property(ClientProperties.REQUEST_ENTITY_PROCESSING, "CHUNKED");
				try {
					String serverUrl = getClusterService().getServerUrl(activeServer);
					var target = client.target(serverUrl)
							.path("~api/cluster/cache")
							.queryParam("projectId", projectId)
							.queryParam("cacheId", cacheId)
							.queryParam("cachePaths", Joiner.on('\n').join(cachePaths));
					Invocation.Builder builder = target.request();
					builder.header(HttpHeaders.AUTHORIZATION,
							KubernetesHelper.BEARER + " " + getClusterService().getCredential());
					try (Response response = builder.get()) {
						KubernetesHelper.checkStatus(response);
						try (var is = response.readEntity(InputStream.class)) {
							if (is.read() == 1) {
								untar(cacheDirs, is);
								return true;
							} else {
								return false;
							}
						} catch (IOException e) {
							throw new RuntimeException(e);
						}
					}
				} finally {
					client.close();
				}
			}
		} else {
			return false;
		}
	}

	@Override
	protected boolean uploadCache(SetupCacheFacade cacheConfig, List<File> cacheDirs) {
		var cacheKey = cacheConfig.getKey();
		var projectPath = cacheConfig.getUploadProjectPath();
		var accessTokenValue = cacheConfig.getUploadAccessToken();
		var cachePaths = cacheConfig.getPaths();
		var projectId = getSessionService().call(() -> {
			Project project;
			if (projectPath != null) {
				project = getProjectService().findByPath(projectPath);
				if (project == null)
					throw new ExplicitException("Upload project not found: " + projectPath);
			} else {
				project = getProjectService().load(jobContext.getProjectId());
			}
			if (jobContext.canManageProject(project)) {
				return project.getId();
			} else if (accessTokenValue != null) {
				var accessToken = getAccessTokenService().findByValue(accessTokenValue);
				if (accessToken != null && SecurityUtils.canUploadCache(accessToken.asSubject(), project))
					return project.getId();
			} 
			return null;
		});
		
		if (projectId != null) {
			Long cacheId = getCacheService().getCacheIdForUpload(projectId, cacheKey);
			var activeServer = getProjectService().getActiveServer(projectId, true);
			if (activeServer.equals(getClusterService().getLocalServerAddress())) {
				getCacheService().uploadCache(projectId, cacheId, cachePaths, os -> tar(cacheDirs, os));
			} else {
				Client client = ClientBuilder.newClient();
				client.property(ClientProperties.REQUEST_ENTITY_PROCESSING, "CHUNKED");
				try {
					String serverUrl = getClusterService().getServerUrl(activeServer);
					var target = client.target(serverUrl)
							.path("~api/cluster/cache")
							.queryParam("projectId", projectId)
							.queryParam("cacheId", cacheId)
							.queryParam("cachePaths", Joiner.on('\n').join(cachePaths));
					Invocation.Builder builder = target.request();
					builder.header(HttpHeaders.AUTHORIZATION,
							KubernetesHelper.BEARER + " " + getClusterService().getCredential());
					StreamingOutput output = os -> tar(cacheDirs, os);
					try (Response response = builder.post(Entity.entity(output, MediaType.APPLICATION_OCTET_STREAM))) {
						KubernetesHelper.checkStatus(response);
					}
				} finally {
					client.close();
				}
			}
			return true;
		} else {
			return false;
		}
	}
	
	private ClusterService getClusterService() {
		return OneDev.getInstance(ClusterService.class);
	}
	
	private SessionService getSessionService() {
		return OneDev.getInstance(SessionService.class);
	}
	
	private ProjectService getProjectService() {
		return OneDev.getInstance(ProjectService.class);
	}
	
	private AccessTokenService getAccessTokenService() {
		return OneDev.getInstance(AccessTokenService.class);
	}
	
	private JobCacheService getCacheService() {
		return OneDev.getInstance(JobCacheService.class);
	}
	
}
