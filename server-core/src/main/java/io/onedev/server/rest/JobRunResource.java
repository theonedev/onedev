package io.onedev.server.rest;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.Valid;
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
import org.eclipse.jgit.lib.ObjectId;
import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.buildspec.job.JobManager;
import io.onedev.server.buildspec.job.SubmitReason;
import io.onedev.server.entitymanager.BuildManager;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.entitymanager.PullRequestManager;
import io.onedev.server.model.Build;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.validation.annotation.CommitHash;

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

    @POST
    public Long save(@NotNull @Valid JobRun jobRun) {
    	Project project = projectManager.load(jobRun.projectId);
		if (!SecurityUtils.canRunJob(project, jobRun.jobName))		
			throw new UnauthorizedException();
		
		SubmitReason reason = new SubmitReason() {

			@Override
			public String getRefName() {
				return jobRun.refName;
			}

			@Override
			public PullRequest getPullRequest() {
				if (jobRun.pullRequestId != null)
					return pullRequestManager.load(jobRun.pullRequestId);
				else
					return null;
			}

			@Override
			public String getDescription() {
				return jobRun.reason;
			}
			
		};
		return jobManager.submit(project, ObjectId.fromString(jobRun.commitHash), 
				jobRun.jobName, jobRun.params, reason).getId();
    }

    @Path("/rerun")
    @POST
    public Response save(@NotNull @Valid JobRerun jobRerun) {
    	Build  build = buildManager.load(jobRerun.buildId);
		if (!SecurityUtils.canRunJob(build.getProject(), build.getJobName()))		
			throw new UnauthorizedException();
		jobManager.resubmit(build, jobRerun.params, jobRerun.reason);
		return Response.ok().build();
    }
    
	@Path("/{buildId}")
    @DELETE
    public Response delete(@PathParam("buildId") Long buildId) {
		Build build = buildManager.load(buildId);
		if (!SecurityUtils.canRunJob(build.getProject(), build.getJobName()))		
			throw new UnauthorizedException();
		if (!build.isFinished())
			jobManager.cancel(build);
    	return Response.ok().build();
    }
	
	public static class JobRun implements Serializable {
		
		private static final long serialVersionUID = 1L;

		private Long projectId; 
		
		private String commitHash;
		
		private String jobName;
		
		private Map<String, List<String>> params = new HashMap<>();
		
		private String refName;
		
		private Long pullRequestId;
		
		private String reason;

		@NotNull
		public Long getProjectId() {
			return projectId;
		}

		public void setProjectId(Long projectId) {
			this.projectId = projectId;
		}

		@CommitHash
		@NotEmpty
		public String getCommitHash() {
			return commitHash;
		}

		public void setCommitHash(String commitHash) {
			this.commitHash = commitHash;
		}

		@NotEmpty
		public String getJobName() {
			return jobName;
		}

		public void setJobName(String jobName) {
			this.jobName = jobName;
		}

		@NotNull
		public Map<String, List<String>> getParams() {
			return params;
		}

		public void setParams(Map<String, List<String>> params) {
			this.params = params;
		}

		@NotEmpty
		public String getRefName() {
			return refName;
		}

		public void setRefName(String refName) {
			this.refName = refName;
		}

		public Long getPullRequestId() {
			return pullRequestId;
		}

		public void setPullRequestId(Long pullRequestId) {
			this.pullRequestId = pullRequestId;
		}

		@NotEmpty
		public String getReason() {
			return reason;
		}

		public void setReason(String reason) {
			this.reason = reason;
		}
		
	}
	
	public static class JobRerun implements Serializable {
		
		private static final long serialVersionUID = 1L;

		private Long buildId;
		
		private Map<String, List<String>> params = new HashMap<>();
		
		private String reason;

		@NotNull
		public Long getBuildId() {
			return buildId;
		}

		public void setBuildId(Long buildId) {
			this.buildId = buildId;
		}

		@NotNull
		public Map<String, List<String>> getParams() {
			return params;
		}

		public void setParams(Map<String, List<String>> params) {
			this.params = params;
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
