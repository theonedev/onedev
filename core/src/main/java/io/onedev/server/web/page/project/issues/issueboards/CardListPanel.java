package io.onedev.server.web.page.project.issues.issueboards;

import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import io.onedev.server.OneDev;
import io.onedev.server.manager.IssueManager;
import io.onedev.server.model.Issue;
import io.onedev.server.web.WebConstants;
import io.onedev.server.web.behavior.infinitescroll.InfiniteScrollBehavior;

@SuppressWarnings("serial")
abstract class CardListPanel extends Panel {

	public CardListPanel(String id) {
		super(id);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		RepeatingView cardsView = new RepeatingView("cards");
		for (Issue issue: queryIssues(0, WebConstants.PAGE_SIZE)) {
			Long issueId = issue.getId();
			IModel<Issue> model = new LoadableDetachableModel<Issue>() {

				@Override
				protected Issue load() {
					return OneDev.getInstance(IssueManager.class).load(issueId);
				}
				
			};
			cardsView.add(new BoardCardPanel(cardsView.newChildId(), model));
		}
		add(cardsView);
		
		add(new InfiniteScrollBehavior(WebConstants.PAGE_SIZE) {

			@Override
			protected String getItemSelector() {
				return ">.card";
			}

			@Override
			protected void appendMore(AjaxRequestTarget target, int offset, int count) {
				for (Issue issue: queryIssues(offset, count)) {
					Long issueId = issue.getId();
					IModel<Issue> model = new LoadableDetachableModel<Issue>() {

						@Override
						protected Issue load() {
							return OneDev.getInstance(IssueManager.class).load(issueId);
						}
						
					};
					BoardCardPanel card = new BoardCardPanel(cardsView.newChildId(), model);
					cardsView.add(card);
					String script = String.format("$('#%s').append('<div id=\"%s\"></div>');", 
							getMarkupId(), card.getMarkupId());
					target.prependJavaScript(script);
					target.add(card);
				}
			}
			
		});
	}

	protected abstract List<Issue> queryIssues(int offset, int count);

}
