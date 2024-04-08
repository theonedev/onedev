package io.onedev.server.job;

import com.google.common.base.Joiner;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.TaskLogger;
import io.onedev.k8shelper.CacheHelper;
import io.onedev.k8shelper.KubernetesHelper;
import io.onedev.server.OneDev;
import io.onedev.server.cluster.ClusterManager;
import io.onedev.server.entitymanager.JobCacheManager;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.model.Project;
import io.onedev.server.persistence.SessionManager;
import io.onedev.server.security.SecurityUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.glassfish.jersey.client.ClientProperties;

import javax.annotation.Nullable;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
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
		var cacheInfo = getCacheManager().getCacheInfoForDownload(jobContext.getProjectId(), cacheKey);
		return downloadCache(cacheInfo, cachePaths, cacheDirs);
	}

	@Override
	protected boolean downloadCache(List<String> loadKeys, List<String> cachePaths, List<File> cacheDirs) {
		var cacheInfo = getCacheManager().getCacheInfoForDownload(jobContext.getProjectId(), loadKeys);
		return downloadCache(cacheInfo, cachePaths, cacheDirs);		
	}

	private boolean downloadCache(Pair<Long, Long> cacheInfo, List<String> cachePaths, List<File> cacheDirs) {
		if (cacheInfo != null) {
			var projectId = cacheInfo.getLeft();
			var cacheId = cacheInfo.getRight();
			var activeServer = getProjectManager().getActiveServer(projectId, true);
			if (activeServer.equals(getClusterManager().getLocalServerAddress())) {
				return getCacheManager().downloadCache(projectId, cacheId, cachePaths, is -> untar(cacheDirs, is));
			} else {
				Client client = ClientBuilder.newClient();
				client.property(ClientProperties.REQUEST_ENTITY_PROCESSING, "CHUNKED");
				try {
					String serverUrl = getClusterManager().getServerUrl(activeServer);
					var target = client.target(serverUrl)
							.path("~api/cluster/cache")
							.queryParam("projectId", projectId)
							.queryParam("cacheId", cacheId)
							.queryParam("cachePaths", Joiner.on('\n').join(cachePaths));
					Invocation.Builder builder = target.request();
					builder.header(HttpHeaders.AUTHORIZATION,
							KubernetesHelper.BEARER + " " + getClusterManager().getCredential());
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
	protected boolean uploadCache(String cacheKey, List<String> cachePaths, List<File> cacheDirs,
								  @Nullable String projectPath, @Nullable String accessToken) {
		var projectId = getSessionManager().call(() -> {
			Project uploadProject;
			if (projectPath != null) {
				uploadProject = getProjectManager().findByPath(projectPath);
				if (uploadProject == null)
					throw new ExplicitException("Upload project not found: " + projectPath);
			} else {
				uploadProject = getProjectManager().load(jobContext.getProjectId());
			}
			if (jobContext.canManageProject(uploadProject)) {
				return uploadProject.getId();
			} else if (accessToken != null) {
				var user = getUserManager().findByAccessToken(accessToken);
				if (user != null && SecurityUtils.canUploadCache(user.asSubject(), uploadProject))
					return uploadProject.getId();
			} 
			return null;
		});
		if (projectId != null) {
			getCacheManager().uploadCache(projectId, cacheKey, cachePaths, os -> tar(cacheDirs, os));
			return true;
		} else {
			return false;
		}
	}
	
	private ClusterManager getClusterManager() {
		return OneDev.getInstance(ClusterManager.class);
	}
	
	private SessionManager getSessionManager() {
		return OneDev.getInstance(SessionManager.class);
	}
	
	private ProjectManager getProjectManager() {
		return OneDev.getInstance(ProjectManager.class);
	}
	
	private UserManager getUserManager() {
		return OneDev.getInstance(UserManager.class);
	}
	
	private JobCacheManager getCacheManager() {
		return OneDev.getInstance(JobCacheManager.class);
	}
	
}
