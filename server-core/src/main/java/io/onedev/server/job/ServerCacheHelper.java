package io.onedev.server.job;

import io.onedev.commons.utils.TarUtils;
import io.onedev.commons.utils.TaskLogger;
import io.onedev.k8shelper.CacheHelper;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.JobCacheManager;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.persistence.SessionManager;
import io.onedev.server.security.SecurityUtils;

import javax.annotation.Nullable;
import java.io.File;
import java.util.List;

public class ServerCacheHelper extends CacheHelper {
	
	private final JobContext jobContext;
	
	public ServerCacheHelper(File buildHome, JobContext jobContext, TaskLogger logger) {
		super(buildHome, logger);
		this.jobContext = jobContext;
	}

	@Override
	protected boolean downloadCache(String cacheKey, String cachePath, File cacheDir) {
		return getCacheManager().downloadCacheLocal(jobContext.getProjectId(),
				cacheKey, cachePath, is -> TarUtils.untar(is, cacheDir, true));
	}

	@Override
	protected boolean downloadCache(List<String> cacheLoadKeys, String cachePath, File cacheDir) {
		return getCacheManager().downloadCacheLocal(jobContext.getProjectId(), 
				cacheLoadKeys, cachePath, is -> TarUtils.untar(is, cacheDir, true));
	}

	@Override
	protected boolean uploadCache(String cacheKey, String cachePath,
								  @Nullable String accessToken, File cacheDir) {
		var authorized = getSessionManager().call(() -> {
			var projectId = jobContext.getProjectId();
			var project = getProjectManager().load(projectId);
			if (project.isCommitOnBranch(jobContext.getCommitId(), project.getDefaultBranch())) {
				return true;
			} else if (accessToken != null) {
				var user = getUserManager().findByAccessToken(accessToken);
				return user != null && SecurityUtils.canUploadCache(user.asSubject(), project);
			} else {
				return false;
			}
		});
		if (authorized) {
			getCacheManager().uploadCacheLocal(jobContext.getProjectId(), cacheKey, 
					cachePath, os -> TarUtils.tar(cacheDir, os, true));
			return true;
		} else {
			return false;
		}
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
