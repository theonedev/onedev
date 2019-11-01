package io.onedev.server.model.support.inputspec.userchoiceinput.choiceprovider;

import java.io.Serializable;
import java.util.List;

import io.onedev.server.model.User;
import io.onedev.server.web.editable.annotation.Editable;

@Editable
public interface ChoiceProvider extends Serializable {
	
	List<User> getChoices(boolean allPossible);
	
}
