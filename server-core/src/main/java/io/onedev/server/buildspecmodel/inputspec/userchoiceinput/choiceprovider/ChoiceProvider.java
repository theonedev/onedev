package io.onedev.server.buildspecmodel.inputspec.userchoiceinput.choiceprovider;

import java.io.Serializable;
import java.util.List;

import io.onedev.server.model.User;
import io.onedev.server.annotation.Editable;

@Editable
public interface ChoiceProvider extends Serializable {
	
	List<User> getChoices(boolean allPossible);
	
}
