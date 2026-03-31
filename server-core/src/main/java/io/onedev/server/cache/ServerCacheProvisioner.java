package io.onedev.server.cache;

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
import io.onedev.k8shelper.CacheConfigFacade;
import io.onedev.k8shelper.CacheProvisioner;
import io.onedev.k8shelper.KubernetesHelper;
import io.onedev.server.OneDev;
import io.onedev.server.cluster.ClusterService;
import io.onedev.server.model.Project;
import io.onedev.server.persistence.SessionService;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.service.AccessTokenService;
import io.onedev.server.service.ProjectService;
import io.onedev.server.service.RunCacheService;
import io.onedev.server.service.support.CacheQueryResult;

public abstract class ServerCacheProvisioner extends CacheProvisioner {
		
	public ServerCacheProvisioner(File baseDir, TaskLogger logger) {
		super(baseDir, logger);
	}

	@Override
	protected CacheAvailability downloadCache(String key, @Nullable String checksum,
									String cachePathsString, List<File> cacheDirs) {
		var cacheQueryResult = getCacheService().queryCache(getProjectId(), key, checksum);
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
	protected boolean uploadCache(CacheConfigFacade cacheConfig, List<File> cacheDirs) {
		var key = cacheConfig.getKey();
		var checksum = cacheConfig.getChecksum();
		var cachePathsString = cacheConfig.getPathsAsString();
		
		var projectId = getSessionService().call(() -> {
			Project uploadProject;
			if (cacheConfig.getUploadProjectPath() != null) {
				var uploadProjectPath = cacheConfig.getUploadProjectPath();
				uploadProject = getProjectService().findByPath(uploadProjectPath);
				if (uploadProject == null)
					throw new ExplicitException("Upload project not found: " + uploadProjectPath);
			} else {
				uploadProject = getProjectService().load(getProjectId());
			}		

			var accessTokenValue = cacheConfig.getUploadAccessToken();
			if (canUploadTo(uploadProject)) {
				return uploadProject.getId();
			} else if (accessTokenValue != null) {
				var accessToken = getAccessTokenService().findByValue(accessTokenValue);
				if (accessToken != null && SecurityUtils.canUploadCache(accessToken.asSubject(), uploadProject))
					return uploadProject.getId();
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

	protected abstract Long getProjectId();

	@Sessional
	protected abstract boolean canUploadTo(Project uploadProject);
	
	protected ClusterService getClusterService() {
		return OneDev.getInstance(ClusterService.class);
	}
		
	protected ProjectService getProjectService() {
		return OneDev.getInstance(ProjectService.class);
	}
		
	protected RunCacheService getCacheService() {
		return OneDev.getInstance(RunCacheService.class);
	}
	
	protected SessionService getSessionService() {
		return OneDev.getInstance(SessionService.class);
	}

	protected AccessTokenService getAccessTokenService() {
		return OneDev.getInstance(AccessTokenService.class);
	}

}
