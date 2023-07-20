package io.onedev.server.rest;

import io.onedev.server.entitymanager.BuildManager;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.entitymanager.PullRequestManager;
import io.onedev.server.git.service.GitService;
import io.onedev.server.job.JobManager;
import io.onedev.server.model.Build;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.support.pullrequest.MergePreview;
import io.onedev.server.rest.annotation.Api;
import io.onedev.server.rest.support.JobRun;
import io.onedev.server.rest.support.JobRunOnCommit;
import io.onedev.server.rest.support.JobRunOnPullRequest;
import io.onedev.server.security.SecurityUtils;
import org.apache.shiro.authz.UnauthorizedException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.Valid;
import javax.validation.ValidationException;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.Serializable;
import java.util.UUID;

@Api(order=3500)
@Path("/job-runs")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class JobRunResource {

	private final JobManager jobManager;
	
	private final BuildManager buildManager;
	
	private final ProjectManager projectManager;
	
	private final PullRequestManager pullRequestManager;
	
	private final GitService gitService;
	
	@Inject
	public JobRunResource(JobManager jobManager, BuildManager buildManager, 
			ProjectManager projectManager, PullRequestManager pullRequestManager, 
			GitService gitService) {
		this.jobManager = jobManager;
		this.buildManager = buildManager;
		this.projectManager = projectManager;
		this.pullRequestManager = pullRequestManager;
		this.gitService = gitService;
	}

	@Api(order=100)
    @POST
    public Long runBuild(@NotNull @Valid JobRun jobRun) {
		Project project;
		String refName;
		PullRequest request;
		ObjectId commitId;
		
		if (jobRun instanceof JobRunOnCommit) {
			JobRunOnCommit jobRunOnCommit = (JobRunOnCommit) jobRun;
			
	    	project = projectManager.load(jobRunOnCommit.getProjectId());
			if (!SecurityUtils.canRunJob(project, jobRun.getJobName()))		
				throw new UnauthorizedException();

			if (!jobRunOnCommit.getRefName().startsWith(Constants.R_REFS)) 
				throw new ValidationException("Ref name should start with " + Constants.R_REFS);
			
			RevCommit refCommit = project.getRevCommit(jobRunOnCommit.getRefName(), true);
			
			commitId = ObjectId.fromString(jobRunOnCommit.getCommitHash());
			
			if (!gitService.isMergedInto(project, null, commitId, refCommit)) 
				throw new ValidationException("Specified commit is not reachable from specified ref");
			
			refName = jobRunOnCommit.getRefName();
			request = null;
		} else {
			JobRunOnPullRequest jobRunOnPullRequest = (JobRunOnPullRequest) jobRun;
			request = pullRequestManager.load(jobRunOnPullRequest.getPullRequestId());
			refName = request.getMergeRef();
			project = request.getProject();
			
			if (!SecurityUtils.canRunJob(request.getProject(), jobRun.getJobName()))		
				throw new UnauthorizedException();

			MergePreview preview = request.checkMergePreview();
			if (preview == null)
				throw new ValidationException("Pull request merge preview not calculated yet");
			if (preview.getMergeCommitHash() == null)
				throw new ValidationException("Pull request has merge conflicts");
			
			commitId = ObjectId.fromString(preview.getMergeCommitHash());
		}
		
		Build build = jobManager.submit(project, commitId, jobRun.getJobName(), 
				jobRun.getParams(), UUID.randomUUID().toString(), refName, 
				SecurityUtils.getUser(), request, null, jobRun.getReason());
		return build.getId();
    }

	@Api(order=200)
    @Path("/rebuild")
    @POST
    public Response rebuild(@NotNull @Valid JobRerun jobRerun) {
    	Build  build = buildManager.load(jobRerun.buildId);
		if (!SecurityUtils.canRunJob(build.getProject(), build.getJobName()))		
			throw new UnauthorizedException();
		jobManager.resubmit(build, jobRerun.reason);
		return Response.ok().build();
    }
    
	@Api(order=300)
	@Path("/{buildId}")
    @DELETE
    public Response cancelBuild(@PathParam("buildId") Long buildId) {
		Build build = buildManager.load(buildId);
		if (!SecurityUtils.canRunJob(build.getProject(), build.getJobName()))		
			throw new UnauthorizedException();
		if (!build.isFinished())
			jobManager.cancel(build);
    	return Response.ok().build();
    }
	
	public static class JobRerun implements Serializable {
		
		private static final long serialVersionUID = 1L;

		@Api(order=100)
		private Long buildId;
		
		@Api(order=300)
		private String reason;

		@NotNull
		public Long getBuildId() {
			return buildId;
		}

		public void setBuildId(Long buildId) {
			this.buildId = buildId;
		}

		@NotEmpty
		public String getReason() {
			return reason;
		}

		public void setReason(String reason) {
			this.reason = reason;
		}
		
	}

}
