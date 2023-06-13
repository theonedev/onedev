package io.onedev.server.web.component.commit.status;

import com.google.common.collect.Sets;
import io.onedev.server.buildspec.BuildSpec;
import io.onedev.server.buildspec.job.Job;
import io.onedev.server.job.JobAuthorizationContext;
import io.onedev.server.model.Build;
import io.onedev.server.model.Build.Status;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.component.build.status.BuildStatusIcon;
import io.onedev.server.web.component.floating.FloatingPanel;
import io.onedev.server.web.component.job.joblist.JobListPanel;
import io.onedev.server.web.component.link.DropdownLink;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.panel.IMarkupSourcingStrategy;
import org.apache.wicket.markup.html.panel.PanelMarkupSourcingStrategy;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.eclipse.jgit.lib.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

@SuppressWarnings("serial")
public abstract class CommitStatusLink extends DropdownLink {

	private static final Logger logger = LoggerFactory.getLogger(CommitStatusLink.class);
	
	private final ObjectId commitId;
	
	private final String refName;
	
	private final IModel<List<Job>> jobsModel = new LoadableDetachableModel<List<Job>>() {

		@Override
		protected List<Job> load() {
			List<Job> jobs = new ArrayList<>();
			JobAuthorizationContext.push(new JobAuthorizationContext(getProject(), commitId, SecurityUtils.getUser(), getPullRequest()));
			try {
				BuildSpec buildSpec = getProject().getBuildSpec(commitId);
				if (buildSpec != null)
					jobs.addAll(buildSpec.getJobMap().values());
			} catch (Exception e) {
				logger.error("Error retrieving build spec (project: {}, commit: {})", 
						getProject().getPath(), commitId.name(), e);
			} finally {
				JobAuthorizationContext.pop();
			}
			return jobs;
		}
		
	};
	
	private final IModel<Status> statusModel = new LoadableDetachableModel<Status>() {

		@Override
		protected Status load() {
			return Status.getOverallStatus(getProject().getCommitStatuses(commitId, null, getPullRequest(), refName).values());
		}
		
	};
	
	public CommitStatusLink(String id, ObjectId commitId, @Nullable String refName) {
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
		
		add(new BuildStatusIcon("icon", statusModel) {
			
			@Override
			protected Collection<String> getChangeObservables() {
				return CommitStatusLink.this.getChangeObservables();
			}
			
		});
		
		add(AttributeAppender.replace("title", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				Build.Status status = statusModel.getObject();
				if (status != null) {
					String title;
					if (status != Status.SUCCESSFUL)
						title = "Some builds are "; 
					else
						title = "Builds are "; 
					title += status.toString().toLowerCase() + ", click for details";
					return title;
				} else {
					return "No builds";
				}
			}
			
		}));
		
		add(AttributeAppender.append("class", "commit-status text-nowrap"));
	}
	
	protected Collection<String> getChangeObservables() {
		if (!jobsModel.getObject().isEmpty())
			return Sets.newHashSet(Build.getCommitStatusChangeObservable(getProject().getId(), commitId.name()));
		else
			return new HashSet<>();
	}

	@Override
	protected Component newContent(String id, FloatingPanel dropdown) {
		return new JobListPanel(id, commitId, refName, jobsModel.getObject()) {

			@Override
			protected Project getProject() {
				return CommitStatusLink.this.getProject();
			}

			@Override
			protected void onRunJob(AjaxRequestTarget target) {
				dropdown.close();
			}

			@Override
			protected PullRequest getPullRequest() {
				return CommitStatusLink.this.getPullRequest();
			}

			@Override
			protected String getPipeline() {
				return null;
			}
			
		};
	}

	@Override
	protected IMarkupSourcingStrategy newMarkupSourcingStrategy() {
		return new PanelMarkupSourcingStrategy(false);
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
	
}
