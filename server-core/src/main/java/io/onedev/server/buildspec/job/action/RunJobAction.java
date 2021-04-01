package io.onedev.server.buildspec.job.action;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;
import javax.validation.ValidationException;

import org.apache.wicket.Component;
import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.OneDev;
import io.onedev.server.buildspec.BuildSpec;
import io.onedev.server.buildspec.BuildSpecAware;
import io.onedev.server.buildspec.job.Job;
import io.onedev.server.buildspec.job.JobManager;
import io.onedev.server.buildspec.job.SubmitReason;
import io.onedev.server.buildspec.job.paramspec.ParamSpec;
import io.onedev.server.buildspec.job.paramsupply.ParamSupply;
import io.onedev.server.model.Build;
import io.onedev.server.model.PullRequest;
import io.onedev.server.util.ComponentContext;
import io.onedev.server.util.EditContext;
import io.onedev.server.util.MatrixRunner;
import io.onedev.server.web.editable.annotation.ChoiceProvider;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.OmitName;
import io.onedev.server.web.editable.annotation.ParamSpecProvider;
import io.onedev.server.web.util.WicketUtils;

@Editable(name="Run job", order=100)
public class RunJobAction extends PostBuildAction {

	private static final long serialVersionUID = 1L;

	private String jobName;
	
	private List<ParamSupply> jobParams = new ArrayList<>();
	
	@Editable(order=900, name="Job")
	@ChoiceProvider("getJobChoices")
	@NotEmpty
	public String getJobName() {
		return jobName;
	}

	public void setJobName(String jobName) {
		this.jobName = jobName;
	}

	@SuppressWarnings("unused")
	private static List<String> getJobChoices() {
		return Job.getChoices();
	}
	
	@Editable(name="Job Parameters", order=1000)
	@ParamSpecProvider("getParamSpecs")
	@OmitName
	@Valid
	public List<ParamSupply> getJobParams() {
		return jobParams;
	}

	public void setJobParams(List<ParamSupply> jobParams) {
		this.jobParams = jobParams;
	}
	
	@SuppressWarnings("unused")
	private static List<ParamSpec> getParamSpecs() {
		String jobName = (String) EditContext.get().getInputValue("jobName");
		if (jobName != null) {
			Component component = ComponentContext.get().getComponent();
			BuildSpecAware buildSpecAware = WicketUtils.findInnermost(component, BuildSpecAware.class);
			if (buildSpecAware != null) {
				BuildSpec buildSpec = buildSpecAware.getBuildSpec();
				if (buildSpec != null) {
					Job job = buildSpec.getJobMap().get(jobName);
					if (job != null)
						return job.getParamSpecs();
				}
			}
		} 
		return new ArrayList<>();
	}
	
	@Override
	public void execute(Build build) {
		new MatrixRunner<List<String>>(ParamSupply.getParamMatrix(build, getJobParams())) {
			
			@Override
			public void run(Map<String, List<String>> paramMap) {
				SubmitReason reason = new SubmitReason() {

					@Override
					public String getRefName() {
						return build.getRefName();
					}

					@Override
					public PullRequest getPullRequest() {
						return build.getRequest();
					}

					@Override
					public String getDescription() {
						return "Post build action of job '" + build.getJobName() + "'";
					}
					
				};
				JobManager jobManager = OneDev.getInstance(JobManager.class);
				jobManager.submit(build.getProject(), build.getCommitId(), getJobName(), 
						paramMap, reason); 
			}
			
		}.run();
	}

	@Override
	public String getDescription() {
		return "Run job '" + jobName + "'";
	}

	@Override
	public void validateWithContext(BuildSpec buildSpec, Job job) {
		super.validateWithContext(buildSpec, job);
		
		Job jobToRun = buildSpec.getJobMap().get(jobName);
		if (jobToRun != null) {
			try {
				ParamSupply.validateParams(null, jobToRun.getParamSpecs(), jobParams);
			} catch (ValidationException e) {
				throw new ValidationException("Error validating parameters of run job '" 
						+ jobToRun.getName() + "': " + e.getMessage());
			}
		} else {
			throw new ValidationException("Run job not found: " + jobName);
		}
	}

}
