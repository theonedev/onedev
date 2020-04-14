package io.onedev.server.web.component.job;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.eclipse.jgit.lib.ObjectId;

import com.google.common.base.Preconditions;

import io.onedev.server.OneDev;
import io.onedev.server.buildspec.BuildSpec;
import io.onedev.server.buildspec.job.Job;
import io.onedev.server.buildspec.job.JobManager;
import io.onedev.server.buildspec.job.paramspec.ParamSpec;
import io.onedev.server.buildspec.job.paramsupply.ParamSupply;
import io.onedev.server.model.Build;
import io.onedev.server.model.Project;
import io.onedev.server.util.SecurityUtils;
import io.onedev.server.util.inputspec.InputContext;
import io.onedev.server.util.script.identity.JobIdentity;
import io.onedev.server.util.script.identity.ScriptIdentity;
import io.onedev.server.util.script.identity.ScriptIdentityAware;
import io.onedev.server.web.component.beaneditmodal.BeanEditModalPanel;
import io.onedev.server.web.page.project.builds.detail.dashboard.BuildDashboardPage;
import io.onedev.server.web.page.project.builds.detail.log.BuildLogPage;

@SuppressWarnings("serial")
public abstract class RunJobLink extends AjaxLink<Void> {

	private final ObjectId commitId;
	
	private final String jobName;
	
	public RunJobLink(String componentId, ObjectId commitId, String jobName) {
		super(componentId);
		
		this.commitId = commitId;
		this.jobName = jobName;
	}
	
	protected abstract Project getProject();
	
	@Override
	public void onClick(AjaxRequestTarget target) {
		BuildSpec buildSpec = Preconditions.checkNotNull(getProject().getBuildSpec(commitId));
		
		Job job = Preconditions.checkNotNull(buildSpec.getJobMap().get(jobName));
		if (!job.getParamSpecs().isEmpty()) {
			Serializable paramBean;
			try {
				paramBean = ParamSupply.defineBeanClass(job.getParamSpecs()).newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				throw new RuntimeException(e);
			}
			
			new ParamEditModalPanel(target, paramBean) {

				@Override
				protected void onSave(AjaxRequestTarget target, Serializable bean) {
					Map<String, List<String>> paramMap = ParamSupply.getParamMap(
							job, bean, job.getParamSpecMap().keySet());
					Build build = OneDev.getInstance(JobManager.class).submit(getProject(), 
							commitId, job.getName(), paramMap, null);
					setResponsePage(BuildDashboardPage.class, BuildDashboardPage.paramsOf(build));
				}

				@Override
				public List<String> getInputNames() {
					return new ArrayList<>(job.getParamSpecMap().keySet());
				}

				@Override
				public ParamSpec getInputSpec(String paramName) {
					return Preconditions.checkNotNull(job.getParamSpecMap().get(paramName));
				}

				@Override
				public ScriptIdentity getScriptIdentity() {
					return new JobIdentity(getProject(), commitId);
				}

			};
		} else {
			Build build = OneDev.getInstance(JobManager.class).submit(getProject(), commitId, 
					job.getName(), new HashMap<>(), null);
			setResponsePage(BuildLogPage.class, BuildLogPage.paramsOf(build));
		}
	}

	@Override
	protected void onConfigure() {
		super.onConfigure();
		setVisible(SecurityUtils.canRunJob(getProject(), jobName));
	}

	private abstract class ParamEditModalPanel extends BeanEditModalPanel implements InputContext, ScriptIdentityAware {

		public ParamEditModalPanel(AjaxRequestTarget target, Serializable bean) {
			super(target, bean);
		}

	}
	
}
