package io.onedev.server.web.component.codecomment;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.CodeCommentManager;
import io.onedev.server.model.CodeComment;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.util.ConfirmOnClick;

@SuppressWarnings("serial")
public class InvalidCodeCommentPanel extends Panel {

	private final CodeCommentListPanel codeCommentListPanel;
	
	private final Long codeCommentId;
	
	public InvalidCodeCommentPanel(CodeCommentListPanel codeCommentListPanel, Long codeCommentId) {
		super(codeCommentListPanel.getId());
		this.codeCommentListPanel = codeCommentListPanel;
		this.codeCommentId = codeCommentId;
	}

	private CodeComment getCodeComment() {
		return OneDev.getInstance(CodeCommentManager.class).load(codeCommentId);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		add(new Link<Void>("delete") {

			@Override
			public void onClick() {
				OneDev.getInstance(CodeCommentManager.class).delete(getCodeComment());
				InvalidCodeCommentPanel.this.replaceWith(codeCommentListPanel);
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(SecurityUtils.canAdministrate(getCodeComment().getProject().getFacade()));
			}
			
		}.add(new ConfirmOnClick("Really want to delete this code comment?")));

		add(new Link<Void>("backToList") {

			@Override
			public void onClick() {
				InvalidCodeCommentPanel.this.replaceWith(codeCommentListPanel);
			}
			
		});
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new CodeCommentCssResourceReference()));
	}
	
}
