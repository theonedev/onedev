package io.onedev.server.rest.resource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.constraints.NotEmpty;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.NotAcceptableException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.apache.shiro.authz.UnauthorizedException;
import org.apache.shiro.util.ThreadContext;
import org.eclipse.jgit.revwalk.RevCommit;
import org.jspecify.annotations.Nullable;

import io.onedev.commons.utils.StringUtils;
import io.onedev.server.git.GitUtils;
import io.onedev.server.job.JobService;
import io.onedev.server.model.Project;
import io.onedev.server.rest.annotation.Api;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.service.AccessTokenService;
import io.onedev.server.service.ProjectService;

@Api(description="This resource provides an alternative way to run job by passing all parameters via url")
@Path("/trigger-job")
@Consumes(MediaType.WILDCARD)
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class TriggerJobResource {

	private static final String PARAM_PROJECT = "project";
	
	private static final String PARAM_BRANCH = "branch";
	
	private static final String PARAM_TAG = "tag";
	
	private static final String PARAM_JOB = "job";
	
	private static final String PARAM_ACCESS_TOKEN = "access-token";
	
	private static final String PARAM_REBUILD_IF_FINISHED = "rebuild-if-finished";
	
	private static final String DESCRIPTION = "Trigger specified job. Query parameters other than listed below "
			+ "will be interpreted as job params";
	
	private static final String REF_DESCRIPTION = "Specify branch or tag to be triggered against. If none specified, default branch will be used";
	
	private static final String ACCESS_TOKEN_DESCRIPTION = "OneDev access token with permission to trigger the job";
	
	private final JobService jobService;
	
	private final ProjectService projectService;
	
	private final AccessTokenService accessTokenService;
	
	@Inject
	public TriggerJobResource(JobService jobService, ProjectService projectService, AccessTokenService accessTokenService) {
		this.jobService = jobService;
		this.projectService = projectService;
		this.accessTokenService = accessTokenService;
	}

	@Api(order=100, description=DESCRIPTION)
	@GET
    public Long triggerJobViaGet(
    		@Api(description="Path of the project") @QueryParam(PARAM_PROJECT) @NotEmpty String projectPath, 
    		@Api(description=REF_DESCRIPTION) @QueryParam(PARAM_BRANCH) @Nullable String branch, 
    		@Api(description=REF_DESCRIPTION) @QueryParam(PARAM_TAG) @Nullable String tag, 
    		@QueryParam(PARAM_JOB) @NotEmpty String job, 
    		@Api(description=ACCESS_TOKEN_DESCRIPTION) @QueryParam(PARAM_ACCESS_TOKEN) @NotEmpty String accessToken, 
    		@Context UriInfo uriInfo) {
		return triggerJob(projectPath, branch, tag, job, accessToken, uriInfo);
    }
	
	@Api(order=100, description=DESCRIPTION)
	@POST
    public Long triggerJobViaPost(
    		@Api(description="Path of the project") @QueryParam(PARAM_PROJECT) @NotEmpty String projectPath, 
    		@Api(description=REF_DESCRIPTION) @QueryParam(PARAM_BRANCH) @Nullable String branch, 
    		@Api(description=REF_DESCRIPTION) @QueryParam(PARAM_TAG) @Nullable String tag, 
    		@QueryParam(PARAM_JOB) @NotEmpty String job,
			@Api(description=ACCESS_TOKEN_DESCRIPTION) @QueryParam(PARAM_ACCESS_TOKEN) @NotEmpty String accessToken, 
    		@Context UriInfo uriInfo) {
		return triggerJob(projectPath, branch, tag, job, accessToken, uriInfo);
    }

    private Long triggerJob(String projectPath, @Nullable String branch, @Nullable String tag, String job,
							String accessTokenValue, UriInfo uriInfo) {
		Project project = projectService.findByPath(projectPath);
		if (project == null)
			throw new NotAcceptableException("Project not found: " + projectPath);

		var accessToken = accessTokenService.findByValue(accessTokenValue);
		if (accessToken == null)
			throw new NotAcceptableException("Invalid access token");
		
		var subject = accessToken.asSubject();
		var user = SecurityUtils.getUser(subject);
		ThreadContext.bind(subject);
		try {
			if (!SecurityUtils.canRunJob(subject, project, job))		
				throw new UnauthorizedException();

			if (StringUtils.isNotBlank(branch) && StringUtils.isNotBlank(tag)) 
				throw new NotAcceptableException("Either branch or tag should be specified, but not both");
			
			String refName;
			if (branch != null)
				refName = GitUtils.branch2ref(branch);
			else if (tag != null)
				refName = GitUtils.tag2ref(tag);
			else
				refName = GitUtils.branch2ref(project.getDefaultBranch());
			
			RevCommit commit = project.getRevCommit(refName, false);
			if (commit == null)
				throw new NotAcceptableException("Ref not found: " + refName);
			
			Map<String, List<String>> jobParams = new HashMap<>();
			for (Map.Entry<String, List<String>> entry: uriInfo.getQueryParameters().entrySet()) {
				if (!entry.getKey().equals(PARAM_PROJECT) && !entry.getKey().equals(PARAM_BRANCH)
						&& !entry.getKey().equals(PARAM_TAG) && !entry.getKey().equals(PARAM_JOB) 
						&& !entry.getKey().equals(PARAM_ACCESS_TOKEN)
						&& !entry.getKey().equals(PARAM_REBUILD_IF_FINISHED)) {
					jobParams.put(entry.getKey(), entry.getValue());
				}
			}
			
			var build = jobService.submit(user, project, commit.copy(), job, jobParams, refName, 
			null, null, "Triggered via restful api");
			if (build.isFinished())
				jobService.resubmit(user, build, "Rebuild via restful api");
			return build.getId();
		} finally {
			ThreadContext.unbindSubject();
		}
    }
	
}
