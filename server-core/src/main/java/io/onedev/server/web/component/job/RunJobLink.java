package io.onedev.server.web.component.job;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.eclipse.jgit.lib.ObjectId;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import io.onedev.server.OneDev;
import io.onedev.server.buildspec.BuildSpec;
import io.onedev.server.buildspec.job.Job;
import io.onedev.server.buildspec.job.SubmitReason;
import io.onedev.server.buildspec.param.ParamUtils;
import io.onedev.server.buildspec.param.spec.ParamSpec;
import io.onedev.server.git.service.RefFacade;
import io.onedev.server.infomanager.CommitInfoManager;
import io.onedev.server.job.JobManager;
import io.onedev.server.model.Build;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.script.identity.JobIdentity;
import io.onedev.server.util.script.identity.ScriptIdentity;
import io.onedev.server.web.component.modal.message.MessageModal;
import io.onedev.server.web.page.project.builds.detail.dashboard.BuildDashboardPage;

@SuppressWarnings("serial")
public abstract class RunJobLink extends AjaxLink<Void> {

	private final String refName;
	
	private final ObjectId commitId;
	
	private final String jobName;
	
	public RunJobLink(String componentId, ObjectId commitId, String jobName, @Nullable String refName) {
		super(componentId);
		
		this.commitId = commitId;
		this.jobName = jobName;
		this.refName = refName;
	}
	
	protected abstract Project getProject();
	
	protected abstract String getPipeline();
	
	@Nullable
	protected abstract PullRequest getPullRequest();
	
	@Override
	public void onClick(AjaxRequestTarget target) {
		BuildSpec buildSpec = Preconditions.checkNotNull(getProject().getBuildSpec(commitId));
		
		Collection<ObjectId> descendants = OneDev.getInstance(CommitInfoManager.class)
				.getDescendants(getProject().getId(), Sets.newHashSet(commitId));
		descendants.add(commitId);
	
		List<RefFacade> refs = new ArrayList<>();
		refs.addAll(getProject().getBranchRefs());
		refs.addAll(getProject().getTagRefs());
		
		List<String> refNames;
		
		if (refName != null) {
			refNames = Lists.newArrayList(refName);
		} else if (getPullRequest() != null) {
			refNames = Lists.newArrayList(getPullRequest().getMergeRef());
		} else {
			refNames = refs.stream()
					.filter(it->descendants.contains(it.getPeeledObj()))
					.map(it->it.getName())
					.collect(Collectors.toList());
		}

		if (!refNames.isEmpty()) {
			Job job = Preconditions.checkNotNull(buildSpec.getJobMap().get(jobName));
			if (refNames.size() > 1 || !job.getParamSpecs().isEmpty()) {
				Serializable paramBean;
				try {
					paramBean = ParamUtils.defineBeanClass(job.getParamSpecs())
							.getDeclaredConstructor().newInstance();
				} catch (InstantiationException | IllegalAccessException | IllegalArgumentException 
						| InvocationTargetException | NoSuchMethodException | SecurityException e) {
					throw new RuntimeException(e);
				}
				
				new BuildOptionModalPanel(target, refNames, paramBean) {

					@Override
					protected void onSave(AjaxRequestTarget target, Collection<String> selectedRefNames, 
							Serializable populatedParamBean) {
						Map<String, List<String>> paramMap = ParamUtils.getParamMap(
								job, populatedParamBean, job.getParamSpecMap().keySet());
						String pipeline = getPipeline();
						List<Build> builds = new ArrayList<>();
						for (String refName: selectedRefNames) {
							SubmitReason reason = new SubmitReason() {

								@Override
								public String getRefName() {
									return refName;
								}

								@Override
								public PullRequest getPullRequest() {
									return RunJobLink.this.getPullRequest();
								}

								@Override
								public String getDescription() {
									return "Submitted manually";
								}
								
							};
							builds.add(getJobManager().submit(getProject(), commitId, job.getName(), 
									paramMap, pipeline, reason));
						}
						if (builds.size() == 1)
							setResponsePage(BuildDashboardPage.class, BuildDashboardPage.paramsOf(builds.iterator().next()));
						else
							close();
						if (builds.stream().allMatch(it->it.isFinished()))
							Session.get().warn("Build already fired in current pipeline");
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
				SubmitReason reason = new SubmitReason() {

					@Override
					public String getRefName() {
						return refNames.iterator().next();
					}

					@Override
					public PullRequest getPullRequest() {
						return RunJobLink.this.getPullRequest();
					}

					@Override
					public String getDescription() {
						return "Submitted manually";
					}
					
				};
				Build build = getJobManager().submit(getProject(), commitId, job.getName(), 
						new HashMap<>(), getPipeline(), reason);
				setResponsePage(BuildDashboardPage.class, BuildDashboardPage.paramsOf(build));
				if (build.isFinished())
					Session.get().warn("Build already fired in current pipeline");
			}
		} else {
			new MessageModal(target) {

				@Override
				protected Component newMessageContent(String componentId) {
					return new Label(componentId, "No refs to build on behalf of");
				}
				
			};
		}
	}
	
	private JobManager getJobManager() {
		return OneDev.getInstance(JobManager.class);
	}

	@Override
	protected void onConfigure() {
		super.onConfigure();
		setVisible(SecurityUtils.canRunJob(getProject(), jobName));
	}
	
}
