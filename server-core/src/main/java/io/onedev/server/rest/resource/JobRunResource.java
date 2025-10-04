package io.onedev.server.rest.resource;

import io.onedev.server.service.BuildService;
import io.onedev.server.service.ProjectService;
import io.onedev.server.service.PullRequestService;
import io.onedev.server.git.service.GitService;
import io.onedev.server.job.JobService;
import io.onedev.server.model.Build;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.support.pullrequest.MergePreview;
import io.onedev.server.rest.annotation.Api;
import io.onedev.server.rest.resource.support.JobRun;
import io.onedev.server.rest.resource.support.JobRunOnCommit;
import io.onedev.server.rest.resource.support.JobRunOnPullRequest;
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

@Path("/job-runs")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class JobRunResource {

	private final JobService jobService;
	
	private final BuildService buildService;
	
	private final ProjectService projectService;
	
	private final PullRequestService pullRequestService;
	
	private final GitService gitService;
	
	@Inject
	public JobRunResource(JobService jobService, BuildService buildService,
                          ProjectService projectService, PullRequestService pullRequestService,
                          GitService gitService) {
		this.jobService = jobService;
		this.buildService = buildService;
		this.projectService = projectService;
		this.pullRequestService = pullRequestService;
		this.gitService = gitService;
	}

	@Api(order=100)
    @POST
    public Long runBuild(@NotNull @Valid JobRun jobRun) {
		Project project;
		String refName;
		PullRequest request;
		ObjectId commitId;
		
		var subject = SecurityUtils.getSubject();
		var user = SecurityUtils.getUser(subject);
		if (jobRun instanceof JobRunOnCommit) {
			JobRunOnCommit jobRunOnCommit = (JobRunOnCommit) jobRun;
			
	    	project = projectService.load(jobRunOnCommit.getProjectId());
			if (!SecurityUtils.canRunJob(subject, project, jobRun.getJobName()))		
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
			request = pullRequestService.load(jobRunOnPullRequest.getPullRequestId());
			refName = request.getMergeRef();
			project = request.getProject();
			
			if (!SecurityUtils.canRunJob(subject, request.getProject(), jobRun.getJobName()))		
				throw new UnauthorizedException();

			MergePreview preview = request.checkMergePreview();
			if (preview == null)
				throw new ValidationException("Pull request merge preview not calculated yet");
			if (preview.getMergeCommitHash() == null)
				throw new ValidationException("Pull request has merge conflicts");
			
			commitId = ObjectId.fromString(preview.getMergeCommitHash());
		}
		
		Build build = jobService.submit(user, project, commitId, jobRun.getJobName(), 
				jobRun.getParams(), refName, request, null, jobRun.getReason());
		if (build.isFinished())
			jobService.resubmit(user, build, jobRun.getReason());
		return build.getId();
    }

	@Api(order=200)
    @Path("/rebuild")
    @POST
    public Response rebuild(@NotNull @Valid JobRerun jobRerun) {
    	Build  build = buildService.load(jobRerun.buildId);
		var subject = SecurityUtils.getSubject();
		var user = SecurityUtils.getUser(subject);
		if (!SecurityUtils.canRunJob(subject, build.getProject(), build.getJobName()))		
			throw new UnauthorizedException();
		jobService.resubmit(user, build, jobRerun.reason);
		return Response.ok().build();
    }
    
	@Api(order=300)
	@Path("/{buildId}")
    @DELETE
    public Response cancelBuild(@PathParam("buildId") Long buildId) {
		Build build = buildService.load(buildId);
		if (!SecurityUtils.canRunJob(build.getProject(), build.getJobName()))		
			throw new UnauthorizedException();
		if (!build.isFinished())
			jobService.cancel(build);
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
