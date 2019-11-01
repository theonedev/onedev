package io.onedev.server.web.component.job;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.model.IModel;
import org.eclipse.jgit.lib.ObjectId;

import com.google.common.base.Preconditions;

import io.onedev.server.OneDev;
import io.onedev.server.ci.CISpec;
import io.onedev.server.ci.job.Job;
import io.onedev.server.ci.job.JobManager;
import io.onedev.server.ci.job.param.JobParam;
import io.onedev.server.ci.job.paramspec.ParamSpec;
import io.onedev.server.model.Build;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.inputspec.InputContext;
import io.onedev.server.util.SecurityUtils;
import io.onedev.server.util.scriptidentity.JobIdentity;
import io.onedev.server.util.scriptidentity.ScriptIdentity;
import io.onedev.server.util.scriptidentity.ScriptIdentityAware;
import io.onedev.server.web.component.beaneditmodal.BeanEditModalPanel;
import io.onedev.server.web.model.EntityModel;
import io.onedev.server.web.page.project.builds.detail.dashboard.BuildDashboardPage;
import io.onedev.server.web.page.project.builds.detail.log.BuildLogPage;

@SuppressWarnings("serial")
public class RunJobLink extends AjaxLink<Void> {

	private final IModel<Project> projectModel;
	
	private final ObjectId commitId;
	
	private final String jobName;
	
	public RunJobLink(String componentId, Project project, ObjectId commitId, String jobName) {
		super(componentId);
		
		this.projectModel = new EntityModel<Project>(project);
		this.commitId = commitId;
		this.jobName = jobName;
	}
	
	private Project getProject() {
		return projectModel.getObject();
	}
	
	@Override
	public void onClick(AjaxRequestTarget target) {
		CISpec ciSpec = Preconditions.checkNotNull(getProject().getCISpec(commitId));
		
		Job job = Preconditions.checkNotNull(ciSpec.getJobMap().get(jobName));
		if (!job.getParamSpecs().isEmpty()) {
			Serializable paramBean;
			try {
				paramBean = JobParam.defineBeanClass(job.getParamSpecs()).newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				throw new RuntimeException(e);
			}
			
			new ParamEditModalPanel(target, paramBean) {

				@Override
				protected void onSave(AjaxRequestTarget target, Serializable bean) {
					Map<String, List<String>> paramMap = JobParam.getParamMap(
							job, bean, job.getParamSpecMap().keySet());
					Build build = OneDev.getInstance(JobManager.class).submit(getProject(), 
							commitId, job.getName(), paramMap, SecurityUtils.getUser());
					setResponsePage(BuildDashboardPage.class, BuildDashboardPage.paramsOf(build, null));
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
					job.getName(), new HashMap<>(), SecurityUtils.getUser());
			setResponsePage(BuildLogPage.class, BuildLogPage.paramsOf(build, null));
		}
	}

	@Override
	protected void onConfigure() {
		super.onConfigure();
		setVisible(SecurityUtils.canRunJob(getProject(), jobName));
	}

	@Override
	protected void onDetach() {
		projectModel.detach();
		super.onDetach();
	}

	private abstract class ParamEditModalPanel extends BeanEditModalPanel implements InputContext, ScriptIdentityAware {

		public ParamEditModalPanel(AjaxRequestTarget target, Serializable bean) {
			super(target, bean);
		}

	}
	
}
