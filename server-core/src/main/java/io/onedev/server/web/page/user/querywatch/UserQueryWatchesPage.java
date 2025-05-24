package io.onedev.server.web.page.user.querywatch;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.web.component.user.querywatch.QueryWatchesPanel;
import io.onedev.server.web.page.user.UserPage;

public class UserQueryWatchesPage extends UserPage {

    private static final String PARAM_TAB = "tab";

    private final String tabName;

    public UserQueryWatchesPage(PageParameters params) {
        super(params);
        if (getUser().isServiceAccount() || getUser().isDisabled())
            throw new IllegalStateException();
        tabName = params.get(PARAM_TAB).toString(QueryWatchesPanel.TAB_ISSUE);
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();

        add(new QueryWatchesPanel("content", userModel, tabName) {

            @Override
            protected void onTabSelected(AjaxRequestTarget target, String tabName) {
                var params = paramsOf(userModel.getObject());
                params.add(PARAM_TAB, tabName);
                setResponsePage(UserQueryWatchesPage.class, params);
            }

        });
    }

} 