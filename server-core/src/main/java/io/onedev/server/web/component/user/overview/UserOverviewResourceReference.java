package io.onedev.server.web.component.user.overview;

import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;

import io.onedev.server.web.asset.echarts.EChartsResourceReference;
import io.onedev.server.web.page.base.BaseDependentCssResourceReference;
import io.onedev.server.web.page.base.BaseDependentResourceReference;

public class UserOverviewResourceReference extends BaseDependentResourceReference {

    public UserOverviewResourceReference() {
        super(UserOverviewResourceReference.class, "user-overview.js");
    }

    @Override
    public List<HeaderItem> getDependencies() {
        var dependencies = super.getDependencies();
        dependencies.add(JavaScriptHeaderItem.forReference(new EChartsResourceReference()));
        dependencies.add(CssHeaderItem.forReference(new BaseDependentCssResourceReference(
            UserOverviewResourceReference.class, "user-overview.css")));
        return dependencies;
    }
}