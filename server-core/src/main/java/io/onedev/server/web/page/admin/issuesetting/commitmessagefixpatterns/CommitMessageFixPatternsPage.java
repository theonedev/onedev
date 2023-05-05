package io.onedev.server.web.page.admin.issuesetting.commitmessagefixpatterns;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.support.issue.CommitMessageFixPatterns;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.page.admin.issuesetting.IssueSettingPage;
import org.apache.wicket.Component;
import org.apache.wicket.Session;
import org.apache.wicket.feedback.FencedFeedbackPanel;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.request.mapper.parameter.PageParameters;

public class CommitMessageFixPatternsPage extends IssueSettingPage {

	public CommitMessageFixPatternsPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		CommitMessageFixPatterns patterns = getSettingManager().getIssueSetting().getCommitMessageFixPatterns();
		Form<?> form = new Form<Void>("form") {
			@Override
			protected void onSubmit() {
				super.onSubmit();
				var issueSetting = getSettingManager().getIssueSetting();
				issueSetting.setCommitMessageFixPatterns(patterns);
				getSettingManager().saveIssueSetting(issueSetting);
				Session.get().success("Setting updated");
			}
		};
		form.add(new FencedFeedbackPanel("feedback", form));
		form.add(BeanContext.edit("editor", patterns));
		add(form);
	}
	
	private SettingManager getSettingManager() {
		return OneDev.getInstance(SettingManager.class);
	}

	@Override
	protected Component newTopbarTitle(String componentId) {
		return new Label(componentId, "<span class='text-truncate'>Commit Message Fix Patterns</span>").setEscapeModelStrings(false);
	}
	
}
