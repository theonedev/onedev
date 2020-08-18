package io.onedev.server.web.component.build.status;

import java.util.Collection;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import com.google.common.collect.Sets;

import io.onedev.server.model.Build.Status;
import io.onedev.server.web.behavior.WebSocketObserver;
import io.onedev.server.web.component.svg.SpriteImage;

@SuppressWarnings("serial")
public class BuildStatusIcon extends GenericPanel<Status> {

	public BuildStatusIcon(String id, IModel<Status> model) {
		super(id, model);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new SpriteImage("status", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				Status status = BuildStatusIcon.this.getModelObject();
				if (status != null) {
				switch (status ) {
					case SUCCESSFUL:
						return "tick-circle-o";
					case FAILED:
						return "times-circle-o";
					case CANCELLED:
						return "cancel";
					case PENDING:
						return "target";
					case RUNNING:
						return "spin";
					case TIMED_OUT:
						return "clock";
					case WAITING:
						return "pause-circle";
					default:
						throw new RuntimeException("Unexpected build status: " + status);
					}
				} else {
					return "dot";
				}
			}
			
		}).add(AttributeAppender.append("class", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				if (getModelObject() != null)
					return "build-status-" + getModelObject().name().toLowerCase();
				else
					return "build-status-none";
			}
			
		})));
		
		add(new WebSocketObserver() {
			
			@Override
			public void onObservableChanged(IPartialPageRequestHandler handler) {
				handler.add(component);
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
