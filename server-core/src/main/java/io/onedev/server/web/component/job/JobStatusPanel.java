package io.onedev.server.web.component.job;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.eclipse.jgit.lib.ObjectId;

import com.google.common.collect.Lists;

import io.onedev.server.OneDev;
import io.onedev.server.ci.job.Job;
import io.onedev.server.entitymanager.BuildManager;
import io.onedev.server.model.Build;
import io.onedev.server.model.Build.Status;
import io.onedev.server.model.Project;
import io.onedev.server.web.component.build.simplelist.SimpleBuildListPanel;
import io.onedev.server.web.component.build.status.BuildStatusIcon;
import io.onedev.server.web.component.commit.status.CommitStatusCssResourceReference;
import io.onedev.server.web.component.floating.FloatingPanel;
import io.onedev.server.web.component.link.DropdownLink;
import io.onedev.server.web.model.EntityModel;
import io.onedev.server.web.page.project.builds.ProjectBuildsPage;

@SuppressWarnings("serial")
public class JobStatusPanel extends Panel {

	private final IModel<Project> projectModel;
	
	private final ObjectId commitId;
	
	private final String jobName;
	
	private final IModel<Status> statusModel = new LoadableDetachableModel<Status>() {

		@Override
		protected Status load() {
			return getProject().getCommitStatus(commitId).get(jobName);
		}
		
	};
	
	public JobStatusPanel(String id, Project project, ObjectId commitId, String jobName) {
		super(id);
		
		this.projectModel = new EntityModel<Project>(project);
		this.commitId = commitId;
		this.jobName = jobName;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		DropdownLink link = new DropdownLink("link") {

			@Override
			protected Component newContent(String id, FloatingPanel dropdown) {
				return new SimpleBuildListPanel(id, new LoadableDetachableModel<List<Build>>() {

					@Override
					protected List<Build> load() {
						BuildManager buildManager = OneDev.getInstance(BuildManager.class);
						List<Build> builds = new ArrayList<>(buildManager.query(getProject(), commitId, jobName));
						builds.sort(Comparator.comparing(Build::getNumber));
						return builds;
					}
					
				}) {

					@Override
					protected Component newListLink(String componentId) {
						return new BookmarkablePageLink<Void>(componentId, ProjectBuildsPage.class, 
								ProjectBuildsPage.paramsOf(getProject(), Job.getBuildQuery(commitId, jobName), 0)) {
							
							@Override
							protected void onConfigure() {
								super.onConfigure();
								setVisible(!getBuilds().isEmpty());
							}
							
						};
					}

				};
			}
			
		};
		link.add(new BuildStatusIcon("icon", statusModel) {

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
				return Lists.newArrayList("job-status:" + getProject().getId() + ":" + commitId.name() + ":" + jobName);
			}

		});
		add(link);
	}
	
	private Project getProject() {
		return projectModel.getObject();
	}
	
	@Override
	protected void onDetach() {
		projectModel.detach();
		statusModel.detach();
		super.onDetach();
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new CommitStatusCssResourceReference()));
	}
	
}
