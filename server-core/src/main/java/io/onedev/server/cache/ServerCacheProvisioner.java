package io.onedev.server.cache;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

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
import io.onedev.commons.utils.TarUtils;
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

public abstract class ServerCacheProvisioner extends CacheProvisioner {

	public ServerCacheProvisioner(CacheConfigFacade config, int configIndex) {
		super(config, configIndex);
	}

	@Override
	protected CacheAvailability download(String key, @Nullable String checksum,
									String path, File pathDir) {
		var cacheFindResult = getCacheService().findCache(getProjectId(), key, checksum, path);
		if (cacheFindResult != null) {
			var projectId = cacheFindResult.getProjectId();
			var activeServer = getProjectService().getActiveServer(projectId, true);
			if (activeServer.equals(getClusterService().getLocalServerAddress())) {
				return getCacheService().downloadCache(cacheFindResult, 
						is -> TarUtils.untar(is, pathDir, false));
			} else {
				Client client = ClientBuilder.newClient();
				client.property(ClientProperties.REQUEST_ENTITY_PROCESSING, "CHUNKED");
				try {
					String serverUrl = getClusterService().getServerUrl(activeServer);
					var target = client.target(serverUrl)
							.path("~api/cluster/cache")
							.queryParam("projectId", projectId)
							.queryParam("dirName", cacheFindResult.getDirName())
							.queryParam("pathIndex", cacheFindResult.getPathIndex())
							.queryParam("exactMatch", cacheFindResult.isExactMatch());
					Invocation.Builder builder = target.request();
					builder.header(HttpHeaders.AUTHORIZATION,
							KubernetesHelper.BEARER + " " + getClusterService().getCredential());
					try (Response response = builder.get()) {
						KubernetesHelper.checkStatus(response);
						try (var is = response.readEntity(InputStream.class)) {
							var cacheAvailability = CacheAvailability.values()[is.read()];
							if (cacheAvailability != CacheAvailability.NOT_FOUND)
								TarUtils.untar(is, pathDir, false);
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
	protected boolean upload(CacheConfigFacade config, String path, File pathDir) {
		var key = config.getKey();
		var checksum = config.getChecksum();

		var projectId = getSessionService().call(() -> {
			Project uploadProject;
			if (config.getUploadProjectPath() != null) {
				var uploadProjectPath = config.getUploadProjectPath();
				uploadProject = getProjectService().findByPath(uploadProjectPath);
				if (uploadProject == null)
					throw new ExplicitException("Upload project not found: " + uploadProjectPath);
			} else {
				uploadProject = getProjectService().load(getProjectId());
			}

			var accessTokenValue = config.getUploadAccessToken();
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
			var activeServer = getProjectService().getActiveServer(projectId, true);
			if (activeServer.equals(getClusterService().getLocalServerAddress())) {
				getCacheService().uploadCache(projectId, key, checksum, path,
						os -> TarUtils.tar(pathDir, os, false));
			} else {
				Client client = ClientBuilder.newClient();
				client.property(ClientProperties.REQUEST_ENTITY_PROCESSING, "CHUNKED");
				try {
					String serverUrl = getClusterService().getServerUrl(activeServer);
					var target = client.target(serverUrl)
							.path("~api/cluster/cache")
							.queryParam("projectId", projectId)
							.queryParam("key", key)
							.queryParam("checksum", checksum)
							.queryParam("path", path);
					Invocation.Builder builder = target.request();
					builder.header(HttpHeaders.AUTHORIZATION,
							KubernetesHelper.BEARER + " " + getClusterService().getCredential());
					StreamingOutput output = os -> TarUtils.tar(pathDir, os, false);
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
