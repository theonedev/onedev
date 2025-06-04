package io.onedev.server.web.page.my.profile;

import static io.onedev.server.web.translation.Translation._T;

import java.io.Serializable;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.model.User;
import io.onedev.server.util.DateRange;
import io.onedev.server.web.component.user.profile.UserProfilePanel;
import io.onedev.server.web.page.my.MyPage;

public class MyProfilePage extends MyPage {

    private static final String PARAM_ACTIVITY_DATE_RANGE = "activityDateRange";
    
    private DateRange activityDateRange;
    
    public MyProfilePage(PageParameters params) {
        super(params);

        var activityDateRangeString = params.get(PARAM_ACTIVITY_DATE_RANGE).toString();
        if (activityDateRangeString != null) 
            activityDateRange = DateRange.fromString(activityDateRangeString);
    }

    @Override
    protected Component newTopbarTitle(String componentId) {
        return new Label(componentId, _T("My Profile"));
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();

        add(newUserProfile());
    }

    private Component newUserProfile() {
        return new UserProfilePanel("profile", new AbstractReadOnlyModel<User>() {

            @Override
            public User getObject() {
                return getUser();
            }

        }, activityDateRange) {

            @Override
            protected void onDateRangeChanged(AjaxRequestTarget target, DateRange dateRange) {
                activityDateRange = dateRange;
                var url = RequestCycle.get().urlFor(MyProfilePage.class, paramsOf(activityDateRange));
                pushState(target, url.toString(), activityDateRange);
            }
            
        };
    }

    @Override
    protected void onPopState(AjaxRequestTarget target, Serializable data) {
        super.onPopState(target, data);
        activityDateRange = (DateRange) data;
        var profile = newUserProfile();
        replace(profile);
        target.add(profile);
    }

    public static PageParameters paramsOf(DateRange activityDateRange) {
        var params = new PageParameters();
        params.add(PARAM_ACTIVITY_DATE_RANGE, activityDateRange.toString());
        return params;
    }

}
