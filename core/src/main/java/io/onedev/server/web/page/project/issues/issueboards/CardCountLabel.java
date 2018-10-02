package io.onedev.server.web.page.project.issues.issueboards;

import java.util.Collection;

import javax.annotation.Nullable;

import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.LoadableDetachableModel;

import com.google.common.collect.Sets;

import io.onedev.server.OneDev;
import io.onedev.server.manager.IssueManager;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.issue.IssueBoard;
import io.onedev.server.search.entity.issue.IssueQuery;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.behavior.WebSocketObserver;

@SuppressWarnings("serial")
abstract class CardCountLabel extends Label {
	
	public CardCountLabel(String id) {
		super(id);
		
		setDefaultModel(new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				return String.valueOf(OneDev.getInstance(IssueManager.class).count(getProject(), SecurityUtils.getUser(), getQuery().getCriteria()));
			}
		
		});
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new WebSocketObserver() {
			
			@Override
			public void onObservableChanged(IPartialPageRequestHandler handler, String observable) {
				handler.add(component);
			}
			
			@Override
			public void onConnectionOpened(IPartialPageRequestHandler handler) {
			}
			
			@Override
			public Collection<String> getObservables() {
				return Sets.newHashSet(IssueBoard.getWebSocketObservable(getProject().getId()));
			}
			
		});
		
		setOutputMarkupId(true);
	}

	@Override
	protected void onConfigure() {
		super.onConfigure();
		setVisible(getQuery() != null);
	}

	protected abstract Project getProject();
	
	@Nullable
	protected abstract IssueQuery getQuery();
	
}
