package io.onedev.server.web.page.project.builds.detail.log;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.link.ResourceLink;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.component.build.log.BuildLogPanel;
import io.onedev.server.web.page.project.builds.detail.BuildDetailPage;
import io.onedev.server.web.resource.BuildLogResource;
import io.onedev.server.web.resource.BuildLogResourceReference;

@SuppressWarnings("serial")
public class BuildLogPage extends BuildDetailPage {

	public BuildLogPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new BuildLogPanel("log", buildModel));
	}

	@Override
	protected boolean isPermitted() {
		return SecurityUtils.canAccessLog(getBuild());
	}

	public Component renderOptions(String componentId) {
		Fragment fragment = new Fragment(componentId, "optionsFrag", this);
		fragment.add(new ResourceLink<Void>("download", new BuildLogResourceReference(), 
				BuildLogResource.paramsOf(projectModel.getObject(), getBuild().getNumber())));
		return fragment;
	}
}
