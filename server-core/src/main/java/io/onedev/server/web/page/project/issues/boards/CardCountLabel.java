package io.onedev.server.web.page.project.issues.boards;

import com.google.common.collect.Sets;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.web.behavior.ChangeObserver;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.LoadableDetachableModel;

import java.util.Collection;

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
		
		add(new ChangeObserver() {
			
			@Override
			public Collection<String> findObservables() {
				return Sets.newHashSet(Issue.getListChangeObservable(getProject().getId()));
			}
			
		});
		
		setOutputMarkupId(true);
	}

	protected abstract Project getProject();
	
	protected abstract int getCount();
}
