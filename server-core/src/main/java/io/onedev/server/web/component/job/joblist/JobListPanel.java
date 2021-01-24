package io.onedev.server.web.component.job.joblist;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.eclipse.jgit.lib.ObjectId;

import com.google.common.collect.Sets;

import io.onedev.server.OneDev;
import io.onedev.server.buildspec.job.Job;
import io.onedev.server.entitymanager.BuildManager;
import io.onedev.server.model.Build;
import io.onedev.server.model.Build.Status;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.behavior.WebSocketObserver;
import io.onedev.server.web.component.build.simplelist.SimpleBuildListPanel;
import io.onedev.server.web.component.job.JobDefLink;
import io.onedev.server.web.component.job.RunJobLink;
import io.onedev.server.web.page.project.builds.ProjectBuildsPage;

@SuppressWarnings("serial")
public abstract class JobListPanel extends Panel {

	private final ObjectId commitId;
	
	private final String refName;
	
	private final List<Job> jobs;
	
	private final IModel<List<Job>> accessibleJobsModel = new LoadableDetachableModel<List<Job>>() {

		@Override
		protected List<Job> load() {
			List<Job> accessibleJobs = new ArrayList<>();
			for (Job job: jobs) {
				if (SecurityUtils.canAccess(getProject(), job.getName()))
					accessibleJobs.add(job);
			}
			return accessibleJobs;
		}
		
	};
	
	public JobListPanel(String id, ObjectId commitId, @Nullable String refName, List<Job> jobs) {
		super(id);
		this.commitId = commitId;
		this.refName = refName;
		this.jobs = jobs;
	}
	
	protected abstract Project getProject();
	
	@Nullable
	protected abstract PullRequest getPullRequest();
	
	protected abstract void onRunJob(AjaxRequestTarget target);

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new WebMarkupContainer("note") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(accessibleJobsModel.getObject().size() != jobs.size());
			}
			
		});
				
		RepeatingView jobsView = new RepeatingView("jobs");
		add(jobsView);
		for (Job job: accessibleJobsModel.getObject()) {
			WebMarkupContainer jobItem = new WebMarkupContainer(jobsView.newChildId());
			Status status = getProject().getCommitStatus(commitId, getPullRequest(), refName).get(job.getName());
					
			Link<Void> defLink = new JobDefLink("name", commitId, job.getName()) {

				@Override
				protected Project getProject() {
					return JobListPanel.this.getProject();
				}
						
			};
			defLink.add(new Label("label", job.getName()));
			jobItem.add(defLink);
				
			jobItem.add(new RunJobLink("run", commitId, job.getName(), refName) {

				@Override
				public void onClick(AjaxRequestTarget target) {
					super.onClick(target);
					onRunJob(target);
				}

				@Override
				protected Project getProject() {
					return JobListPanel.this.getProject();
				}

				@Override
				protected PullRequest getPullRequest() {
					return JobListPanel.this.getPullRequest();
				}
				
			});
			
			jobItem.add(new BookmarkablePageLink<Void>("showInList", ProjectBuildsPage.class, 
					ProjectBuildsPage.paramsOf(getProject(), Job.getBuildQuery(commitId, job.getName(), refName, getPullRequest()), 0)) {
				
				@Override
				protected void onConfigure() {
					super.onConfigure();
					setVisible(status != null);
				}
				
			});
			
			IModel<List<Build>> buildsModel = new LoadableDetachableModel<List<Build>>() {

				@Override
				protected List<Build> load() {
					BuildManager buildManager = OneDev.getInstance(BuildManager.class);
					List<Build> builds = new ArrayList<>(buildManager.query(getProject(), commitId, 
							job.getName(), refName, Optional.ofNullable(getPullRequest()), new HashMap<>()));
					builds.sort(Comparator.comparing(Build::getNumber));
					return builds;
				}
				
			};
			jobItem.add(new SimpleBuildListPanel("detail", buildsModel));
			
			jobItem.setOutputMarkupId(true);
			jobsView.add(jobItem);
		}
		
		add(new WebSocketObserver() {
			
			@Override
			public void onObservableChanged(IPartialPageRequestHandler handler) {
				handler.add(component);
			}
			
			@Override
			public Collection<String> getObservables() {
				return getWebSocketObservables();
			}
			
		});
	}
	
	private Collection<String> getWebSocketObservables() {
		return Sets.newHashSet("commit-status:" + getProject().getId() + ":" + commitId.name());
	}

	@Override
	protected void onDetach() {
		accessibleJobsModel.detach();
		super.onDetach();
	}

	@Override
	protected void onConfigure() {
		super.onConfigure();
		setVisible(!jobs.isEmpty());
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new JobListCssResourceReference()));
	}
	
}
