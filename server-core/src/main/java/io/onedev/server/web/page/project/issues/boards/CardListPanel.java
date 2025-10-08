package io.onedev.server.web.page.project.issues.boards;

import static java.lang.Long.parseLong;
import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.substringAfterLast;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.jspecify.annotations.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.apache.wicket.feedback.FencedFeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.util.visit.IVisitor;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.OneDev;
import io.onedev.server.service.IssueService;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.persistence.TransactionService;
import io.onedev.server.search.entity.issue.IssueQuery;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.ProjectScope;
import io.onedev.server.web.WebConstants;
import io.onedev.server.web.behavior.ChangeObserver;
import io.onedev.server.web.behavior.infinitescroll.InfiniteScrollBehavior;
import io.onedev.server.web.util.Cursor;
import io.onedev.server.web.util.WicketUtils;

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
		try {
			List<Issue> issues = queryIssues(0, WebConstants.PAGE_SIZE);
			for (Issue issue: issues) 
				cardsView.add(newCard(issue.getId()));
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
				for (Issue issue: queryIssues(offset, count)) {
					var card = newCard(issue.getId());
					cardsView.add(card);
					String script = format("$('#%s').append('<div id=\"%s\"></div>');", 
							getMarkupId(), card.getMarkupId());
					target.prependJavaScript(script);
					target.add(card);
				}
			}
			
		});
		
		add(new ChangeObserver() {
			
			@Override
			public void onObservableChanged(IPartialPageRequestHandler handler, Collection<String> changedObservables) {
				if (getQuery() != null) {
					Project.push(getProject());
					try {
						for (var observable : changedObservables) {
							var issueId = parseLong(substringAfterLast(observable, ":"));
							Component card = findCard(issueId);
							if (getQuery().matches(getIssueService().load(issueId))) {
								if (card != null) 
									handler.add(card);
								else 
									behavior.refresh(handler);
							} else {
								if (card != null) {
									cardsView.remove(card);
									behavior.check(handler);
									handler.appendJavaScript(format("" +
											"$('#%s').remove();" +
											"onedev.server.issueBoards.changeCardCount('%s', -1);", 
											card.getMarkupId(), getMarkupId()));
								}
							}
							if (card == null)
								updateCardCount(handler);
						}
					} finally {
						Project.pop();
					}
				}
			}
			
			@Override
			public Collection<String> findObservables() {
				Collection<String> observables = new HashSet<>();
				for (var projectId: WicketUtils.getProjectCache().getSubtreeIds(getProject().getId()))
					observables.add(Issue.getListChangeObservable(projectId));
				for (var ancestor: getProject().getAncestors())
					observables.add(Issue.getListChangeObservable(ancestor.getId()));
				return observables;
			}
			
		});
		
	}
	
	private BoardCardPanel newCard(Long issueId) {
		return new BoardCardPanel(String.valueOf(issueId), issueId) {

			@SuppressWarnings("deprecation")
			@Override
			protected Cursor getCursor() {
				IssueQuery query = getQuery();
				if (query != null) {
					for (var i = 0; i < cardsView.size(); i++) {
						if (cardsView.get(i) == this)
							return new Cursor(query.toString(), getCardCount(), i, getProjectScope());
					}
				}
				return null;
			}

			@Override
			protected Project getProject() {
				return CardListPanel.this.getProject();
			}

			@Override
			protected void onDeleteIssue(AjaxRequestTarget target) {
				onCardRemoved(target, issueId);
			}

		};
	}
		
	private IssueService getIssueService() {
		return OneDev.getInstance(IssueService.class);
	}

	private List<Issue> queryIssues(int offset, int count) {
		if (getQuery() != null) {
			return getIssueService().query(SecurityUtils.getSubject(), getProjectScope(), getQuery(), true, offset, count);
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
	
	protected abstract void updateCardCount(IPartialPageRequestHandler handler);
	
	private void onCardRemoved(IPartialPageRequestHandler handler, Long issueId) {
		for (var card: cardsView) {
			if (((BoardCardPanel)card).getIssueId().equals(issueId)) {
				cardsView.remove(card);
				handler.appendJavaScript(format("$('#%s').remove();", card.getMarkupId()));
				behavior.check(handler);
				break;
			}
		}
		handler.appendJavaScript(format("onedev.server.issueBoards.changeCardCount('%s', -1);", getMarkupId()));
	}

	void onCardAdded(AjaxRequestTarget target, Long issueId) {
		var card = newCard(issueId);		
		cardsView.add(card);
		target.add(card);
		var script = format("" +
				"$(\"<div id='%s'></div>\").insertAfter($('#%s').children().eq(0));" +
				"onedev.server.issueBoards.changeCardCount('%s', 1);", 
				card.getMarkupId(), getMarkupId(), getMarkupId());
		target.prependJavaScript(script);		
	}

	@Nullable
	BoardCardPanel findCard(@Nullable Long issueId) {
		return visitChildren(BoardCardPanel.class, (IVisitor<BoardCardPanel, BoardCardPanel>) (object, visit) -> {
			if (issueId == null || issueId.equals(object.getIssueId()))
				visit.stop(object);
		});
	}
	
	void onCardDropped(AjaxRequestTarget target, Long issueId, int cardIndex, boolean accepted) {
		findParent(RepeatingView.class).visitChildren(CardListPanel.class, (IVisitor<CardListPanel, Void>) (cardListPanel, visit) -> {
			for (int i=0; i<cardListPanel.cardsView.size(); i++) {
				@SuppressWarnings("deprecation")
				var card = (BoardCardPanel) cardListPanel.cardsView.get(i);
				if (card.getIssueId().equals(issueId)) {
					if (accepted) {
						if (cardListPanel == CardListPanel.this) {
							if (i != cardIndex) {
								moveCard(i, cardIndex);
								updateCardPositions(cardIndex);
							}
						} else {
							cardListPanel.cardsView.remove(card);
							cardListPanel.behavior.check(target);
							cardsView.add(card);
							moveCard(cardsView.size() - 1, cardIndex);
							updateCardPositions(cardIndex);
						}
						target.add(card);
					}
					target.appendJavaScript(format(
							"onedev.server.issueBoards.onCardDropped('%s', %d, '%s', %d, %b);", 
							cardListPanel.getMarkupId(), i, 
							CardListPanel.this.getMarkupId(), cardIndex, 
							accepted));
					visit.stop();
				}
			}
		});
	}
	
	@SuppressWarnings("deprecation")
	private void moveCard(int fromIndex, int toIndex) {
		if (fromIndex < toIndex) {
			for (int i=0; i<toIndex-fromIndex; i++)
				cardsView.swap(fromIndex+i, fromIndex+i+1);
		} else {
			for (int i=0; i<fromIndex-toIndex; i++)
				cardsView.swap(fromIndex-i, fromIndex-i-1);
		}
	}
	
	@SuppressWarnings("deprecation")
	private Issue getIssue(int cardIndex) {
		return getIssueService().load(((BoardCardPanel) cardsView.get(cardIndex)).getIssueId());
	}
	
	private void updateCardPositions(int index) {
		int basePosition;
		int baseIndex;
		if (index < cardsView.size() - 1) 
			baseIndex = index + 1;
		else 
			baseIndex = index;
		basePosition = getIssue(baseIndex).getBoardPosition();
		OneDev.getInstance(TransactionService.class).run(() -> {
			for (var i=0; i<baseIndex; i++)
				getIssue(i).setBoardPosition(basePosition-baseIndex+i);
		});
	}

}
