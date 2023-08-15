package io.onedev.server.web.behavior;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;

import java.util.Collection;
import java.util.HashSet;

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
	
	public void onObservableChanged(IPartialPageRequestHandler handler, 
									Collection<String> changedObservables) {
		if (component.isVisibleInHierarchy())
			handler.add(component);
	}

	public static Collection<String> filterObservables(Collection<String> observingObservables,
													   Collection<String> changedObservables) {
		Collection<String> observingChangedObservables = new HashSet<>();
		for (var observingObservable: observingObservables) {
			for (var changedObservable: changedObservables) {
				if (containsObservable(observingObservable, changedObservable))
					observingChangedObservables.add(changedObservable);
			}
		}
		return observingChangedObservables;
	}
	
	public static boolean containsObservable(String observingObservable, String changedObservable) {
		if (changedObservable.startsWith(observingObservable)) {
			var remaining = changedObservable.substring(observingObservable.length());
			return remaining.length() == 0 || remaining.startsWith(":");
		} else {
			return false;
		}
	}
	
}
