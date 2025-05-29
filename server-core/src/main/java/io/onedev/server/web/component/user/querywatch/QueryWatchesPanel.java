package io.onedev.server.web.component.user.querywatch;

import static io.onedev.server.web.translation.Translation._T;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import io.onedev.server.model.User;
import io.onedev.server.web.component.tabbable.AjaxActionTab;
import io.onedev.server.web.component.tabbable.Tab;
import io.onedev.server.web.component.tabbable.Tabbable;

public abstract class QueryWatchesPanel extends GenericPanel<User> {
    
    public static final String TAB_ISSUE = "issue";

    public static final String TAB_PULL_REQUEST = "pull-request";

    public static final String TAB_BUILD = "build";
    
    public static final String TAB_PACK = "pack";

    public static final String TAB_COMMIT = "commit";

    private final String tabName;

    public QueryWatchesPanel(String id, IModel<User> model, String tabName) {
        super(id, model);
        this.tabName = tabName;
    }    
    
    @Override
    protected void onInitialize() {
        super.onInitialize();
        
        List<Tab> tabs = new ArrayList<>();
        
        var issueTab = new AjaxActionTab(Model.of(_T("Issue")), null) {
            @Override
            protected void onSelect(AjaxRequestTarget target, Component tabLink) {
                onTabSelected(target, TAB_ISSUE);
            }
        };
        tabs.add(issueTab);        
        
        var pullRequestTab = new AjaxActionTab(Model.of(_T("Pull Request")), null) {
            @Override
            protected void onSelect(AjaxRequestTarget target, Component tabLink) {
                onTabSelected(target, TAB_PULL_REQUEST);
            }
        };
        tabs.add(pullRequestTab);

        var buildTab = new AjaxActionTab(Model.of(_T("Build")), null) {
            @Override
            protected void onSelect(AjaxRequestTarget target, Component tabLink) {
                onTabSelected(target, TAB_BUILD);
            }
        };
        tabs.add(buildTab);
        
        var packTab = new AjaxActionTab(Model.of(_T("Pack")), null) {
            @Override
            protected void onSelect(AjaxRequestTarget target, Component tabLink) {
                onTabSelected(target, TAB_PACK);
            }
        };
        tabs.add(packTab);

        var commitTab = new AjaxActionTab(Model.of(_T("Commit")), null) {
            @Override
            protected void onSelect(AjaxRequestTarget target, Component tabLink) {
                onTabSelected(target, TAB_COMMIT);
            }
        };
        tabs.add(commitTab);
        
        if (tabName.equals(TAB_ISSUE)) {
            issueTab.setSelected(true);
            add(new IssueQueryWatchesPanel("content", getModel()));
        } else if (tabName.equals(TAB_PULL_REQUEST)) {
            pullRequestTab.setSelected(true);
            add(new PullRequestQueryWatchesPanel("content", getModel()));
        } else if (tabName.equals(TAB_BUILD)) {
            buildTab.setSelected(true);
            add(new BuildQueryWatchesPanel("content", getModel()));
        } else if (tabName.equals(TAB_PACK)) {
            packTab.setSelected(true);
            add(new PackQueryWatchesPanel("content", getModel()));
        } else if (tabName.equals(TAB_COMMIT)) {
            commitTab.setSelected(true);
            add(new CommitQueryWatchesPanel("content", getModel()));
        } else {
            throw new RuntimeException("Unexpected tab name: " + tabName);
        }
        add(new Tabbable("tabs", tabs));
    }
    
    protected abstract void onTabSelected(AjaxRequestTarget target, String tabName);

}
