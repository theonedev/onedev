package io.onedev.server.ee.dashboard.widgets.projectoverview;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;

import com.google.common.collect.Lists;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.BuildManager;
import io.onedev.server.entitymanager.IssueManager;
import io.onedev.server.entitymanager.PullRequestManager;
import io.onedev.server.model.Build;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequest.Status;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.ProjectBuildStats;
import io.onedev.server.util.ProjectIssueStats;
import io.onedev.server.util.ProjectPullRequestStats;
import io.onedev.server.web.component.markdown.MarkdownViewer;
import io.onedev.server.web.component.project.stats.build.BuildStatsPanel;
import io.onedev.server.web.component.project.stats.code.CodeStatsPanel;
import io.onedev.server.web.component.project.stats.issue.IssueStatsPanel;
import io.onedev.server.web.component.project.stats.pullrequest.PullRequestStatsPanel;

@SuppressWarnings("serial")
public class ProjectOverviewPanel extends GenericPanel<Project> {

	public ProjectOverviewPanel(String id, IModel<Project> model) {
		super(id, model);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		if (getProject().getDescription() != null)
			add(new MarkdownViewer("description", Model.of(getProject().getDescription()), null));
		else 
			add(new WebMarkupContainer("description").setVisible(false));
		
        if (getProject().isCodeManagement() && SecurityUtils.canReadCode(getProject())) { 
        	add(new CodeStatsPanel("codeStats", getModel()));
        	add(new PullRequestStatsPanel("pullRequestStats", getModel(), new LoadableDetachableModel<Map<PullRequest.Status, Long>>() {

				@Override
				protected Map<Status, Long> load() {
					Map<PullRequest.Status, Long> statusCount = new LinkedHashMap<>();
					for (ProjectPullRequestStats stats: OneDev.getInstance(PullRequestManager.class).queryStats(Lists.newArrayList(getProject()))) {
						statusCount.put(stats.getPullRequestStatus(), stats.getStatusCount());
					}
					return statusCount;
				}
        		
        	}));
        } else {
        	add(new WebMarkupContainer("codeStats").setVisible(false));
        	add(new WebMarkupContainer("pullRequestStats").setVisible(false));
        }
        
        if (getProject().isIssueManagement()) {
        	add(new IssueStatsPanel("issueStats", getModel(), new LoadableDetachableModel<Map<Integer, Long>>() {

				@Override
				protected Map<Integer, Long> load() {
					Map<Integer, Long> stateCount = new LinkedHashMap<>();
					for (ProjectIssueStats stats: OneDev.getInstance(IssueManager.class).queryStats(Lists.newArrayList(getProject()))) {
						stateCount.put(stats.getStateOrdinal(), stats.getStateCount());
					}
					return stateCount;
				}
        		
        	}));
        } else {
        	add(new WebMarkupContainer("issueStats").setVisible(false));
        }
        
    	add(new BuildStatsPanel("buildStats", getModel(), new LoadableDetachableModel<Map<Build.Status, Long>>() {

			@Override
			protected Map<Build.Status, Long> load() {
				Map<Build.Status, Long> statusCount = new LinkedHashMap<>();
				for (ProjectBuildStats stats: OneDev.getInstance(BuildManager.class).queryStats(Lists.newArrayList(getProject()))) {
					statusCount.put(stats.getBuildStatus(), stats.getStatusCount());
				}
				return statusCount;
			}
    		
    	}));
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new ProjectOverviewCssResourceReference()));
	}

	private Project getProject() {
		return getModelObject();
	}
	
}
