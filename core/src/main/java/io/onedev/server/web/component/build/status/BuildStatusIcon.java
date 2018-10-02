package io.onedev.server.web.component.build.status;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;

import io.onedev.server.model.Build;
import io.onedev.server.model.Build.Status;

@SuppressWarnings("serial")
public class BuildStatusIcon extends GenericPanel<Build> {

	public BuildStatusIcon(String id, IModel<Build> model) {
		super(id, model);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new WebMarkupContainer("status") {

			@Override
			protected void onComponentTag(ComponentTag tag) {
				super.onComponentTag(tag);

				Build build = getModelObject();
				if (build.getStatus() == Status.ERROR) {
					tag.put("class", "build-status error fa fa-warning");
					tag.put("title", "Build is in error");
				} else if (build.getStatus() == Status.FAILURE) {
					tag.put("class", "build-status failure fa fa-times");
					tag.put("title", "Build is failed");
				} else if (build.getStatus() == Status.RUNNING) {
					tag.put("class", "build-status running fa fa-circle");
					tag.put("title", "Build is running");
				} else if (build.getStatus() == Status.SUCCESS) {
					tag.put("class", "build-status success fa fa-check");
					tag.put("title", "Build is successful");
				}
			}
			
		});
		
		setOutputMarkupPlaceholderTag(true);
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new BuildCssResourceReference()));
	}
	
}
