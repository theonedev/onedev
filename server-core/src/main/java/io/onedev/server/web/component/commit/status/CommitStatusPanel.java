package io.onedev.server.web.component.commit.status;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.eclipse.jgit.lib.ObjectId;

import com.google.common.collect.Lists;

import io.onedev.server.OneDev;
import io.onedev.server.ci.CISpec;
import io.onedev.server.ci.job.Job;
import io.onedev.server.entitymanager.BuildManager;
import io.onedev.server.model.Build;
import io.onedev.server.model.Build.Status;
import io.onedev.server.model.Project;
import io.onedev.server.web.component.build.simplelist.SimpleBuildListPanel;
import io.onedev.server.web.component.build.status.BuildStatusIcon;
import io.onedev.server.web.component.floating.FloatingPanel;
import io.onedev.server.web.component.job.JobDefLink;
import io.onedev.server.web.component.job.RunJobLink;
import io.onedev.server.web.component.link.DropdownLink;
import io.onedev.server.web.model.EntityModel;
import io.onedev.server.web.page.project.builds.ProjectBuildsPage;

@SuppressWarnings("serial")
public class CommitStatusPanel extends Panel {

	private final IModel<Project> projectModel;
	
	private final ObjectId commitId;
	
	private final IModel<List<Job>> jobsModel = new LoadableDetachableModel<List<Job>>() {

		@Override
		protected List<Job> load() {
			CISpec ciSpec = getProject().getCISpec(commitId);
			if (ciSpec != null)
				return ciSpec.getSortedJobs();
			else
				return new ArrayList<>();
		}
		
	};
	
	public CommitStatusPanel(String id, Project project, ObjectId commitId) {
		super(id);
		
		this.projectModel = new EntityModel<Project>(project);
		this.commitId = commitId;
	}
	
	private Project getProject() {
		return projectModel.getObject();
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		DropdownLink link = new DropdownLink("link") {

			private boolean isShowJobDetail(WebMarkupContainer jobItem) {
				return (boolean) jobItem.getDefaultModelObject();
			}
			
			@Override
			protected Component newContent(String id, FloatingPanel dropdown) {
				Fragment fragment = new Fragment(id, "detailFrag", CommitStatusPanel.this);
				
				RepeatingView jobsView = new RepeatingView("jobs");
				fragment.add(jobsView);
				for (Job job: jobsModel.getObject()) {
					WebMarkupContainer jobItem = new WebMarkupContainer(jobsView.newChildId(), Model.of(false));
					AjaxLink<Void> detailLink = new AjaxLink<Void>("jobDetailToggle") {

						@Override
						public void onClick(AjaxRequestTarget target) {
							jobItem.setDefaultModelObject(!isShowJobDetail(jobItem));
							target.add(jobItem);
						}
						
					};
					Status status = getProject().getCommitStatus(commitId).get(job.getName());
					detailLink.add(new BuildStatusIcon("icon", Model.of(status)) {
						
						@Override
						protected String getTooltip(Status status) {
							String title;
							if (status != null) {
								if (status != Status.SUCCESSFUL)
									title = "Some builds in job are "; 
								else
									title = "Builds in job are "; 
								title += status.getDisplayName().toLowerCase() + ", click to toggle details";
							} else {
								title = "No builds in job";
							}
							return title;
						}
						
					});
					jobItem.add(detailLink);
					
					Link<Void> defLink = new JobDefLink("jobDef", getProject(), commitId, job.getName());
					defLink.add(new Label("label", job.getName()));
					jobItem.add(defLink);
					
					jobItem.add(new RunJobLink("runJob", getProject(), commitId, job.getName()) {

						@Override
						public void onClick(AjaxRequestTarget target) {
							super.onClick(target);
							dropdown.close();
						}
						
					});
					
					jobItem.add(new BookmarkablePageLink<Void>("showInList", ProjectBuildsPage.class, 
							ProjectBuildsPage.paramsOf(getProject(), Job.getBuildQuery(commitId, job.getName()), 0)) {
						
						@Override
						protected void onConfigure() {
							super.onConfigure();
							setVisible(status != null && isShowJobDetail(jobItem));
						}
						
					});
					
					jobItem.add(new SimpleBuildListPanel("jobDetail", new LoadableDetachableModel<List<Build>>() {

						@Override
						protected List<Build> load() {
							BuildManager buildManager = OneDev.getInstance(BuildManager.class);
							List<Build> builds = new ArrayList<>(buildManager.query(getProject(), commitId, job.getName()));
							builds.sort(Comparator.comparing(Build::getNumber));
							return builds;
						}
						
					}) {

						@Override
						protected void onConfigure() {
							super.onConfigure();
							setVisible(isShowJobDetail(jobItem));
						}
						
					});
					
					jobItem.setOutputMarkupId(true);
					jobsView.add(jobItem);
				}
				
				return fragment;
			}
			
		};
		link.add(new BuildStatusIcon("icon", new LoadableDetachableModel<Status>() {

			@Override
			protected Status load() {
				return Status.getOverallStatus(getProject().getCommitStatus(commitId).values());
			}
			
		}) {
			
			@Override
			protected String getTooltip(Status status) {
				String title;
				if (status != null) {
					if (status != Status.SUCCESSFUL)
						title = "Some builds are "; 
					else
						title = "Builds are "; 
					title += status.getDisplayName().toLowerCase() + ", click for details";
				} else {
					title = "No builds";
				}
				return title;
			}

			@Override
			protected Collection<String> getWebSocketObservables() {
				return Lists.newArrayList("commit-status:" + getProject().getId() + ":" + commitId.name());
			}
			
		});
		add(link);
	}

	@Override
	protected void onDetach() {
		projectModel.detach();
		jobsModel.detach();
		super.onDetach();
	}

	@Override
	protected void onConfigure() {
		super.onConfigure();
		setVisible(!jobsModel.getObject().isEmpty());
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new CommitStatusCssResourceReference()));
	}
	
}
