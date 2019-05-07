package io.onedev.server.web.component.build.status;

import java.util.Collection;

import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;

import com.google.common.collect.Sets;

import io.onedev.server.model.Build;
import io.onedev.server.model.Build.Status;
import io.onedev.server.web.behavior.WebSocketObserver;

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

				String cssClass = "fa fa-fw build-status build-status-" + build.getStatus().name().toLowerCase();
				if (build.getStatus() == Status.RUNNING)
					cssClass += " fa-spin";

				String title;
				
				if (build.getStatus() == Status.WAITING) 
					title = "Waiting for completion of dependency builds";
				else if (build.getStatus() == Status.QUEUEING) 
					title = "Build is being queued due to limited capacity";
				else
					title = "Build is " + build.getStatus().getTitle().toLowerCase();
				
				tag.put("class", cssClass);
				tag.put("title", title);
			}
			
		});
		
		Long buildId = getModelObject().getId();
		
		add(new WebSocketObserver() {
			
			@Override
			public void onObservableChanged(IPartialPageRequestHandler handler, String observable) {
				handler.add(component);
			}
			
			@Override
			public void onConnectionOpened(IPartialPageRequestHandler handler) {
				handler.add(component);
			}
			
			@Override
			public Collection<String> getObservables() {
				return Sets.newHashSet(Build.getWebSocketObservable(buildId));
			}
			
		});
		
		setOutputMarkupPlaceholderTag(true);
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new BuildStatusCssResourceReference()));
	}
	
}
