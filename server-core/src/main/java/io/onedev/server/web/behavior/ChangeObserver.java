package io.onedev.server.web.behavior;

import java.util.Collection;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;

public abstract class ChangeObserver extends Behavior {
	
	private static final long serialVersionUID = 1L;
	
	private Collection<String> observables;
	
	protected Component component;
	
	@Override
	public void bind(Component component) {
		super.bind(component);
		this.component = component;
		component.setOutputMarkupId(true);
		observables = findObservables();
	}

	@Override
	public void beforeRender(Component component) {
		observables = findObservables();
		super.beforeRender(component);
	}

	protected abstract Collection<String> findObservables();
	
	public final Collection<String> getObservables() {
		return observables;
	}
	
	public void onObservableChanged(IPartialPageRequestHandler handler) {
		if (component.isVisibleInHierarchy())
			handler.add(component);
	}

}
