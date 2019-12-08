package io.onedev.server.buildspec.job.action;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;
import javax.validation.ValidationException;

import org.apache.wicket.Component;
import org.hibernate.validator.constraints.NotEmpty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.onedev.server.OneDev;
import io.onedev.server.buildspec.BuildSpec;
import io.onedev.server.buildspec.BuildSpecAware;
import io.onedev.server.buildspec.job.Job;
import io.onedev.server.buildspec.job.JobManager;
import io.onedev.server.buildspec.job.paramspec.ParamSpec;
import io.onedev.server.buildspec.job.paramsupply.ParamSupply;
import io.onedev.server.entitymanager.BuildManager;
import io.onedev.server.model.Build;
import io.onedev.server.persistence.SessionManager;
import io.onedev.server.persistence.TransactionManager;
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

	private static final Logger logger = LoggerFactory.getLogger(RunJobAction.class);
	
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
		Long buildId = build.getId();

		OneDev.getInstance(TransactionManager.class).runAfterCommit(new Runnable() {

			@Override
			public void run() {
				OneDev.getInstance(SessionManager.class).runAsync(new Runnable() {

					@Override
					public void run() {
						Build build = OneDev.getInstance(BuildManager.class).load(buildId);
						Build.push(build);
						try {
							new MatrixRunner<List<String>>(ParamSupply.getParamMatrix(getJobParams())) {
								
								@Override
								public void run(Map<String, List<String>> paramMap) {
									OneDev.getInstance(JobManager.class).submit(build.getProject(), 
											build.getCommitId(), jobName, paramMap); 
								}
								
							}.run();
						} catch (Exception e) {
							String message = String.format("Error submitting build (project: %s, commit: %s, job: %s)", 
									build.getProject().getName(), build.getCommitHash(), jobName);
							logger.error(message, e);
						} finally {
							Build.pop();
						}
					}
					
				});
			}
			
		});
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
				ParamSupply.validateParams(jobToRun.getParamSpecs(), jobParams);
			} catch (ValidationException e) {
				throw new ValidationException("Error validating parameters of run job '" 
						+ jobToRun.getName() + "': " + e.getMessage());
			}
		} else {
			throw new ValidationException("Run job not found: " + jobName);
		}
	}

}
