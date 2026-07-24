package io.onedev.server.web.component.job.joblist;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.eclipse.jgit.lib.ObjectId;
import org.jspecify.annotations.Nullable;

import com.google.common.collect.Sets;

import io.onedev.server.buildspec.job.Job;
import io.onedev.server.model.Build;
import io.onedev.server.model.Build.Status;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.service.BuildService;
import io.onedev.server.util.ProjectScopedCommit;
import io.onedev.server.util.ProjectScopedCommitAware;
import io.onedev.server.web.behavior.ChangeObserver;
import io.onedev.server.web.behavior.InputChangeBehavior;
import io.onedev.server.web.component.build.minilist.MiniBuildListPanel;
import io.onedev.server.web.component.job.JobDefLink;
import io.onedev.server.web.component.job.RunJobLink;
import io.onedev.server.web.page.project.builds.ProjectBuildsPage;

public abstract class JobListPanel extends Panel implements ProjectScopedCommitAware {

	@Inject
	private BuildService buildService;

	private final ObjectId commitId;
	
	private final String refName;
	
	private final List<Job> jobs;

	private WebMarkupContainer jobsContainer;

	private WebMarkupContainer noJobsContainer;

	private TextField<String> searchField;

	private String searchInput;
		
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

		searchField = new TextField<>("search", Model.of(""));
		searchField.setOutputMarkupId(true);
		add(searchField);
		searchField.add(new InputChangeBehavior() {

			@Override
			protected void onInputChange(AjaxRequestTarget target) {
				searchInput = searchField.getInput();
				target.add(jobsContainer);
				target.add(noJobsContainer);
			}

		});
		
		jobsContainer = new WebMarkupContainer("jobs") {

			@Override
			protected void onBeforeRender() {
				RepeatingView jobsView = new RepeatingView("jobs");
				for (Job job: getFilteredJobs()) {
					WebMarkupContainer jobItem = new WebMarkupContainer(jobsView.newChildId());
					Status status = getProject().getCommitStatuses(commitId, getPullRequest(), refName).get(job.getName());
							
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
							List<Build> builds = new ArrayList<>(buildService.query(getProject(), 
									commitId, job.getName(), refName, Optional.ofNullable(getPullRequest()), 
									null, new HashMap<>()));
							builds.sort(Comparator.comparing(Build::getNumber));
							return builds;
						}
						
					};
					jobItem.add(new MiniBuildListPanel("detail", buildsModel));
					
					jobItem.setOutputMarkupId(true);
					jobsView.add(jobItem);
				}
				addOrReplace(jobsView);
				super.onBeforeRender();
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(!getFilteredJobs().isEmpty());
			}
			
		};
		jobsContainer.setOutputMarkupId(true);
		add(jobsContainer);

		noJobsContainer = new WebMarkupContainer("noJobs") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getFilteredJobs().isEmpty());
			}
			
		};
		noJobsContainer.setOutputMarkupPlaceholderTag(true);
		add(noJobsContainer);
		
		add(new ChangeObserver() {
			
			@Override
			public Collection<String> findObservables() {
				return getChangeObservables();
			}
			
		});
	}

	private List<Job> getFilteredJobs() {
		if (searchInput == null || searchInput.isBlank())
			return jobs;
		String query = searchInput.toLowerCase();
		List<Job> filtered = new ArrayList<>();
		for (Job job : jobs) {
			if (job.getName().toLowerCase().contains(query))
				filtered.add(job);
		}
		return filtered;
	}
	
	private Collection<String> getChangeObservables() {
		return Sets.newHashSet(Build.getCommitStatusChangeObservable(getProject().getId(), commitId.name()));
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
		String script = String.format("$('#%s').focus();", searchField.getMarkupId());
		response.render(OnDomReadyHeaderItem.forScript(script));
	}
	
	@Override
	public ProjectScopedCommit getProjectScopedCommit() {
		return new ProjectScopedCommit(getProject(), commitId);
	}
	
}
