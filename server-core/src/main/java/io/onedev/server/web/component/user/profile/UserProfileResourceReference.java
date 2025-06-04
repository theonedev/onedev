package io.onedev.server.web.component.user.profile;

import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;

import io.onedev.server.web.asset.echarts.EChartsResourceReference;
import io.onedev.server.web.page.base.BaseDependentCssResourceReference;
import io.onedev.server.web.page.base.BaseDependentResourceReference;

public class UserProfileResourceReference extends BaseDependentResourceReference {

    public UserProfileResourceReference() {
        super(UserProfileResourceReference.class, "user-profile.js");
    }

    @Override
    public List<HeaderItem> getDependencies() {
        var dependencies = super.getDependencies();
        dependencies.add(JavaScriptHeaderItem.forReference(new EChartsResourceReference()));
        dependencies.add(CssHeaderItem.forReference(new BaseDependentCssResourceReference(
            UserProfileResourceReference.class, "user-profile.css")));
        return dependencies;
    }
}