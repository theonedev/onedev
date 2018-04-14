package io.onedev.server.web.component.verification;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.model.IModel;

import io.onedev.server.OneDev;
import io.onedev.server.manager.VerificationManager;
import io.onedev.server.web.component.select2.Select2MultiChoice;
import io.onedev.server.web.component.stringchoice.StringChoiceProvider;
import io.onedev.server.web.page.project.ProjectPage;
import io.onedev.server.web.util.WicketUtils;

public class VerificationMultiChoice extends Select2MultiChoice<String> {

	private static final long serialVersionUID = 1L;

	public VerificationMultiChoice(String id, IModel<Collection<String>> model) {
		super(id, model, new StringChoiceProvider(getVerifications()));
	}

	private static List<String> getVerifications() {
		ProjectPage projectPage = (ProjectPage) WicketUtils.getPage();
		Collection<String> verifications = OneDev.getInstance(VerificationManager.class)
				.getVerificationNames(projectPage.getProject());
		List<String> verificationList = new ArrayList<>(verifications);
		Collections.sort(verificationList);
		return verificationList;
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		getSettings().setPlaceholder("Choose verifications...");
		getSettings().setFormatResult("onedev.server.verificationChoiceFormatter.formatResult");
		getSettings().setFormatSelection("onedev.server.verificationChoiceFormatter.formatSelection");
		getSettings().setEscapeMarkup("onedev.server.verificationChoiceFormatter.escapeMarkup");
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(JavaScriptHeaderItem.forReference(new VerificationResourceReference()));
	}

}
