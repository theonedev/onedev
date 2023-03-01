package io.onedev.server.buildspecmodel.inputspec.groupchoiceinput.choiceprovider;

import java.io.Serializable;
import java.util.List;

import io.onedev.server.model.Group;
import io.onedev.server.annotation.Editable;

@Editable
public interface ChoiceProvider extends Serializable {
	
	List<Group> getChoices(boolean allPossible);
	
}
