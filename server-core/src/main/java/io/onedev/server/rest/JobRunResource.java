package io.onedev.server.rest;

import java.io.Serializable;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.Valid;
import javax.validation.ValidationException;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.shiro.authz.UnauthorizedException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;
import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.buildspec.job.JobManager;
import io.onedev.server.buildspec.job.SubmitReason;
import io.onedev.server.entitymanager.BuildManager;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.entitymanager.PullRequestManager;
import io.onedev.server.git.GitUtils;
import io.onedev.server.model.Build;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.support.pullrequest.MergePreview;
import io.onedev.server.rest.annotation.Api;
import io.onedev.server.rest.support.JobRun;
import io.onedev.server.rest.support.JobRunOnCommit;
import io.onedev.server.rest.support.JobRunOnPullRequest;
import io.onedev.server.security.SecurityUtils;

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
	
	@Inject
	public JobRunResource(JobManager jobManager, BuildManager buildManager, 
			ProjectManager projectManager, PullRequestManager pullRequestManager) {
		this.jobManager = jobManager;
		this.buildManager = buildManager;
		this.projectManager = projectManager;
		this.pullRequestManager = pullRequestManager;
	}

	@Api(order=100)
    @POST
    public Long runBuild(@NotNull @Valid JobRun jobRun) {
		Project project;
		SubmitReason reason;
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
			
			if (!GitUtils.isMergedInto(project.getRepository(), null, commitId, refCommit)) 
				throw new ValidationException("Specified commit is not reachable from specified ref");
			
			reason = new SubmitReason() {

				@Override
				public String getRefName() {
					return jobRunOnCommit.getRefName();
				}

				@Override
				public PullRequest getPullRequest() {
					return null;
				}

				@Override
				public String getDescription() {
					return jobRun.getReason();
				}
				
			};
		} else {
			JobRunOnPullRequest jobRunOnPullRequest = (JobRunOnPullRequest) jobRun;
			PullRequest pullRequest = pullRequestManager.load(jobRunOnPullRequest.getPullRequestId());
			project = pullRequest.getProject();
			
			if (!SecurityUtils.canRunJob(pullRequest.getProject(), jobRun.getJobName()))		
				throw new UnauthorizedException();

			MergePreview preview = pullRequest.getMergePreview();
			if (preview == null)
				throw new ValidationException("Pull request merge preview not calcualted yet");
			if (preview.getMergeCommitHash() == null)
				throw new ValidationException("Pull request has merge conflicts");
			
			commitId = ObjectId.fromString(preview.getMergeCommitHash());
			
			reason = new SubmitReason() {

				@Override
				public String getRefName() {
					return pullRequest.getMergeRef();
				}

				@Override
				public PullRequest getPullRequest() {
					return pullRequest;
				}

				@Override
				public String getDescription() {
					return jobRun.getReason();
				}
				
			};
		}
		
		return jobManager.submit(project, commitId, jobRun.getJobName(), 
				jobRun.getParams(), UUID.randomUUID().toString(), reason).getId();
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
