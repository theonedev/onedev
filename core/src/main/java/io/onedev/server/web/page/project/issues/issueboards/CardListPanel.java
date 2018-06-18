package io.onedev.server.web.page.project.issues.issueboards;

import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;

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
		for (Issue issue: queryIssues(0)) {
			cardsView.add(new BoardCardPanel(cardsView.newChildId()) {

				@Override
				protected Issue getIssue() {
					return issue;
				}
				
			});
		}
		add(cardsView);
		
		add(new InfiniteScrollBehavior(WebConstants.PAGE_SIZE) {

			@Override
			protected String getItemSelector() {
				return ">.card";
			}

			@Override
			protected void appendPage(AjaxRequestTarget target, int page) {
				for (Issue issue: queryIssues(page-1)) {
					BoardCardPanel card = new BoardCardPanel(cardsView.newChildId()) {

						@Override
						protected Issue getIssue() {
							return issue;
						}
						
					};
					cardsView.add(card);
					String script = String.format("$('#%s').append('<div id=\"%s\"></div>');", 
							getMarkupId(), card.getMarkupId());
					target.prependJavaScript(script);
					target.add(card);
				}
			}
			
		});
	}

	protected abstract List<Issue> queryIssues(int page);
}
