package io.onedev.server.job;

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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import static io.onedev.k8shelper.CacheHelper.untar;

public class ServerCacheHelper extends CacheHelper {
	
	private final JobContext jobContext;
	
	public ServerCacheHelper(File buildHome, JobContext jobContext, TaskLogger logger) {
		super(buildHome, logger);
		this.jobContext = jobContext;
	}

	@Override
	protected boolean downloadCache(String cacheKey, LinkedHashMap<String, File> cacheDirs) {
		return getCacheManager().downloadCacheLocal(
				jobContext.getProjectId(),
				cacheKey, 
				new ArrayList<>(cacheDirs.keySet()),
				is -> untar(new ArrayList<>(cacheDirs.values()), is));
	}

	@Override
	protected boolean downloadCache(List<String> cacheLoadKeys, LinkedHashMap<String, File> cacheDirs) {
		return getCacheManager().downloadCacheLocal(
				jobContext.getProjectId(), 
				cacheLoadKeys, 
				new ArrayList<>(cacheDirs.keySet()),
				is -> untar(new ArrayList<>(cacheDirs.values()), is));
	}

	@Override
	protected boolean uploadCache(String cacheKey, LinkedHashMap<String, File> cacheDirs,
								  @Nullable String accessToken) {
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
			getCacheManager().uploadCacheLocal(
					jobContext.getProjectId(), 
					cacheKey, 
					new ArrayList<>(cacheDirs.keySet()), 
					os -> tar(new ArrayList<>(cacheDirs.values()), os));
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
