package io.onedev.server.web.page.project.issues.boards;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.LoadableDetachableModel;

import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.web.behavior.WebSocketObserver;

@SuppressWarnings("serial")
abstract class CardCountLabel extends Label {
	
	public CardCountLabel(String id) {
		super(id);
		
		setDefaultModel(new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				return String.valueOf(getCount());
			}
		
		});
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new WebSocketObserver() {
			
			@Override
			public void onObservableChanged(IPartialPageRequestHandler handler) {
				handler.add(component);
			}
			
			@Override
			public Collection<String> getObservables() {
				Set<String> observables = new HashSet<>();
				observables.add(Issue.getListWebSocketObservable(getProject().getId()));
				for (Project project: getProject().getDescendants())
					observables.add(Issue.getListWebSocketObservable(project.getId()));
				return observables;
			}
			
		});
		
		setOutputMarkupId(true);
	}

	protected abstract Project getProject();
	
	protected abstract int getCount();
}
