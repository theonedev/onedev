package io.onedev.server.job;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.glassfish.jersey.client.ClientProperties;
import org.jspecify.annotations.Nullable;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.TaskLogger;
import io.onedev.k8shelper.CacheAvailability;
import io.onedev.k8shelper.CacheHelper;
import io.onedev.k8shelper.KubernetesHelper;
import io.onedev.k8shelper.SetupCacheFacade;
import io.onedev.server.OneDev;
import io.onedev.server.cluster.ClusterService;
import io.onedev.server.model.Project;
import io.onedev.server.persistence.SessionService;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.service.AccessTokenService;
import io.onedev.server.service.ProjectService;
import io.onedev.server.service.RunCacheService;
import io.onedev.server.service.support.CacheQueryResult;

public class ServerCacheHelper extends CacheHelper {
	
	private final JobContext jobContext;
	
	public ServerCacheHelper(File buildDir, JobContext jobContext, TaskLogger logger) {
		super(buildDir, logger);
		this.jobContext = jobContext;
	}

	@Override
	protected CacheAvailability downloadCache(String key, @Nullable String checksum,
									String cachePathsString, List<File> cacheDirs) {
		var cacheQueryResult = getCacheService().queryCache(jobContext.getProjectId(), key, checksum);
		return downloadCache(cacheQueryResult, cachePathsString, cacheDirs);
	}

	private CacheAvailability downloadCache(@Nullable CacheQueryResult cacheQueryResult, String cachePathsString, List<File> cacheDirs) {
		if (cacheQueryResult != null) {
			var projectId = cacheQueryResult.getProjectId();
			var cacheId = cacheQueryResult.getCacheId();
			var activeServer = getProjectService().getActiveServer(projectId, true);
			if (activeServer.equals(getClusterService().getLocalServerAddress())) {
				return getCacheService().downloadCache(cacheQueryResult, cachePathsString, is -> untar(cacheDirs, is));
			} else {
				Client client = ClientBuilder.newClient();
				client.property(ClientProperties.REQUEST_ENTITY_PROCESSING, "CHUNKED");
				try {
					String serverUrl = getClusterService().getServerUrl(activeServer);
					var target = client.target(serverUrl)
							.path("~api/cluster/cache")
							.queryParam("projectId", projectId)
							.queryParam("cacheId", cacheId)
							.queryParam("exactMatch", cacheQueryResult.isExactMatch())
							.queryParam("cachePathsString", cachePathsString);
					Invocation.Builder builder = target.request();
					builder.header(HttpHeaders.AUTHORIZATION,
							KubernetesHelper.BEARER + " " + getClusterService().getCredential());
					try (Response response = builder.get()) {
						KubernetesHelper.checkStatus(response);
						try (var is = response.readEntity(InputStream.class)) {
							var cacheAvailability = CacheAvailability.values()[is.read()];
							if (cacheAvailability != CacheAvailability.NOT_FOUND) 
								untar(cacheDirs, is);
							return cacheAvailability;
						} catch (IOException e) {
							throw new RuntimeException(e);
						}
					}
				} finally {
					client.close();
				}
			}
		} else {
			return CacheAvailability.NOT_FOUND;
		}
	}

	@Override
	protected boolean uploadCache(SetupCacheFacade cacheConfig, List<File> cacheDirs) {
		var key = cacheConfig.getKey();
		var checksum = cacheConfig.getChecksum();
		var projectPath = cacheConfig.getUploadProjectPath();
		var accessTokenValue = cacheConfig.getUploadAccessToken();
		var cachePathsString = cacheConfig.getPathsAsString();
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
			Long cacheId = getCacheService().createCache(projectId, key, checksum);
			var activeServer = getProjectService().getActiveServer(projectId, true);
			if (activeServer.equals(getClusterService().getLocalServerAddress())) {
				getCacheService().uploadCache(projectId, cacheId, cachePathsString, os -> tar(cacheDirs, os));
			} else {
				Client client = ClientBuilder.newClient();
				client.property(ClientProperties.REQUEST_ENTITY_PROCESSING, "CHUNKED");
				try {
					String serverUrl = getClusterService().getServerUrl(activeServer);
					var target = client.target(serverUrl)
							.path("~api/cluster/cache")
							.queryParam("projectId", projectId)
							.queryParam("cacheId", cacheId)
							.queryParam("cachePathsString", cachePathsString);
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
	
	private RunCacheService getCacheService() {
		return OneDev.getInstance(RunCacheService.class);
	}
	
}
