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

import io.onedev.server.model.Build.Status;
import io.onedev.server.web.behavior.WebSocketObserver;

@SuppressWarnings("serial")
public class BuildStatusIcon extends GenericPanel<Status> {

	public BuildStatusIcon(String id, IModel<Status> model) {
		super(id, model);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new WebMarkupContainer("status") {

			@Override
			protected void onComponentTag(ComponentTag tag) {
				super.onComponentTag(tag);

				Status status = getModelObject();
				String cssClass = "fa build-status build-status-";
				if (status != null)
					cssClass += status.name().toLowerCase();
				else
					cssClass += "none";
				if (status == Status.RUNNING)
					cssClass += " fa-spin";
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
				/*
				 *  Do not refresh on connection as otherwise project commit status cache will not take
				 *  effect on commit list page 
				 */
				// handler.add(component);
			}
			
			@Override
			public Collection<String> getObservables() {
				return getWebSocketObservables();
			}
			
		});
		
		setOutputMarkupId(true);
	}
	
	protected Collection<String> getWebSocketObservables() {
		return Sets.newHashSet();
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new BuildStatusCssResourceReference()));
	}
	
}
