package io.onedev.server.web.page.project.issues.boards;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.apache.wicket.feedback.FencedFeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.IssueManager;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.search.entity.issue.IssueQuery;
import io.onedev.server.web.WebConstants;
import io.onedev.server.web.behavior.WebSocketObserver;
import io.onedev.server.web.behavior.infinitescroll.InfiniteScrollBehavior;
import io.onedev.server.web.util.Cursor;

@SuppressWarnings("serial")
abstract class CardListPanel extends Panel {

	public CardListPanel(String id) {
		super(id);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(new FencedFeedbackPanel("feedback", this));
		
		RepeatingView cardsView = new RepeatingView("cards");
		int index = 0;
		try {
			List<Issue> issues = queryIssues(0, WebConstants.PAGE_SIZE);
			for (Issue issue: issues) {
				Long issueId = issue.getId();
				IModel<Issue> model = new LoadableDetachableModel<Issue>() {
	
					@Override
					protected Issue load() {
						return OneDev.getInstance(IssueManager.class).load(issueId);
					}
					
				};
				int cardOffset = index;
				cardsView.add(new BoardCardPanel(cardsView.newChildId(), model) {
	
					@Override
					protected Cursor getCursor() {
						IssueQuery query = getQuery();
						if (query != null)
							return new Cursor(query.toString(), getCardCount(), cardOffset, getProject());
						else
							return null;
					}
	
					@Override
					protected Project getProject() {
						return CardListPanel.this.getProject();
					}
					
				});
				index++;
			}
		} catch (ExplicitException e) {
			error(e.getMessage());
		}
		add(cardsView);
		
		InfiniteScrollBehavior behavior;
		add(behavior = new InfiniteScrollBehavior(WebConstants.PAGE_SIZE) {

			@Override
			protected String getItemSelector() {
				return ">.board-card";
			}

			@Override
			protected void appendMore(AjaxRequestTarget target, int offset, int count) {
				int index = offset;
				for (Issue issue: queryIssues(offset, count)) {
					Long issueId = issue.getId();
					IModel<Issue> model = new LoadableDetachableModel<Issue>() {

						@Override
						protected Issue load() {
							return OneDev.getInstance(IssueManager.class).load(issueId);
						}
						
					};
					int cardOffset = index;
					BoardCardPanel card = new BoardCardPanel(cardsView.newChildId(), model) {

						@Override
						protected Cursor getCursor() {
							IssueQuery query = getQuery();
							if (query != null)
								return new Cursor(query.toString(), getCardCount(), cardOffset, getProject());
							else
								return null;
						}

						@Override
						protected Project getProject() {
							return CardListPanel.this.getProject();
						}
						
					};
					cardsView.add(card);
					String script = String.format("$('#%s').append('<div id=\"%s\"></div>');", 
							getMarkupId(), card.getMarkupId());
					target.prependJavaScript(script);
					target.add(card);
					index++;
				}
			}
			
		});
		
		add(new WebSocketObserver() {
			
			@Override
			public void onObservableChanged(IPartialPageRequestHandler handler) {
				behavior.refresh(handler);
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
		
	}
	
	private IssueManager getIssueManager() {
		return OneDev.getInstance(IssueManager.class);
	}

	private List<Issue> queryIssues(int offset, int count) {
		if (getQuery() != null) 
			return getIssueManager().query(getProject(), true, getQuery(), offset, count, true);
		else 
			return new ArrayList<>();
	}
	
	protected abstract Project getProject();
	
	@Nullable
	protected abstract IssueQuery getQuery();

	protected abstract int getCardCount();
	
}
