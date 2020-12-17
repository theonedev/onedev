package io.onedev.server.web.component.commit.status;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.eclipse.jgit.lib.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

import io.onedev.server.buildspec.BuildSpec;
import io.onedev.server.buildspec.job.Job;
import io.onedev.server.model.Build;
import io.onedev.server.model.Build.Status;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.web.component.build.status.BuildStatusIcon;
import io.onedev.server.web.component.floating.FloatingPanel;
import io.onedev.server.web.component.job.joblist.JobListPanel;
import io.onedev.server.web.component.link.DropdownLink;

@SuppressWarnings("serial")
public abstract class CommitStatusPanel extends Panel {

	private static final Logger logger = LoggerFactory.getLogger(CommitStatusPanel.class);
	
	private final ObjectId commitId;
	
	private final String refName;
	
	private final IModel<List<Job>> jobsModel = new LoadableDetachableModel<List<Job>>() {

		@Override
		protected List<Job> load() {
			List<Job> jobs = new ArrayList<>();
			try {
				BuildSpec buildSpec = getProject().getBuildSpec(commitId);
				if (buildSpec != null)
					jobs.addAll(buildSpec.getJobs());
			} catch (Exception e) {
				logger.error("Error retrieving build spec (project: {}, commit: {})", 
						getProject().getName(), commitId.name(), e);
			}
			return jobs;
		}
		
	};
	
	private final IModel<Status> statusModel = new LoadableDetachableModel<Status>() {

		@Override
		protected Status load() {
			return Status.getOverallStatus(getProject().getCommitStatus(commitId, getPullRequest(), refName).values());
		}
		
	};
	
	public CommitStatusPanel(String id, ObjectId commitId, @Nullable String refName) {
		super(id);
		this.commitId = commitId;
		this.refName = refName;
	}
	
	protected abstract Project getProject();
	
	@Nullable
	protected abstract PullRequest getPullRequest();

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		DropdownLink statusLink = new DropdownLink("status") {

			@Override
			protected Component newContent(String id, FloatingPanel dropdown) {
				return new JobListPanel(id, commitId, refName, jobsModel.getObject()) {

					@Override
					protected Project getProject() {
						return CommitStatusPanel.this.getProject();
					}

					@Override
					protected void onRunJob(AjaxRequestTarget target) {
						dropdown.close();
					}

					@Override
					protected PullRequest getPullRequest() {
						return CommitStatusPanel.this.getPullRequest();
					}
					
				};
			}

			@Override
			protected void onComponentTag(ComponentTag tag) {
				super.onComponentTag(tag);
				
				String cssClasses = "commit-status text-nowrap ";
				String title;
				Build.Status status = statusModel.getObject();
				if (status != null) {
					if (status != Status.SUCCESSFUL)
						title = "Some builds are "; 
					else
						title = "Builds are "; 
					title += status.getDisplayName().toLowerCase() + ", click for details";
				} else {
					title = "No builds";
				}
				if (getCssClasses() != null)
					cssClasses += getCssClasses();
				tag.put("class", cssClasses);
				tag.put("title", title);
			}
			
		};
		statusLink.add(new BuildStatusIcon("icon", statusModel) {
			
			@Override
			protected Collection<String> getWebSocketObservables() {
				return CommitStatusPanel.this.getWebSocketObservables();
			}
			
		});
		add(statusLink);
	}
	
	private Collection<String> getWebSocketObservables() {
		return Sets.newHashSet("commit-status:" + getProject().getId() + ":" + commitId.name());
	}

	@Override
	protected void onDetach() {
		jobsModel.detach();
		statusModel.detach();
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
	
	@Nullable
	protected String getCssClasses() {
		return null;
	}
	
}
