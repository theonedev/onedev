package io.onedev.server.web.component.rolechoice;

import static io.onedev.server.web.translation.Translation._T;

import java.util.Collection;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.model.IModel;

import io.onedev.server.model.Role;
import io.onedev.server.web.component.select2.Select2Choice;

public class RoleSingleChoice extends Select2Choice<Role> {

	public RoleSingleChoice(String id, IModel<Role> selectionModel, IModel<Collection<Role>> choicesModel) {
		super(id, selectionModel, new RoleChoiceProvider(choicesModel));
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		getSettings().setAllowClear(!isRequired());
		if (isRequired())
			getSettings().setPlaceholder(_T("Choose role..."));
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