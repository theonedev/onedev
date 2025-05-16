package io.onedev.server.web.component.issue.tabs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;

import com.google.common.collect.Lists;

import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.search.entity.build.BuildQuery;
import io.onedev.server.search.entity.build.FixedIssueCriteria;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.behavior.ChangeObserver;
import io.onedev.server.web.component.build.list.BuildListPanel;
import io.onedev.server.web.component.issue.activities.IssueActivitiesPanel;
import io.onedev.server.web.component.issue.authorizations.IssueAuthorizationsPanel;
import io.onedev.server.web.component.issue.commits.IssueCommitsPanel;
import io.onedev.server.web.component.issue.pullrequests.IssuePullRequestsPanel;
import io.onedev.server.web.component.tabbable.AjaxActionTab;
import io.onedev.server.web.component.tabbable.Tab;
import io.onedev.server.web.component.tabbable.Tabbable;

public class IssueTabsPanel extends GenericPanel<Issue> {

    private static final String TAB_CONTENT_ID = "tabContent";

    private IssueActivitiesPanel activities;

    public IssueTabsPanel(String id, IModel<Issue> model) {
        super(id, model);
    }

	private IssueActivitiesPanel newActivitiesPanel() {
		IssueActivitiesPanel activities = new IssueActivitiesPanel(TAB_CONTENT_ID) {

			@Override
			protected Issue getIssue() {
				return IssueTabsPanel.this.getIssue();
			}
			
		};
		activities.setOutputMarkupId(true);
		return activities;
	}

    @Override
    protected void onInitialize() {
        super.onInitialize();

		add(new Tabbable("tabs", new LoadableDetachableModel<>() {

			@Override
			protected List<? extends Tab> load() {
				List<Tab> tabs = new ArrayList<>();

				tabs.add(new AjaxActionTab(Model.of("Activities")) {

					@Override
					protected void onSelect(AjaxRequestTarget target, Component tabLink) {
						activities = newActivitiesPanel();
						IssueTabsPanel.this.replace(activities);
						target.add(activities);
					}

                    @Override
                    public boolean isSelected() {
                        return IssueTabsPanel.this.get(TAB_CONTENT_ID) instanceof IssueActivitiesPanel;
                    }

					@Override
					protected Component renderOptions(String componentId) {
						return activities.renderOptions(componentId);
					}
					
				});

				if (!getIssue().getFixCommits(false).isEmpty()) {
					if (SecurityUtils.canReadCode(getIssue().getProject())) {
						tabs.add(new AjaxActionTab(Model.of("Fixing Commits")) {
		
							@Override
							protected void onSelect(AjaxRequestTarget target, Component tabLink) {
								Component content = new IssueCommitsPanel(TAB_CONTENT_ID, IssueTabsPanel.this.getModel());
								content.setOutputMarkupId(true);
								IssueTabsPanel.this.replace(content);
								target.add(content);
							}
							
                            @Override
                            public boolean isSelected() {
                                return IssueTabsPanel.this.get(TAB_CONTENT_ID) instanceof IssueCommitsPanel;
                            }
        
						});
						if (!getIssue().getPullRequests().isEmpty()) {
							tabs.add(new AjaxActionTab(Model.of("Pull Requests")) {
		
								@Override
								protected void onSelect(AjaxRequestTarget target, Component tabLink) {
									Component content = new IssuePullRequestsPanel(TAB_CONTENT_ID, new AbstractReadOnlyModel<Issue>() {
		
										@Override
										public Issue getObject() {
											return getIssue();
										}
										
									});
									IssueTabsPanel.this.replace(content);
									target.add(content);
								}
								
                                @Override
                                public boolean isSelected() {
                                    return IssueTabsPanel.this.get(TAB_CONTENT_ID) instanceof IssuePullRequestsPanel;
                                }
            
							});
						}
					}
					
					tabs.add(new AjaxActionTab(Model.of("Fixing Builds")) {
		
						@Override
						protected void onSelect(AjaxRequestTarget target, Component tabLink) {
							Component content = new BuildListPanel(TAB_CONTENT_ID, Model.of((String)null), true, true) {
		
								@Override
								protected BuildQuery getBaseQuery() {
									return new BuildQuery(new FixedIssueCriteria(getIssue()), new ArrayList<>());
								}
		
								@Override
								protected Project getProject() {
									return getIssue().getProject();
								}
		
							}.setOutputMarkupId(true);
							
							IssueTabsPanel.this.replace(content);
							target.add(content);
						}

                        @Override
                        public boolean isSelected() {
                            return IssueTabsPanel.this.get(TAB_CONTENT_ID) instanceof BuildListPanel;
                        }
						
					});			
				}
		
				if (getIssue().isConfidential() && SecurityUtils.canModifyIssue(getIssue())) {
					tabs.add(new AjaxActionTab(Model.of("Authorizations")) {
		
						@Override
						protected void onSelect(AjaxRequestTarget target, Component tabLink) {
							Component content = new IssueAuthorizationsPanel(TAB_CONTENT_ID) {
		
								@Override
								protected Issue getIssue() {
									return IssueTabsPanel.this.getIssue();
								}
		
							}.setOutputMarkupId(true);
							
							IssueTabsPanel.this.replace(content);
							target.add(content);
						}

                        @Override
                        public boolean isSelected() {
                            return IssueTabsPanel.this.get(TAB_CONTENT_ID) instanceof IssueAuthorizationsPanel;
                        }
						
					});
				}
						
				return tabs;
			}

		}) {
			@Override
			public void onInitialize() {
				super.onInitialize();
				
				add(new ChangeObserver() {

					@Override
					public Collection<String> findObservables() {
						return Lists.newArrayList(Issue.getDetailChangeObservable(getIssue().getId()));
					}

				});
				
				setOutputMarkupId(true);
			}
		});

		add(activities = newActivitiesPanel());		        
    }

    private Issue getIssue() {
        return getModelObject();
    }

}
