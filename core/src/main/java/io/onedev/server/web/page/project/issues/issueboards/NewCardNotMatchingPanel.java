package io.onedev.server.web.page.project.issues.issueboards;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;

import io.onedev.server.model.Issue;
import io.onedev.server.web.page.project.issues.issuedetail.IssueDetailPage;

@SuppressWarnings("serial")
abstract class NewCardNotMatchingPanel extends Panel {

	public NewCardNotMatchingPanel(String id) {
		super(id);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		Link<Void> link = new BookmarkablePageLink<Void>("link", IssueDetailPage.class, 
				IssueDetailPage.paramsOf(getIssue(), null));
		link.add(new Label("label", "#" + getIssue().getNumber()));
		add(link);
		add(new AjaxLink<Void>("ok") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				onClose(target);
			}
			
		});
	}

	protected abstract Issue getIssue();
	
	protected abstract void onClose(AjaxRequestTarget target);
}
