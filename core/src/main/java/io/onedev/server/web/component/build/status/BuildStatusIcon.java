package io.onedev.server.web.component.build.status;

import java.util.Collection;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import com.google.common.collect.Sets;

import io.onedev.server.model.Build;
import io.onedev.server.model.Build.Status;
import io.onedev.server.web.behavior.WebSocketObserver;
import io.onedev.server.web.model.EntityModel;

@SuppressWarnings("serial")
public class BuildStatusIcon extends Panel {

	private final IModel<Build> buildModel;
	
	private final boolean withTooltip;
	
	public BuildStatusIcon(String id, Build build, boolean withTooltip) {
		super(id);
		this.buildModel = new EntityModel<Build>(build);
		this.withTooltip = withTooltip;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new WebMarkupContainer("status") {

			@Override
			protected void onComponentTag(ComponentTag tag) {
				super.onComponentTag(tag);

				Build.Status status = getBuild().getStatus();
				String cssClass = "fa fa-fw build-status build-status-" + status.name().toLowerCase();
				if (status == Status.RUNNING)
					cssClass += " fa-spin";

				if (withTooltip) {
					String title;
					
					if (status == Status.WAITING) 
						title = "Waiting for completion of dependency builds";
					else if (status == Status.QUEUEING) 
						title = "Queued due to limited capacity";
					else
						title = StringUtils.capitalize(status.getDisplayName().toLowerCase());
					
					tag.put("title", title);
				}
				
				tag.put("class", cssClass);
			}
			
		});
		
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
				return Sets.newHashSet(Build.getWebSocketObservable(getBuild().getId()));
			}
			
		});
		
		setOutputMarkupPlaceholderTag(true);
	}
	
	protected Build getBuild() {
		return buildModel.getObject();
	}
	
	@Override
	protected void onDetach() {
		buildModel.detach();
		super.onDetach();
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new BuildStatusCssResourceReference()));
	}
	
}
