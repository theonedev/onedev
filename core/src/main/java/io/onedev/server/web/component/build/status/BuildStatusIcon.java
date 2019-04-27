package io.onedev.server.web.component.build.status;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;

import io.onedev.server.OneException;
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

				String cssClass = "build-status fa fa-fw " + build.getStatus().name().toLowerCase() + " ";
				String title;
				
				if (build.getStatus() == Status.WAITING) {
					cssClass += "fa-pause";
					title = "Waiting for completion of dependency builds";
				} else if (build.getStatus() == Status.QUEUEING) {
					cssClass += "fa-hourglass-1";
					title = "Build is being queued due to limited capacity";
				} else if (build.getStatus() == Status.IN_ERROR) {
					cssClass += "fa-warning";
					if (build.getStatusMessage() != null)
						title = build.getStatusMessage();
					else
						title = "Build is in error";
				} else if (build.getStatus() == Status.FAILED) {
					cssClass += "fa-times";
					title = "Build is failed";
					if (build.getStatusMessage() != null)
						title = build.getStatusMessage();
					else
						title = "Build is failed";
				} else if (build.getStatus() == Status.RUNNING) {
					cssClass += "fa-circle";
					title = "Build is running";
				} else if (build.getStatus() == Status.SUCCESSFUL) {
					cssClass += "fa-check";
					title = "Build is successful";
				} else if (build.getStatus() == Status.CANCELLED) {
					cssClass += "fa-ban";
					if (build.getStatusMessage() != null)
						title = build.getStatusMessage();
					else
						title = "Build is cancelled";
				} else if (build.getStatus() == Status.TIMED_OUT) {
					cssClass += "fa-clock-o";
					title = "Build timed out";
				} else {
					throw new OneException("Unexpected build status: " + build.getStatus());
				}
				
				tag.put("class", cssClass);
				tag.put("title", title);
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
