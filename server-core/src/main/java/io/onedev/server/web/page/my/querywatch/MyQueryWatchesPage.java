package io.onedev.server.web.page.my.querywatch;

import static io.onedev.server.web.translation.Translation._T;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.model.User;
import io.onedev.server.web.component.user.querywatch.QueryWatchesPanel;
import io.onedev.server.web.page.my.MyPage;

public class MyQueryWatchesPage extends MyPage {

    private static final String PARAM_TAB = "tab";

    private final String tabName;

    public MyQueryWatchesPage(PageParameters params) {
        super(params);
        if (getUser().isServiceAccount() || getUser().isDisabled())
            throw new IllegalStateException();
        tabName = params.get(PARAM_TAB).toString(QueryWatchesPanel.TAB_ISSUE);
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
        
        add(new QueryWatchesPanel("content", new AbstractReadOnlyModel<>() {

            @Override
            public User getObject() {
                return getUser();
            }

        }, tabName) {

            @Override
            protected void onTabSelected(AjaxRequestTarget target, String tabName) {
                var params = new PageParameters();
                params.add(PARAM_TAB, tabName);
                setResponsePage(MyQueryWatchesPage.class, params);
            }

        });
    }

    @Override
    protected Component newTopbarTitle(String componentId) {
        return new Label(componentId, _T("Query Watches"));
    }

} 