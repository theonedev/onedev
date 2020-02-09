package io.onedev.server.web.behavior;

import java.util.Collection;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;

public abstract class WebSocketObserver extends Behavior {

	protected Component component;
	
	@Override
	public void bind(Component component) {
		super.bind(component);
		this.component = component;
	}

	private static final long serialVersionUID = 1L;

	public abstract Collection<String> getObservables();
	
	public abstract void onObservableChanged(IPartialPageRequestHandler handler);

}
