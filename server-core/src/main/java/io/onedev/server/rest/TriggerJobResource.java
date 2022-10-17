package io.onedev.server.rest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
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
import javax.validation.constraints.NotEmpty;

import io.onedev.commons.utils.StringUtils;
import io.onedev.server.buildspec.job.SubmitReason;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.git.GitUtils;
import io.onedev.server.job.JobManager;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.User;
import io.onedev.server.rest.annotation.Api;
import io.onedev.server.rest.exception.InvalidParamException;
import io.onedev.server.security.SecurityUtils;

@Api(order=3510, description="This resource provides an alternative way to run job by passing all parameters via url")
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
	
	private static final String DESCRIPTION = "Trigger specified job. Query parameters other than listed below "
			+ "will be interpreted as job params";
	
	private static final String REF_DESCRIPTION = "Either branch or tag should be specified, but not both";
	
	private static final String ACCESS_TOKEN_DESCRIPTION = "OneDev access token with permission to trigger the job";
	
	private final JobManager jobManager;
	
	private final ProjectManager projectManager;
	
	private final UserManager userManager;
	
	@Inject
	public TriggerJobResource(JobManager jobManager, ProjectManager projectManager, UserManager userManager) {
		this.jobManager = jobManager;
		this.projectManager = projectManager;
		this.userManager = userManager;
	}

	@Api(order=100, description=DESCRIPTION)
	@GET
    public Long triggerJobViaGet(
    		@Api(description="Path of the project") @QueryParam(PARAM_PROJECT) @NotEmpty String projectPath, 
    		@Api(description=REF_DESCRIPTION) @QueryParam(PARAM_BRANCH) String branch, 
    		@Api(description=REF_DESCRIPTION) @QueryParam(PARAM_TAG) String tag, 
    		@QueryParam(PARAM_JOB) @NotEmpty String job, 
    		@Api(description=ACCESS_TOKEN_DESCRIPTION) @QueryParam(PARAM_ACCESS_TOKEN) @NotEmpty String accessToken, 
    		@Context UriInfo uriInfo) {
		return triggerJob(projectPath, branch, tag, job, accessToken, uriInfo);
    }
	
	@Api(order=100, description=DESCRIPTION)
	@POST
    public Long triggerJobViaPost(
    		@Api(description="Path of the project") @QueryParam(PARAM_PROJECT) @NotEmpty String projectPath, 
    		@Api(description=REF_DESCRIPTION) @QueryParam(PARAM_BRANCH) String branch, 
    		@Api(description=REF_DESCRIPTION) @QueryParam(PARAM_TAG) String tag, 
    		@QueryParam(PARAM_JOB) @NotEmpty String job, 
    		@Api(description=ACCESS_TOKEN_DESCRIPTION) @QueryParam(PARAM_ACCESS_TOKEN) @NotEmpty String accessToken, 
    		@Context UriInfo uriInfo) {
		return triggerJob(projectPath, branch, tag, job, accessToken, uriInfo);
    }

    private Long triggerJob(String projectPath, String branch, String tag, String job, 
    		String accessToken, UriInfo uriInfo) {
		Project project = projectManager.findByPath(projectPath);
		if (project == null)
			throw new InvalidParamException("Project not found: " + projectPath);

		User user = userManager.findByAccessToken(accessToken);
		if (user == null)
			throw new InvalidParamException("Invalid access token");
		
		ThreadContext.bind(user.asSubject());
		try {
			if (!SecurityUtils.canRunJob(project, job))		
				throw new UnauthorizedException();

			if (StringUtils.isBlank(branch) && StringUtils.isBlank(tag)) 
				throw new InvalidParamException("Either branch or tag should be specified");
				
			if (StringUtils.isNotBlank(branch) && StringUtils.isNotBlank(tag)) 
				throw new InvalidParamException("Either branch or tag should be specified, but not both");
			
			String refName;
			if (branch != null)
				refName = GitUtils.branch2ref(branch);
			else 
				refName = GitUtils.tag2ref(tag);
			
			RevCommit commit = project.getRevCommit(refName, false);
			if (commit == null)
				throw new InvalidParamException("Ref not found: " + refName);
			
			Map<String, List<String>> jobParams = new HashMap<>();
			for (Map.Entry<String, List<String>> entry: uriInfo.getQueryParameters().entrySet()) {
				if (!entry.getKey().equals(PARAM_PROJECT) && !entry.getKey().equals(PARAM_BRANCH)
						&& !entry.getKey().equals(PARAM_TAG) && !entry.getKey().equals(PARAM_JOB) 
						&& !entry.getKey().equals(PARAM_ACCESS_TOKEN)) {
					jobParams.put(entry.getKey(), entry.getValue());
				}
			}
			
			SubmitReason reason = new SubmitReason() {

				@Override
				public String getRefName() {
					return refName;
				}

				@Override
				public PullRequest getPullRequest() {
					return null;
				}

				@Override
				public String getDescription() {
					return "Triggered via restful api";
				}
				
			};
			
			return jobManager.submit(project, commit.copy(), job, 
					jobParams, UUID.randomUUID().toString(), reason).getId();
		} finally {
			ThreadContext.unbindSubject();
		}
    }
	
}
