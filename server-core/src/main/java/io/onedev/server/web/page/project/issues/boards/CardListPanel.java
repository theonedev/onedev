package io.onedev.server.web.page.project.issues.boards;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.IssueManager;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.search.entity.issue.IssueQuery;
import io.onedev.server.util.ProjectScope;
import io.onedev.server.web.WebConstants;
import io.onedev.server.web.behavior.ChangeObserver;
import io.onedev.server.web.behavior.infinitescroll.InfiniteScrollBehavior;
import io.onedev.server.web.util.Cursor;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.apache.wicket.feedback.FencedFeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import static java.lang.Long.parseLong;
import static org.apache.commons.lang3.StringUtils.substringAfterLast;

@SuppressWarnings("serial")
abstract class CardListPanel extends Panel {

	private RepeatingView cardsView;
	
	private InfiniteScrollBehavior behavior;
	
	public CardListPanel(String id) {
		super(id);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(new FencedFeedbackPanel("feedback", this));
		
		cardsView = new RepeatingView("cards");
		int index = 0;
		try {
			List<Issue> issues = queryIssues(0, WebConstants.PAGE_SIZE);
			for (Issue issue: issues) {
				int cardOffset = index;
				var issueId = issue.getId();
				cardsView.add(new BoardCardPanel(cardsView.newChildId(), issueId) {
	
					@Override
					protected Cursor getCursor() {
						IssueQuery query = getQuery();
						if (query != null)
							return new Cursor(query.toString(), getCardCount(), cardOffset, getProjectScope());
						else
							return null;
					}
	
					@Override
					protected Project getProject() {
						return CardListPanel.this.getProject();
					}

					@Override
					protected void onDeleteIssue(AjaxRequestTarget target) {
						removeCard(target, issueId);
					}

				});
				index++;
			}
		} catch (ExplicitException e) {
			error(e.getMessage());
		}
		add(cardsView);
		
		add(behavior = new InfiniteScrollBehavior(WebConstants.PAGE_SIZE) {

			@Override
			protected String getItemSelector() {
				return ">.board-card";
			}

			@Override
			protected void appendMore(AjaxRequestTarget target, int offset, int count) {
				int index = offset;
				for (Issue issue: queryIssues(offset, count)) {
					int cardOffset = index;
					var issueId = issue.getId();
					BoardCardPanel card = new BoardCardPanel(cardsView.newChildId(), issueId) {

						@Override
						protected Cursor getCursor() {
							IssueQuery query = getQuery();
							if (query != null)
								return new Cursor(query.toString(), getCardCount(), cardOffset, getProjectScope());
							else
								return null;
						}

						@Override
						protected Project getProject() {
							return CardListPanel.this.getProject();
						}

						@Override
						protected void onDeleteIssue(AjaxRequestTarget target) {
							removeCard(target, issueId);
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
		
		add(new ChangeObserver() {
			
			@Override
			public void onObservableChanged(IPartialPageRequestHandler handler, Collection<String> changedObservables) {
				Project.push(getProject());
				try {
					for (var observable : changedObservables) {
						var issueId = parseLong(substringAfterLast(observable, ":"));
						var issue = getIssueManager().load(issueId);
						if (getQuery() == null || getQuery().matches(issue)) {
							boolean found = false;
							for (var card : cardsView) {
								if (((BoardCardPanel) card).getIssueId().equals(issueId)) {
									handler.add(card);
									found = true;
									break;
								}
							}
							if (!found) {
								behavior.refresh(handler);
								onUpdate(handler);
							}
						} else {
							removeCard(handler, issueId);
						}
					}
				} finally {
					Project.pop();
				}
			}
			
			@Override
			public Collection<String> findObservables() {
				Collection<String> observables = new HashSet<>();
				for (var projectId: getProjectManager().getSubtreeIds(getProject().getId()))
					observables.add(Issue.getListChangeObservable(projectId));
				for (var ancestor: getProject().getAncestors())
					observables.add(Issue.getListChangeObservable(ancestor.getId()));
				return observables;
			}
			
		});
		
	}
	
	private ProjectManager getProjectManager() {
		return OneDev.getInstance(ProjectManager.class); 
	}
	
	private IssueManager getIssueManager() {
		return OneDev.getInstance(IssueManager.class);
	}

	private List<Issue> queryIssues(int offset, int count) {
		if (getQuery() != null) {
			return getIssueManager().query(getProjectScope(), getQuery(), true, offset, count);
		} else { 
			return new ArrayList<>();
		}
	}
	
	private Project getProject() {
		return getProjectScope().getProject();
	}
	
	protected abstract ProjectScope getProjectScope();
	
	@Nullable
	protected abstract IssueQuery getQuery();

	protected abstract int getCardCount();
	
	protected abstract void onUpdate(IPartialPageRequestHandler handler);
	
	private void removeCard(IPartialPageRequestHandler handler, Long issueId) {
		for (var card: cardsView) {
			if (((BoardCardPanel)card).getIssueId().equals(issueId)) {
				cardsView.remove(card);
				handler.appendJavaScript(String.format("$('#%s').remove();", card.getMarkupId()));
				behavior.check(handler);
				onUpdate(handler);
				break;
			}
		}
	}
}
