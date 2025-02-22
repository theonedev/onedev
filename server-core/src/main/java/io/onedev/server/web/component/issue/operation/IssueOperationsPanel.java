package io.onedev.server.web.component.issue.operation;

import com.google.common.collect.Lists;
import io.onedev.server.model.Issue;
import io.onedev.server.web.behavior.ChangeObserver;
import io.onedev.server.web.component.issue.IssueStateBadge;
import io.onedev.server.web.page.project.issues.create.NewIssuePage;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.LoadableDetachableModel;

import java.util.Collection;

public abstract class IssueOperationsPanel extends Panel {
	
	public IssueOperationsPanel(String id) {
		super(id);
	}
	
	@Override
	protected void onBeforeRender() {
		var transitionMenuLink = new TransitionMenuLink("state") {
			
			@Override
			protected Issue getIssue() {
				return IssueOperationsPanel.this.getIssue();
			}
			
		};
		addOrReplace(transitionMenuLink);
		
		transitionMenuLink.add(new IssueStateBadge("name", new LoadableDetachableModel<>() {
			@Override
			protected Issue load() {
				return getIssue();
			}
		}, true));
		
		addOrReplace(new BookmarkablePageLink<Void>("newIssue", NewIssuePage.class, 
				NewIssuePage.paramsOf(getIssue().getProject())));
		
		super.onBeforeRender();
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new ChangeObserver() {
			
			@Override
			public Collection<String> findObservables() {
				return Lists.newArrayList(Issue.getDetailChangeObservable(getIssue().getId()));
			}
			
		});
		
		setOutputMarkupId(true);
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new IssueOperationsCssResourceReference()));
	}

	protected abstract Issue getIssue();
	
}
