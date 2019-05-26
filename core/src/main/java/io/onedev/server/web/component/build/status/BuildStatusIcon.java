package io.onedev.server.web.component.build.status;

import java.util.Collection;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.LoadableDetachableModel;

import com.google.common.collect.Sets;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.BuildManager;
import io.onedev.server.model.Build;
import io.onedev.server.model.Build.Status;
import io.onedev.server.web.behavior.WebSocketObserver;

@SuppressWarnings("serial")
public class BuildStatusIcon extends GenericPanel<Build> {

	private final Long buildId;
	
	private final boolean withTooltip;
	
	public BuildStatusIcon(String id, Long buildId, boolean withTooltip) {
		super(id);
		
		this.buildId = buildId;
		setModel(new LoadableDetachableModel<Build>() {

			@Override
			protected Build load() {
				return OneDev.getInstance(BuildManager.class).load(buildId);
			}
			
		});
		this.withTooltip = withTooltip;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new WebMarkupContainer("status") {

			@Override
			protected void onComponentTag(ComponentTag tag) {
				super.onComponentTag(tag);

				Build build = getModelObject();

				Build.Status status = build.getStatus();
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
