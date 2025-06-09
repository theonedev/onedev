package io.onedev.server.web.component.rolechoice;

import static io.onedev.server.web.translation.Translation._T;

import java.util.Collection;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.model.IModel;

import io.onedev.server.model.Role;
import io.onedev.server.web.component.select2.Select2MultiChoice;

public class RoleMultiChoice extends Select2MultiChoice<Role> {

	public RoleMultiChoice(String id, IModel<Collection<Role>> selectionsModel, IModel<Collection<Role>> choicesModel) {
		super(id, selectionsModel, new RoleChoiceProvider(choicesModel));
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		if (isRequired())
			getSettings().setPlaceholder(_T("Choose roles..."));
		else
			getSettings().setPlaceholder(_T("Not specified"));
		getSettings().setFormatResult("onedev.server.roleChoiceFormatter.formatResult");
		getSettings().setFormatSelection("onedev.server.roleChoiceFormatter.formatSelection");
		getSettings().setEscapeMarkup("onedev.server.roleChoiceFormatter.escapeMarkup");
		setConvertEmptyInputStringToNull(true);
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(JavaScriptHeaderItem.forReference(new RoleChoiceResourceReference()));
	}

}
