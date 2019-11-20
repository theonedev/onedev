package io.onedev.server.util.inputspec.userchoiceinput.choiceprovider;

import java.util.List;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.model.User;
import io.onedev.server.web.editable.annotation.Editable;

@Editable(order=130, name="All Users in System")
public class AllUsers implements ChoiceProvider {

	private static final long serialVersionUID = 1L;

	@Override
	public List<User> getChoices(boolean allPossible) {
		return OneDev.getInstance(UserManager.class).query();
	}

}
