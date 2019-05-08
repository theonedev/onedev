package io.onedev.server.web.page.project.builds.detail;

import org.apache.wicket.Component;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.link.ResourceLink;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.web.component.build.log.BuildLogPanel;
import io.onedev.server.web.download.BuildLogDownloadResource;
import io.onedev.server.web.download.BuildLogDownloadResourceReference;

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
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(OnDomReadyHeaderItem.forScript("onedev.server.buildDetail.onLogDomReady();"));
	}

	public Component renderOptions(String componentId) {
		Fragment fragment = new Fragment(componentId, "optionsFrag", this);
		fragment.add(new ResourceLink<Void>("download", new BuildLogDownloadResourceReference(), 
				BuildLogDownloadResource.paramsOf(projectModel.getObject(), getBuild().getNumber())));
		return fragment;
	}
}
